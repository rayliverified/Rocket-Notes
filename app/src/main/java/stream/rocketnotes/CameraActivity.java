package stream.rocketnotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialcamera.MaterialCamera;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;
import com.uxcam.UXCam;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraActivity extends AppCompatActivity{

    private MixpanelAPI mixpanel;
    private String mActivity = "CameraActivity";

    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private final static int CAMERA_RQ = 6969;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeAnalytics();
        if (Build.VERSION.SDK_INT >= 23) {
            final String[] permissions = {
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

            final List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    AnalyticEvent("Permission", "Request");
                    Log.d("Permission Request", permission);
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CAMERA_PERMISSIONS);
            } else StartCamera();
        } else {
            StartCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("Request Code", String.valueOf(requestCode));
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AnalyticEvent("Permission", "Granted");
                    StartCamera();
                } else {
                    AnalyticEvent("Permission", "Denied");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void StartCamera() {

        AnalyticEvent("Camera", "Start");

        File dir = new File(getFilesDir() + "/.Pictures");
        if (!dir.exists()) {
            dir.mkdirs();
            Log.d("Directory", "Created");
        }
        else
        {
            Log.d("Directory", "Exists");
        }
        if (!noMediaExists())
        {
            try {
                createNoMediaFile();
                Log.d("No Media", "Created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.d("No Media", "Exists");
        }
        File storageDir = new File(getFilesDir(), ".Pictures");
        new MaterialCamera(this)
                /** all the previous methods can be called, but video ones would be ignored */
                .stillShot() // launches the Camera in stillshot mode
                .saveDir(storageDir)
                .labelRetry(R.string.camera_retake)
                .labelConfirm(R.string.camera_save)
                .start(CAMERA_RQ);

        Intent notification = new Intent(Constants.RECEIVED);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(notification);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_RQ && resultCode == RESULT_OK) {
            Log.d("Camera Result", data.getDataString());

            Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
            saveNote.putExtra(Constants.IMAGE, data.getDataString());
            saveNote.setAction(Constants.NEW_NOTE);
            getApplicationContext().startService(saveNote);
        }
        finish();
    }

    boolean noMediaExists() {
        File imagePath = new File(getFilesDir(), ".Pictures");
        File file = new File(imagePath, ".nomedia");
        if (file != null) {
            return file.exists();
        }
        return false;
    }

    private void createNoMediaFile() throws IOException
    {
        File imagePath = new File(getFilesDir(), ".Pictures");
        File noMediaFile = new File(imagePath, ".nomedia");
        noMediaFile.createNewFile();
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
        UXCam.occludeSensitiveScreen(true);
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
