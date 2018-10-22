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
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import stream.rocketnotes.R;

public class SyncHeaderViewholder extends AbstractFlexibleItem<SyncHeaderViewholder.MyViewHolder> {

    private String id;
    private Activity activity;

    private final String mActivity = this.getClass().getSimpleName();

    public SyncHeaderViewholder(String id, Activity activity) {
        this.id = id;
        this.activity = activity;
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
        holder.setFullSpan(true);
    }

    public static class MyViewHolder extends FlexibleViewHolder {

        public LinearLayout mLayout;
        public TextView mSubtitle;
        ImageView mDismissIcon;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            mLayout = view.findViewById(R.id.item_sync);

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