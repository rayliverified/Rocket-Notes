package stream.notesapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import frescoimageviewer.ImageViewer;
import me.relex.photodraweeview.PhotoDraweeView;

public class ImageViewerActivity extends AppCompatActivity {

    private PhotoDraweeView mPhotoDraweeView;
    private ImageOverlayView overlayView;
    private ArrayList<String> recentImages;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        /**
         * IMPORTANT! Enable the configuration below, if you expect to open really large images.
         * Also you can add the {@code android:largeHeap="true"} to Manifest file to avoid an OOM error.*/
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);

        //Remove notification bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_imageviewer);

        Integer position = getIntent().getIntExtra("IMAGE_PATH", 0);
        Log.d("Received Image Path", String.valueOf(position));
        recentImages = lastFileModified(mContext);

        showPicker(position);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void showPicker(int startPosition) {
        overlayView = new ImageOverlayView(this);
        new ImageViewer.Builder(this, recentImages)
                .setStartPosition(startPosition)
                //.hideStatusBar(false)
                .setImageMargin(this, R.dimen.image_margin)
                .setImageChangeListener(getImageChangeListener())
                .setOnDismissListener(getDismissListener())
                .setCustomDraweeHierarchyBuilder(getHierarchyBuilder())
                .setOverlayView(overlayView)
                .show();
    }

    private ImageViewer.OnImageChangeListener getImageChangeListener() {
        return new ImageViewer.OnImageChangeListener() {
            @Override
            public void onImageChange(int position) {
                String url = recentImages.get(position);
                overlayView.setShareText(url);
//                overlayView.setDescription(descriptions[position]);
            }
        };
    }

    private ImageViewer.OnDismissListener getDismissListener() {
        return new ImageViewer.OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
            }
        };
    }

    private GenericDraweeHierarchyBuilder getHierarchyBuilder() {
        return GenericDraweeHierarchyBuilder.newInstance(getResources());
    }

    public static ArrayList<String> lastFileModified(Context context) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        ArrayList<NotesItem> notesItems = dbHelper.GetRecentImages();
        ArrayList<String> imageItems = new ArrayList<String>();
        for (NotesItem note : notesItems)
        {
            imageItems.add(note.getNotesImage());
        }

        return imageItems;
    }
}