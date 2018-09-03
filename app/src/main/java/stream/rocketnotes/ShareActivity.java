package stream.rocketnotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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

import com.github.ybq.android.spinkit.style.Wave;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.pyze.android.Pyze;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.service.SaveImageService;
import stream.rocketnotes.service.SaveNoteService;
import stream.rocketnotes.ui.CustomImageView;
//import stream.rocketnotes.utils.AnalyticsUtils;
import stream.rocketnotes.utils.FileUtils;
import stream.rocketnotes.utils.PermissionUtils;
import stream.rocketnotes.utils.Units;

public class ShareActivity extends Activity {

    EditText editText;
    TextView editDetails;
    ImageButton editSubmit;
    CustomImageView editImage;
    Intent shareIntent;
    String noteType;
    Uri imageUri;
    String imageName;
    ProgressBar progressBar;
    Future<File> downloading;
    DatabaseHelper dbHelper;
    ArrayList<NotesItem> NoteList = new ArrayList<>();
    RecyclerView mRecyclerView;
    ShareAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;
    String mActivity = this.getClass().getSimpleName();
    Context mContext;

    private boolean textNote = false;
    private boolean savedNote = false;
    private boolean fileDownloading = false;
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
        lp.y = Units.dpToPx(mContext, 35); // top margin
        lp.gravity = (Gravity.TOP);
        window.setAttributes(lp);

        //Flag allows window to overlap status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        //FLAG_NOT_TOUCH_MODAL passes through touch events to objects underneath view
        //FLAG_WATCH_OUTSIDE_TOUCH dismisses window when outside touch is detected
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Hides keyboard from focusing on editText when Activity starts
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

//        InitializeAnalytics();

        //Focus defaults to editText, set again just in case
        editText = findViewById(R.id.edit_edit);
        editText.clearFocus();

        editDetails = findViewById(R.id.edit_details);
        editSubmit = findViewById(R.id.edit_submit);
        editImage = findViewById(R.id.edit_image);
        progressBar = findViewById(R.id.edit_progress);

        if (!PermissionUtils.IsPermissionEnabled(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //Notify user that permission is not enabled via editText
            editText.setText("Storage permission required to save notes.");
            editText.setFocusable(false);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSIONS);
        }

        //Check if shared data is text or image.
        boolean validShare = false;

        //Get data shared to app
        if (getIntent() != null) {
            if (getIntent().getAction() != null) {
                shareIntent = getIntent();
                String action = getIntent().getAction();
                noteType = getIntent().getType();
                if (action.equals(Intent.ACTION_SEND) && noteType != null) {
                    Log.d("ShareActivity", action);
                    Log.d("ShareActivity", noteType);
                    if ("text/plain".equals(noteType)) {
                        textNote = shareText(shareIntent);
                        validShare = true;
                    } else if (noteType.startsWith("image/")) {
                        shareImage(shareIntent);
                        validShare = true;
                    }
                }
            }
        }

        if (!validShare) {
            Toasty.error(mContext, "Invalid content", Toast.LENGTH_SHORT).show();
            finish();
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

        if (textNote) {
            ShowRecentNotes();
        }
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
//                    AnalyticsUtils.AnalyticEvent(mActivity, "Permission", "Granted");
                    recreate();
                } else {
//                    AnalyticsUtils.AnalyticEvent(mActivity, "Permission", "Denied");
                    Toasty.error(mContext, "Permission Denied", Toast.LENGTH_SHORT, true).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        //Listen for new messages received
        Log.d(mActivity, "onResume");
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        Log.d(mActivity, "OnPause");
        //Share attempt has been canceled by user. Delete any remnants
        if (!savedNote && downloading != null) {
            Log.d("Download", "Cancel");
            //Cancel pending download if not completed
            downloading.cancel();
            downloading = null;
            //If download has been saved, delete download.
            if (fileDownloaded == true && imageUri != null) {
                File imageFile = new File(imageUri.getPath());
                imageFile.delete();
                Log.d("Download", "Delete");
                finish();
            }
        }
    }

