package stream.rocketnotes.ui;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class MoveViewOnScrollListener extends RecyclerView.OnScrollListener {

    private float alpha = 1f;
    private float scrollY = 0f;
    private float scrollThreshold;

    private int heightViewToHide;
    private final View mView;

    public MoveViewOnScrollListener(final View view) {
        this.mView = view;

        scrollThreshold = view.getY();
        Log.d("Threshhold", String.valueOf(scrollThreshold));
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        scrollY += dy;

        if (scrollY < scrollThreshold)
        {
            mView.setTranslationY(dy);
        }
    }
}