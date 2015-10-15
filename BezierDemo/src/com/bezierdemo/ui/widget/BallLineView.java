package com.bezierdemo.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class BallLineView extends View {
    private int mStaticBallCount = 4;
    private Ball[] mStaticBalls;
    private Ball mDynamicBall = new Ball();

    private boolean mHasInitStaticBalls = false;
    private float mDynamicMinX, mDynamicMaxX;

    Paint mCirclePaint, mLinePaint;
    Path mPath;

    private float mInterpolatedTime;
    MoveAnimation mAnimation;

    public BallLineView(Context context) {
        this(context, null);
    }

    public BallLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BallLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mHasInitStaticBalls) {
            initBalls();
        }
        // 计算动态小球的x
        mDynamicBall.x = mDynamicMinX + mInterpolatedTime * (mDynamicMaxX - mDynamicMinX);

        drawBalls(canvas);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == GONE || visibility == INVISIBLE) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    private void initBalls() {
        int width = getWidth();
        int height = getHeight();
        float staticBallRadius;
        // 静态小球横向排列，两个小球间距以及整体左右间距均为1.5个小球，
        // 静态球计算半径的时候要综合考虑View的长和宽，还要考虑左右padding
        int radiusCount = mStaticBallCount * 5 + 3; // 总距离等于半径的倍数
        if (height / 2 * radiusCount < width - getPaddingLeft() - getPaddingRight()) {
            staticBallRadius = height / 2;
        } else {
            staticBallRadius = (float)(width - getPaddingLeft() - getPaddingRight()) / radiusCount;
        }
        mStaticBalls = new Ball[mStaticBallCount];
        for (int i = 0; i < mStaticBallCount; i++) {
            Ball ball = new Ball();
            ball.x = getPaddingLeft() + staticBallRadius * (4 + i * 5);
            ball.y = height / 2;
            ball.radius = staticBallRadius;
            mStaticBalls[i] = ball;
        }
        // 动态小球的默认半径是静态小球的3/4
        mDynamicBall.radius = 3 * staticBallRadius / 4;

        mDynamicBall.y = mStaticBalls[0].y;
        mDynamicMinX = getPaddingLeft() + mDynamicBall.radius;
        mDynamicMaxX = getPaddingLeft() + staticBallRadius * (5 * mStaticBallCount + 3) - mDynamicBall.radius;
    }

    private void initPaint() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.RED);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(Color.RED);

        mPath = new Path();
    }

    /**
     * 根据所有圆的位置绘制圆
     */
    private void drawBalls(Canvas canvas) {
        // 绘制动态球
        canvas.drawCircle(mDynamicBall.x, mDynamicBall.y, mDynamicBall.radius, mCirclePaint);
        // 绘制静态球
        for (int i = 0; i < mStaticBallCount; i++) {
            Ball staticBall = mStaticBalls[i];
            canvas.drawCircle(staticBall.x, staticBall.y, staticBall.radius, mCirclePaint);

            // 两个球有连接且不相交才绘制中间的连接曲线
            if (isConnect(staticBall, mDynamicBall) && !isIntersect(staticBall, mDynamicBall)) {
                float start1X = staticBall.x;
                float start1Y = staticBall.y - staticBall.radius;

                float end1X = mDynamicBall.x;
                float end1Y = mDynamicBall.y - mDynamicBall.radius;

                float start2X = mDynamicBall.x;
                float start2Y = mDynamicBall.y + mDynamicBall.radius;

                float end2X = staticBall.x;
                float end2Y = staticBall.y + staticBall.radius;

                float controlX = (staticBall.x + mDynamicBall.x) / 2;
                float controlY = (staticBall.y + mDynamicBall.y) / 2;

                mPath.reset();
                mPath.moveTo(start1X, start1Y);
                mPath.quadTo(controlX, controlY, end1X, end1Y);
                mPath.lineTo(start2X, start2Y);
                mPath.quadTo(controlX, controlY, end2X, end2Y);
                mPath.lineTo(start1X, start1Y);
                canvas.drawPath(mPath, mLinePaint);
            }
        }
    }

    /**
     * 两个小球是否相交
     */
    private boolean isIntersect(Ball a, Ball b) {
        return Math.abs(a.x - b.x) < a.radius + b.radius;
    }

    /**
     * 两个小球是否有连接，小球圆心离开大球距离超过大球直径时算彻底断开连接
     */
    private boolean isConnect(Ball a, Ball b) {
        float bigBallRadius = a.radius > b.radius ? a.radius : b.radius;
        return Math.abs(a.x - b.x) < 3 * bigBallRadius;
    }

    private void stopAnimation() {
        this.clearAnimation();
        postInvalidate();
    }

    private void startAnimation() {
        mAnimation = new MoveAnimation();
        mAnimation.setDuration(2500);
        mAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        startAnimation(mAnimation);
    }

    private class MoveAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mInterpolatedTime = interpolatedTime;
            invalidate();
        }
    }
}
