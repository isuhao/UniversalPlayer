package com.sszyc8.universalplayer.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2016-01-08 11:57
 * 描述:
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2016-01-08 11:57
 * 修改原因描述:
 * 版本号:       1.0
 */
public abstract class MyAdapter<T> extends BaseAdapter {

    public List<T> list;
    public Context context;

    public MyAdapter(Context context, List<T> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
