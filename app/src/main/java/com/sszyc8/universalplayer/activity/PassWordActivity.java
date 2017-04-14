package com.sszyc8.universalplayer.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.sszyc8.universalplayer.R;
import com.sszyc8.universalplayer.adapter.SSAdapter;
import com.sszyc8.universalplayer.anim.Animation;
import com.sszyc8.universalplayer.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sszyc8 on 16/3/20.
 */
public class PassWordActivity extends Activity {

    final private int CODE_SETTING_PASSWORD_RESULT = 101; //  当前页面的设置密码的resultcode

    //  用于记录用户是否正常设置完成密码
    final private boolean CODE_SETTING_PASSWORD_STATUS_CORRECTENSS = true;  //  正确
    final private boolean CODE_SETTING_PASSWORD_STATUS_ERROR = false;       //  错误

    //  密码输入错误
    final private int CODE_PASSWORD_INPUT_ERROR = 0;
    //  密码输入完成
    final private int CODE_PASSWORD_INPUT_COMPLETE = 1;
    //  密码输入正确
    final private int CODE_PASSWORD_INPUT_CORRECTENSS = 2;
    //  设置密码完成
    final private int CODE_PASSWORD_SETTING_COMPLETE = 3;
    //  设置密码2次密码不一致
    final private int CODE_PASSWORD_SETTING_INCOMPATIBLE = 4;

    //  用于本地保存数据
    private SharedPreferences mSp;

    //  密码显示框中数字增加删除索引
    private int mStartPassIndex = 1;

    private TextView xml_tv_msg;
    private GridView xml_gv_pass;
    private GridView xml_gv_show_pass;

    //  用于记录用户输入的密码
    private String password = "";

    //  输入密码的list
    private List<String> mGvPassWordList = new ArrayList<String>();
    //  显示密码的list
    private List mGvShowPassWordList = new ArrayList<String>();


