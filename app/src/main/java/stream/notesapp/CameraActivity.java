package stream.notesapp;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialcamera.MaterialCamera;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity{

    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private final static int CAMERA_RQ = 6969;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 15) {
            final String[] permissions = {
                    Manifest.permission.CAMERA};

            final List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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
                    StartCamera();
                } else {

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void StartCamera() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_RQ && resultCode == RESULT_OK) {
            Log.d("Camera Result", data.getDataString());
            int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ImageWidget.class));

            for (int id : widgetIDs)
            {
                AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.id.image_gridview);
            }
            moveTaskToBack(true);
        }
        else
        {
            moveTaskToBack(true);
        }
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
}
