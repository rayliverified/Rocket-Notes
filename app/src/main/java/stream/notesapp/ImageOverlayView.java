package stream.notesapp;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ImageOverlayView extends RelativeLayout {

    private TextView tvDescription;

    private String sharingText;

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

    private void init() {
        View view = inflate(getContext(), R.layout.view_image_overlay, this);
        tvDescription = (TextView) view.findViewById(R.id.tvDescription);
        view.findViewById(R.id.button_share).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShareIntent();
            }
        });
        view.findViewById(R.id.button_gallery).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryIntent();
            }
        });
    }

    private void sendShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sharingText);
        sendIntent.setType("text/plain");
        getContext().startActivity(sendIntent);
    }

    private void openGalleryIntent()
    {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        ArrayList<NotesItem> notesItems = dbHelper.GetRecentImages();
        ArrayList<String> imageItems = new ArrayList<String>();
        for (NotesItem note : notesItems)
        {
            imageItems.add(note.getNotesImage());
        }
    }
}
