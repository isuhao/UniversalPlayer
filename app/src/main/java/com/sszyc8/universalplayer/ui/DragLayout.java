package com.sszyc8.universalplayer.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;
import com.sszyc8.universalplayer.R;
import com.sszyc8.universalplayer.utils.Utils;

/**
 * 侧滑面板
 */
public class DragLayout extends FrameLayout {

    private static final String TAG = "tag";
    private ViewDragHelper mDragHelper;

    private ViewGroup mLeftContent;
    private ViewGroup mMainContent;

    //  屏幕宽高
    private int mMeasuredWidth;
    private int mMeasuredHeight;

    //  屏幕宽度*0.6
    private int mRange;

    //  默认为关闭状态
    private Status mStatus = Status.CLOSE;
    private OnDragStatusChangeListener mDragStatusChangeListener;


    public enum Status {
        CLOSE,  //  关闭状态
        OPEN,   //  开启状态
        DRAGING;    //  拖动状态
    }

    public interface OnDragStatusChangeListener {
        void onClose(); //关闭

        void onOpen();//开启

        void onDraging(float percent);//拖拽状态
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    public void setDragStatusListener(OnDragStatusChangeListener onDragStatusChangeListener) {
        this.mDragStatusChangeListener = onDragStatusChangeListener;
    }

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //  1.初始化操作  通过静态方法
        mDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {

            //  根据返回结果决定当前chile是否可以拖拽
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                Log.d(TAG, "tryCaptureView:" + child);
                //  判child是否是主页面
                return true;
            }

            //  当capturedChild被捕获时,调用
            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                Log.d(TAG, "onViewCaptured:" + capturedChild);
                super.onViewCaptured(capturedChild, activePointerId);
            }

            //  设置拖动范围
            @Override
            public int getViewHorizontalDragRange(View child) {
                //  屏幕宽度*0.6
                return mRange;
            }

            //  根据建议值修正将要移动的位置
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                //   判断当前child是否是主面板
                if (child == mMainContent) {
                    //   控制拖动范围
                    left = fixLeft(left);
                }

