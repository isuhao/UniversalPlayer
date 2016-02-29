package com.sszyc8.universalplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.sszyc8.universalplayer.R;

import org.xutils.x;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2015-12-05 19:50
 * 描述:      欢迎页面
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2015-12-05 19:50
 * 修改原因描述:
 * 版本号:       1.0
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏状态栏
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //  初始化xutils
        x.Ext.init(this.getApplication());

        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intoMain();
            }
        }, 2000);
    }

    private void intoMain() {
        startActivity(new Intent(this, VideoListActivity.class));
        finish();
    }
}
