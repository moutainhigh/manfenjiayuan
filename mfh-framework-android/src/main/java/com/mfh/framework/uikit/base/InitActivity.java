package com.mfh.framework.uikit.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.IBaseViewComponent;
import com.mfh.framework.core.logic.MyMultiAsyncTask;
import com.mfh.framework.core.logic.UpdateResultsRunable;
import com.mfh.framework.core.utils.DialogUtil;

/**
 * 系统初始化界面，做版本检测、数据库安装等所有初始化工作
 * @author zhangyz created on 2013-4-5
 * @since Framework 1.0
 */
public abstract class InitActivity extends BaseActivity implements OnClickListener,
        IBaseViewComponent<Object, Object> {

    private Handler mHandler = null;//用于异步更新界面

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //SDK在统计Fragment时，需要关闭Activity自带的页面统计，
        //然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
//        MobclickAgent.openActivityDurationTrack(false);

        //MobclickAgent.setAutoLocation(true);
        //MobclickAgent.setSessionContinueMillis(1000);

        //发送策略定义了用户由统计分析SDK产生的数据发送回友盟服务器的频率
//        MobclickAgent.updateOnlineConfig(this);

//        doAsyncTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart(TAG);
//        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd(TAG);
//        MobclickAgent.onPause(this);
    }

    @Override
    public void doAsyncTask() {
        new MyMultiAsyncTask<>(this).execute();
    }

    @Override
    public void doAsyncTaskWithParam(Object... param) {
        new MyMultiAsyncTask<>(this).execute(param);
    }
    
    @Override
    public void doAsyncTask(int taskKind) {
        new MyMultiAsyncTask<>(this, taskKind).execute();
    }
    
    @Override
    public void doAsyncTask(int taskKind, Object... param) {
        new MyMultiAsyncTask<>(this, taskKind).execute(param);
    }
    
    @Override
    public void doAsyncUpdateUi(Object... param) {
        if (mHandler == null)
            mHandler = new Handler();
        mHandler.post(new UpdateResultsRunable<>(this, param));
    }
    
    @Override
    public Object doInBackground(int taskKind, Object... params) {
        initPrimary();

        InitService.getService(InitActivity.this).init(InitActivity.this);

        initSecondary();
        return 0;
    }
    
    @Override
    public void onPostExecute(int taskKind, Object result, Object...params) {
        initComleted();
    }    
    
    @Override
    public ProgressDialog onPreExecute(int taskKind) {
        // 开启进度条
//        return ProgressDialog.show(this, "请稍等...", "正在处理中...", true);
        return null;
    }

    @Override
    public void onProgressUpdate(int taskKind, Integer... values) {
        // TODO Auto-generated method stub
    }

    @Override
    public void doInBackgroundException(int taskKind, Throwable ex, Object... params) {
        //java.lang.ExceptionInInitializerError,静态块初始化过程失败
        //java.lang.NoClassDefFoundError
        ZLogger.e("执行出错:" + ex.toString());
        DialogUtil.showHint("执行出错:" + ex.getMessage());
    }

    @Override
    public void onClick(View view) {
        
    }

    /**
     * 初始化之前的准备工作
     */
    public void initPrimary() {
    }

    /**
     * 其他初始化工作
     */
    protected void initSecondary() {
        //获取服务器配置
        //还没有登录，会返回400
//        ServerConfig config = ServerConfig.getServerConfig(this);
//        config.init();
    }

    /**
     * 执行启动主界面
     */
    protected abstract void initComleted();
}