    public boolean shareText(Intent intent) {
        String shareText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (shareText != null) {
            Log.d("ShareText", "Not Null");
            //Detect if sharedText is URL link
            if (android.util.Patterns.WEB_URL.matcher(shareText).matches()) {
                Log.d("ShareURL", shareText);
                //Detect if shared URL is an Image
                String imageType = shareText.substring(shareText.lastIndexOf(".") + 1);
                if ("png".equals(imageType) || "jpg".equals(imageType) ||
                        "jpeg".equals(imageType) || "bmp".equals(imageType)) {
                    fileDownloading = true;
                    //Create location to save image
                    FileUtils.InitializePicturesFolder(mContext);
                    //Set editDetails text to Image Note
                    editDetails.setText("New Image Note • now");
                    //Passed URL must start with http:// to be accepted by image loader
                    if (!shareText.startsWith("http")) {
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
                                    fileDownloading = false;
                                    if (file != null) {
                                        //Load downloaded file into ImageView
                                        Ion.with(mContext)
                                                .load(file)
                                                .withBitmap()
                                                .intoImageView(editImage);
                                        //Set imageURI to File scheme where image has been saved.
                                        imageUri = Uri.parse("file://" + file.getAbsolutePath());
                                        Log.d("ImageURI", String.valueOf(imageUri));
                                        fileDownloaded = true;
                                    } else {
                                        //Image URL is invalid, display editText instead.
                                        progressBar.setVisibility(View.GONE);
                                        editImage.setVisibility(View.GONE);
                                        editText.setEnabled(true);
                                        //Set editDetails text to Text Note
                                        editDetails.setText("New Text Note • now");
                                        ShowRecentNotes();
                                    }
                                }
                            });
                    return false;
                } else {
                    editText.setText(shareText);
                    return true;
                }
            } else {
                editText.setText(shareText);
                return true;
            }
        }
        return false;
    }

    public void shareImage(Intent intent) {
        //Set editDetails text to Image Note
        editDetails.setText("New Image Note • now");
        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.d("Image URI", String.valueOf(imageUri));
            setImageName();
            //Enable imageView and display shared image.
            editImage.setVisibility(View.VISIBLE);
            Picasso.get().load(imageUri).into(editImage);
            //Create Pictures folder to prepare to copy file into folder on savedNote clicked.
            FileUtils.InitializePicturesFolder(mContext);
        }
    }

    private void saveNote() {
        //Wait for images to load before allowing user to save.
        if (!fileDownloading) {
            //Save note and close activity
            //Multiple content may be sent to Rocket Notes. Force refresh of main feed.
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.REFRESH, true);
            editor.apply();

            if ("text/plain".equals(noteType)) {
                //Shared text may have originated as an Image URL
                if (imageUri != null) {
                    //Note is saved. Do not delete downloaded image file.
                    savedNote = true;
                    Intent saveNote = new Intent(mContext, SaveNoteService.class);
                    saveNote.putExtra(Constants.IMAGE, imageUri.toString());
                    saveNote.setAction(Constants.NEW_NOTE);
                    mContext.startService(saveNote);
                    finish();
                } else if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
//                    AnalyticsUtils.AnalyticEvent(mActivity, "SaveNote", "Text");
                    Intent saveNote = new Intent(mContext, SaveNoteService.class);
                    saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                    saveNote.setAction(Constants.NEW_NOTE);
                    mContext.startService(saveNote);
                    finish();
                } else {
                    Toasty.warning(mContext, "Note Empty", Toast.LENGTH_SHORT, true).show();
                }
            } else if (noteType.startsWith("image/")) {
                if (imageUri != null) {
                    Log.d("Image URI", String.valueOf(imageUri));
//                    AnalyticsUtils.AnalyticEvent(mActivity, "SaveImage", "Image");
                    String savePath = getFilesDir() + "/.Pictures";
                    Intent savePicture = new Intent(mContext, SaveImageService.class);
                    savePicture.putExtra(Constants.SOURCE_PATH, imageUri.toString());
                    savePicture.putExtra(Constants.SAVE_PATH, savePath);
                    mContext.startService(savePicture);
                    finish();
                } else {
                    Toasty.error(mContext, "Image Unsupported", Toast.LENGTH_SHORT, true).show();
                }
            }
        } else {
            Toasty.warning(mContext, "Please wait", Toast.LENGTH_SHORT, true).show();
        }
    }

    private void setImageName() {
        //Use editText to display image name.
        //Get File Name from URI and encode into readable format
        imageName = FileUtils.GetFileNameFromUri(mContext, imageUri);
        if (imageName != null) {
            editText.setText(imageName);
            editText.setEnabled(false);
        }
    }

    @Subscribe(sticky = false, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateMainEvent event) {
        Log.d(mActivity, event.getAction());
        if (event.getAction().equals(Constants.ADDTO_NOTE)) {
            UpdateNote(event);
        }
    }

    public void UpdateNote(UpdateMainEvent event) {
        Integer noteID = event.getID();
        String noteTextRaw = event.getNoteText();

        if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
            Intent saveNote = new Intent(mContext, SaveNoteService.class);
            saveNote.putExtra(Constants.ID, noteID);
            saveNote.putExtra(Constants.BODY, noteTextRaw + "<br>" + editText.getText().toString().trim());
            saveNote.setAction(Constants.UPDATE_NOTE);
            mContext.startService(saveNote);
        }
        RemoveSticky();
    }

    public void RemoveSticky() {
        UpdateMainEvent stickyEvent = EventBus.getDefault().getStickyEvent(UpdateMainEvent.class);
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent);
        }
    }

    private void ShowRecentNotes() {
        dbHelper = new DatabaseHelper(mContext);
        mRecyclerView = findViewById(R.id.share_recycler);
        mRecyclerView.setVisibility(View.VISIBLE);
        mAdapter = new ShareAdapter(this, NoteList);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(layoutManager);

        LoadNotes();
    }


    private void LoadNotes() {
        NoteList.addAll(dbHelper.GetTextNotes(Constants.RECENT_NOTES));
        mAdapter.notifyDataSetChanged();
    }

    public void InitializeAnalytics() {
        Pyze.initialize(getApplication());
//        UXCam.startWithKey(Constants.UXCAM_API_KEY);
//        UXCam.occludeSensitiveScreen(true);
    }
}
