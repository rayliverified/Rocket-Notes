package stream.rocketnotes;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v13.app.ActivityCompat;
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
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;
import com.squareup.picasso.Picasso;
import com.uxcam.UXCam;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.ui.CustomImageView;
import stream.rocketnotes.utils.PermissionUtils;

public class ShareActivity extends Activity {

    private EditText editText;
    private ImageButton editSubmit;
    private CustomImageView editImage;
    private Intent shareIntent;
    private String noteType;
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

        editSubmit = (ImageButton) findViewById(R.id.edit_submit);
        editImage = (CustomImageView) findViewById(R.id.edit_image);

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
                finish();
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
            editText.setText(shareText);
        }
    }

    public void shareImage(Intent intent)
    {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.d("Image URI", String.valueOf(imageUri));
            //Loading image requires Storage permission on Marshmallow and above
            if (PermissionUtils.IsPermissionEnabled(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                //Use editText to display image name.
                String imageName = null;
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
                //Enable imageView and display shared image.
                editImage.setVisibility(View.VISIBLE);
                Picasso.with(mContext).load(imageUri).into(editImage);
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
        if (!TextUtils.isEmpty(editText.getText().toString().trim()))
        {
            Intent saveNote = new Intent(mContext, SaveNoteService.class);
            if ("text/plain".equals(noteType))
            {
                AnalyticEvent("SaveNote", "Text");
                saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                saveNote.setAction(Constants.NEW_NOTE);
                mContext.startService(saveNote);
            }
            else if (noteType.startsWith("image/"))
            {
                AnalyticEvent("SaveNote", "Image");
                //TODO: Share Image to Rocket Notes
            }
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
