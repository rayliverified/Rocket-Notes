package stream.notesapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;

import me.relex.photodraweeview.PhotoDraweeView;

public class ImageViewerActivity extends AppCompatActivity {

    private PhotoDraweeView mPhotoDraweeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_imageviewer);

        mPhotoDraweeView = (PhotoDraweeView) findViewById(R.id.photo_drawee_view);
        String imagePath = getIntent().getStringExtra("IMAGE_PATH");
        Log.d("Received Image Path", imagePath);
        Uri imageUri = Uri.fromFile(new File(imagePath));
        Log.d("Received Image Uri", String.valueOf(imageUri));
        mPhotoDraweeView.setPhotoUri(imageUri);
    }
}