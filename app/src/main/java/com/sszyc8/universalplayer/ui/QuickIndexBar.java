package com.sszyc8.universalplayer.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.sszyc8.universalplayer.service.OnLetterUpdateListenerService;

/**
 * 项目工程名:   快速索引
 * 作者：      sszyc8
 * 创建日期：  2016-01-08 10:29
 * 描述:      快速索引
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2016-01-08 10:29
 * 修改原因描述:
 * 版本号:       1.0
 */
public class QuickIndexBar extends View {

    private Paint mPaint;
    //  A-Z字母
    private static final String[] LETTERS = new String[]{
            "A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X",
            "Y", "Z"};

    private float mCellHeigjt;  //  每个条目cell的高度
    private int mCellWidth;     //  每个条目cell的宽度

    int touchIndex = -1;        //  触摸索引

    private OnLetterUpdateListenerService listener;

    public QuickIndexBar(Context context) {
        this(context, null);
    }

    public QuickIndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickIndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);   //  设置字体颜色
        mPaint.setTextSize(18);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);//设置字体圆滑
    }

    /**
     * 绘制A-Z字母
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //  循环绘制 A-Z字母
        for (int i = 0; i < LETTERS.length; i++) {
            String text = LETTERS[i];
            int x = (int) (mCellWidth / 2.0f - mPaint.measureText(text) / 2.0f);
            //  获取文本高度
            Rect rect = new Rect();
            mPaint.getTextBounds(text, 0, text.length(), rect);
            int textHeight = rect.height();
            int y = (int) (mCellHeigjt / 2.0f + textHeight / 2.0f + i * mCellHeigjt);

            //   根据按下的字母来更换颜色
            mPaint.setColor(touchIndex == i ? Color.GRAY : Color.WHITE);
            //  绘制 A-Z
            canvas.drawText(text, x, y, mPaint);
        }
    }

    /**
     * 触摸事件监听
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index;
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:   //  触摸:按下
                //  获取当前触摸到的字母索引
                index = (int) (event.getY() / mCellHeigjt);
                if (index >= 0 && index < LETTERS.length) {
                    //  判断是否和上一次触摸到的一样
                    if (index != touchIndex) {
                        if (listener != null) {
                            listener.onLetterUpdate(LETTERS[index]);
                        }
                        touchIndex = index;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:   //  触摸:移动
                index = (int) (event.getY() / mCellHeigjt);
                if (index >= 0 && index < LETTERS.length) {
                    //  判断是否和上一次触摸到的一样
                    if (index != touchIndex) {
                        if (listener != null) {
                            listener.onLetterUpdate(LETTERS[index]);
                        }
                        touchIndex = index;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:     //  触摸:离开
                //  恢复触摸索引
                touchIndex = -1;
                break;
            default:
                break;
        }

        //  兼容低版本(刷新UI)
        invalidate();

        return true;
    }

    /**
     * 这个方法会在这个view的大小发生改变是被系统调用，view大小变化，就调用这个方法
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //  获取单元格的宽和高
        mCellWidth = getMeasuredWidth();
        int mMeasureHeight = getMeasuredHeight();

        //  每个条目的宽度
        mCellHeigjt = mMeasureHeight * 1.0f / LETTERS.length;
    }


    public OnLetterUpdateListenerService getListener() {
        return listener;
    }

    public void setListener(OnLetterUpdateListenerService listener) {
        this.listener = listener;
    }
}
