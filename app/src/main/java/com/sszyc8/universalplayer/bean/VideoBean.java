package com.sszyc8.universalplayer.bean;

import android.text.TextUtils;

import com.sszyc8.universalplayer.utils.PinyinUtils;

import java.io.Serializable;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2015-12-06 14:55
 * 描述:      接收视屏实体
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2015-12-06 14:55
 * 修改原因描述:
 * 版本号:       1.0
 */
public class VideoBean implements Serializable, Comparable<VideoBean> {

    private String title;   //  视屏标题
    private String duration;//  视屏长度
    private long size;    //  视屏大小
    private String date;    //  视屏的绝对地址
    /**
     * 此字段--只用于快速索引 名称拼音
     */
    private String pinyin;

    public VideoBean() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (!TextUtils.isEmpty(title)) {
            //  获得第一个字母并将第一个字母转换成大写
            this.pinyin = PinyinUtils.exChange(PinyinUtils.getPinyin(title).charAt(0) + "");
            System.out.print(pinyin);
        }
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Override
    public int compareTo(VideoBean lhs) {
        return this.pinyin.compareTo(lhs.getPinyin());
    }


}
