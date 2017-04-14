package com.sszyc8.universalplayer.anim;

import android.view.View;

import com.nineoldandroids.animation.Keyframe;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;

/**
 * Created by sszyc8 on 16/3/20.
 */
public class Animation {

    static public void setRotate(View view) {
        // 设置在动画开始时,旋转角度为0度
        Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        // 设置在动画执行50%时,旋转角度为360度
        Keyframe kf1 = Keyframe.ofFloat(.5f, 360f);
        // 设置在动画结束时,旋转角度为0度
        Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        // 使用PropertyValuesHolder进行属性名称和值集合的封装
        PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        // 通过ObjectAnimator进行执行
        ObjectAnimator.ofPropertyValuesHolder(view, pvhRotation)
                // 设置执行时间(1000ms)
                .setDuration(1000)
                        // 开始动画
                .start();
    }
}
