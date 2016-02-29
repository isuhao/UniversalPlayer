package com.sszyc8.universalplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.sszyc8.universalplayer.R;
import com.sszyc8.universalplayer.bean.VideoBean;
import com.sszyc8.universalplayer.utils.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2015-12-06 19:00
 * 描述:      系统视屏播放器
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2015-12-06 19:00
 * 修改原因描述:
 * 版本号:       1.0
 */
public class VideoPlayActivity extends Activity {


    private static final int CODE_PROGRESS = 1; //  更新进度代码
    private static final int CODE_GONE_PALY_CONTROL = 2;    //  设置控制面板隐藏代码
    private static final int CODE_FINISH = 3;           //  关闭当前activity 代码


    private VideoView vvVideoPlay;
    private TextView tvTitle;
    private TextView tvSystemTime;
    private SeekBar sbVolume;
    private TextView tvCurrentTime;
    private SeekBar sbvideo;
    private TextView tvDuration;
    private Button btnPlay;

    private AudioManager audioManager;
    private int currentVolume;  //  当前音量
    private int maxVolume;      //  最大音量


    //  播放状态  true 为播放状态/   false 为暂停状态
    private boolean isPlayStatus = true;

    //  activity状态  true 为已经被销毁  /    false 为未销毁
    private boolean isActivityStatus = false;

    //  播放面板显示状态 true为显示 /  false为不显示
    private boolean isVideoPlayerContrlStatus = false;

    //  控制音量是否是静音  true 为静音  /  false 为非静音
    private boolean isMuteStatus = false;

    //  1.定义手势识别器
    private GestureDetector gestureDetector;

    //  屏幕宽高
    private int screenWidth;
    private int screenHeight;

    //  handler 时时更新播放时间
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_PROGRESS:
                    //  1.得到视屏的当前播放时间
                    int currentTime = vvVideoPlay.getCurrentPosition();
                    tvCurrentTime.setText(utils.stringForTime(currentTime));

                    //  2.更新进度条
                    sbvideo.setProgress(currentTime);

                    //  3.设置时间
                    tvSystemTime.setText(utils.getSystemTime());

                    //  4.设置缓冲进度(缓冲比例值 0-100)
                    int percentage = vvVideoPlay.getBufferPercentage();
                    sbvideo.setSecondaryProgress((percentage * sbvideo.getMax()) / 100);

