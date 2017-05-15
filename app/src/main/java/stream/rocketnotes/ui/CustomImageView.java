package stream.rocketnotes.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import stream.rocketnotes.R;

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

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomImageView, 0, defStyle);
        if (a != null) {
            try {
                mTopLeftRadius = a.getInteger(R.styleable.CustomImageView_topLeftRadius, 0);
                mTopRightRadius = a.getInteger(R.styleable.CustomImageView_topRightRadius, 0);
                mBottomLeftRadius = a.getInteger(R.styleable.CustomImageView_bottomLeftRadius, 0);
                mBottomRightRadius = a.getInteger(R.styleable.CustomImageView_bottomRightRadius, 0);
                setRadius(mTopLeftRadius, mTopRightRadius, mBottomLeftRadius, mBottomRightRadius);
            }
            finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width;
        int height;

        width = MeasureSpec.getSize(widthMeasureSpec);
        if (getDrawable() != null)
        {
            height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
            if (height > 720)
            {
                height = 720;
            }
        }
        else
        {
            height = MeasureSpec.getSize(0);
        }

        //MUST call this to store the measurements
        setMeasuredDimension(width, height);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (!isInEditMode() && attrs != null) {
            initAttributes(context, attrs, defStyleAttr);
        }
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomImageView, 0, defStyleAttr);
        if (a != null) {
            try {
                mTopLeftRadius = a.getInteger(R.styleable.CustomImageView_topLeftRadius, 0);
                mTopRightRadius = a.getInteger(R.styleable.CustomImageView_topRightRadius, 0);
                mBottomLeftRadius = a.getInteger(R.styleable.CustomImageView_bottomLeftRadius, 0);
                mBottomRightRadius = a.getInteger(R.styleable.CustomImageView_bottomRightRadius, 0);
                getRoundRectPath();
            } finally {
                a.recycle();
            }
        }
    }

    public void setRadius(int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius)
    {
        mTopLeftRadius = topLeftRadius;
        mTopRightRadius = topRightRadius;
        mBottomLeftRadius = bottomLeftRadius;
        mBottomRightRadius = bottomRightRadius;
        path = getRoundRectPath();
        invalidate();
    }

    private Path getRoundRectPath() {
        path = new Path();

        rect = new RectF(0, 0, getWidth(), getHeight());
        radius = new float[]{mTopLeftRadius, mTopLeftRadius,
                mTopRightRadius, mTopRightRadius,
                mBottomRightRadius, mBottomRightRadius,
                mBottomLeftRadius, mBottomLeftRadius};

        path.addRoundRect(rect, radius, Path.Direction.CW);
        return path;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipPath(getRoundRectPath());
        super.onDraw(canvas);
    }
}
