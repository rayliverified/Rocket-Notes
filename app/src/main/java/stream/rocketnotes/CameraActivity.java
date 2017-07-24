package stream.rocketnotes;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialcamera.MaterialCamera;
import com.flurry.android.FlurryAgent;
import com.pyze.android.Pyze;
import com.uxcam.UXCam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import stream.rocketnotes.service.SaveNoteService;
import stream.rocketnotes.utils.AnalyticsUtils;
import stream.rocketnotes.utils.FileUtils;

public class CameraActivity extends AppCompatActivity{

    private String mActivity = "CameraActivity";
    private Context mContext;

    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private final static int CAMERA_RQ = 6969;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        initializeAnalytics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String[] permissions = {
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

            final List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Permission", "Request");
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("Request Code", String.valueOf(requestCode));
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Permission", "Granted");
                    StartCamera();
                } else {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Permission", "Denied");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void StartCamera() {

        AnalyticsUtils.AnalyticEvent(mActivity, "Camera", "Start");

        FileUtils.InitializePicturesFolder(mContext);
        File storageDir = new File(getFilesDir(), ".Pictures");
        new MaterialCamera(this)
                /** all the previous methods can be called, but video ones would be ignored */
                .stillShot() // launches the Camera in stillshot mode
                .saveDir(storageDir)
                .labelRetry(R.string.camera_retake)
                .labelConfirm(R.string.camera_save)
                .start(CAMERA_RQ);
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

    public void initializeAnalytics()
    {
        if (FlurryAgent.isSessionActive() == false)
        {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, Constants.FLURRY_API_KEY);
        }
        Pyze.initialize(getApplication());
//        UXCam.startWithKey(Constants.UXCAM_API_KEY);
//        UXCam.occludeSensitiveScreen(true);
    }
}
