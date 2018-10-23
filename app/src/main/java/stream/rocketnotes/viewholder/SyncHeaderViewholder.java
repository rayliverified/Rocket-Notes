package stream.rocketnotes.viewholder;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import stream.rocketnotes.R;

public class SyncHeaderViewholder extends AbstractFlexibleItem<SyncHeaderViewholder.MyViewHolder> {

    public static final String SYNC_STATE_LOGGEDOUT = "SYNC_STATE_LOGGEDOUT";
    public static final String SYNC_STATE_BACKINGUP = "SYNC_STATE_BACKINGUP";
    public static final String SYNC_STATE_BACKEDUP = "SYNC_STATE_BACKEDUP";

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
    public void bindViewHolder(FlexibleAdapter adapter, MyViewHolder holder, int position, List payloads) {
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
                holder.mTitle.setText(context.getString(R.string.sync_loggedout_text));
                holder.mImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_cloud_off));
                break;
            case SYNC_STATE_BACKINGUP:
                break;
            case SYNC_STATE_BACKEDUP:
                break;
        }
    }

    public static class MyViewHolder extends FlexibleViewHolder {

        public LinearLayout mLayout;
        public TextView mTitle;
        ImageView mImage;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            mLayout = view.findViewById(R.id.item_sync);
            mTitle = view.findViewById(R.id.text_title);
            mImage = view.findViewById(R.id.image_sync);

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