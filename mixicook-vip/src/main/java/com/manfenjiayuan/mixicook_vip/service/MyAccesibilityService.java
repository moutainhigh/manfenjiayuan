package com.manfenjiayuan.mixicook_vip.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;


import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

import net.sourceforge.simcpux.WXHelper;

import java.util.List;

/**
 * 无障碍辅助服务
 * <a href="https://developer.android.com/training/accessibility/service.html">AccessibilityService</a>
 * AccessibilityService具有很高的系统权限，所以，系统不会让App直接设置是否启用，需要用户进入设置-辅助功能中去手动启用，这样在一定程度上，保护了用户数据的安全。
 * <p>
 * <p>
 * <p><a href="mailto:bingshanguxue@gmail.com">Email:bingshanguxue@gmail.com</a></p>
 * Created by bingshanguxue on 08/08/2017.
 */

public class MyAccesibilityService extends BaseAccessibilityService {
    private static final String PAKAGE_QQ = "com.tencent.mobileqq";
    private static final String PAKAGE_MM = "com.tencent.mm";

    private static final String TENCENT_MM_PLUGIN_LUCKMONEY_RECEIVE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    private static final String TENCENT_MM_PLUGIN_LUCKMONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    /**
     * 分享选择好友
     */
    private static final String TENCENT_MM_UI_SELECTCONVERSATION = "com.tencent.mm.ui.transmit.SelectConversationUI";
    private static final String TENCENT_MM_SEND_APPMESSAGE_WRAPPER_UI = "com.tencent.mm.ui.transmit.SendAppMessageWrapperUI";
    /**
     * 微信聊天界面
     */
    private static final String TENCENT_MM_UI_LAUNCHER = "com.tencent.mm.ui.LauncherUI";


    private String lastMmUserName;
    private boolean replyOnce = true;//true,在聊天页面仅回复一次，false,收到消息会自动触发自动回复


    /**
     * 必须重写的方法：此方法用了接受系统发来的event。在你注册的event发生是被调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String packageName = (String) event.getPackageName();
        ZLogger.d(String.format("eventType=%d, packageName=%s", eventType, packageName));

        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //界面点击
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                //界面文字改动
                break;
            //每次在聊天界面中有新消息到来时都出触发该事件
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                ZLogger.d("TYPE_VIEW_SCROLLED");
                //获取当前聊天页面的根布局
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                //获取聊天信息
                getWeChatLog(rootNode);
                break;
            // 监听到通知栏改变，执行相关操作
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
                ZLogger.d("TYPE_NOTIFICATION_STATE_CHANGED");

                if (PAKAGE_MM.equals(packageName)) {
                    notifyWechat(event);
                } else {
                    notifyOther(event);
                }
                break;
            }
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                ZLogger.d("TYPE_WINDOW_STATE_CHANGED");
                //  监听微信的窗口变化，获取到最后一条语音消息以及其长度，进行播放，播放完成后，自动转回App。
                if (PAKAGE_MM.equals(packageName)) {
                    performWechat(event);
                } else {

                }

                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        //服务中断，如授权关闭或者将服务杀死
        ZLogger.w("服务中断，如授权关闭或者将服务杀死");
    }


    /**
     * 在手机设置里找到“辅助服务”或“无障碍”开启后调用此方法初始化数据
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        ZLogger.i("服务连接成功");
        Toast.makeText(this, "已连接用户辅助服务", Toast.LENGTH_SHORT).show();


//        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
//        ZLogger.d("serviceInfo: " + serviceInfo.toString());
//        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        serviceInfo.packageNames = new String[]{"com.tencent.mm"};
//        serviceInfo.notificationTimeout = 100;
//        setServiceInfo(serviceInfo);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZLogger.d("bind TtsService");
        Intent bindIntent = new Intent(this, TtsService.class);
        bindService(bindIntent, ttsServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 服务已断开
     * 在正常关闭这个服务时会被调用。在这个方法中进行一些释放资源的工作。
     */
    @Override
    public boolean onUnbind(Intent intent) {
        ZLogger.i("服务断开");
        Toast.makeText(this, "服务已断开", Toast.LENGTH_SHORT).show();
        unbindService(ttsServiceConnection);

        return super.onUnbind(intent);
    }

