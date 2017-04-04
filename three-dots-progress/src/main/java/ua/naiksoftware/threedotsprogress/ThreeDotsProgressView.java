package ua.naiksoftware.threedotsprogress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by savchenko_n on 04.04.17.
 */
public class ThreeDotsProgressView extends View {

    public static final int DEFAULT_SPEED = 200; // ms

    private int mDotSizeNormal;
    private int mDotSizeBig;
    private int mDotColorNormal;
    private int mDotColorBig;
    private int mSpeed;

    private DotState[] mDotStates;
    private ValueAnimator mAnimator;
    private int mCurrentAnimatedDot;
    private Paint mPaint;

    public ThreeDotsProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public ThreeDotsProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ThreeDotsProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ThreeDotsProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        Resources resources = context.getResources();
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ThreeDotsProgressView);

        mDotSizeNormal = attributes.getDimensionPixelSize(R.styleable.ThreeDotsProgressView_dotSizeNormal, resources.getDimensionPixelSize(R.dimen.default_dot_size_normal));
        mDotSizeBig = attributes.getDimensionPixelSize(R.styleable.ThreeDotsProgressView_dotSizeBig, resources.getDimensionPixelSize(R.dimen.default_dot_size_big));
        mDotColorNormal = attributes.getColor(R.styleable.ThreeDotsProgressView_dotColorNormal, resources.getColor(R.color.default_dot_color_normal));
        mDotColorBig = attributes.getColor(R.styleable.ThreeDotsProgressView_dotColorBig, resources.getColor(R.color.default_dot_color_big));
        mSpeed = attributes.getInteger(R.styleable.ThreeDotsProgressView_dotSpeed, DEFAULT_SPEED);

        attributes.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mDotStates = new DotState[3];
        for (int i = 0; i < mDotStates.length; i++) {
            mDotStates[i] = new DotState(mDotColorNormal, mDotSizeNormal);
        }

        if (getVisibility() == VISIBLE) startAnimation();
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE) startAnimation();
        else stopAnimation();
        super.setVisibility(visibility);
    }

    private void startAnimation() {
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(mSpeed);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int dotIndex = mCurrentAnimatedDot;
                DotState dot;
                float fraction = animation.getAnimatedFraction();
                for (int i = 0; i < mDotStates.length; i++) {
                    dot = mDotStates[i];
                    if (i == dotIndex) {
                        dot.color = interpolateColor(fraction, mDotColorNormal, mDotColorBig);
                        dot.size = (int) (mDotSizeNormal + (mDotSizeBig - mDotSizeNormal) * fraction);
                    } else {
                        dot.color = mDotColorNormal;
                        dot.size = mDotSizeNormal;
                    }
                }
                invalidate();
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            boolean skipRepeat = true;
            @Override
            public void onAnimationRepeat(Animator animation) {
                if (!skipRepeat) {
                    int nextDot = mCurrentAnimatedDot + 1;
                    if (nextDot >= mDotStates.length) nextDot = 0;
                    mCurrentAnimatedDot = nextDot;
                }
                skipRepeat = !skipRepeat;
            }
        });
        mAnimator.start();
    }

    private void stopAnimation() {
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        DotState dot;
        for (int i = 0; i < mDotStates.length; i++) {
            dot = mDotStates[i];
            mPaint.setColor(dot.color);
            canvas.drawCircle(mDotSizeBig * i + mDotSizeBig / 2, mDotSizeBig / 2, dot.size / 2, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(mDotSizeBig * 3, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mDotSizeBig, MeasureSpec.EXACTLY));
    }

    public static int interpolateColor(float fraction, int startValue, int endValue) {
        int startA = (startValue >> 24) & 0xff;
        int startR = (startValue >> 16) & 0xff;
        int startG = (startValue >> 8) & 0xff;
        int startB = startValue & 0xff;
        int endA = (endValue >> 24) & 0xff;
        int endR = (endValue >> 16) & 0xff;
        int endG = (endValue >> 8) & 0xff;
        int endB = endValue & 0xff;
        return ((startA + (int) (fraction * (endA - startA))) << 24) |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB))));
    }

    private static final class DotState {
        int color, size;

        DotState(int color, int size) {
            this.color = color;
            this.size = size;
        }
    }
}
