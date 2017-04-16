package stream.rocketnotes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;
import com.uxcam.UXCam;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import frescoimageviewer.ImageViewer;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageOverlayView overlayView;
    private ArrayList<NotesItem> mNotesItem;
    private MixpanelAPI mixpanel;
    private String mActivity = "ImageViewerActivity";
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getApplicationContext();
        initializeAnalytics();
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

        Integer position = getIntent().getIntExtra(Constants.IMAGE, 0);
        Integer noteID = getIntent().getIntExtra(Constants.ID, -1);
        Log.d("Image Position", String.valueOf(position));
        if (getIntent().getAction().equals(Constants.OPEN_IMAGE))
        {
            mNotesItem = recentImages(mContext);
        }
        else if (getIntent().getAction().equals(Constants.OPEN_IMAGE_SINGLE))
        {
            mNotesItem = singleImage(mContext, noteID);
        }
        showPicker(position);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void showPicker(int startPosition) {
        overlayView = new ImageOverlayView(this);
        ArrayList<String> imageItems = new ArrayList<String>();
        for (NotesItem note : mNotesItem)
        {
            imageItems.add(note.getNotesImage());
            Log.d("Image URL", note.getNotesImage());
        }
        new ImageViewer.Builder(this, imageItems)
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
                String url = mNotesItem.get(position).getNotesImage();
                overlayView.setShareText(url);
                overlayView.setNoteID(mNotesItem.get(position).getNotesID());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateMainEvent event) {
        Log.d("ImageViewerActivity", "Update Received");
        if (event.getAction().equals(Constants.DELETE_NOTE)) {
            getDismissListener().onDismiss();
        }
    }

    @Override
    protected void onResume() {
        //Listen for new messages received
        Log.d("LocalBroadcastManager", "onResume");
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("LocalBroadcastManager", "onPause");
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    private GenericDraweeHierarchyBuilder getHierarchyBuilder() {
        return GenericDraweeHierarchyBuilder.newInstance(getResources());
    }

    public static ArrayList<NotesItem> recentImages(Context context) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        ArrayList<NotesItem> notesItems = dbHelper.GetRecentImages();

        return notesItems;
    }

    public static ArrayList<NotesItem> singleImage(Context context, Integer id) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        NotesItem note = dbHelper.GetNote(id);
        ArrayList<NotesItem> notesItems = new ArrayList<NotesItem>();
        notesItems.add(note);

        return notesItems;
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