package com.sszyc8.universalplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sszyc8.universalplayer.R;
import com.sszyc8.universalplayer.ui.DragLayout;
import com.sszyc8.universalplayer.ui.QuickIndexBar;
import com.sszyc8.universalplayer.utils.Utils;
import com.sszyc8.universalplayer.view.MyRelativeLayout;

import me.xiaopan.switchbutton.SwitchButton;

/**
 * 项目工程名: (万能播放器)UniversalPlayer
 * 作者：      sszyc8
 * 创建日期：  2016-03-02 18:27
 * 描述:      设置类
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2016-03-02 18:27
 * 修改原因描述:
 * 版本号:       1.0
 */
public class SettingActivity extends Activity {

    final private int CODE_SETTING_PASSWORD_RESULT = 101; //  当前页面的设置密码的resultcode

    private SharedPreferences mSp;

    private SwitchButton xml_sb_update;
    private SwitchButton xml_sb_privacy;
    private ImageButton xml_btn_back;
    private DragLayout mDragLayout;

    private RadioButton xml_rb_list;
    private RadioButton xml_rb_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        this.initView();
        this.initdata();
        this.initListener();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        //  自动更新
        xml_sb_update = (SwitchButton) findViewById(R.id.switch_update);
        //  隐私模式
        xml_sb_privacy = (SwitchButton) findViewById(R.id.switch_privacy);
        //  返回按钮
        xml_btn_back = (ImageButton) findViewById(R.id.btn_back);

        mDragLayout = (DragLayout) findViewById(R.id.set_dl);


        xml_rb_list = (RadioButton) findViewById(R.id.rb_list);
        xml_rb_setting = (RadioButton) findViewById(R.id.rb_setting);

        xml_rb_setting.setChecked(true);
    }

    /**
     * 初始化数据
     */
    private void initdata() {
        //  存储数据
        mSp = getSharedPreferences("config", MODE_PRIVATE);

        //  初始化设置中心里面自动检查更新开关状态（默认为开启状态）
        xml_sb_update.setChecked(mSp.getBoolean("setting_auto_update", true));

        //新页面接收数据(用户初始化密码完成后会走这个判断内)
        Bundle b = this.getIntent().getExtras();
        if (b != null) {
            boolean setting_status = b.getBoolean("setting_status");
            mSp.edit().putBoolean("setting_privacy_status", setting_status).commit();

        }
        //  初始化设置中心里面隐私模式开关状态（默认为关闭状态）
        xml_sb_privacy.setChecked(mSp.getBoolean("setting_privacy_status", false));

        MyRelativeLayout myRelativeLayout = (MyRelativeLayout) findViewById(R.id.mll_set);

        //  设置引用
        myRelativeLayout.setDragLayout(mDragLayout);
    }

    /**
     * 设置监听
     */
    public void initListener() {
        //  1.  设置自动更新开关监听
        xml_sb_update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSp.edit().putBoolean("setting_auto_update", isChecked).commit();
            }
        });

        //  2.  设置启用隐私模式监听
        xml_sb_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CompoundButton) v).isChecked();

                String password = mSp.getString("setting_password", "");
                if (TextUtils.isEmpty(password)) {
                    startActivityForResult(new Intent(SettingActivity.this, PassWordActivity.class), CODE_SETTING_PASSWORD_RESULT);
                } else {
                    mSp.edit().putBoolean("setting_privacy_status", isChecked).commit();
                }
            }
        });
        xml_btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, VideoListActivity.class));
                finish();
            }
        });
        xml_rb_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, VideoListActivity.class));
                finish();
            }
        });
    }

    // 回调方法，从第二个页面回来的时候会执行这个方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  设置密码页面返回的resultCode
        if (CODE_SETTING_PASSWORD_RESULT == resultCode) {
            if (data != null) {
                //得到新Activity关闭后返回的数据
                boolean settingPasswordStatus = data.getBooleanExtra("setting_status", false);
                //  设置中心里面隐私模式开关状态（默认为关闭状态）
                xml_sb_privacy.setChecked(settingPasswordStatus);
                //  设置隐私模式开启状态
                mSp.edit().putBoolean("setting_privacy_status", settingPasswordStatus).commit();
            }
        }
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(this, VideoListActivity.class));

        super.onBackPressed();
    }
}
