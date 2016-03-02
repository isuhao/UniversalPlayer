package com.sszyc8.universalplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.sszyc8.universalplayer.R;
import com.sszyc8.universalplayer.utils.Utils;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private static final int CODE_ERROR = 0;  //  设置错误码
    private static final int CODE_SHOW_UPDATE_DIALOG = 1;  //  设置显示升级对话框
    private static final int CODE_ENTER_HOME = 2;   //  设置回到主页码

    private String mVersionName;//  版本名称
    private int mVersionCode;   //  版本号
    private String mDescription;//  版本描述
    private String mDownloadUrl;//  下载地址


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CODE_SHOW_UPDATE_DIALOG:
                    Utils.showToast(SplashActivity.this, "有新版本要更新");
                    showUpdateDailog();
                    break;
                case CODE_ENTER_HOME:
                    Utils.showToast(SplashActivity.this, "回到主页面");
                    intoMain();
                    break;
                case CODE_ERROR:
                    Utils.showToast(SplashActivity.this, "更新出错,回到主页面");
                    intoMain();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏状态栏
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //  初始化xutils
        x.Ext.init(this.getApplication());

        setContentView(R.layout.activity_splash);

        //  初始化布局
        this.initView();
        //  初始化数据
        this.initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {

        //  得到是启动更新状态(在设置中打开开启/关闭)
        SharedPreferences mSp = getSharedPreferences("config", MODE_PRIVATE);
        boolean isAutoUpdate = mSp.getBoolean("auto_update", true);
        //  判断是否要更新
        if (isAutoUpdate) {
            //调用检查版本
            this.checkVersion();
        } else {
            //  2秒后发送消息(返回主页)
            mHandler.sendEmptyMessageDelayed(CODE_ENTER_HOME, 2000);
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //动态设置版本号
        TextView tvVersion = (TextView) findViewById(R.id.tv_version);
//        tvVersion.setText("版本名:" + getVersionName());
    }


    /**
     * 进入视屏列表页面(主页面)
     */
    private void intoMain() {
        startActivity(new Intent(this, VideoListActivity.class));
        finish();
    }

    /**
     * 检查版本
     */
    private void checkVersion() {
        final long startTime = System.currentTimeMillis();

        Thread thread = new Thread() {
            @Override
            public void run() {
                //  请求的地址
                String urlPath = "http://192.168.2.101:8080/json/update.json";
                HttpURLConnection conn = null;
                Message msg = Message.obtain();
                try {
                    URL url = new URL(urlPath);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");// 设置请求方法
                    conn.setConnectTimeout(5000);// 设置连接超时
                    conn.setReadTimeout(5000);// 设置响应超时, 连接上了,但服务器迟迟不给响应
                    conn.connect();// 连接服务器

                    int responseCode = conn.getResponseCode();// 获取响应码
                    if (responseCode == 200) {
                        InputStream inputStream = conn.getInputStream();
                        String result = Utils.readFromStream(inputStream);

                        // 解析json
                        JSONObject jo = new JSONObject(result);
                        mVersionName = jo.getString("versionName");
                        mVersionCode = jo.getInt("versionCode");
                        mDescription = jo.getString("description");
                        mDownloadUrl = jo.getString("downloadUrl");
                        //  判断是否新的版本
                        if (mVersionCode > getVersionCode()) {// 判断是否有更新
                            msg.what = CODE_SHOW_UPDATE_DIALOG;
                        } else {
                            msg.what = CODE_ENTER_HOME;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = CODE_ERROR;
                } finally {
                    long endTime = System.currentTimeMillis();
                    long timeUsed = endTime - startTime;// 访问网络花费的时间
                    if (timeUsed < 2000) {
                        // 强制休眠一段时间,保证闪屏页展示2秒钟
                        try {
                            Thread.sleep(2000 - timeUsed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(msg);
                    if (conn != null) {
                        conn.disconnect();// 关闭网络连接
                    }
                }
            }
        };

        thread.start();
    }

    /**
     * 升级对话框
     */
    private void showUpdateDailog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("最新版本:" + mVersionName);// 设置标题
        alertDialog.setMessage(mDescription);   //  设置信息
        alertDialog.setPositiveButton("立刻更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                download();
            }
        });
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intoMain();
            }
        });
        //  返回事件
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                intoMain();
            }
        });
        alertDialog.show();
    }

    /**
     * 升级包下载地址
     */
    private void download() {
        //  判断内存卡是否可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            RequestParams params = new RequestParams(mDownloadUrl);
            //  路径加文件名
            params.setSaveFilePath(Environment.getExternalStorageDirectory() + "/UniversalPlayer " + getVersionName() + ".apk");
            x.http().get(params, new Callback.CommonCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    //  跳转到下载页面
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setDataAndType(Uri.fromFile(result),
                            "application/vnd.android.package-archive");
//                    startActivity(intent);
                    //  下载完新版本后,会跳转到安装页面,如果用户点击取消按钮,回返回结果
                    //  和onActivityResult()对应
                    startActivityForResult(intent, 0);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Toast.makeText(SplashActivity.this, "下载错误", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(CancelledException cex) {
                }

                @Override
                public void onFinished() {
                    Toast.makeText(SplashActivity.this, "下载完毕", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "存储卡不可用", Toast.LENGTH_SHORT);
            this.intoMain();
        }
    }

    /**
     * 用户点击取消安装会调用此方法,和startActivityForResult()对应
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        intoMain();
        super.onActivityResult(requestCode, resultCode, data);
    }

//  -------------------------- 工具相关--------------------------

    /**
     * 获取本地版版本名
     *
     * @return
     */
    public String getVersionName() {
        //  拿到包的管理器
        PackageManager packageManager = getPackageManager();
        try {
            //  得到包得信息(getPackageName()获取包名)
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //  没有找到对应的包名
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取本地版版本号
     *
     * @return
     */
    public int getVersionCode() {
        //  拿到包的管理器
        PackageManager packageManager = getPackageManager();
        try {
            //  得到包得信息(getPackageName()获取包名)
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //  没有找到对应的包名
            e.printStackTrace();
        }
        return 0;
    }

}
