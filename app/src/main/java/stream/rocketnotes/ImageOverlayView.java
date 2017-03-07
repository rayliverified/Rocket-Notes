package stream.rocketnotes;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import frescoimageviewer.OnDismissListener;

public class ImageOverlayView extends RelativeLayout {

    private TextView tvDescription;
    private String sharingText;
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

    public void setDescription(String description) {
        tvDescription.setText(description);
    }

    public void setShareText(String text) {
        this.sharingText = text;
    }

    public void setNoteID(Integer noteID) {
        this.noteID = noteID;
    }

    private void init() {

        mContext = getContext();
        View view = inflate(mContext, R.layout.view_image_overlay, this);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
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
        view.findViewById(R.id.button_delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openDeleteIntent();
            }
        });
    }

    private void sendShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        sendIntent.setType("text/plain");
        mContext.startActivity(sendIntent);
    }

    private void openGalleryIntent()
    {
        NotificationSender();
        Intent galleryIntent = new Intent(mContext, MainActivity.class);
        galleryIntent.setAction(Constants.STICKY);
        mContext.startActivity(galleryIntent);
    }

    private void openDeleteIntent()
    {
        Intent deleteNote = new Intent(mContext, DeleteNoteService.class);
        deleteNote.putExtra(Constants.ID, noteID);
        deleteNote.setAction(Constants.DELETE_NOTE);
        mContext.startService(deleteNote);
        NotificationDelete();
    }


    public void NotificationSender()
    {
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.FILTER_IMAGES));
        Log.d("Notification", Constants.FILTER_IMAGES);
    }

    public void NotificationDelete()
    {
        EventBus.getDefault().post(new UpdateMainEvent(Constants.DELETE_NOTE));
        Log.d("Notification", Constants.DELETE_NOTE);
    }
}
