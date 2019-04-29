package stream.rocketnotes.viewholder;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;

import java.util.Collections;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import stream.rocketnotes.R;

public class SyncHeaderViewholder extends AbstractFlexibleItem<SyncHeaderViewholder.MyViewHolder> {

    public static final String SYNC_STATE_LOGGEDOUT = "SYNC_STATE_LOGGEDOUT";
    public static final String SYNC_STATE_BACKINGUP = "SYNC_STATE_BACKINGUP";
    public static final String SYNC_STATE_BACKEDUP = "SYNC_STATE_BACKEDUP";
    public static final String SYNC_STATE_ERROR = "SYNC_STATE_ERROR";

    private String id;
    private String state;
    private String text = "";
    private Activity activity;

    private final String mActivity = this.getClass().getSimpleName();

    public SyncHeaderViewholder(String id, String state, Activity activity) {
        this.id = id;
        this.state = state;
        this.activity = activity;
    }

    public SyncHeaderViewholder(String id, String state, Activity activity, String text) {
        this.id = id;
        this.state = state;
        this.activity = activity;
        this.text = text;
    }

    @Override
    public boolean equals(Object inObject) {
        return inObject instanceof SyncHeaderViewholder;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_sync;
    }

    @Override
    public MyViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new MyViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, MyViewHolder holder, int position, List payloads) {
        if(!payloads.isEmpty()) {
            if (payloads.get(0) instanceof String) {
                if (text.contains("/") && text.length() >= 3) {
                    int progress = Integer.valueOf(text.substring(0, text.indexOf("/")));
                    int total = Integer.valueOf(text.substring(text.lastIndexOf("/") + 1));
                    holder.mProgressBar.setIndeterminate(false);
                    holder.mProgressBar.setMax(total);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        holder.mProgressBar.setProgress(progress, true);
                    } else {
                        holder.mProgressBar.setProgress(progress);
                    }
                    holder.mProgressText.setText(text);
                }
            }
        }else {
            final Context context = holder.itemView.getContext();
            holder.setFullSpan(true);

            switch (state) {
                case SYNC_STATE_LOGGEDOUT:
                    holder.mLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Choose authentication providers
                            List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build());
                            activity.startActivityForResult(AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(), 1);
                        }
                    });
                    holder.mTitle.setVisibility(View.VISIBLE);
                    holder.mProgressBar.setVisibility(View.GONE);
                    holder.mButton.setVisibility(View.VISIBLE);
                    holder.mProgressText.setVisibility(View.GONE);
                    holder.mTitle.setText(context.getString(R.string.sync_loggedout_text));
                    holder.mImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_cloud_off));
                    holder.mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.setAnimationOnForwardScrolling(true).setAnimationOnReverseScrolling(true);
                            adapter.removeScrollableHeader(SyncHeaderViewholder.this);
                        }
                    });
                    break;
                case SYNC_STATE_BACKINGUP:
                    holder.mLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                    holder.mTitle.setVisibility(View.GONE);
                    holder.mProgressBar.setVisibility(View.VISIBLE);
                    holder.mButton.setVisibility(View.GONE);
                    holder.mProgressText.setVisibility(View.VISIBLE);
                    holder.mImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_cloud_upload));
                    holder.mProgressBar.setIndeterminate(true);
                    holder.mProgressText.setText(context.getString(R.string.sync_starting_text));
                    break;
                case SYNC_STATE_BACKEDUP:
                    holder.mLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.setAnimationOnForwardScrolling(true).setAnimationOnReverseScrolling(true);
                            adapter.removeScrollableHeader(SyncHeaderViewholder.this);
                        }
                    });
                    holder.mTitle.setVisibility(View.VISIBLE);
                    holder.mProgressBar.setVisibility(View.GONE);
                    holder.mButton.setVisibility(View.VISIBLE);
                    holder.mProgressText.setVisibility(View.GONE);
                    holder.mTitle.setText(context.getString(R.string.sync_backedup_text));
                    holder.mImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_cloud_done));
                    holder.mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.setAnimationOnForwardScrolling(true).setAnimationOnReverseScrolling(true);
                            adapter.removeScrollableHeader(SyncHeaderViewholder.this);
                        }
                    });
                    break;
                case SYNC_STATE_ERROR:
                    holder.mLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.setAnimationOnForwardScrolling(true).setAnimationOnReverseScrolling(true);
                            adapter.removeScrollableHeader(SyncHeaderViewholder.this);
                        }
                    });
                    holder.mTitle.setVisibility(View.VISIBLE);
                    holder.mProgressBar.setVisibility(View.GONE);
                    holder.mButton.setVisibility(View.VISIBLE);
                    holder.mProgressText.setVisibility(View.GONE);
                    holder.mTitle.setText(context.getString(R.string.sync_error_text));
                    holder.mImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_check_fit)); //TODO REMOVE Error drawable icon.
                    holder.mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.setAnimationOnForwardScrolling(true).setAnimationOnReverseScrolling(true);
                            adapter.removeScrollableHeader(SyncHeaderViewholder.this);
                        }
                    });
                    break;
            }
        }
    }

    public static class MyViewHolder extends FlexibleViewHolder {

        LinearLayout mLayout;
        TextView mTitle;
        ImageView mImage;
        ProgressBar mProgressBar;
        ImageButton mButton;
        TextView mProgressText;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            mLayout = view.findViewById(R.id.item_sync);
            mTitle = view.findViewById(R.id.text_title);
            mImage = view.findViewById(R.id.image_sync);
            mProgressBar = view.findViewById(R.id.progress_horizontal);
            mButton = view.findViewById(R.id.btn_action);
            mProgressText = view.findViewById(R.id.text_progress);

            //Set fullwidth.
            setFullSpan(true);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0);
            AnimatorHelper.setDuration(animators, 500L);
        }
    }
}