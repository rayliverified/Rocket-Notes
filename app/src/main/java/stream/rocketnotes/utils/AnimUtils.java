package stream.rocketnotes.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import stream.rocketnotes.ui.BounceInterpolator;

public class AnimUtils {

    public static Animation Bounce(Context context, int anim, int duration) {
        // Load the animation
        final Animation animation = AnimationUtils.loadAnimation(context, anim);
        double animationDuration = duration;
        animation.setDuration((long) animationDuration);

        // Use custom animation interpolator to achieve the bounce effect
        BounceInterpolator interpolator = new BounceInterpolator(0.3, 15);
        animation.setInterpolator(interpolator);

        return animation;
    }
}
