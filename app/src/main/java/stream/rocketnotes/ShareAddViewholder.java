package stream.rocketnotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ShareAddViewholder extends RecyclerView.ViewHolder {

    public RelativeLayout mContainer;
    public NotesItem note;
    public TextView mTitle;
    public TextView mBody;
    public Button mBtnSend;
    public Context mContext;

    public ShareAddViewholder(View itemView) {
        super(itemView);

        mContainer = (RelativeLayout) itemView.findViewById(R.id.container);
        mTitle = (TextView) itemView.findViewById(R.id.note_title);
        mBody = (TextView) itemView.findViewById(R.id.note_body);
        mBtnSend = (Button) itemView.findViewById(R.id.btn_send);
        mContext = itemView.getContext();
    }

    public void setNote(final NotesItem note) {

        this.note = note;

        ArrayList<String> noteText = NoteHelper.getNote(stream.rocketnotes.utils.TextUtils.Compatibility(note.getNotesNote()));
        mTitle.setText(stream.rocketnotes.utils.TextUtils.fromHtml(noteText.get(0)));
        if (!TextUtils.isEmpty(noteText.get(1)))
        {
            mBody.setText(stream.rocketnotes.utils.TextUtils.fromHtml(noteText.get(1).replaceAll("<b>", " ")));
        }
        else
        {
            mBody.setVisibility(View.GONE);
        }
    }
}
