package com.sszyc8.universalplayer.utils;

import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
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

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    public Utils() {
        //  转换成字符串时间
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    }

    /**
     * 把秒转换成 时:分:秒
     *
     * @param time
     * @return
     */
    public String stringForTime(int time) {
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
}
