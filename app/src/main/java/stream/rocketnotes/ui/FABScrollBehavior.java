package stream.rocketnotes.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.FabSpeedDialBehaviour;

//Scroll behavior code from https://github.com/yavski/fab-speed-dial/issues/59

public class FABScrollBehavior extends FabSpeedDialBehaviour {

    int scrollDist = 0;
    boolean isVisible = true;
    static final float MINIMUM = 0;

    public FABScrollBehavior(Context context, AttributeSet attributeSet) {
        super();
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FabSpeedDial child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

//        if (isVisible && scrollDist > MINIMUM) {
//            hide(child);
//            scrollDist = 0;
//            isVisible = false;
//        }
//        else if (!isVisible && scrollDist < -MINIMUM) {
//            show(child);
//            scrollDist = 0;
//            isVisible = true;
//        }
//        if ((isVisible && dyConsumed > 0) || (!isVisible && dyConsumed < 0)) {
//            scrollDist += dyConsumed;
//        }

        if (dyConsumed > 0) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            int fab_bottomMargin = layoutParams.bottomMargin;
            child.animate().translationY(child.getHeight() + fab_bottomMargin).setInterpolator(new LinearInterpolator()).start();
        } else if (dyConsumed < 0) {
            child.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FabSpeedDial child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    public void show(FabSpeedDial child) {
        child.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    public void hide(FabSpeedDial child) {
        child.animate().translationY(child.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();
    }
}
