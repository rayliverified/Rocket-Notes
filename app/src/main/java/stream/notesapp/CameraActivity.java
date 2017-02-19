package stream.notesapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.ZoomStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity{

    public static final String FRAGMENT_TAG = "camera";
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (Build.VERSION.SDK_INT > 15) {
            final String[] permissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};

            final List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CAMERA_PERMISSIONS);
            } else addCamera();
        } else {
            addCamera();
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void addCamera() {
        Intent intent = new com.commonsware.cwac.cam2.CameraActivity.IntentBuilder(CameraActivity.this)
                .skipConfirm()
                .facing(Facing.BACK)
                .debug()
                .zoomStyle(ZoomStyle.PINCH)
                .updateMediaStore()
                .build();

        startActivityForResult(intent, 0);
    }
}