                return left;
            }

            //  当view位置改变的时候回调用这个函数(更新状态,重绘界面)
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);

                int newLeft = left;
                if (changedView == mLeftContent) {
                    newLeft = mMainContent.getLeft() + dx;
                }

                //  修正移动值
                newLeft = fixLeft(newLeft);

                if (changedView == mLeftContent) {
                    //  当左面板移动后,在强制放回去
                    mLeftContent.layout(0, 0, mMeasuredWidth, mMeasuredHeight);
                    mMainContent.layout(newLeft, 0, newLeft + mMeasuredWidth, mMeasuredHeight);
                }

                dispatchDragEvent(newLeft);


                //  为了兼容低版本 ,每次修改值之后重绘
                invalidate();
            }

            //  当view被释放的时候,处理的事情(执行动画)
            // View releasedChild, 被释放的view
            // float xvel, 水平方向的速度
            // float yvel, 垂直方向的速度
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                //  判断开启关闭
                if (xvel == 0 && mMainContent.getLeft() > mRange / 2.0f) {
                    open(true);
                } else if (xvel > 0) {
                    open(true);
                } else {
                    close(true);
                }

            }

            @Override
            public void onViewDragStateChanged(int state) {
                super.onViewDragStateChanged(state);
            }
        });

    }

    /**
     * 跟新状态执行动画
     * 1.做面板:缩放动画,平移动画,透明度动画
     * 2.主面板:缩放动画
     * 3.背景动画:亮度变化
     *
     * @param newLeft
     */
    private void dispatchDragEvent(int newLeft) {
        float percent = newLeft * 1.0f / mRange;

        if (mDragStatusChangeListener != null) {
            mDragStatusChangeListener.onDraging(percent);
        }
        //  更新状态,执行回调
        //  上一次的状态
        Status preStatus = mStatus;
        mStatus = getCurrentStatus(percent);
        if (mStatus != preStatus) {
            if (mStatus == Status.CLOSE) {
                if (mDragStatusChangeListener != null) {
                    mDragStatusChangeListener.onClose();
                }
            } else if (mStatus == Status.OPEN) {
                if (mDragStatusChangeListener != null) {
                    mDragStatusChangeListener.onOpen();
                }
            }
        }

        //  动画效果
        animViews(percent);
    }

    /**
     * 得到当前状态
     *
     * @param percent
     * @return
     */
    private Status getCurrentStatus(float percent) {
        if (percent == 0f) {
            return Status.CLOSE;
        } else if (percent == 1.0f) {
            return Status.OPEN;
        }
        return Status.DRAGING;
    }

    /**
     * 伴随动画
     *
     * @param percent
     */
    private void animViews(float percent) {

//        ViewHelper.setScaleX(mLeftContent, 0.5f * percent + 0.5f);
//        ViewHelper.setScaleY(mLeftContent, 0.5f * percent + 0.5f);

        //  平移动画
        ViewHelper.setTranslationX(mLeftContent, evaluate(percent, -mMeasuredWidth / 2.0f, 0));

        //  透明度动画
        ViewHelper.setAlpha(mLeftContent, evaluate(percent, 0.5f, 1.0f));

        //  主面板缩放
//        ViewHelper.setScaleX(mMainContent, evaluate(percent, 1.0f, 0.9f));
//        ViewHelper.setScaleY(mMainContent, evaluate(percent, 1.0f, 0.9f));

        //  设置背景动画(黑色到透明)
        getBackground().setColorFilter((int) evaluateColor(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }


    /**
     * 颜色变化
     * This function returns the calculated in-between value for a color
     * given integers that represent the start and end values in the four
     * bytes of the 32-bit int. Each channel is separately linearly interpolated
     * and the resulting calculated values are recombined into the return value.
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue A 32-bit int value representing colors in the
     *                   separate bytes of the parameter
     * @param endValue   A 32-bit int value representing colors in the
     *                   separate bytes of the parameter
     * @return A value that is calculated to be the linearly interpolated
     * result, derived by separating the start and end values into separate
     * color channels and interpolating each one separately, recombining the
     * resulting values in the same way.
     */
    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                (int) ((startB + (int) (fraction * (endB - startB))));
    }

    /**
     * 估值器
     *
     * @param fraction
     * @param startValue
     * @param endValue
     * @return
     */
    private float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

    /**
     * 关闭
     *
     * @param isSmooth 是否开启平滑动画
     */
    public void close(boolean isSmooth) {
        int l = 0;
        if (isSmooth) {
            boolean isMoveOk = mDragHelper.smoothSlideViewTo(mMainContent, l, 0);
            if (isMoveOk) { //  表示还没有移动到指定位置,需要刷新界面
                //  刷新界面 与invalidate();方法类似,这是专为动画设计的重绘界面
                //  this代表view所在的ViewGroup
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(l, 0, l + mMeasuredWidth, mMeasuredHeight);
        }
    }

    /**
     * 打开
     *
     * @param isSmooth 是否开启平滑动画
     */
    public void open(boolean isSmooth) {
        int l = mRange;
        if (isSmooth) {
            boolean isMoveOk = mDragHelper.smoothSlideViewTo(mMainContent, l, 0);
            if (isMoveOk) { //  表示还没有移动到指定位置,需要刷新界面
                //  刷新界面 与invalidate();方法类似,这是专为动画设计的重绘界面
                //  this代表view所在的ViewGroup
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            mMainContent.layout(l, 0, l + mMeasuredWidth, mMeasuredHeight);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 根据范围修正坐标值
     *
     * @param left
     * @return
     */
    private int fixLeft(int left) {
        if (left < 0) {
            return 0;
        } else if (left > mRange) {
            return mRange;
        }
        return left;
    }

    //  2.传递触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //  传递给mDragHelper
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mDragHelper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  true持续接收事件,
        return true;
    }

    //
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //  判断布局个数
        if (getChildCount() < 2) {
            //  抛出非法状态异常
            throw new IllegalStateException("最少得有2个布局组成");
        }

        //  必须都是ViewGroup子类
        if (!(getChildAt(0) instanceof ViewGroup && getChildAt(1) instanceof ViewGroup)) {
            //  抛出非法参数异常
            throw new IllegalArgumentException("子View必须是ViewGroup的子类!!");
        }
        //  获得到第一个
        mLeftContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);

    }

    //  尺寸改变后
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //  得到屏幕宽高
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();

        mRange = (int) (mMeasuredWidth * 0.4f);
    }
}
