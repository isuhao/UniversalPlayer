package com.sszyc8.universalplayer.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2015-12-06 18:41
 * 描述:      工具类
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2015-12-06 18:41
 * 修改原因描述:
 * 版本号:       1.0
 */
public class Utils {


    public Utils() {
    }

    /**
     * 把秒转换成 时:分:秒
     *
     * @param time
     * @return
     */
    public static String stringForTime(int time) {
        //  转换成字符串时间
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());


        int totalSeconds = time / 1000;
        int secondes = totalSeconds % 60;

        int minutes = (totalSeconds / 60) % 60;

        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, secondes).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, secondes).toString();
        }
    }

    /**
     * 获取系统时间
     *
     * @return 返回系统时间
     */
    public String getSystemTime() {
        //  格式化时分秒
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    public static Toast mToast;

    /**
     * 显示toast
     *
     * @param context
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }

        mToast.setText(msg);
        mToast.show();
    }

    /**
     * 将输入流读取成String后返回
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static String readFromStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = 0;
        byte[] buffer = new byte[1024];

        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        String result = out.toString();
        in.close();
        out.close();
        return result;
    }


    /**
     * 得到一个自定义大小的list集合
     *
     * @param list 集合
     * @param size 大小
     * @return
     */
    public static List getCustomSizeList(List list, int size) {
        for (int i = 1; i <= size; i++) {
            list.add(i);
        }
        return list;
    }


    /**
     * scaleAnim
     */
    public static void playAnimScale(View view) {
        //  属性动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 2f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setDuration(500);
        animSet.start();
    }
}
