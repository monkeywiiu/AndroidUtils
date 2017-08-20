package com.coder.rain.library.utils.weight;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.coder.rain.library.R;

import java.util.ArrayList;

/**
 * Describe :仿支付宝咻一咻动画
 * Email:baossrain99@163.com
 * Created by Rain on 17-5-16.
 */
public class WheelView extends RelativeLayout {

    private static final String TAG = "RippleLayout";

    /**
     * static final fields
     */
    private static final int DEFAULT_RIPPLE_COUNT = 6;
    private static final int DEFAULT_DURATION_TIME = 3000;
    private static final float DEFAULT_SCALE = 4.0f;
    private static final int DEFAULT_RIPPLE_COLOR = Color.rgb(0x33, 0x99, 0xcc);
    private static final int DEFAULT_STROKE_WIDTH = 0;
    private static final int DEFAULT_RADIUS = 60;
    private static final boolean DEFAULT_SHOW_RIPPLE = false;

    /**
     *
     */
    private int mRippleColor = DEFAULT_RIPPLE_COLOR;
    private float mStrokeWidth = DEFAULT_STROKE_WIDTH;
    private float mRippleRadius = DEFAULT_RADIUS;
    private int mAnimDuration;
    private int mRippleViewNums;
    private int mAnimDelay;
    private float mRippleScale;
    private boolean mShowRipple = false;
    private boolean animationRunning = false;
    /**
     *
     */
    private Paint mPaint = new Paint();

    /**
     * 动画集,执行缩放、alpha动画,使得背景色渐变
     */
    private AnimatorSet mAnimatorSet = new AnimatorSet();
    /**
     * 动画列表,保存几个动画
     */
    private ArrayList<Animator> mAnimatorList = new ArrayList<Animator>();
    /**
     * RippleView Params
     */
    private LayoutParams mRippleViewParams;
    private RippleView rippleView;

    /**
     * @param context
     */
    public WheelView(Context context) {
        super(context);
        init(context, null);
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        if (null != attrs) {
            initTypedArray(context, attrs);
        }

        initPaint();
        initRippleViewLayoutParams();
        generateRippleViews();

    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RippleLayout);
        //
        mRippleColor = typedArray.getColor(R.styleable.RippleLayout_color,
                DEFAULT_RIPPLE_COLOR);
        mStrokeWidth = typedArray.getDimension(R.styleable.RippleLayout_strokeWidth,
                DEFAULT_STROKE_WIDTH);
        mRippleRadius = typedArray.getDimension(R.styleable.RippleLayout_circleRadius,
                DEFAULT_RADIUS);
        mAnimDuration = typedArray.getInt(R.styleable.RippleLayout_duration,
                DEFAULT_DURATION_TIME);
        mRippleViewNums = typedArray.getInt(R.styleable.RippleLayout_rippleNum,
                DEFAULT_RIPPLE_COUNT);
        mRippleScale = typedArray.getFloat(R.styleable.RippleLayout_scale,
                DEFAULT_SCALE);
        mShowRipple = typedArray.getBoolean(R.styleable.RippleLayout_showRipple,
                DEFAULT_SHOW_RIPPLE);
        // oh, baby, don't forget recycle the typedArray !!
        typedArray.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mStrokeWidth = 0;
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mRippleColor);
    }

    private void initRippleViewLayoutParams() {
        // ripple view的大小为 半径 + 笔宽的两倍
        int rippleSide = (int) (2 * (mRippleRadius + mStrokeWidth));
        mRippleViewParams = new LayoutParams(rippleSide, rippleSide);
        // 居中显示
        mRippleViewParams.addRule(CENTER_IN_PARENT, TRUE);
    }

    /**
     * 计算每个RippleView之间的动画时间间隔,从而产生波纹效果
     */
    private void calculateAnimDelay() {
        mAnimDelay = mAnimDuration / mRippleViewNums;
    }

    /**
     * 初始化RippleViews，并且将动画设置到RippleView上,使之在x, y不断扩大,并且背景色逐渐淡化
     */
    private void generateRippleViews() {

        calculateAnimDelay();
        initAnimSet();
        // 添加RippleView
        for (int i = 0; i < mRippleViewNums; i++) {
            RippleView rippleView = new RippleView(getContext());
            addView(rippleView, mRippleViewParams);
            // 添加动画
            addAnimToRippleView(rippleView, i);
        }

        // x, y, alpha动画一块执行
        mAnimatorSet.playTogether(mAnimatorList);
    }

    private void initAnimSet() {
        mAnimatorSet.setDuration(mAnimDuration);
        mAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    /**
     * 为每个RippleView添加动画效果,并且设置动画延时,每个视图启动动画的时间不同,就会产生波纹
     *
     * @param rippleView
     * @param i          视图所在的索引
     */
    private void addAnimToRippleView(RippleView rippleView, int i) {

        // x轴的缩放动画
        final ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "scaleX",
                1.0f, mRippleScale);
        scaleXAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
        scaleXAnimator.setStartDelay(i * mAnimDelay);
        scaleXAnimator.setDuration(mAnimDuration);
        mAnimatorList.add(scaleXAnimator);

        // y轴的缩放动画
        final ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "scaleY",
                1.0f, mRippleScale);
        scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
        scaleYAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        scaleYAnimator.setStartDelay(i * mAnimDelay);
        scaleYAnimator.setDuration(mAnimDuration);
        mAnimatorList.add(scaleYAnimator);

        // 颜色的alpha渐变动画
        final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, "alpha", 1.0f,
                0f);
        alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
        alphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        alphaAnimator.setDuration(mAnimDuration);
        alphaAnimator.setStartDelay(i * mAnimDelay);
        mAnimatorList.add(alphaAnimator);
    }

    public void startRippleAnimation() {
        Log.d(TAG, "startRippleAnimation: rippleAnimation is start...");
        if (!isRippleAnimationRunning()) {
            removeView(rippleView);
            makeRippleViewsVisible();
            mAnimatorSet.start();
            animationRunning = true;
        }
    }

    private void makeRippleViewsVisible() {
        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = this.getChildAt(i);
            if (childView instanceof RippleView) {
                childView.setVisibility(VISIBLE);
            }
        }
    }

    public void stopRippleAnimation() {
        Log.d(TAG, "stopRippleAnimation: rippleAnimation is stop...");
        if (isRippleAnimationRunning()) {
            mAnimatorSet.end();
            animationRunning = false;
            rippleView = new RippleView(getContext());
            addView(rippleView, mRippleViewParams);
        }
    }

    public boolean isRippleAnimationRunning() {
        return animationRunning;
    }

    /**
     * RippleView产生波纹效果, 默认不可见,当启动动画时才设置为可见
     *
     * @author mrsimple
     */
    private class RippleView extends View {

        public RippleView(Context context) {
            super(context);
            if (mShowRipple)
                this.setVisibility(View.VISIBLE);
            else this.setVisibility(INVISIBLE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int radius = (Math.min(getWidth(), getHeight())) / 2;
            canvas.drawCircle(radius, radius, radius - mStrokeWidth, mPaint);
        }
    }
}

/*
 *   ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 *     ┃　　　┃
 *     ┃　　　┃
 *     ┃　　　┗━━━┓
 *     ┃　　　　　　　┣┓
 *     ┃　　　　　　　┏┛
 *     ┗┓┓┏━┳┓┏┛
 *       ┃┫┫　┃┫┫
 *       ┗┻┛　┗┻┛
 *        神兽保佑
 *        代码无BUG!
 */
