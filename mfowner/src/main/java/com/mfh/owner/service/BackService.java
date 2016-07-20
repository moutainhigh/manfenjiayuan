package com.mfh.owner.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.manfenjiayuan.im.IMConstants;
import com.manfenjiayuan.im.database.entity.IMConversation;
import com.manfenjiayuan.im.database.service.IMConversationService;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
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
    private IMConversationService sessionService = ServiceFactory.getService(IMConversationService.class);
    private TimerTask task;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZLogger.d("BackService.onCreate");
        mInstance = this;
        registerMsgReceiver();
        start();
    }

    public static BackService getInstance() {
        return mInstance;
    }

    /*public void sendError(final String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }*/

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

    private void registerMsgReceiver() {
       final BackService that = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction(IMConstants.ACTION_RECEIVE_MSG_BACK);//提醒有新的消息
        //filter.addAction(IMConstants.HAVE_NEW_WORK_ORDER);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                ZLogger.d("BackService.onReceive.action=" + action);
               /* //获取设置值
                SharedPreferences sharedPref = PreferenceManager
                        .getDefaultSharedPreferences(BackService.this);
                boolean ownerMsg = sharedPref.getBoolean(getResources()
                        .getString(R.string.switch_ownerMsg_key), true);
                boolean woAlertMsg = sharedPref.getBoolean(getResources()
                        .getString(R.string.switch_newWorkOrderMsg_key), true);*/

                if (IMConstants.ACTION_RECEIVE_MSG_BACK.equals(action)) {
                    String data = intent.getStringExtra(DATA_VALUE);
                    String name = intent.getStringExtra(NAME_VALUE);
                    Long sessionId = intent.getLongExtra(GE_TUI_SESSION_ID, 0);

                    if (data == null)
                        data = "";
                    if (name == null)
                        name = "您有新的消息:";
                    else
                        name += ":";
//                    Log.d("Nat", String.format("data=%s, name=%s, sessionId=%s", data, name, sessionId));

                    //if (ownerMsg == true ){//用sessionId
                    if  (sessionId == 0 || "0".equals(sessionId))
                        NoticeUtilForPmb.showNotification(that, IMConstants.MSG_NOTIFICATION, "消息提示", name + data);
                    else if (sessionService.getDao().entityExistById(sessionId)){
                        IMConversation session = sessionService.getDao().getEntityById(sessionId);
                        NoticeUtilForPmb.showNotificationWithSessionId(that,
                                session, "消息提示", name + data );
                    }else{
                        ZLogger.d("未找到对应的session");
                    }
                }
                //else if (IMConstants.HAVE_NEW_WORK_ORDER.equals(action)) {
                    //if (woAlertMsg == true){
                        //NoticeUtilForPmb.showNotification(that, IMConstants.WORK_ORDER_NOTIFICATION, "物业", "您有新的工单");
                    }
              //  }
           // }
        };
       /* if (receiver != null)
            registerReceiver(receiver, filter);*/
    }

    public void showNotification(int id, String title, String content) {
        NoticeUtilForPmb.showNotification(this, IMConstants.MSG_NOTIFICATION, title, content);
    }

    /**
     * 显示通知
     * */
    public void showNotification(Long sid, String title, String content) {
        ZLogger.d(String.format("sessionId=%s", String.valueOf(sid)));
        //使用下面的方法跳转至ChatActivity页面，出现问题：如果在MainActivity页面按返回按键退出到后台，
        // 则点击通知后进入对话然后按返回，不会进入MainActivity页面
        NoticeUtilForPmb.showNotification(this, IMConstants.NOTIFICATION_NEW_MESSAGE, sid, title, content);
        //使用下面的方法先跳转到MainActivity页面，然后再跳转至ChatActivity页面，出现问题：总是跳转至同一个对话。
//        NoticeUtilForPmb.showNotification(this, MsgConstants.MSG_NOTIFICATION_SESSIOIN, sid, title, content);
//        NoticeUtilForPmb.showNotification(this, MsgConstants.MSG_NOTIFICATION_SESSIOIN, title, content);
    }

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
        super.onDestroy();
        if (timer == null)
            return;
        task.cancel();
        timer.cancel();//终止
        task = null;
        timer = null;
    }
}
