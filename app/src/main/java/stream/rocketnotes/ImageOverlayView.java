package stream.rocketnotes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.provider.StreamProvider;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.service.DeleteNoteService;
import stream.rocketnotes.service.SaveFileService;

public class ImageOverlayView extends RelativeLayout {

    private TextView tvDescription;
    private String imageUri;
    private Integer noteID;
    private Context mContext;

    public ImageOverlayView(Context context) {
        super(context);
        init();
    }

    public ImageOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mContext = getContext();
        View view = inflate(mContext, R.layout.image_overlay_view, this);
        tvDescription = view.findViewById(R.id.tvDescription);
        view.findViewById(R.id.button_gallery).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryIntent();
            }
        });
        view.findViewById(R.id.button_share).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShareIntent();
            }
        });
        view.findViewById(R.id.button_save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openSaveIntent();
            }
        });
        view.findViewById(R.id.button_delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openDeleteIntent();
            }
        });
    }

    public void setDescription(String description) {
        tvDescription.setText(description);
    }

    public void setImageUri(String text) {
        this.imageUri = text;
    }

    public void setNoteID(Integer noteID) {
        this.noteID = noteID;
    }

    private void sendShareIntent() {

        //Sharing image requires creating a content provider. Register provider and set image path.
        Uri provider = Uri.parse("content://" + Constants.AUTHORITY);
        Uri imageUri = Uri.parse(this.imageUri);
        String imagePath = imageUri.getPath();
        String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        imagePath = ".Pictures/" + imageName;

        provider = provider
                .buildUpon()
                .appendPath(StreamProvider.getUriPrefix(Constants.AUTHORITY))
                .appendPath(imagePath)
                .build();

        String extension = imagePath.substring(imagePath.lastIndexOf('.') + 1);
        extension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        Log.d("Provider URI", String.valueOf(provider));
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, provider);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, imageName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, imageName);
        shareIntent.setType(extension);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mContext.startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    private void openGalleryIntent() {
        NotificationSender();
        Intent galleryIntent = new Intent(mContext, MainActivity.class);
        galleryIntent.setAction(Constants.STICKY);
        mContext.startActivity(galleryIntent);
    }

    private void openSaveIntent() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = new File("/mnt/sdcard/Pictures");
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(mContext, properties);
        dialog.setTitle("Save Location");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (files.length >= 1) {
                    DatabaseHelper dbHelper = new DatabaseHelper(mContext);
                    NotesItem note = dbHelper.GetNote(noteID);
                    Intent savePicture = new Intent(mContext, SaveFileService.class);
                    savePicture.putExtra(Constants.SOURCE_PATH, note.getNotesImage());
                    savePicture.putExtra(Constants.SAVE_PATH, files[0]);
                    mContext.startService(savePicture);
                } else {
                    Toasty.error(mContext, "No Location Selected", Toast.LENGTH_SHORT).show();
                }
                for (String filePath : files) {
                    Log.d("File Path", filePath);
                }
            }
        });
        dialog.show();
    }

    private void openDeleteIntent() {
        Intent deleteNote = new Intent(mContext, DeleteNoteService.class);
        deleteNote.putExtra(Constants.ID, noteID);
        deleteNote.setAction(Constants.DELETE_NOTE);
        mContext.startService(deleteNote);
        NotificationDelete();
    }

    public void NotificationSender() {
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.FILTER_IMAGES));
        Log.d("Notification", Constants.FILTER_IMAGES);
    }

    public void NotificationDelete() {
        EventBus.getDefault().post(new UpdateMainEvent(Constants.DELETE_NOTE));
        Log.d("Notification", Constants.DELETE_NOTE);
    }
}
