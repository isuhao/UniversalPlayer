package com.sszyc8.universalplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sszyc8.universalplayer.ui.DragLayout;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2016-01-07 16:12
 * 描述:
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2016-01-07 16:12
 * 修改原因描述:
 * 版本号:       1.0
 */
public class MyRelativeLayout extends RelativeLayout {
    private DragLayout mDragLayout;

    public MyRelativeLayout(Context context) {
        super(context);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDragLayout(DragLayout dragLayout) {
        this.mDragLayout = dragLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //  如果当前是状态(开启/关闭)
        if (mDragLayout.getStatus() == DragLayout.Status.CLOSE) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDragLayout.getStatus() == DragLayout.Status.CLOSE) {
            return super.onTouchEvent(event);
        } else {
            //  手指抬起,调用关闭操作
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mDragLayout.close(true);
            }
            return true;
        }
    }
}
