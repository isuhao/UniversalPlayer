package com.sszyc8.universalplayer.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sszyc8.universalplayer.R;
import com.sszyc8.universalplayer.adapter.SSAdapter;
import com.sszyc8.universalplayer.bean.VideoBean;
import com.sszyc8.universalplayer.service.OnLetterUpdateListenerService;
import com.sszyc8.universalplayer.ui.DragLayout;
import com.sszyc8.universalplayer.ui.QuickIndexBar;
import com.sszyc8.universalplayer.utils.Utils;
import com.sszyc8.universalplayer.view.MyRelativeLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoListActivity extends Activity {

    private DragLayout mDragLayout;
    private MyRelativeLayout myRelativeLayout;

    private TextView tvNotVideo;
    private ListView lvVideo;
    private List<VideoBean> videoList; //  视屏数据集合
    private QuickIndexBar mQuickIndexBar;
    private TextView tvDialogCenter;
    private ListView lvLeft;    //  左侧页面中的列表
    private String[] leftStrs = {"列表", "设置", "关于"};

    private Utils utils;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (videoList == null || videoList.size() == 0) {
                //  设置没有数据提示显示
                tvNotVideo.setVisibility(View.VISIBLE);
            } else {
                fillAndSortDate(videoList);

                lvVideo.setAdapter(new SSAdapter<VideoBean>(videoList, VideoListActivity.this) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        ViewHolder holder = null;
                        if (convertView == null) {
                            convertView = View.inflate(VideoListActivity.this, R.layout.video_list_item, null);

                            holder = new ViewHolder();
                            holder.tvIndex = (TextView) convertView.findViewById(R.id.tv_index);
                            holder.tvVideoNname = (TextView) convertView.findViewById(R.id.tv_video_name);
                            holder.tvVideoDuration = (TextView) convertView.findViewById(R.id.tv_video_duration);
                            holder.tvVideoSize = (TextView) convertView.findViewById(R.id.tv_video_size);

                            convertView.setTag(holder);
                        } else {
                            holder = (ViewHolder) convertView.getTag();
                        }

                        //  得到当前视屏信息实体
                        VideoBean video = videoList.get(position);
                        //  得到首字母
                        String currentLetter = video.getPinyin().charAt(0) + "";
                        //  去重复
                        String str = null;
                        if (position == 0) {
                            str = currentLetter;
                        } else {
                            //  得到上一个视屏实体信息
                            VideoBean preVideo = videoList.get(position - 1);
                            //  获取上一个的字母
                            String preLetter = preVideo.getPinyin().charAt(0) + "";
                            //  判断当前的和上次的是否一致
                            if (!TextUtils.equals(preLetter, currentLetter)) {
                                str = currentLetter;
                            }
                        }
                        //  设置索引显示隐藏
                        holder.tvIndex.setVisibility(str == null ? View.GONE : View.VISIBLE);
                        holder.tvIndex.setText(str);

                        holder.tvVideoNname.setText(video.getTitle());
                        holder.tvVideoDuration.setText(utils.stringForTime(Integer.parseInt(video.getDuration())));
                        holder.tvVideoSize.setText(Formatter.formatFileSize(VideoListActivity.this, video.getSize()));
                        return convertView;
                    }
                });
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_list);
        utils = new Utils();

        initVideoListView();
        initVideoListDate();
    }


    /**
     * 加载视图
     */
    private void initVideoListView() {
        mDragLayout = (DragLayout) findViewById(R.id.dl);
        myRelativeLayout = (MyRelativeLayout) findViewById(R.id.mll);

        tvNotVideo = (TextView) findViewById(R.id.tv_not_video);
        lvVideo = (ListView) findViewById(R.id.lv_video);
        mQuickIndexBar = (QuickIndexBar) findViewById(R.id.qib);
        tvDialogCenter = (TextView) findViewById(R.id.tv_dialog_center);
        lvLeft = (ListView) findViewById(R.id.lv_left);


        //  设置点击
        lvVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //  将视屏列表携带过去
                Intent intent = new Intent(VideoListActivity.this, VideoPlayActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("videoList", (Serializable) videoList);
                intent.putExtras(bundle);
                //  将视频在列表中的位置携带过去
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        //  接口监听
        mQuickIndexBar.setListener(new OnLetterUpdateListenerService() {
            /**
             * 更新字母
             * @param letter 字母
             */
            @Override
            public void onLetterUpdate(String letter) {
                //  显示屏幕中间字母提示框
                showLetter(letter);
                //  根据字母定位listView
                for (int i = 0; i < videoList.size(); i++) {
                    VideoBean nameBean = videoList.get(i);
                    //  获得第一个字母
                    String l = nameBean.getPinyin().charAt(0) + "";
                    if (TextUtils.equals(letter, l)) {
                        //  根据右侧索引条跳转到listView跳到相应的条目
                        lvVideo.setSelection(i);
                        break;
                    }
                }
            }
        });

        //  设置引用
        myRelativeLayout.setDragLayout(mDragLayout);
        //  设置监听
        mDragLayout.setDragStatusListener(new DragLayout.OnDragStatusChangeListener() {
            @Override
            public void onOpen() {
//                Utils.showToast(VideoListActivity.this, "onOpen");
            }

            @Override
            public void onClose() {
//                Utils.showToast(VideoListActivity.this, "onClose");

                //  设置主页面头像抖动
//                ObjectAnimator animator = ObjectAnimator.ofFloat(ivIcon, "translationX", 15.0f);
//                animator.setInterpolator(new CycleInterpolator(4));
//                animator.setDuration(500);
//                animator.start();
            }

            @Override
            public void onDraging(float percent) {
                //  设置主页面头像透明度
//                ViewHelper.setAlpha(ivIcon, 1 - percent);
            }
        });
    }

    /**
     * 加载数据
     */
    private void initVideoListDate() {
        //  用于装载视屏
        videoList = new ArrayList<>();

        //  启用子线程加载视屏
        new Thread() {
            @Override
            public void run() {
                //  读取手机中所有视屏
                ContentResolver contentResolver = getContentResolver();
                //  uri
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Video.Media.TITLE,       //  视屏标题
                        MediaStore.Video.Media.DURATION,    //  视屏长度
                        MediaStore.Video.Media.SIZE,        //  视屏大小
                        MediaStore.Video.Media.DATA,        //  视屏的绝对地址
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);

                while (cursor.moveToNext()) {
                    VideoBean video = new VideoBean();
                    video.setTitle(cursor.getString(0));
                    video.setDuration(cursor.getString(1));
                    video.setSize(cursor.getLong(2));
                    video.setDate(cursor.getString(3));

                    videoList.add(video);

                    //  发送消息主线程刷新UI
                    mHandler.sendEmptyMessage(0);
                }
            }
        }.start();


        lvLeft.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, leftStrs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = (TextView) view;
                mText.setTextColor(Color.WHITE);
                return view;
            }
        });
    }

    /**
     * 集合排序
     *
     * @param list
     */
    private void fillAndSortDate(List<VideoBean> list) {
        //  进行排序,这里需要在实体中实现Comparable接口中的compareTo方法
        Collections.sort(list);
    }

    /**
     * 显示提示框
     *
     * @param letter 字母
     */
    private void showLetter(String letter) {
        //  设置提示框显示
        tvDialogCenter.setVisibility(View.VISIBLE);
        tvDialogCenter.setText(letter);
        //  清除之前的所有消息
        mHandler.removeCallbacksAndMessages(null);
        //  设置提示框显示1秒后隐藏
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvDialogCenter.setVisibility(View.GONE);
            }
        }, 1000);
    }

    /**
     * 自定义类目(用于在BaseAdapter中使用)
     */
    static class ViewHolder {
        TextView tvIndex;
        TextView tvVideoNname;
        TextView tvVideoDuration;
        TextView tvVideoSize;
    }

}
