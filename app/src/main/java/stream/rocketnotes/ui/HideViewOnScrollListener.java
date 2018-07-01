package stream.rocketnotes.ui;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

public class HideViewOnScrollListener extends RecyclerView.OnScrollListener {

    private float alpha = 1.f;
    private float scrolly = 0.f;

    private int heightViewToHide;
    private final View viewToHide;

    public HideViewOnScrollListener(final View viewToHide) {
        this.viewToHide = viewToHide;

        heightViewToHide = viewToHide.getHeight();
        if (heightViewToHide == 0) {

            ViewTreeObserver viewTreeObserver = viewToHide.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    heightViewToHide = viewToHide.getHeight();

                    if (heightViewToHide > 0)
                        viewToHide.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        scrolly += dy;

        Log.d("ScrollY", String.valueOf(scrolly));
        Log.d("Scroll", String.valueOf(dy));

        alpha = (heightViewToHide - scrolly) / heightViewToHide;

        if (alpha < 0.f) alpha = 0.f;
        if (alpha > 1.0f) alpha = 1.f;

        if (dy < 0 && scrolly > heightViewToHide) {
            alpha = 0.f;
        }

        viewToHide.setAlpha(alpha);
    }
}