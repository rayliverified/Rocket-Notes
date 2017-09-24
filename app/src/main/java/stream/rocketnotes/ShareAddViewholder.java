package stream.rocketnotes;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import stream.rocketnotes.ui.BounceInterpolator;

public class ShareAddViewholder extends RecyclerView.ViewHolder {

    public NotesItem note;
    public TextView mTitle;
    public TextView mBody;
    private String noteTextRaw;
    public ImageButton mBtnSend;
    public Context mContext;
    public ShareAdapterInterface mShareAdapterInterface;
    public final String mActivity = "ShareAddViewHolder";

    public boolean sharedNote;

    public ShareAddViewholder(View itemView) {
        super(itemView);

        mTitle = (TextView) itemView.findViewById(R.id.note_title);
        mBody = (TextView) itemView.findViewById(R.id.note_body);
        mBtnSend = (ImageButton) itemView.findViewById(R.id.btn_send);
        mContext = itemView.getContext();
    }

    public void setNote(final NotesItem note) {

        this.note = note;

        sharedNote = note.getShared();
        noteTextRaw = note.getNotesNote();
        final ArrayList<String> noteText = NoteHelper.getNote(stream.rocketnotes.utils.TextUtils.Compatibility(note.getNotesNote()));
        mTitle.setText(stream.rocketnotes.utils.TextUtils.fromHtml(noteText.get(0)));
        if (!TextUtils.isEmpty(noteText.get(1))) {
            mBody.setText(stream.rocketnotes.utils.TextUtils.fromHtml(noteText.get(1).replaceAll("<b>", " ")));
        } else {
            mBody.setVisibility(View.GONE);
        }

        if (sharedNote) {
            SetButtonShared();
        }
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnSend.startAnimation(AnimateButton());
                SetButtonShared();
                if (!sharedNote) {
                    EventBus.getDefault().post(new UpdateMainEvent(Constants.ADDTO_NOTE, note.getNotesID(), noteTextRaw));
                    sharedNote = true;
                    SetSharedNote(sharedNote);
                }
            }
        });
    }

    private void SetButtonShared() {
        mBtnSend.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_check_fit));
        mBtnSend.setBackground(ContextCompat.getDrawable(mContext, R.drawable.icon_add_activated));
    }

    private Animation AnimateButton() {
        // Load the animation
        final Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_button);
        double animationDuration = 1250;
        animation.setDuration((long) animationDuration);

        // Use custom animation interpolator to achieve the bounce effect
        BounceInterpolator interpolator = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(interpolator);

        return animation;

        // Run button animation again after it finished
//        animation.setAnimationListener(new Animation.AnimationListener(){
//            @Override
//            public void onAnimationStart(Animation arg0) {}
//
//            @Override
//            public void onAnimationRepeat(Animation arg0) {}
//
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//                AnimateButton();
//            }
//        });
    }

    public void SetSharedNote(boolean shared) {
        mShareAdapterInterface.ShareNote(getAdapterPosition(), shared);
    }

    public void setShareAdapterInterface(ShareAdapterInterface shareAdapterInterface) {
        this.mShareAdapterInterface = shareAdapterInterface;
    }
}