                    //  循环发送消息
                    if (!isActivityStatus) {
                        //  必须移除
                        handler.removeMessages(CODE_PROGRESS);
                        handler.sendEmptyMessageDelayed(CODE_PROGRESS, 1000);
                    }
                    break;
                case CODE_GONE_PALY_CONTROL:
                    //  设置控制面板隐藏
                    setHideVideoPlayContrl();
                    break;
                case CODE_FINISH:
                    //  关闭activity
                    finish();
                    break;
            }
        }
    };
    private Utils utils;
    private Uri uri;
    private List<VideoBean> videoList;
    private int position;
    private Button btn_pre;
    private Button btn_next;
    private RelativeLayout rlVideoPlayControl;
    private LinearLayout llPlayNetLoading;
    private LinearLayout llplayNetBuffering;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        LibsChecker .isLibraryLoaded();
        setContentView(R.layout.activity_video_play);
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        utils = new Utils();

        //  初始化布局
        this.initVideoPalyView();

        //  初始化数据
        this.initDate();

        //  设置监听
        this.setListener();

    }

    /**
     * 设置监听
     */
    private void setListener() {
        // 监听视屏是否准备要播放了
        vvVideoPlay.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //  开始播放视频
                vvVideoPlay.start();

                //  得到视屏的长度,并设置总进度
                int duration = vvVideoPlay.getDuration();
                tvDuration.setText(utils.stringForTime(duration));

                //  1.视屏的总时长要关联进度条
                sbvideo.setMax(duration);

                //  视屏加载完成开始播放时,将加载页面隐藏
                llPlayNetLoading.setVisibility(View.INVISIBLE);

                //  立刻发送消息,更新播放时间进度
                handler.sendEmptyMessage(CODE_PROGRESS);
            }
        });

        //  设置播放完成监听
        vvVideoPlay.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //  播放下一个视屏
                setPlayNextVideo();
            }
        });

        //  设置进度条监听
        sbvideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /**
             * 当进度条状态发生变化,回调这个方法
             * @param seekBar   自身
             * @param progress  等于进度条的位置   视屏位置==progress
             * @param fromUser  用户操作
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //  控制进度条拖动
                    vvVideoPlay.seekTo(progress);
                }
            }

            /**
             * 手指拖动走这个方法
             * @param seekBar
             */

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //  拖动的时候移除发送的隐藏控制面板的消息
                handler.removeMessages(CODE_GONE_PALY_CONTROL);
            }

            /**
             * 手指离开时 走这个方法
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //  手指离开时继续发送控制面板隐藏消息
                handler.sendEmptyMessageDelayed(CODE_GONE_PALY_CONTROL, 3000);
            }
        });
        //  不要系统控制栏
        // vvVideoPlay.setMediaController(new MediaController(this));

        //  设置音量控制条
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //  拖动的时候移除发送的隐藏控制面板的消息
                handler.removeMessages(CODE_GONE_PALY_CONTROL);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //  默认为false  ,人为操作为true
                if (fromUser) {
                    //  设置音量
                    setVolume(progress);
                }
            }
        });

        //  监听播放卡(网络资源) Android 2.3 以后才有的
        vvVideoPlay.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:    //  卡或者拖动卡
                        //  设置卡顿加载显示
                        llplayNetBuffering.setVisibility(View.VISIBLE);
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:    // 卡顿结束
                        //  设置卡顿加载隐藏
                        llplayNetBuffering.setVisibility(View.INVISIBLE);
                        break;
                    default:

                        break;
                }
                return true;
            }
        });

        //  设置监听是否播放出错了
        vvVideoPlay.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(VideoPlayActivity.this, "格式不支持,跳转到万能播放器", Toast.LENGTH_LONG).show();
                //  启动万能播放器
                startVitamioPlayer();
                return true;
            }
        });
    }

    /**
     * 启动万能播放器
     */
    private void startVitamioPlayer() {
        //  将视屏列表携带过去
        Intent intent = new Intent(this, VitamioPlayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("videoList", (Serializable) videoList);
        intent.putExtras(bundle);
        //  将视频在列表中的位置携带过去
        intent.putExtra("position", position);
        intent.setData(uri);
        startActivity(intent);

        //  关闭当前的activity(延迟)
        handler.sendEmptyMessageDelayed(CODE_FINISH, 2000);
    }


    /**
     * 播放下一个视屏
     */
    private void setPlayNextVideo() {
        //  1.如果没有下一个视屏了就退出
        //  2.如果有下一个就播放下一个

        if (videoList != null && videoList.size() > 0) {
            //  视屏列表索引增加..播放下一个
            position++;

            if (position < videoList.size()) {
                //  获取下一个视屏
                VideoBean vide = videoList.get(position);
                //  设置播放地址
                vvVideoPlay.setVideoPath(vide.getDate());
                //  设置标题
                tvTitle.setText(vide.getTitle());

                //  如果是最后一个视屏,下一步按钮就应该隐藏
                if (position == 0) {    //  如果是第一个视屏
                    //  设置上一步隐藏
                    btn_pre.setVisibility(View.INVISIBLE);
                } else if (position == videoList.size() - 1) {  //  如果是最后一个视屏
                    //  设置下一步隐藏
                    btn_next.setVisibility(View.INVISIBLE);
                } else {
                    //  设置可见
                    btn_pre.setVisibility(View.VISIBLE);
                    btn_next.setVisibility(View.VISIBLE);
                }

            } else {    //  最后一个视屏
                //  最后一个视屏的位置
                position = videoList.size() - 1;
                Toast.makeText(VideoPlayActivity.this, "最后一个视屏了", Toast.LENGTH_SHORT).show();
                //  退出播放器
                finish();
            }
        } else if (uri != null) {
            Toast.makeText(VideoPlayActivity.this, "播放完成", Toast.LENGTH_SHORT).show();
            //  退出播放器
            finish();
        }
    }

    /**
     * 播放上一个视屏
     */
    private void setPlayPreVideo() {
        if (videoList != null && videoList.size() > 0) {
            //  视屏列表索引增加..播放下一个
            position--;

            if (position >= 0) {
                //  获取下一个视屏
                VideoBean vide = videoList.get(position);
                //  设置播放地址
                vvVideoPlay.setVideoPath(vide.getDate());
                //  设置标题
                tvTitle.setText(vide.getTitle());

                //  如果是最后一个视屏,下一步按钮就应该隐藏
                if (position == 0) {    //  如果是第一个视屏
                    //  设置上一步隐藏
                    btn_pre.setVisibility(View.INVISIBLE);
                } else if (position == videoList.size() - 1) {  //  如果是最后一个视屏
                    //  设置下一步隐藏
                    btn_next.setVisibility(View.INVISIBLE);
                } else {
                    //  设置可见
                    btn_pre.setVisibility(View.VISIBLE);
                    btn_next.setVisibility(View.VISIBLE);
                }

            } else {    //  最后一个视屏
                //  最后一个视屏的位置
                position = 0;
                Toast.makeText(VideoPlayActivity.this, "第一个视屏了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initVideoPalyView() {
        rlVideoPlayControl = (RelativeLayout) findViewById(R.id.rl_video_play_control);
        llPlayNetLoading = (LinearLayout) findViewById(R.id.ll_play_net_loading);
        llplayNetBuffering = (LinearLayout) findViewById(R.id.ll_play_net_buffering);

        vvVideoPlay = (VideoView) findViewById(R.id.vv_video_play);

        tvTitle = (TextView) findViewById(R.id.tv_video_paly_title);
        tvSystemTime = (TextView) findViewById(R.id.tv_system_time);

        sbVolume = (SeekBar) findViewById(R.id.sb_volume);

        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        sbvideo = (SeekBar) findViewById(R.id.db_video);
        tvDuration = (TextView) findViewById(R.id.tv_duration);


        //  这里以后要修改
        btnPlay = (Button) findViewById(R.id.btn_play);
        btn_pre = (Button) findViewById(R.id.btn_pre);
        btn_next = (Button) findViewById(R.id.btn_next);
    }


    /**
     * 初始化数据
     */
    public void initDate() {
        //  1.设置播放面板为隐藏状态
        setHideVideoPlayContrl();

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();


        //  得到播放列表(在跳转到本activity中携带过来的)
        //  视屏列表
        videoList = (List<VideoBean>) getIntent().getSerializableExtra("videoList");
        //  当前视屏在播放列表中的位置
        position = getIntent().getIntExtra("position", 0);

        //视屏播放地址
        uri = getIntent().getData();

        if (videoList != null && videoList.size() > 0) {
            //  得到视屏数据
            VideoBean video = videoList.get(position);
            //  设置播放地址
            vvVideoPlay.setVideoPath(video.getDate());
            //  设置标题
            tvTitle.setText(video.getTitle());

        } else if (uri != null) {
            //  设置播放地址
            vvVideoPlay.setVideoURI(uri);
            //  设置标题
            tvTitle.setText(uri.toString());

            btn_pre.setVisibility(View.INVISIBLE);
            btn_next.setVisibility(View.INVISIBLE);

        }

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            /**
             * 双击屏幕
             * @param e
             * @return
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);

            }

            /**
             * 长按屏幕
             * @param e
             */
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                //  设置暂停或者播放
                setPlayStartOrPause();
            }

            /**
             * 单击屏幕
             * @param e
             * @return
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //  判断播放控制面板显示状态
                if (isVideoPlayerContrlStatus) {
                    //  隐藏控制面板
                    setHideVideoPlayContrl();
                } else {
                    //  显示控制面板
                    setShowVideoPlayContrl();
                }
                return true;
            }
        });

        //  得到当前的音量和最大的音量值
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //  当前音量
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //  最大音量 [0-15] --15 不确定
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //  音量条和音量最大关联
        sbVolume.setMax(maxVolume);
        //  音量条关联当前音量
        sbVolume.setProgress(currentVolume);
    }

    float audioTouchRang = 0;   //  屏幕滑动的范围
    float startY = 0;   //  记录开始Y坐标
    float startX;   //  记录开始X坐标

    float mVol = 0; //  滑动前的音量值

    /**
     * 使用触摸事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {


        //  执行父类的方法
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                audioTouchRang = Math.min(screenHeight, screenWidth);
                mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                //  计算偏移量
                float distanceY = startY - endY;
                //  计算屏幕滑动比例
                float datel = distanceY / audioTouchRang;
                //  计算改变的音量值
                float volume = distanceY / audioTouchRang * maxVolume;
                //  屏蔽非法值和找出设置的音量值
                float volumeS = Math.min(Math.max(volume + mVol, 0), maxVolume);

                if (datel != 0) {
                    setVolume((int) volumeS);
                }

                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        //  对事件进行处理
        return true;
    }

    /**
     * 设置控制面板隐藏
     */
    private void setHideVideoPlayContrl() {
        //  设置状态为隐藏状态
        isVideoPlayerContrlStatus = false;
        //  设置面板隐藏
        rlVideoPlayControl.setVisibility(View.GONE);
        //  移除消息
        handler.removeMessages(CODE_GONE_PALY_CONTROL);
    }

    /**
     * 设置播放面板显示
     */
    private void setShowVideoPlayContrl() {
        //  设置状态为显示状态
        isVideoPlayerContrlStatus = true;
        //  设置面板为显示
        rlVideoPlayControl.setVisibility(View.VISIBLE);
        //  发送消息自动隐藏
        handler.sendEmptyMessageDelayed(CODE_GONE_PALY_CONTROL, 3000);
    }

    /**
     * 调节音量按钮响应事件
     *
     * @param view
     */
    public void onBtnVolume(View view) {
        isMuteStatus = !isMuteStatus;
        //  判断静音状态
        setVolume(currentVolume);
    }

    /**
     * 切换播放模式按钮响应事件
     *
     * @param view
     */
    public void onBtnSwich(View view) {
        new AlertDialog.Builder(this)
                .setMessage("当前是系统播播放,是否切换到万能播放?")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startVitamioPlayer();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 退出按钮响应事件
     *
     * @param view
     */
    public void onBtnExit(View view) {

    }

    /**
     * 上一步按钮响应事件
     *
     * @param view
     */
    public void onBtnPre(View view) {
        this.setPlayPreVideo();
    }

    /**
     * 播放/暂停按钮响应事件
     *
     * @param view
     */
    public void onBtnPlay(View view) {
        //  设置视屏暂停/播放
        this.setPlayStartOrPause();
    }

    /**
     * 设置视屏播放/暂停
     */
    private void setPlayStartOrPause() {
        if (isPlayStatus) {
            vvVideoPlay.pause();
            btnPlay.setText("播放");
        } else {
            vvVideoPlay.start();
            btnPlay.setText("暂停");
        }
        isPlayStatus = !isPlayStatus;
    }

    /**
     * 下一步按钮响应事件
     *
     * @param view
     */
    public void onBtnNext(View view) {
        //  播放下一个视屏
        this.setPlayNextVideo();
    }

    /**
     * 切换屏幕按钮响应事件
     *
     * @param view
     */
    public void onBtnScreen(View view) {

    }

    /**
     * 退出
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityStatus = !isActivityStatus;
    }

    /**
     * 设置音量
     *
     * @param volume 音量值
     */
    public void setVolume(int volume) {
        if (isMuteStatus) { //  静音
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            //  设置进度条
            sbVolume.setProgress(0);
        } else {    //  非静音
            //  第三个参数用于是否显示系统音量条 1 为显示  /   0 为不显示
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            //  设置进度条
            sbVolume.setProgress(volume);
        }
        currentVolume = volume;
    }
}
