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
import android.support.v4.content.ContextCompat;
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
import com.github.ybq.android.spinkit.style.Wave;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.pyze.android.Pyze;
import com.squareup.picasso.Picasso;
import com.uxcam.UXCam;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.service.SaveImageService;
import stream.rocketnotes.service.SaveNoteService;
import stream.rocketnotes.ui.CustomImageView;
import stream.rocketnotes.utils.AnalyticsUtils;
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
    private Future<File> downloading;
    private String mActivity = "ShareActivity";
    private Context mContext;

    private boolean saveNote = false;
    private boolean submitEnabled = false;
    private boolean fileDownloaded = false;
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

        if (!PermissionUtils.IsPermissionEnabled(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            //Notify user that permission is not enabled via editText
            editText.setText("Storage permission required to save notes.");
            editText.setFocusable(false);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSIONS);
        }

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
                    AnalyticsUtils.AnalyticEvent(mActivity, "Permission", "Granted");
                    recreate();
                } else {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Permission", "Denied");
                    Toasty.error(mContext, "Permission Denied", Toast.LENGTH_SHORT, true).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ShareActivity", "OnPause");
        //Share attempt has been canceled by user. Delete any remnants
        if (!saveNote && downloading != null)
        {
            Log.d("Download", "Cancel");
            //Cancel pending download if not completed
            downloading.cancel();
            downloading = null;
            //If download has been saved, delete download.
            if (fileDownloaded == true && imageUri != null)
            {
                File imageFile = new File(imageUri.getPath());
                imageFile.delete();
                Log.d("Download", "Delete");
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
                    //Set progress bar loader
                    Wave wave = new Wave();
                    wave.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
                    progressBar.setIndeterminateDrawable(wave);
                    //Download image with Ion
                    downloading = Ion.with(mContext)
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
                                        //Load downloaded file into ImageView
                                        Ion.with(mContext)
                                                .load(file)
                                                .withBitmap()
                                                .intoImageView(editImage);
                                        //Set imageURI to File scheme where image has been saved.
                                        imageUri = Uri.parse("file://" + file.getAbsolutePath());
                                        Log.d("ImageURI", String.valueOf(imageUri));
                                        fileDownloaded = true;
                                    }
                                    else
                                    {
                                        //Image URL is invalid, display editText instead.
                                        progressBar.setVisibility(View.GONE);
                                        editImage.setVisibility(View.GONE);
                                        editText.setEnabled(true);
                                        //Set editDetails text to Text Note
                                        editDetails.setText("New Text Note • now");
                                    }
                                    submitEnabled = true;
                                }
                            });
                }
                else
                {
                    editText.setText(shareText);
                    submitEnabled = true;
                }
            }
            else
            {
                editText.setText(shareText);
                submitEnabled = true;
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
            setImageName();
            //Enable imageView and display shared image.
            editImage.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(imageUri).into(editImage);
            //Create Pictures folder to prepare to copy file into folder on saveNote clicked.
            FileUtils.InitializePicturesFolder(mContext);
            submitEnabled = true;
        }
    }

    private void saveNote()
    {
        //Set save attempt flag to true. Do not delete downloaded image file.
        saveNote = true;
        //Wait for images to load before allowing user to save.
        if (submitEnabled == true)
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
                    AnalyticsUtils.AnalyticEvent(mActivity, "SaveNote", "Text");
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
                    Log.d("Image URI", String.valueOf(imageUri));
                    AnalyticsUtils.AnalyticEvent(mActivity, "SaveImage", "Image");
                    String savePath = getFilesDir() + "/.Pictures";
                    Intent savePicture = new Intent(mContext, SaveImageService.class);
                    savePicture.putExtra(Constants.SOURCE_PATH, imageUri.toString());
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
        else
        {
            Toasty.warning(mContext, "Please wait", Toast.LENGTH_SHORT, true).show();
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
        Pyze.initialize(getApplication());
        UXCam.startWithKey(Constants.UXCAM_API_KEY);
        UXCam.occludeSensitiveScreen(true);
    }
}
