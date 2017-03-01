package stream.notesapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CustomImageView extends ImageView {

    private Context context;

    private int mTopLeftRadius;
    private int mTopRightRadius;
    private int mBottomLeftRadius;
    private int mBottomRightRadius;
    private float[] radius;
    private Path path;
    private RectF rect;

    public CustomImageView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs, 0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width;
        int height;

        width = MeasureSpec.getSize(widthMeasureSpec);
        if (getDrawable() != null)
        {
            height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
        }
        else
        {
            height = MeasureSpec.getSize(0);
        }

        //MUST call this to store the measurements
        setMeasuredDimension(width, height);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (!isInEditMode()) {

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