    /**
     * 监听微信的推送通知，如果是文字类型，则直接取出，如果是语音类型，则跳到微信进行播放。
     */
    private void notifyWechat(AccessibilityEvent event) {
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            if (isScreenLocked()) {
                wakeAndUnlock();
            }
            Notification notification = (Notification) event.getParcelableData();
            ZLogger.d("tickerText=" + notification.tickerText);

            Bundle extras = notification.extras;
            if (null != extras) {
                String user = extras.getString("android.title");
                String content = extras.getString("android.text");
                ZLogger.d("user=" + user);
                lastMmUserName = user;

                if (!TextUtils.isEmpty(content) && content.contains("[语音]")) {
                    ZLogger.d("content=" + content);

//                        needPlayVoice = true;
                    Toast.makeText(this, user + "发来语音", Toast.LENGTH_LONG).show();
//                        MainActivity.playTTS(user + "发来语音");
                    performClickNotification(notification.contentIntent);
                } else {
                    if (content.indexOf(':') != -1) {
                        content = content.substring(content.indexOf(':') + 1);
                    }
                    ZLogger.d("content=" + content);
                    Toast.makeText(this, user + "发来消息:" + content, Toast.LENGTH_LONG).show();
//                            if (mTtsBinder != null) {
//                                mTtsBinder.cloudSpeak(content);
//                            }
//                            performClickNotification(notification.contentIntent);

                    // TODO: 09/08/2017 测试--回复随机字符串消息
//                    WXHelper.getInstance().sendMusicToWX();
                    //分享回复
//                    WXProxy.getInstance().sendTextToWX(StringUtils.genNonceStringByLength("TEST", 20), SendMessageToWX.Req.WXSceneSession);
                    //微信回复,模拟点击通知，进入微信页面
                    replyOnce = true;
                    performClickNotification(notification.contentIntent);
                }
            }
        }
    }

    private void notifyOther(AccessibilityEvent event) {
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            if (isScreenLocked()) {
                wakeAndUnlock();
            }
            Notification notification = (Notification) event.getParcelableData();
            ZLogger.d("tickerText=" + notification.tickerText);

            Bundle extras = notification.extras;
            if (null != extras) {
                String user = extras.getString("android.title");
                String content = extras.getString("android.text");
                ZLogger.d("user=" + user);

                //模拟手势
                switch (content) {
                    case "B":
                        performScrollBackward();
                        break;
                    case "F":
                        performScrollForward();
                        break;
                    case "BB"://OK
                        performBackClick();
                        break;
                    case "R"://OK
//                            Recod();
                        break;
                }
            }
        }
    }


    /**
     * 微信操作
     */
    private void performWechat(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = event.getSource();
        decodeAccessibilityNodeInfo(nodeInfo);

        String clazzName = event.getClassName().toString();
        ZLogger.d("clazzName=" + clazzName);

        //分享选择好友
        if (TENCENT_MM_UI_SELECTCONVERSATION.equals(clazzName)) {
            mmShareSession(nodeInfo, lastMmUserName);
        }
        //自动分享好友并关闭分享页面返回应用
        else if ("com.tencent.mm.ui.base.h".equals(clazzName)) {
            clickByText(nodeInfo, "Share");
            clickByText(nodeInfo, "分享");
            clickByText(nodeInfo, "Back to 冰珊孤雪");
        } else if (TENCENT_MM_UI_LAUNCHER.equals(clazzName)) {// && needPlayVoice){
            //TODO: 15/08/2017 如果需要自动回复，可以去掉判断
            if (replyOnce) {
                mmReplySession(nodeInfo);
                replyOnce = false;
            }

//            ZLogger.d("BACK TO APP");
////                needPlayVoice = false;
//            int ms = playAudioMsg(nodeInfo);
//            back(ms * 1000);
        } else if (TENCENT_MM_SEND_APPMESSAGE_WRAPPER_UI.equals(clazzName)) {
            clickByText(nodeInfo, "Share");
            clickByText(nodeInfo, "分享");
        } else if (clazzName.equals("com.tencent.mm.ui.base.aa")) {
            clickByText(nodeInfo, "分享");
        }

    }

    /**
     * 聊天对象
     */
    private String wexinChatName;
    /**
     * 聊天最新一条记录
     */
    private String wexinChatRecord;
    /**
     * 小视频的秒数，格式为00:00
     */
    private String wexinVideoSecond;

    /**
     * 遍历所有控件获取聊天信息
     *
     * @param rootNode
     */

    private void getWeChatLog(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            //获取所有聊天的线性布局
            List<AccessibilityNodeInfo> listChatRecord = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/p");
            if (listChatRecord.size() == 0) {
                ZLogger.w("find 0 chat record > com.tencent.mm:id/p");

                listChatRecord = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a4l");
                if (listChatRecord.size() == 0) {
                    ZLogger.w("find 0 chat record > com.tencent.mm:id/a4l");
                    return;
                }
            }

            //获取最后一行聊天的线性布局（即是最新的那条消息）
            AccessibilityNodeInfo finalNode = listChatRecord.get(listChatRecord.size() - 1);
            //获取聊天对象
            getChatName(finalNode);
            //获取聊天内容
//            getWeChatRecord(finalNode);
            getLatestWeChatRecord(getLatestChatName(finalNode), finalNode);
        }
    }

    /**
     * 遍历所有控件，找到头像Imagview，里面有对联系人的描述
     */
    private void getChatName(AccessibilityNodeInfo node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo node1 = node.getChild(i);
            if ("android.widget.ImageView".equals(node1.getClassName()) && node1.isClickable()) {
                //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                if (!TextUtils.isEmpty(node1.getContentDescription())) {
                    wexinChatName = node1.getContentDescription().toString();
                    if (wexinChatName.contains("头像")) {
                        wexinChatName = wexinChatName.replace("头像", "");
                    } else if (wexinChatName.contains("Profile Photo")) {
                        wexinChatName = wexinChatName.replace("Profile Photo", "");
                    }
                    ZLogger.d("wexinChatName=" + wexinChatName);
                }
            }
            getChatName(node1);
        }
    }


    private String getLatestChatName(AccessibilityNodeInfo node) {
        String chatName = null;
        if (node != null && node.getChildCount() > 0) {
            AccessibilityNodeInfo child = node.getChild(node.getChildCount() - 1);
            if ("android.widget.ImageView".equals(child.getClassName()) && child.isClickable()) {
                //获取聊天对象,这里两个if是为了确定找到的这个ImageView是头像的
                if (!TextUtils.isEmpty(child.getContentDescription())) {
                    chatName = child.getContentDescription().toString();
                    if (chatName.contains("头像")) {
                        chatName = chatName.replace("头像", "");
                    } else if (chatName.contains("Profile Photo")) {
                        chatName = chatName.replace("Profile Photo", "");
                    }
                }
            }
        }
        ZLogger.d("chatName=" + chatName);
        return chatName;
    }

    public void getLatestWeChatRecord(String chatName, AccessibilityNodeInfo node) {
        String chatRecord = null;
        if (node != null && node.getChildCount() > 0) {
            AccessibilityNodeInfo nodeChild = node.getChild(node.getChildCount() - 1);
            //聊天内容是:文字聊天(包含语音秒数)
            if ("android.widget.TextView".equals(nodeChild.getClassName()) && "android.widget.RelativeLayout".equals(nodeChild.getParent().getClassName().toString())) {
                if (!TextUtils.isEmpty(nodeChild.getText())) {
                    String RecordText = nodeChild.getText().toString();
                    //这里加个if是为了防止多次触发TYPE_VIEW_SCROLLED而打印重复的信息
                    if (!RecordText.equals(chatRecord)) {
                        chatRecord = RecordText;
                        //判断是语音秒数还是正常的文字聊天,语音的话秒数格式为5"
                        if (chatRecord.contains("\"")) {
                            Toast.makeText(this, chatName + "发了一条" + chatRecord + "的语音", Toast.LENGTH_SHORT).show();

                            ZLogger.i(chatName + "发了一条" + chatRecord + "的语音");
                        } else {
                            //这里在加多一层过滤条件，确保得到的是聊天信息，因为有可能是其他TextView的干扰，例如名片等
                            if (nodeChild.isLongClickable()) {
                                Toast.makeText(this, chatName + "：" + chatRecord, Toast.LENGTH_SHORT).show();

                                ZLogger.i(chatName + "：" + chatRecord);
                            }
                        }
                        return;
                    }
                }
            }

            //聊天内容是:表情
            if ("android.widget.ImageView".equals(nodeChild.getClassName()) && "android.widget.LinearLayout".equals(nodeChild.getParent().getClassName().toString())) {
                Toast.makeText(this, chatName + "发的是表情", Toast.LENGTH_SHORT).show();

                ZLogger.e(chatName + "发的是表情");

                return;
            }

            //聊天内容是:图片
            if ("android.widget.ImageView".equals(nodeChild.getClassName())) {
                //安装软件的这一方发的图片（另一方发的暂时没实现）
                if ("android.widget.FrameLayout".equals(nodeChild.getParent().getClassName().toString())) {
                    if (!TextUtils.isEmpty(nodeChild.getContentDescription())) {
                        if (nodeChild.getContentDescription().toString().contains("图片")) {
                            Toast.makeText(this, chatName + "发的是图片", Toast.LENGTH_SHORT).show();

                            ZLogger.e(chatName + "发的是图片");
                        }
                    }
                }
            }

            //聊天内容是:小视频秒数,格式为00：00
            if ("android.widget.TextView".equals(nodeChild.getClassName()) && "android.widget.FrameLayout".equals(nodeChild.getParent().getClassName().toString())) {
                if (!TextUtils.isEmpty(nodeChild.getText())) {
                    String second = nodeChild.getText().toString().replace(":", "");
                    //正则表达式，确定是不是纯数字,并且做重复判断
                    if (second.matches("[0-9]+") && !second.equals(wexinVideoSecond)) {
                        Toast.makeText(this, chatName + "发了一段" + second + "的小视频", Toast.LENGTH_SHORT).show();

                        ZLogger.e("发了一段" + second + "的小视频");
                    }
                }

            }
        }
    }

    /**
     * 遍历所有控件:这里分四种情况
     * 文字聊天: 一个TextView，并且他的父布局是android.widget.RelativeLayout
     * 语音的秒数: 一个TextView，并且他的父布局是android.widget.RelativeLayout，但是他的格式是0"的格式，所以可以通过这个来区分
     * 图片:一个ImageView,并且他的父布局是android.widget.FrameLayout,描述中包含“图片”字样（发过去的图片），发回来的图片现在还无法监听
     * 表情:也是一个ImageView,并且他的父布局是android.widget.LinearLayout
     * 小视频的秒数:一个TextView，并且他的父布局是android.widget.FrameLayout，但是他的格式是00:00"的格式，所以可以通过这个来区分
     *
     * @param node
     */
    public void getWeChatRecord(AccessibilityNodeInfo node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo nodeChild = node.getChild(i);

            //聊天内容是:文字聊天(包含语音秒数)
            if ("android.widget.TextView".equals(nodeChild.getClassName()) && "android.widget.RelativeLayout".equals(nodeChild.getParent().getClassName().toString())) {
                if (!TextUtils.isEmpty(nodeChild.getText())) {
                    String RecordText = nodeChild.getText().toString();
                    //这里加个if是为了防止多次触发TYPE_VIEW_SCROLLED而打印重复的信息
                    if (!RecordText.equals(wexinChatRecord)) {
                        wexinChatRecord = RecordText;
                        //判断是语音秒数还是正常的文字聊天,语音的话秒数格式为5"
                        if (wexinChatRecord.contains("\"")) {
                            Toast.makeText(this, wexinChatName + "发了一条" + wexinChatRecord + "的语音", Toast.LENGTH_SHORT).show();

                            ZLogger.i(wexinChatName + "发了一条" + wexinChatRecord + "的语音");
                        } else {
                            //这里在加多一层过滤条件，确保得到的是聊天信息，因为有可能是其他TextView的干扰，例如名片等
                            if (nodeChild.isLongClickable()) {
                                Toast.makeText(this, wexinChatName + "：" + wexinChatRecord, Toast.LENGTH_SHORT).show();

                                ZLogger.i(wexinChatName + "：" + wexinChatRecord);
                            }
                        }
                        return;
                    }
                }
            }

            //聊天内容是:表情
            if ("android.widget.ImageView".equals(nodeChild.getClassName()) && "android.widget.LinearLayout".equals(nodeChild.getParent().getClassName().toString())) {
                Toast.makeText(this, wexinChatName + "发的是表情", Toast.LENGTH_SHORT).show();

                ZLogger.e(wexinChatName + "发的是表情");

                return;
            }

            //聊天内容是:图片
            if ("android.widget.ImageView".equals(nodeChild.getClassName())) {
                //安装软件的这一方发的图片（另一方发的暂时没实现）
                if ("android.widget.FrameLayout".equals(nodeChild.getParent().getClassName().toString())) {
                    if (!TextUtils.isEmpty(nodeChild.getContentDescription())) {
                        if (nodeChild.getContentDescription().toString().contains("图片")) {
                            Toast.makeText(this, wexinChatName + "发的是图片", Toast.LENGTH_SHORT).show();

                            ZLogger.e(wexinChatName + "发的是图片");
                        }
                    }
                }
            }

            //聊天内容是:小视频秒数,格式为00：00
            if ("android.widget.TextView".equals(nodeChild.getClassName()) && "android.widget.FrameLayout".equals(nodeChild.getParent().getClassName().toString())) {
                if (!TextUtils.isEmpty(nodeChild.getText())) {
                    String second = nodeChild.getText().toString().replace(":", "");
                    //正则表达式，确定是不是纯数字,并且做重复判断
                    if (second.matches("[0-9]+") && !second.equals(wexinVideoSecond)) {
                        wexinVideoSecond = second;
                        Toast.makeText(this, wexinChatName + "发了一段" + nodeChild.getText().toString() + "的小视频", Toast.LENGTH_SHORT).show();

                        ZLogger.e("发了一段" + nodeChild.getText().toString() + "的小视频");
                    }
                }

            }

            getWeChatRecord(nodeChild);
        }
    }

    /**
     * 点击通知栏信息
     */
    private void performClickNotification(final PendingIntent pendingIntent) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);
    }

    protected TtsService.TtsBinder mTtsBinder;
    private ServiceConnection ttsServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ZLogger.d("onServiceDisconnected: " + name);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ZLogger.d("onServiceConnected: " + name);
            mTtsBinder = (TtsService.TtsBinder) service;
