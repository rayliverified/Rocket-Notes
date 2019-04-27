package stream.rocketnotes;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialcamera.MaterialCamera;

import java.io.File;

import stream.custompermissionsdialogue.PermissionsDialogue;
import stream.custompermissionsdialogue.utils.PermissionUtils;
import stream.rocketnotes.service.SaveNoteService;
import stream.rocketnotes.utils.AnalyticsUtils;
import stream.rocketnotes.utils.FileUtils;

public class CameraActivity extends AppCompatActivity {

    private Context mContext;
    private String mActivity = this.getClass().getSimpleName();

    private final static int CAMERA_RQ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        AnalyticsUtils.InitializeAnalytics(getApplication());
        //Camera usage requires camera and storage permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

            if (!PermissionUtils.IsPermissionsEnabled(mContext, permissions)) {
                PermissionsDialogue.Builder alertPermissions = new PermissionsDialogue.Builder(CameraActivity.this)
                        .setMessage(getString(R.string.app_name) + " requires the following permissions to take photos: ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setRequireCamera(PermissionsDialogue.REQUIRED)
                        .setRequireStorage(PermissionsDialogue.REQUIRED)
                        .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                                StartCamera();
                            }
                        })
                        .setDecorView(getWindow().getDecorView())
                        .build();
                alertPermissions.show();
            } else {
                StartCamera();
            }
        } else {
            StartCamera();
        }
    }

    public void StartCamera() {

        AnalyticsUtils.AnalyticEvent(mActivity, "Camera", "Start");

        FileUtils.InitializePicturesFolder(mContext); //Create pictures folder if folder does not exist.
        File storageDir = new File(getFilesDir(), ".Pictures"); //Create storage location.
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

            //Send captured image location to SaveNoteService.
            Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
            saveNote.putExtra(Constants.IMAGE, data.getDataString());
            saveNote.setAction(Constants.NEW_NOTE);
            getApplicationContext().startService(saveNote);
        }
        finish();
    }
}
