package stream.rocketnotes.viewholder;

import android.animation.Animator;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import stream.rocketnotes.R;

public class SyncHeaderViewholder extends AbstractFlexibleItem<SyncHeaderViewholder.MyViewHolder> {

    private String id;

    public SyncHeaderViewholder(String id) {
        this.id = id;
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
        Context context = holder.itemView.getContext();
        holder.setFullSpan(true);
    }

    public static class MyViewHolder extends FlexibleViewHolder {

        public TextView mTitle;
        public TextView mSubtitle;
        ImageView mDismissIcon;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);

            // Support for StaggeredGridLayoutManager
            setFullSpan(true);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.scaleAnimator(animators, itemView, 0);
            AnimatorHelper.setDuration(animators, 500L);
        }
    }
}