    //  handler处理消息
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_PASSWORD_INPUT_ERROR: //  密码输入错误
//                    Utils.showToast(PassWordActivity.this, "密码输入错误");
                    //  输入密码错误
                    inputPasswordError("密码错误，请再次输入");
                    break;
                case CODE_PASSWORD_INPUT_COMPLETE:  //  密码输入完成
//                    Utils.showToast(PassWordActivity.this, "密码输入完成");
                    checkPassword();
                    break;
                case CODE_PASSWORD_INPUT_CORRECTENSS:   //  密码输入正确
//                    Utils.showToast(PassWordActivity.this, "密码输入正确");
                    //  密码输入正确
                    inputPasswordCorrectenss();
                    break;
                case CODE_PASSWORD_SETTING_INCOMPATIBLE:    //  设置密码2次不一致
                    //  确认密码输入错误
                    inputPasswordError("确认密码错误，请再次输入");
                    break;
                case CODE_PASSWORD_SETTING_COMPLETE:    //  密码设置完成
                    settingPasswordComplete();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_password);

        initView();
        initData();
        initListener();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        xml_tv_msg = (TextView) findViewById(R.id.tv_msg);
        //  显示密码的GridView
        xml_gv_show_pass = (GridView) findViewById(R.id.lv_showPass);
        //  设置密码的GridView
        xml_gv_pass = (GridView) findViewById(R.id.gv_pass);
    }

    /**
     * 初始化数据
     */
    private void initData() {

        //  新页面接收数据
        Bundle b = this.getIntent().getExtras();
        //  用于输入确认密码时所用
        if (b != null) {
            xml_tv_msg.setText("请再次输入确认密码");
        }

        //  存储数据
        mSp = getSharedPreferences("config", MODE_PRIVATE);

        //  初始化显示密码list
        mGvShowPassWordList = Utils.getCustomSizeList(mGvShowPassWordList, 4);
        //  初始化输入密码的list
        mGvPassWordList = getGvPassWordList();

        //  设置显示密码gridView适配器
        xml_gv_show_pass.setAdapter(new SSAdapter<String>(this, mGvShowPassWordList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //  得到主页项视图
                View view = android.view.View.inflate(PassWordActivity.this, R.layout.pass_show_gv_item, null);
                return view;
            }
        });

        //  设置输入密码gridView适配器(数据少，无需处理..)
        xml_gv_pass.setAdapter(new SSAdapter<String>(this, mGvPassWordList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                //  得到主页项视图
                View view = android.view.View.inflate(PassWordActivity.this, R.layout.pass_gv_item, null);
                //  得到主页项的icon 和  文字
                TextView tv_main_text = (TextView) view.findViewById(R.id.tv_main_text);
                //  从新给主页项的icon和文字赋值
                tv_main_text.setText(this.getList().get(position));

                return view;
            }
        });
    }


    /**
     * 初始化监听
     */
    private void initListener() {
        //  1.  设置密码输入监听
        xml_gv_pass.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 9:
                        //  Utils.showToast(PassWordActivity.this, "清空");
                        cleanPassword();
                        break;
                    case 11:
                        //  Utils.showToast(PassWordActivity.this, "删除");
                        //  删除密码操作
                        deletePassWord();
                        break;
                    default:
                        //  设置密码
                        setPassWord(mGvPassWordList.get(position));
                        //  播放放大缩小动画
                        Utils.playAnimScale(view);
                        break;
                }
            }
        });
    }

    /**
     * 设置密码
     *
     * @param num 密码数字
     */
    private void setPassWord(String num) {
        //  已经输入够了4位密码
        if (mStartPassIndex <= mGvShowPassWordList.size()) {

            //  获取将要改变的View
            View view = xml_gv_show_pass.getChildAt(mStartPassIndex - 1);
            //  获取将要改变的textView
            TextView xml_tv_pass = (TextView) view.findViewById(R.id.tv_pass);
            //  设置改变值
            xml_tv_pass.setText(num);

            //  用于密码传递，保存
            password = password + num;

            //  判断用户是否输入密码完成
            if (mStartPassIndex == mGvShowPassWordList.size()) {
                //  立刻发送密码输入完成的消息,
                mHandler.sendEmptyMessage(CODE_PASSWORD_INPUT_COMPLETE);
            }
            //  密码索引加1
            mStartPassIndex++;
        }
    }

    /**
     * 删除密码
     */
    private void deletePassWord() {
        //  密码已经删除完成
        if (mStartPassIndex == 1) {
            return;
        }

        //  获取将要改变的View
        View view = xml_gv_show_pass.getChildAt(mStartPassIndex - 2);
        //  获取将要改变的textView
        TextView xml_tv_pass = (TextView) view.findViewById(R.id.tv_pass);
        //  设置改变值
        xml_tv_pass.setText("");
        password = password.substring(0, password.length() - 1);

        //  密码索引加1
        mStartPassIndex--;
    }

    /**
     * 清空密码
     */
    private void cleanPassword() {
        for (int i = 0; i < password.length(); i++) {
            //  获取将要改变的View
            View view = xml_gv_show_pass.getChildAt(i);
            //  获取将要改变的textView
            TextView xml_tv_pass = (TextView) view.findViewById(R.id.tv_pass);
            //  设置改变值
            xml_tv_pass.setText("");
        }
        password = "";
        mStartPassIndex = 1;
    }

    /**
     * 键盘布局的list
     *
     * @return list
     */
    private List<String> getGvPassWordList() {
        for (int i = 1; i <= 9; i++) {
            mGvPassWordList.add(i + "");
        }
        mGvPassWordList.add("清空");
        mGvPassWordList.add("0");
        mGvPassWordList.add("<-");
        return mGvPassWordList;
    }

    /**
     * 密码设置完成
     */
    private void settingPasswordComplete() {
        //  保存密码
        mSp.edit().putString("setting_password", password).commit();
        //  跳转到视频列表页面

        //新建一个显式意图，第一个参数为当前Activity类对象，第二个参数为你要打开的Activity类
        Intent intent = new Intent(PassWordActivity.this, SettingActivity.class);

        //用Bundle携带数据
        Bundle bundle = new Bundle();
        bundle.putBoolean("setting_status", CODE_SETTING_PASSWORD_STATUS_CORRECTENSS);
        intent.putExtras(bundle);

        startActivity(intent);

        finish();
    }

    /**
     * 密码输入正确
     */
    private void inputPasswordCorrectenss() {
        //  跳转到视频列表页面
        startActivity(new Intent(PassWordActivity.this, VideoListActivity.class));
        finish();
    }

    /**
     * 密码输入错误
     */
    private void inputPasswordError(String msg) {
        //  1.修改提示文字
        xml_tv_msg.setText(msg);
        xml_tv_msg.setTextColor(0xFFCD5C5C);

        //  抖动效果
        android.view.animation.Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        xml_gv_show_pass.startAnimation(shake);
    }

    /**
     * 密码输入完成，检查密码
     */
    private void checkPassword() {
        //新页面接收数据
        Bundle b = this.getIntent().getExtras();

        //  判断是否是第一次输入
        if (b == null) {
            String spPassword = mSp.getString("setting_password", "");
            if (!TextUtils.isEmpty(spPassword)) {
                if (password.equals(spPassword)) {
                    //  立刻发送密码输入正确的消息,
                    mHandler.sendEmptyMessage(CODE_PASSWORD_INPUT_CORRECTENSS);
                } else {
                    //  立刻发送密码输入错误的消息,
                    mHandler.sendEmptyMessage(CODE_PASSWORD_INPUT_ERROR);
                }
                return;
            }
            //新建一个显式意图，第一个参数为当前Activity类对象，第二个参数为你要打开的Activity类
            Intent intent = new Intent(PassWordActivity.this, PassWordActivity.class);

            //用Bundle携带数据
            Bundle bundle = new Bundle();
            bundle.putString("password", password);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();

        } else {    //  不是第一次输入
            String prePassword = b.getString("password");
            //  上一次密码和当前密码比较
            if (prePassword.equals(password)) {
                //  立刻发送密码设置完成的消息,
                mHandler.sendEmptyMessage(CODE_PASSWORD_SETTING_COMPLETE);
            } else {
                //  立刻发送密码设置不一致的消息,
                mHandler.sendEmptyMessage(CODE_PASSWORD_SETTING_INCOMPATIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //新页面接收数据
        Bundle b = this.getIntent().getExtras();
        if (b == null) {    //  首次输入密码页面
            //  用户点击返回键，吧当前设置密码开启状态传到设置页面
            Intent mIntent = new Intent(PassWordActivity.this, SettingActivity.class);
            mIntent.putExtra("setting_status", CODE_SETTING_PASSWORD_STATUS_ERROR);
            // 设置结果，并进行传送
            setResult(CODE_SETTING_PASSWORD_RESULT, mIntent);
        } else {    //  再次输入密码页面
            startActivity(new Intent(this, SettingActivity.class));
        }
        super.onBackPressed();
    }
}
