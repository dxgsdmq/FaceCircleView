package com.yangcoder.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

public class FaceCircleView extends AppCompatImageView implements ValueAnimator.AnimatorUpdateListener {

    private final int defaultAnimLineHeight = 10;
    private final int defaultAnimLineWidth = 40;
    private final int defaultAnimLineColor = Color.argb(255, 255, 0, 0);
    private final int defaultBgMaskColor = Color.argb(250, 255, 255, 255);
    private final float defaultFaceCircleWidthRatio = 2f / 3;
    private final float defaultFaceCircleTopRatio = 1f / 3;
    private final int defaultAnimFaceCircleGap = 20;

    private int mAnimLineHeight = defaultAnimLineHeight;
    private int mAnimLineWidth = defaultAnimLineWidth;
    private int mAnimLineColor = defaultAnimLineColor;
    private int mAnimBaseAlpha = 0;

    private int mBgMaskColor = defaultBgMaskColor;

    private float mFaceCircleWidthRatio = defaultFaceCircleWidthRatio;
    private float mFaceCircleTopRatio = defaultFaceCircleTopRatio;
    private int mAnimFaceCircleGap = defaultAnimFaceCircleGap;

    private int mFaceCircleWidth;
    private int mAnimCircleWidth;

    private Paint mPaint;
    private PorterDuffXfermode mPorterDuffXfermode;
    private ValueAnimator mAnimator;

    public FaceCircleView(Context context) {
        super(context);
        init(context, null);
    }

    public FaceCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FaceCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FaceCircleView);
            mAnimLineHeight = ta.getDimensionPixelSize(R.styleable.FaceCircleView_animLineHeight, defaultAnimLineHeight);
            mAnimLineWidth = ta.getDimensionPixelSize(R.styleable.FaceCircleView_animLineWidth, defaultAnimLineWidth);
            mAnimLineColor = ta.getColor(R.styleable.FaceCircleView_animLineColor, defaultAnimLineColor);
            mBgMaskColor = ta.getColor(R.styleable.FaceCircleView_bgMaskColor, defaultBgMaskColor);
            mFaceCircleWidthRatio = ta.getFloat(R.styleable.FaceCircleView_faceCircleWidthRatio, defaultFaceCircleWidthRatio);
            mFaceCircleTopRatio = ta.getFloat(R.styleable.FaceCircleView_faceCircleTopRatio, defaultFaceCircleTopRatio);
            mAnimFaceCircleGap = ta.getDimensionPixelSize(R.styleable.FaceCircleView_animFaceCircleGap, defaultAnimFaceCircleGap);
        }

        mPaint = new Paint();
        mPaint.setColor(android.graphics.Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mAnimLineHeight);

        //合成模式，负责挖出一个空心圆
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        //改变画笔alpha值
        mAnimator = ValueAnimator.ofInt(0, 255);
        mAnimator.setDuration(2 * 1000);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(Integer.MAX_VALUE);
        mAnimator.setRepeatCount(100);
        mAnimator.addUpdateListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        //1、画空心圆，通过颜色合成裁剪出中间的空心圆
        //1.1、背景填充
        mPaint.setColor(mBgMaskColor);
        canvas.drawRect(0, 0, width, height, mPaint);
        //1.2、设置合成模式，然后画圆挖空
        mPaint.setXfermode(mPorterDuffXfermode);
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
        mPaint.setColor(Color.YELLOW);
        canvas.drawCircle(width / 2, height * mFaceCircleTopRatio, mFaceCircleWidth / 2, mPaint);

        //2、画圆环
        //2.1、最开始画笔的笔尖在左顶点，先把笔尖移到View的正中心
        mPaint.setXfermode(null);
        setLayerType(LAYER_TYPE_NONE, mPaint);
        mPaint.setColor(mAnimLineColor);
        canvas.translate(width / 2, height * mFaceCircleTopRatio);

        //2.2、遍历把每条线画出来
        //把圆360度分为60份，每份6度，下面的360，60，6值得就是这个意思
        for (int i = 0; i < 60; i++) {
            //每次遍历开始，笔尖都是在正中心
            canvas.save();
            //1、设置画笔透明度为255的60分之i
            int alpha = (int) ((mAnimBaseAlpha + i / 60f * 255) % 255);
            mPaint.setAlpha(alpha);
            //旋转画笔，每个格子 6 度，第i个格子旋转 i*6 度，用360减一下是为了调整旋转方向为顺时针，你可以实现直接用i*6就是逆时针旋转
            //旋转之后笔尖还是在View的正中心，直接画线的方向跟旋转的度数相关
            canvas.rotate(360 - i * 6);
            //把笔尖从View的正中心移动到圆的边上
            canvas.translate(mAnimCircleWidth / 2 - mAnimLineWidth, 0);
            //划线
            canvas.drawLine(0, 0, mAnimLineWidth, 0, mPaint);
            //恢复画布
            canvas.restore();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        this.mAnimBaseAlpha = (int) animation.getAnimatedValue();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mFaceCircleWidth = (int) (w * mFaceCircleWidthRatio);
        mAnimCircleWidth = mFaceCircleWidth + mAnimLineWidth * 2 + mAnimFaceCircleGap * 2;
    }
}
