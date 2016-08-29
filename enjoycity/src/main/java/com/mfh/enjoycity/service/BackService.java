package com.mfh.enjoycity.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 专门用来做消息提醒的一个Service
 * 值得注意的是，这个Service不是我们框架内的Service，是安卓的后台服务Service
 * Created by 李潇阳 on 14-8-8.
 */
public class BackService extends Service {
    public static String DATA_VALUE = "data";
    public static String NAME_VALUE = "name.value";
    public static String GE_TUI_SESSION_ID = "ge.tui.session.id";
    private static final String TAG = "MsgService";
    //private static final String ACTION = ".com.mfh.comna.android_service.MsgService";
    private BroadcastReceiver receiver = null;
    private Timer timer;
    private static BackService mInstance = null;
    private TimerTask task;
    private SharedPreferences sp;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZLogger.d("BackService.onCreate");
        mInstance = this;
        start();
    }

    public static BackService getInstance() {
        return mInstance;
    }


    public void start() {
                task = new TimerTask() {
                    @Override
                    public void run() {
                        //发送消息触发
                        Message message = new Message();
                        handler.sendMessage(message);
                    }
                };
                timer = new Timer();
                timer.schedule(task, 10000, 10000);//2秒后，2秒一次
            }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                //sessionService = ServiceFactory.getService(IMConversationService.class);
               // WorkOrderService workOrderService = ServiceFactory.getService(WorkOrderService.class);
                //workOrderService.queryFromNet();
                //sp = getSharedPreferences("msgMode", 0);
                //int msgMode = sp.getInt("msgModez", -2);
               //if (msgMode != -2) {
                    //sessionService.setMsgMode(msgMode);
                    //sessionService.queryFromNet();
              //  }
                //LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                //Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            }
            catch (Throwable ex) {

                DialogUtil.showHint(MfhApplication.getAppContext(), ex.getMessage());
            }
        }
    };


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        if (receiver != null)
            unregisterReceiver(receiver);
        if (sp != null)
            sp.edit().clear().commit();
        super.onDestroy();
        if (timer == null)
            return;
        task.cancel();
        timer.cancel();//终止
        task = null;
        timer = null;
    }
}
