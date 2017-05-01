package stream.rocketnotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.ActivityCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;
import com.squareup.picasso.Picasso;
import com.uxcam.UXCam;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.service.SaveImageService;
import stream.rocketnotes.service.SaveNoteService;
import stream.rocketnotes.ui.CustomImageView;
import stream.rocketnotes.utils.FileUtils;
import stream.rocketnotes.utils.PermissionUtils;

public class ShareActivity extends Activity {

    private EditText editText;
    private TextView editDetails;
    private ImageButton editSubmit;
    private CustomImageView editImage;
    private Intent shareIntent;
    private String noteType;
    private Uri imageUri;
    private String imageName;
    private ProgressBar progressBar;
    private MixpanelAPI mixpanel;
    private String mActivity = "ShareActivity";
    private Context mContext;

    private static final int REQUEST_STORAGE_PERMISSIONS = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.activity_share, null);
        setContentView(view);

        mContext = getApplicationContext();

        //Configure floating window
        Window window = getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes()); //Inherit transparent window attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; //Floating window WRAPS_CONTENT by default. Force fullscreen
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = 150; // top margin
        lp.gravity = (Gravity.TOP);
        window.setAttributes(lp);
        // set right and bottom margin implicitly by calculating width and height of dialog
//        Point displaySize = getDisplayDimensions(mContext);
//        int width = displaySize.x - 120;
//        int height = displaySize.y - 120;
//        window.setLayout(width, height);

        //Flag allows window to overlap status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        //FLAG_NOT_TOUCH_MODAL passes through touch events to objects underneath view
        //FLAG_WATCH_OUTSIDE_TOUCH dismisses window when outside touch is detected
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Hides keyboard from focusing on editText when Activity starts
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initializeAnalytics();

        //Focus defaults to editText, set again just in case
        editText = (EditText) findViewById(R.id.edit_edit);
        editText.clearFocus();

        editDetails = (TextView) findViewById(R.id.edit_details);
        editSubmit = (ImageButton) findViewById(R.id.edit_submit);
        editImage = (CustomImageView) findViewById(R.id.edit_image);
        progressBar = (ProgressBar) findViewById(R.id.edit_progress);

        //Get data shared to app
        shareIntent = getIntent();
        String action = getIntent().getAction();
        noteType = getIntent().getType();
        if (action.equals(Intent.ACTION_SEND) && noteType != null) {
            Log.d("ShareActivity", action);
            Log.d("ShareActivity", noteType);
            if ("text/plain".equals(noteType)) {
                shareText(shareIntent);
            }
            else if (noteType.startsWith("image/"))
            {
                shareImage(shareIntent);
            }
        }

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setCursorVisible(true);
            }
        });

        editSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            finish();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("Request Code", String.valueOf(requestCode));
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AnalyticEvent("Permission", "Granted");
                    //Relaunch sharedIntent to access image
                    shareImage(shareIntent);
                } else {
                    AnalyticEvent("Permission", "Denied");
                    Toasty.error(mContext, "Permission Denied", Toast.LENGTH_SHORT, true).show();
                }
                return;
            }
        }
    }

    public void shareText(Intent intent)
    {
        String shareText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (shareText != null) {
            Log.d("ShareText", "Not Null");
            //Detect if sharedText is URL link
            if (android.util.Patterns.WEB_URL.matcher(shareText).matches())
            {
                Log.d("ShareURL", shareText);
                //Detect if shared URL is an Image
                String imageType = shareText.substring(shareText.lastIndexOf(".") + 1);
                if ("png".equals(imageType) || "jpg".equals(imageType) ||
                        "jpeg".equals(imageType) || "bmp".equals(imageType))
                {
                    //Set editDetails text to Image Note
                    editDetails.setText("New Image Note • now");
                    //Passed URL must start with http:// to be accepted by image loader
                    if(!shareText.startsWith("http"))
                    {
                        shareText = "http://" + shareText;
                    }
                    Log.d("ParsedURL", shareText);
                    Log.d("ShareImage", imageType);
                    //Load image name into editText
                    imageName = shareText.substring(shareText.lastIndexOf("/") + 1);
                    editText.setText(imageName);
                    editText.setEnabled(false);
                    //Display ImageView and ProgressBar
                    editImage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    Ion.with(mContext)
                            .load(shareText)
                            .progressBar(progressBar)
                            .progress(new ProgressCallback() {
                                @Override
                                public void onProgress(long downloaded, long total) {
                                    //Download progress
                                    Log.d("Downloaded", downloaded + " / " + total);
                                }
                            })
                            .write(new File(getFilesDir() + "/.Pictures/" + imageName))
                            .setCallback(new FutureCallback<File>() {
                                @Override
                                public void onCompleted(Exception e, File file) {
                                    Log.d("Ion", "onCompleted");
                                    if (file != null)
                                    {
                                        Ion.with(mContext)
                                                .load(file)
                                                .withBitmap()
                                                .intoImageView(editImage);
                                        //Set imageURI to File scheme where image has been saved.
                                        imageUri = Uri.parse("file://" + file.getAbsolutePath());
                                        Log.d("ImageURI", String.valueOf(imageUri));
                                    }
                                    else
                                    {
                                        //Image URL is invalid, display editText instead.
                                        progressBar.setVisibility(View.GONE);
                                        editImage.setVisibility(View.GONE);
                                        editText.setEnabled(true);
                                    }
                                }
                            });
                }
                else
                {
                    editText.setText(shareText);
                }
            }
            else
            {
                editText.setText(shareText);
            }
        }
    }

    public void shareImage(Intent intent)
    {
        //Set editDetails text to Image Note
        editDetails.setText("New Image Note • now");
        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.d("Image URI", String.valueOf(imageUri));
            //Loading image requires Storage permission on Marshmallow and above
            if (PermissionUtils.IsPermissionEnabled(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                setImageName();
                //Enable imageView and display shared image.
                editImage.setVisibility(View.VISIBLE);
                Picasso.with(mContext).load(imageUri).into(editImage);
                //Create Pictures folder to prepare to copy file into folder on saveNote clicked.
                FileUtils.InitializePicturesFolder(mContext);
            }
            else
            {
                //Notify user that permission is not enabled via editText
                editText.setText("Storage permission required to access images.");
                editText.setFocusable(false);
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSIONS);
            }
        }
    }

    private void saveNote()
    {
        //Save note and close activity
        //Multiple content may be sent to Rocket Notes. Force refresh of main feed.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Constants.REFRESH, true);
        editor.apply();

        if ("text/plain".equals(noteType))
        {
            //Shared text may be Image URL.
            if (imageUri != null)
            {
                Intent saveNote = new Intent(mContext, SaveNoteService.class);
                saveNote.putExtra(Constants.IMAGE, imageUri.toString());
                saveNote.setAction(Constants.NEW_NOTE);
                mContext.startService(saveNote);
                finish();
            }
            else if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                AnalyticEvent("SaveNote", "Text");
                Intent saveNote = new Intent(mContext, SaveNoteService.class);
                saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                saveNote.setAction(Constants.NEW_NOTE);
                mContext.startService(saveNote);
                finish();
            }
            else
            {
                Toasty.warning(mContext, "Note Empty", Toast.LENGTH_SHORT, true).show();
            }
        }
        else if (noteType.startsWith("image/"))
        {
            if (imageUri != null)
            {
                //Sometimes Content URI is obtained if file is shared from file manager. Get usable File URI.
                Log.d("File URI", FileUtils.GetFileUriFromContentUri(mContext, imageUri));
                AnalyticEvent("SaveImage", "Image");
                String savePath = getFilesDir() + "/.Pictures";
                Intent savePicture = new Intent(mContext, SaveImageService.class);
                savePicture.putExtra(Constants.SOURCE_PATH, FileUtils.GetFileUriFromContentUri(mContext, imageUri));
                savePicture.putExtra(Constants.SAVE_PATH, savePath);
                mContext.startService(savePicture);
                finish();
            }
            else
            {
                Toasty.error(mContext, "Image Unsupported", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    private void setImageName()
    {
        //Use editText to display image name.
        try {
            //Get File Name from URI and encode into readable format
            imageName = URLDecoder.decode(String.valueOf(imageUri).substring(String.valueOf(imageUri).lastIndexOf("/")+1), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (imageName != null)
        {
            editText.setText(imageName);
            editText.setEnabled(false);
        }
    }

    private File createFileFromInputStream() {

        String outputfile = getFilesDir() + "/.Pictures/" + imageName;
        Log.d("OutputFile", outputfile);
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try{
            File f = new File(outputfile);
            f.setWritable(true, false);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            System.out.println("error creating file");
            e.printStackTrace();
        }

        return null;
    }

    public static Point getDisplayDimensions(Context context )
    {
        WindowManager wm = ( WindowManager ) context.getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics( metrics );
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // find out if status bar has already been subtracted from screenHeight
        display.getRealMetrics( metrics );
        int physicalHeight = metrics.heightPixels;
        int statusBarHeight = getStatusBarHeight( context );
        int navigationBarHeight = getNavigationBarHeight( context );
        int heightDelta = physicalHeight - screenHeight;
        if ( heightDelta == 0 || heightDelta == navigationBarHeight )
        {
            screenHeight -= statusBarHeight;
        }

        return new Point( screenWidth, screenHeight );
    }

    public static int getStatusBarHeight( Context context )
    {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "status_bar_height", "dimen", "android" );
        return ( resourceId > 0 ) ? resources.getDimensionPixelSize( resourceId ) : 0;
    }

    public static int getNavigationBarHeight( Context context )
    {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "navigation_bar_height", "dimen", "android" );
        return ( resourceId > 0 ) ? resources.getDimensionPixelSize( resourceId ) : 0;
    }

    public void initializeAnalytics()
    {
        if (FlurryAgent.isSessionActive() == false)
        {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, Constants.FLURRY_API_KEY);
        }
        mixpanel = MixpanelAPI.getInstance(this, Constants.MIXPANEL_API_KEY);
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        Pyze.initialize(getApplication());
        UXCam.startWithKey(Constants.UXCAM_API_KEY);
        UXCam.addVerificationListener(new UXCam.OnVerificationListener() {
            @Override
            public void onVerificationSuccess() {
                //Tag Mixpanel events with UXCam recording URLS. Example:
                JSONObject eventProperties = new JSONObject();
                try {
                    eventProperties.put("UXCam: Session Recording link", UXCam.urlForCurrentSession());
                } catch (JSONException exception) {
                }
                mixpanel.track("UXCam Session URL", eventProperties);
                //Tag Mixpanel profile with UXCam user URLS. Example:
                mixpanel.getPeople().set("UXCam User URL", UXCam.urlForCurrentUser());
            }
            @Override
            public void onVerificationFailed(String errorMessage) {
            }
        });
    }

    public void AnalyticEvent(String object, String value)
    {
        try {
            JSONObject mixObject = new JSONObject();
            mixObject.put(object, value);
            mixpanel.track(mActivity, mixObject);
        } catch (JSONException e) {
            Log.e(Constants.APP_NAME, "Unable to add properties to JSONObject", e);
        }
        //Flurry
        Map<String, String> params = new HashMap<String, String>();
        params.put(object, value);
        FlurryAgent.logEvent(mActivity, params);
        //UXCam
        UXCam.addTagWithProperties(mActivity, params);
        //Pyze
        HashMap <String, String> attributes = new HashMap<String, String>();
        attributes.put(object, String.valueOf(value));
        PyzeEvents.postCustomEventWithAttributes(mActivity, attributes);
    }
}
