package com.sszyc8.universalplayer;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2015-12-06 15:05
 * 描述:
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2015-12-06 15:05
 * 修改原因描述:
 * 版本号:       1.0
 */
public abstract class SSAdapter<T> extends BaseAdapter {

    private List<T> list;
    private Context context;

    public SSAdapter(List<T> list, Context context) {
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