//            mTtsBinder.startDownload();
        }
    };

    /**
     * 播放语音
     */
    private int playAudioMsg(AccessibilityNodeInfo nodeInfo) {
        if (null == nodeInfo) return 0;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("语音");
        if (null != list && list.size() > 0) {
            AccessibilityNodeInfo node = list.get(list.size() - 1);
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            String content = node.getContentDescription().toString();
            String number = content.substring(2, content.length() - 2);
            return Integer.parseInt(number);
        }
        return 0;
    }

    /**
     * 自动跳回
     */
    private void back2App(int time) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                if (intent != null) {
                    startActivity(intent);
                }
            }
        }, time);
    }

    private void decodeAccessibilityNodeInfo(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        int childCount = nodeInfo.getChildCount();

        ZLogger.d(String.format("<%s#%s> %s %s",
                nodeInfo.getPackageName(), nodeInfo.getClassName(), nodeInfo.getText(), nodeInfo.toString()));

        for (int i = 0; i < childCount; i++) {
            decodeAccessibilityNodeInfo(nodeInfo.getChild(i));
        }
    }

    /**
     * 通过分享方式发送消息给好友
     */
    private void mmShareSession(AccessibilityNodeInfo nodeInfo, String user) {
        if (null == nodeInfo) return;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(user);
        if (null != list && list.size() > 0) {
            AccessibilityNodeInfo node = list.get(list.size() - 1);
            ZLogger.d("node=" + node.toString());
            while (!node.isClickable()) {
                node = node.getParent();
                if (node.isClickable()) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        } else {
            ZLogger.d("can not find " + user);
        }
    }

    /**
     * 拉起微信界面，通过回复发送消息给好友
     */
    private void mmReplySession(AccessibilityNodeInfo nodeInfo) {
        //getRootInActiveWindow()
        if (fill(nodeInfo)) {
            send(nodeInfo);
        }
    }

    public AccessibilityNodeInfo findEditText2(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return null;
        }
        if ("android.widget.EditText".equals(rootNode.getClassName())) {
            ZLogger.w("find a EditText");
            return rootNode;
        }

        ZLogger.i(rootNode.toString());
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            AccessibilityNodeInfo item = findEditText2(nodeInfo);
            if (item != null && "android.widget.EditText".equals(item.getClassName())) {
                ZLogger.w("find a EditText");
                return item;
            }
        }

        return null;
    }

    private boolean fill(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            ZLogger.w("fill: " + nodeInfo.toString());
            decodeAccessibilityNodeInfo(nodeInfo);

            AccessibilityNodeInfo inputNode = findEditText2(nodeInfo);
            if (inputNode != null) {
                inputText(inputNode, StringUtils.genNonceStringByLength("TEST", 20));
                return true;
            } else {
                ZLogger.w("not find editText");
            }
        } else {
            ZLogger.w("rootNode is null");
        }
        return false;
    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    @SuppressLint("NewApi")
    private void send(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            ZLogger.w("send: " + nodeInfo.toString());
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("发送");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            } else {
                List<AccessibilityNodeInfo> liste = nodeInfo
                        .findAccessibilityNodeInfosByText("Send");
                if (liste != null && liste.size() > 0) {
                    for (AccessibilityNodeInfo n : liste) {
                        if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
//            performBackClick();
            back2App(100);
        }
    }

    /**
     * 根据文本搜索控件并点击
     */
    private void clickByText(AccessibilityNodeInfo nodeInfo, String text) {
        if (null == nodeInfo) return;
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (null != list && list.size() > 0) {
            ZLogger.d("click " + text);
            AccessibilityNodeInfo node = list.get(list.size() - 1);
            decodeAccessibilityNodeInfo(node);
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }
}
