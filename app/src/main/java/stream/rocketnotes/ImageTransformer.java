package stream.rocketnotes;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Transformation;

public class ImageTransformer {

    public static Transformation getTransformation(final ImageView imageView) {
        return new Transformation() {

            @Override
            public Bitmap transform(Bitmap source) {
                int targetWidth = imageView.getWidth();
                Log.d("Image Transform", String.valueOf(targetWidth));
                //Source image does not load fast enough, set to fixed width to prevent crash
                if (targetWidth <= 0)
                {
                    targetWidth = 500;
                }
                //Checks if the width is great than the height (landscape or portrait)
                if (source.getWidth() < source.getHeight())
                {
                    //creates and returns a square of the original bmp if the image is portrait orientation
                    return(Bitmap.createBitmap(source, 0, 0, targetWidth, targetWidth));
                }
                else
                {
                    //Calculates source image aspect ratio
                    double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                    //Sets transformed image target height
                    int targetHeight = (int) (targetWidth * aspectRatio);
                    Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                    if (result != source) {
                        // Same bitmap is returned if sizes are the same
                        source.recycle();
                    }
                    return result;
                }
            }

            @Override
            public String key() {
                return "transformation" + " desiredWidth";
            }
        };
    }

    /*private static Bitmap crop(Bitmap srcBmp) {
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    0,
                    srcBmp.getWidth(),
                    srcBmp.getWidth(),
                    null,
                    true
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth(),
                    null,
                    true
            );
        }
        return dstBmp;
    }
    */

}