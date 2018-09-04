package stream.rocketnotes.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Units {

    /**
     * Converts dp to pixels.
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Converts pixels to dp.
     */
    public static int pxToDp(Context context, int px) {
        return Math.round(px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static float spToPx(Context context, float sp) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return sp * scaledDensity;
    }

    public static float pxToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }
}
