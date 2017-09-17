package com.manfenjiayuan.mixicook_vip.service;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.jabra.bean.Contacts;
import com.jabra.bean.Message;
import com.jabra.bean.PlayableItem;
import com.jabra.data.ContactsContainer;
import com.jabra.data.ContactsSortList;
import com.jabra.listener.AudioPlayerListener;
import com.jabra.listener.MsgListener;
import com.jabra.listener.NetworkListener;
import com.jabra.listener.PlayMsgListener;
import com.jabra.listener.WXLuanchListener;
import com.jabra.media.AudioPlayer;
import com.jabra.media.SafeSco;
import com.jabra.receiver.NetworkReceiver;
import com.jabra.receiver.WXLuanchReceiver;
import com.jabra.receiver.WXMsgReceiver;
import com.jabra.utils.EmptyUtil;
import com.jabra.utils.SPFile;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.record.RecordCompleteListener;
import com.manfenjiayuan.mixicook_vip.record.Recorder;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by bingshanguxue on 09/08/2017.
 */

public class MainService extends BaseService {
    public static final String EXTRA_BOOT_LAUNCH = "EXTRA_BOOT_LAUNCH";

    public static boolean IS_RUNNING = false;
    private static final int MAX_AUTO_PLAY_MSG_TIME = 25000;
    private static final int MAX_RESPONSE_TIMEOUT = 10000;
    private static final int REBIND_JABRA_SERVICE_DELAY = 2000;
    private static final String TAG = "MainService";
    private boolean isAbandonCurrentContactsMsgList;
    private boolean isReAutoPlayMsg;
    private ContactsContainer mContactsContainer;
    private Handler mHandler = new Handler();
    //    private HeadsetBtnEventReceiver mHeadsetBtnEventReceiver;
//    private HeadsetConnectReceiver mHeadsetConnectReceiver;
    private NetworkReceiver mNetworkReceiver;

    private AudioManager mAudioManager;
    private AudioPlayer mAudioPlayer;

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                ZLogger.w("Audio 失去焦点  ");
                if (MainService.this.mAudioPlayer.isPlaying()) {
                    ZLogger.d("暂停播放");
                    MainService.this.mAudioPlayer.stop();
                    MainService.this.mAudioPlayer.setStopBySystem(true);
                    MainService.this.mAudioPlayer.clearPreUserId();
                    MainService.this.mAudioPlayer.setCurrentContacts(null, null);
                }
                if (MainService.this.mRecorder.isRecording()) {
                    ZLogger.d("取消录音");
                    MainService.this.mRecorder.cancel();
                }
            } else {
                ZLogger.i("Audio重新获取焦点");
            }
        }
    };

    private AudioPlayerListener mAudioPlayerListener = new AudioPlayerListener() {
        public void onComplete() {
            ZLogger.d("播放完成");
            MainService.this.mAudioManager.abandonAudioFocus(MainService.this.mAudioFocusChangeListener);
        }
    };

    private boolean isTeleBusy;
    private long phoneStateTime;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        private boolean isEffect;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                ZLogger.v("空闲 isEffect:" + this.isEffect);
                MainService.this.isTeleBusy = false;
                return;
//                if (MainService.this.mHeadsetConnectReceiver.getState() == 3) {
//                }
            }

            if (MainService.this.mAudioPlayer.isPlaying()) {
                ZLogger.d("暂停播放");
                MainService.this.mAudioPlayer.stop();
                MainService.this.mAudioPlayer.clearPreUserId();
                MainService.this.mAudioPlayer.setCurrentContacts(null, null);
                MainService.this.mAudioPlayer.setStopBySystem(true);
            } else {
                do {

                } while (System.currentTimeMillis() - MainService.this.phoneStateTime < 10L);

                if (!this.isEffect) {
                    MainService.this.phoneStateTime = System.currentTimeMillis();
                    this.isEffect = true;
                    return;
                }

                MainService.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (MainService.this.receivedMsgWhenPhoneBusy) {
                            MainService.this.receivedMsgWhenPhoneBusy = false;
                            MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
                        }

                        if ((MainService.this.mContactsContainer.getCurrentContacts() == null)
                                || (MainService.this.mContactsContainer.isEmpty())) {
                            ZLogger.w("mContactsContainer is empty");
                            return;
                        }

                        MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
                    }
                }, 600L);
//                return;
                ZLogger.d("通话中");
                MainService.this.isTeleBusy = true;
                if (MainService.this.mRecorder.isRecording()) {
                    ZLogger.d("取消录音");
                    MainService.this.mRecorder.cancel();
                }
                ZLogger.d("mAudioPlayer.isPlaying:" + MainService.this.mAudioPlayer.isPlaying() +
                        "， state:" + MainService.this.mAudioPlayer.getState());
            }
        }

    };

    private PlayMsgListener mPlayMsgListener = new PlayMsgListener() {
        public void onCompleted() {
            ZLogger.d("mPlayMsgListener onCompleted");
            if (MainService.this.isAbandonCurrentContactsMsgList) {
                MainService.this.isAbandonCurrentContactsMsgList = false;
                return;
            }

            ZLogger.d("mContactsContainer.isEmpty():" + MainService.this.mContactsContainer.isEmpty());
            if (!MainService.this.mContactsContainer.isEmpty()) {
                MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
                return;
            }
            MainService.this.mAudioPlayer.removeTailPlayable(new PlayableItem(R.string.tone_new_message));
        }

        public void onPlayEnd(Message message) {
            ZLogger.d("mPlayMsgListener onPlayed :" + message.getContent());
            ContactsContainer localContactsContainer = MainService.this.mContactsContainer;
            String[] arrayOfString = new String[1];
            arrayOfString[0] = message.getMsgId();
            localContactsContainer.setMsgsReaded(arrayOfString);
        }

        public void onPlayStart(Message message) {
        }
    };

    private Recorder mRecorder;
    private SPFile mSP;
    private RecordCompleteListener mRecordCompleteListener = new RecordCompleteListener() {
        private void deleteRecordFile(File file) {
            try {
                file.delete();
            } catch (Exception localException) {
            }
        }

        private void playRecordResult(PlayableItem playableItem) {
            ZLogger.d("isTeleBusy:" + MainService.this.isTeleBusy);
            MainService.this.mHandler.removeCallbacks(MainService.this.uploadTimeoutRunnable);
            MainService.this.isUploadingRecord = false;

            if (MainService.this.isTeleBusy) {
                return;
            }

            if ((MainService.this.mAudioManager.isMusicActive()) && (!MainService.this.mAudioPlayer.isPlaying())) {
                MainService.this.mAudioPlayer.putTailPlayable(playableItem);
                MainService.this.checkHasMsg();
                return;
            }
            MainService.this.mAudioPlayer.putTailPlayable(playableItem);

            if ((MainService.this.mSP.getBoolean("KEY_AUTO_PLAY_MESSAGE", false))
                    && MainService.this.isReAutoPlayMsg) {
                MainService.this.mAudioPlayer.setCurrentContacts(MainService.this.mContactsContainer.getCurrentContacts(), MainService.this.mPlayMsgListener);
                MainService.this.checkHasMsgExceptCurContacts();
            }
            if (MainService.this.receivedMsgWhenRecording) {
                MainService.this.receivedMsgWhenRecording = false;
                MainService.this.checkHasMsg();
            }
        }

        public void onComplete(String path) {
            ZLogger.d(path);
            MainService.this.isUploadingRecord = true;
            final File localFile = new File(path);
            MainService.this.mHandler.postDelayed(MainService.this.uploadTimeoutRunnable, MAX_RESPONSE_TIMEOUT);
//            MobclickAgent.onEvent(MainService.this, "message_record_succeed");
//            MainService.this.mWebManager.uploadAndSendMsg(localFile, MainService.this.mContactsContainer.getCurrentContacts().getUserId(), new WebListener() {
//                public void onError(String paramAnonymous2String) {
//                    ZLogger.e("uploadAndSendMsg onError: " + paramAnonymous2String);
//                    ZLogger.log2File("/Jabra_Social/wechat_api_log", "uploadAndSendMsg onError: " + paramAnonymous2String);
////                    MobclickAgent.onEvent(MainService.this, "message_send_error");
//                    if (paramAnonymous2String != null) {
//                        try {
//                            JSONObject localJSONObject = new JSONObject(paramAnonymous2String);
//                            if (localJSONObject.has("errcode")) {
//                                if (localJSONObject.optString("errcode").equals("40030")) {
//                                    Intent localIntent = new Intent(MainService.this, WXEntryActivity.class);
//                                    localIntent.setFlags(268435456);
//                                    MainService.this.startActivity(localIntent);
//                                    MainService.this.mSP.clear();
//                                    new Handler().postDelayed(new Runnable() {
//                                        public void run() {
//                                            Intent localIntent = new Intent("com.xpg.jabra.wechat.authorize");
//                                            MainService.this.sendUIBroadcast(localIntent);
//                                        }
//                                    }, 1000L);
//                                }
//                                MainService.this.isUploadingRecord = false;
//                                MainService.this.mHandler.removeCallbacks(MainService.this.uploadTimeoutRunnable);
//                                MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(MainService.this.getString(2131230769)));
//                                return;
//                            }
//                        } catch (JSONException localJSONException) {
//                            localJSONException.printStackTrace();
//                        }
//                    }
//                    MainService.this.mWebManager.uploadFaileQueue();
//                    MainService .2.
//                    this.playRecordResult(new PlayableItem(MainService.this.getString(2131230739)));
//                }
//
//                public void onFailed(String paramAnonymous2String) {
//                    ZLogger.e("uploadAndSendMsg onFailed: " + paramAnonymous2String + "   uploadFileName:" + localFile.getAbsolutePath());
//                    ZLogger.log2File("/Jabra_Social/wechat_api_log", "uploadAndSendMsg onFailed: " + paramAnonymous2String + " current account���" + MainService.this.mContactsContainer.getCurrentContacts().getNickName());
//                    MobclickAgent.reportError(MainService.this, paramAnonymous2String.toString() + " ���������������" + MainService.this.mContactsContainer.getCurrentContacts().getNickName());
//                    MainService .2.
//                    this.playRecordResult(new PlayableItem(MainService.this.getString(2131230760)));
//                    MainService .2. this.deleteRecordFile(localFile);
//                    MobclickAgent.onEvent(MainService.this, "message_send_failed");
//                }
//
//                public void onSucceed(Object paramAnonymous2Object) {
//                    ZLogger.e("uploadAndSendMsg onSucceed");
//                    ZLogger.log2File("/Jabra_Social/wechat_api_log", "uploadAndSendMsg onSucceed");
//                    MainService .2. this.playRecordResult(new PlayableItem(2131230768));
//                    MainService .2. this.deleteRecordFile(localFile);
//                }
//            });
            MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(R.string.replying)
                    + MainService.this.mContactsContainer.getCurrentContacts().getNickName());
        }

        public void onError(int code) {
            playRecordResult(new PlayableItem(MainService.this.getString(code)));
        }
    };

    private boolean isUploadingRecord;
    private Runnable uploadTimeoutRunnable = new Runnable() {
        public void run() {
            MainService.this.isUploadingRecord = false;
        }
    };


    private WXLuanchReceiver mWXLuanchReceiver;
    private WXMsgReceiver mWXMsgReceiver;
    //    private WebManager mWebManager;
    private boolean receivedMsgWhenPhoneBusy;
    private boolean receivedMsgWhenRecording;
    private TelephonyManager telephonyManager;


    private void checkHasMsg() {
        if (!this.mContactsContainer.isEmpty()) {
            this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
        }
    }

    private void checkHasMsgExceptCurContacts() {
        if ((EmptyUtil.isEmpty(this.mContactsContainer.getCurrentContacts().getMsgSortList()))
                && (!this.mContactsContainer.isEmpty())) {
            this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
        }
    }

    private void registerReceiver() {
        ZLogger.i("registerReceiver");
        this.mWXLuanchReceiver = new WXLuanchReceiver();
        this.mWXLuanchReceiver.register(this, new WXLuanchListener() {
            public void onWXLuanch() {
                ZLogger.i("微信登录成功");
                if (MainService.this.mWXMsgReceiver != null) {
                    MainService.this.mWXMsgReceiver.stopRegisterThread();
                    MainService.this.mWXMsgReceiver.registerWX(MainService.this);
                    ZLogger.e("重新注册微信监听");
                }
            }
        });
//        this.mHeadsetBtnEventReceiver = new HeadsetBtnEventReceiver(this, new BtnEventListener() {
//            public void onDouble() {
//                if (MainService.this.isTeleBusy) {
//                }
//                boolean bool2;
//                do {
//                    do {
//                        return;
//                        if (!MainService.this.mAudioManager.isBluetoothA2dpOn()) {
//                            Intent localIntent = new Intent("com.xpg.jabra.hs.a2dp.change");
//                            localIntent.putExtra("EXTRA_A2DP_STATE", false);
//                            MainService.this.sendUIBroadcast(localIntent);
//                            MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230737));
//                            return;
//                        }
//                    }
//                    while ((MainService.this.mRecorder.isRecording()) || (MainService.this.isUploadingRecord));
//                    MainService.this.mAudioManager.requestAudioFocus(MainService.this.mAudioFocusChangeListener, 3, 2);
//                    if (EmptyUtil.isEmpty(MainService.this.mSP.getString("KEY_OPEN_ID", ""))) {
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230736));
//                        return;
//                    }
//                    if (MainService.this.mContactsContainer.getCurrentContacts() == null) {
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230758));
//                        return;
//                    }
//                    boolean bool1 = MainService.this.mSP.getBoolean("KEY_SKIP_MESSAGE", true);
//                    bool2 = MainService.this.mSP.getBoolean("KEY_AUTO_PLAY_MESSAGE", false);
//                    if (bool1) {
//                        ZLogger.e("mAudioPlayer.isPlayingMsg():" + MainService.this.mAudioPlayer.isPlayingMsg() + "   play:" + MainService.this.mAudioPlayer.getState());
//                        if (MainService.this.mAudioPlayer.isPlayingMsg()) {
//                            MainService.this.mAudioPlayer.removeTailPlayable(new PlayableItem(R.string.tone_new_message));
//                            MainService.this.isAbandonCurrentContactsMsgList = true;
//                            MainService.this.mContactsContainer.abandonCurrentContactsMsgList();
//                        }
//                        while (!MainService.this.mContactsContainer.isEmpty()) {
//                            Contacts localContacts3 = MainService.this.mContactsContainer.recently();
//                            MainService.this.mAudioPlayer.playContacts(localContacts3, MainService.this.mPlayMsgListener);
//                            return;
//                            if (!MainService.this.mContactsContainer.isEmpty()) {
//                                MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230775));
//                                return;
//                            }
//                        }
//                        Contacts localContacts2 = MainService.this.mContactsContainer.next();
//                        MainService.this.mAudioPlayer.clearPreUserId();
//                        if (bool2) {
//                            MainService.this.mAudioPlayer.setCurrentContacts(localContacts2, null);
//                        }
//                        for (; ; ) {
//                            AudioPlayer localAudioPlayer2 = MainService.this.mAudioPlayer;
//                            String str2 = MainService.this.getString(2131230754);
//                            Object[] arrayOfObject2 = new Object[1];
//                            arrayOfObject2[0] = localContacts2.getNickName();
//                            localAudioPlayer2.playPrompt(String.format(str2, arrayOfObject2));
//                            return;
//                            MainService.this.mAudioPlayer.setCurrentContacts(null, null);
//                        }
//                    }
//                } while (MainService.this.mAudioPlayer.isPlayingMsg());
//                if (!MainService.this.mContactsContainer.isEmpty()) {
//                    MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230775));
//                    return;
//                }
//                Contacts localContacts1 = MainService.this.mContactsContainer.next();
//                if (localContacts1 == null) {
//                    MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230753));
//                    return;
//                }
//                MainService.this.mAudioPlayer.clearPreUserId();
//                if (bool2) {
//                    MainService.this.mAudioPlayer.setCurrentContacts(localContacts1, null);
//                }
//                for (; ; ) {
//                    AudioPlayer localAudioPlayer1 = MainService.this.mAudioPlayer;
//                    String str1 = MainService.this.getString(2131230754);
//                    Object[] arrayOfObject1 = new Object[1];
//                    arrayOfObject1[0] = localContacts1.getNickName();
//                    localAudioPlayer1.playPrompt(String.format(str1, arrayOfObject1));
//                    return;
//                    MainService.this.mAudioPlayer.setCurrentContacts(null, null);
//                }
//            }
//
//            public void onJBSDisconnected(final String paramAnonymousString) {
//                Intent localIntent = new Intent("com.xpg.jabra.wechat.not.ready");
//                localIntent.putExtra("EXTRA_WECHAT_READY_STATE", false);
//                MainService.this.sendUIBroadcast(localIntent);
//                MainService.this.mHeadsetBtnEventReceiver.unbindJabraService(MainService.this);
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        if (MainService.this.mHeadsetConnectReceiver.getState() == 3) {
//                            MainService.this.mHeadsetBtnEventReceiver.bindJabraService(MainService.this, paramAnonymousString);
//                        }
//                    }
//                }, REBIND_JABRA_SERVICE_DELAY);
//            }
//
//            public void onJBSReady() {
//                MainService.this.mAudioPlayer.setPlayNickname(true);
//                ToastUtil.show(MainService.this, 2131230747);
//                Intent localIntent = new Intent("com.xpg.jabra.wechat.not.ready");
//                localIntent.putExtra("EXTRA_WECHAT_READY_STATE", true);
//                MainService.this.sendUIBroadcast(localIntent);
//                MainService.this.checkHasMsg();
//                MobclickAgent.onEventEnd(MainService.this, "unsolicited_state_notconnected");
//                MobclickAgent.onEventEnd(MainService.this, "using_notconnected");
//            }
//
//            public void onPressEnd() {
//                if (MainService.this.isTeleBusy) {
//                }
//                while ((EmptyUtil.isEmpty(MainService.this.mSP.getString("KEY_OPEN_ID", ""))) || (!MainService.this.mRecorder.isRecording())) {
//                    return;
//                }
//                MainService.this.mRecorder.completeDelayed(200L, MainService.this.mRecordCompleteListener);
//            }
//
//            public void onPressStart() {
//                if (MainService.this.isTeleBusy) {
//                }
//                do {
//                    do {
//                        return;
//                        MainService.this.mAudioManager.requestAudioFocus(MainService.this.mAudioFocusChangeListener, 3, 2);
//                        if (EmptyUtil.isEmpty(MainService.this.mSP.getString("KEY_OPEN_ID", ""))) {
//                            MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230736));
//                            return;
//                        }
//                    } while (MainService.this.mRecorder.isRecording());
//                    if (MainService.this.isUploadingRecord) {
//                        MainService.this.mAudioManager.abandonAudioFocus(MainService.this.mAudioFocusChangeListener);
//                        return;
//                    }
//                    if (!NetWorkUtil.isNetworkConnected(MainService.this)) {
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230752));
//                        return;
//                    }
//                    if ((MainService.this.mContactsContainer.getCurrentContacts() == null) && (MainService.this.mContactsContainer.isEmpty())) {
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230753));
//                        return;
//                    }
//                    if (MainService.this.mContactsContainer.getCurrentContacts() == null) {
//                        MainService.this.mContactsContainer.recently();
//                    }
//                } while (MainService.this.mAudioPlayer.isPlayingMsg());
//                ZLogger.d("onPressStart");
//                MainService.this.mAudioPlayer.stop();
//                MainService.this.mRecorder.start(new RecordErrorListener() {
//                    public void onError() {
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230755));
//                    }
//                });
//            }
//
//            public void onTap() {
//                ZLogger.e("mAudioManager.isMusicActive():" + MainService.this.mAudioManager.isMusicActive() + "   mAudioPlayer.isPlaying():" + MainService.this.mAudioPlayer.isPlaying());
//                if (MainService.this.isTeleBusy) {
//                }
//                do {
//                    return;
//                    if (!MainService.this.mAudioManager.isBluetoothA2dpOn()) {
//                        Intent localIntent = new Intent("com.xpg.jabra.hs.a2dp.change");
//                        localIntent.putExtra("EXTRA_A2DP_STATE", false);
//                        MainService.this.sendUIBroadcast(localIntent);
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230737));
//                        return;
//                    }
//                    MainService.this.mAudioManager.requestAudioFocus(MainService.this.mAudioFocusChangeListener, 3, 2);
//                    if (EmptyUtil.isEmpty(MainService.this.mSP.getString("KEY_OPEN_ID", ""))) {
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230736));
//                        return;
//                    }
//                    ZLogger.e("mAudioPlayer.isPlayingMsg():" + MainService.this.mAudioPlayer.isPlayingMsg() + "   play:" + MainService.this.mAudioPlayer.getState() + "  isUploadingRecord:" + MainService.this.isUploadingRecord);
//                }
//                while ((MainService.this.mRecorder.isRecording()) || (MainService.this.mAudioPlayer.isPlayingMsg()));
//                if (MainService.this.isUploadingRecord) {
//                    MainService.this.mAudioManager.abandonAudioFocus(MainService.this.mAudioFocusChangeListener);
//                    return;
//                }
//                if (MainService.this.mContactsContainer.isEmpty()) {
//                    Contacts localContacts2 = MainService.this.mContactsContainer.getCurrentContacts();
//                    if (localContacts2 == null) {
//                        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(2131230758));
//                        return;
//                    }
//                    AudioPlayer localAudioPlayer = MainService.this.mAudioPlayer;
//                    String str = MainService.this.getString(2131230757);
//                    Object[] arrayOfObject = new Object[1];
//                    arrayOfObject[0] = localContacts2.getNickName();
//                    localAudioPlayer.playPrompt(String.format(str, arrayOfObject));
//                    return;
//                }
//                Contacts localContacts1 = MainService.this.mContactsContainer.recently();
//                MainService.this.mAudioPlayer.playContacts(localContacts1, MainService.this.mPlayMsgListener);
//                MainService.this.isReAutoPlayMsg = true;
//                ZLogger.e("isReAutoPlayMsg:   onTap:" + MainService.this.isReAutoPlayMsg);
//            }
//        });

        this.mNetworkReceiver = new NetworkReceiver();
        this.mNetworkReceiver.register(this, new NetworkListener() {
            Intent intent = new Intent("com.xpg.jabra.network.change");

            public void onNetworkDisable() {
                this.intent.putExtra("EXTRA_NETWORK_STATE", false);
                MainService.this.sendUIBroadcast(this.intent);
            }

            public void onNetworkUsable() {
                this.intent.putExtra("EXTRA_NETWORK_STATE", true);
                MainService.this.sendUIBroadcast(this.intent);
            }
        });

//        this.mHeadsetConnectReceiver = new HeadsetConnectReceiver();
//        this.mHeadsetConnectReceiver.register(this, new HSConnectListener() {
//            public void onHSA2DPConnected(BluetoothDevice paramAnonymousBluetoothDevice) {
//                MainService.this.isReAutoPlayMsg = false;
//                ZLogger.e("isReAutoPlayMsg:   onHSA2DPConnected:" + MainService.this.isReAutoPlayMsg);
//                Intent localIntent = new Intent("com.xpg.jabra.hs.a2dp.change");
//                localIntent.putExtra("EXTRA_A2DP_STATE", true);
//                localIntent.putExtra("EXTRA_BLUETOOTH_DEVICE", paramAnonymousBluetoothDevice);
//                MainService.this.sendUIBroadcast(localIntent);
//                ZLogger.v0("onHSA2DPConnected");
//                if (PackageUtil.isInstallLatest(MainService.this, "com.gnnetcom.jabraservice")) {
//                    MainService.this.mHeadsetBtnEventReceiver.bindJabraService(MainService.this, paramAnonymousBluetoothDevice.getName() + " " + paramAnonymousBluetoothDevice.getAddress());
//                }
//            }
//
//            public void onHSA2DPDisconnected(BluetoothDevice paramAnonymousBluetoothDevice) {
//                Intent localIntent = new Intent("com.xpg.jabra.hs.a2dp.change");
//                localIntent.putExtra("EXTRA_A2DP_STATE", false);
//                localIntent.putExtra("EXTRA_BLUETOOTH_DEVICE", paramAnonymousBluetoothDevice);
//                MainService.this.sendUIBroadcast(localIntent);
//                ZLogger.v0("onHSA2DPDisconnected");
//            }
//
//            public void onHSConnected(BluetoothDevice paramAnonymousBluetoothDevice) {
//                Intent localIntent = new Intent("com.xpg.jabra.hs.connect.change");
//                localIntent.putExtra("EXTRA_BLUETOOTH_STATE", true);
//                localIntent.putExtra("EXTRA_BLUETOOTH_DEVICE", paramAnonymousBluetoothDevice);
//                MainService.this.sendUIBroadcast(localIntent);
//                ZLogger.v0("onHSConnected");
//            }
//
//            public void onHSDisconnected(BluetoothDevice paramAnonymousBluetoothDevice) {
//                Intent localIntent = new Intent("com.xpg.jabra.hs.connect.change");
//                localIntent.putExtra("EXTRA_BLUETOOTH_STATE", false);
//                localIntent.putExtra("EXTRA_BLUETOOTH_DEVICE", paramAnonymousBluetoothDevice);
//                MainService.this.sendUIBroadcast(localIntent);
//                ZLogger.v0("onHSDisconnected");
//                MainService.this.mHeadsetBtnEventReceiver.unbindJabraService(MainService.this);
//                if (MainService.this.mAudioPlayer.isPlaying()) {
//                    MainService.this.mAudioPlayer.stop();
//                    MainService.this.mAudioPlayer.clearPreUserId();
//                    MainService.this.mAudioPlayer.setCurrentContacts(null, null);
//                    MainService.this.mAudioPlayer.setStopBySystem(true);
//                }
//            }
//        });

        this.mWXMsgReceiver = new WXMsgReceiver();
        this.mWXMsgReceiver.register(this, new MsgListener() {
            public void onReceive(ContactsSortList contactsSortList) {
                ZLogger.d("isReAutoPlayMsg: onReceive:" + MainService.this.isReAutoPlayMsg);
//                if (MainService.this.mHeadsetConnectReceiver.getState() == 0) {
//                }

                int i = 0;
                boolean bool3 = false;

                do {
                    do {
                        boolean bool5 = false;
                        do {
//                            return;
                            Iterator localIterator = contactsSortList.iterator();
                            if (!localIterator.hasNext()) {
                                boolean isAutoPlayMsg = MainService.this.mSP.getBoolean("KEY_AUTO_PLAY_MESSAGE", false);
                                ZLogger.d("  isAutoPlayMsg:" + isAutoPlayMsg);
                                if (isAutoPlayMsg) {
                                    break;
                                }

                                if (!MainService.this.isTeleBusy) {
                                    break;
                                }

                                MainService.this.receivedMsgWhenPhoneBusy = true;
                            }

//                            for (; ; ) {
                            MainService.this.mContactsContainer.update(contactsSortList);
                            ZLogger.d("mWXMsgReceiver  update msg");

//                                return;
                            Contacts localContacts = (Contacts) localIterator.next();
                            HashMap localHashMap = new HashMap();
                            localHashMap.put("from_user", localContacts.getUserId());
                            localHashMap.put("from_user_nickname", localContacts.getNickName());
                            localHashMap.put("message_count", localContacts.getMsgSortList().size());
//                                MobclickAgent.onEvent(MainService.this, "received_message", localHashMap);
//                                break;
                            if ((MainService.this.mRecorder.isRecording()) || (MainService.this.isUploadingRecord)) {
                                MainService.this.receivedMsgWhenRecording = true;
                            } else if ((!MainService.this.mAudioPlayer.isPlayingMsg())
                                    || (contactsSortList.size() != 1)
                                    || (!contactsSortList.contains(MainService.this.mContactsContainer.getCurrentContacts()))) {
                                MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
                            }
//                            }
                            if (!MainService.this.mAudioManager.isBluetoothA2dpOn()) {
                                Intent localIntent = new Intent("com.xpg.jabra.hs.a2dp.change");
                                localIntent.putExtra("EXTRA_A2DP_STATE", false);
                                MainService.this.sendUIBroadcast(localIntent);
                                MainService.this.mAudioPlayer.clearPreUserId();
                                MainService.this.mAudioPlayer.setCurrentContacts(null, null);
                                MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
                                MainService.this.mContactsContainer.update(contactsSortList);
                                return;
                            }
                            MainService.this.mContactsContainer.update(contactsSortList);
                            ZLogger.d("mWXMsgReceiver  update msg");
                            if (MainService.this.isTeleBusy) {
                                MainService.this.receivedMsgWhenPhoneBusy = true;
                                return;
                            }
                            boolean bool2 = MainService.this.mRecorder.isRecording();
                            boolean bool4 = false;
                            if (contactsSortList.size() == 1) {
                                i = 1;
                                bool3 = contactsSortList.contains(MainService.this.mContactsContainer.getCurrentContacts());
                                bool4 = MainService.this.mAudioPlayer.isPlaying();
                                bool5 = MainService.this.mAudioPlayer.isPlayingMsg();
                                if ((i == 0) || (!bool3)) {
                                    break;
                                }
                            }
                            for (int j = 1; ; j = 0) {
                                if ((!bool2) && (!MainService.this.isUploadingRecord)) {
                                    break;
                                }
                                if (j != 0) {
                                    break;
                                }
                                MainService.this.receivedMsgWhenRecording = true;
//                                return;
//                                i = 0;
                                break;
                            }
                            if (bool4) {
                                break;
                            }
                            if (!bool3) {
                                break;
                            }
                            if (!MainService.this.isReAutoPlayMsg) {
                                MainService.this.mAudioPlayer.clearPreUserId();
                                MainService.this.mAudioPlayer.setCurrentContacts(null, null);
                                MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
                                return;
                            }
                            MainService.this.mAudioManager.requestAudioFocus(MainService.this.mAudioFocusChangeListener, 3, 2);
                            MainService.this.mAudioPlayer.playContacts(MainService.this.mContactsContainer.getCurrentContacts(), MainService.this.mPlayMsgListener);
                        } while (i != 0);

                        MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
//                        return;
                        if (bool5 && !bool3) {
                            break;
                        }
                        MainService.this.mAudioPlayer.setCurrentContacts(MainService.this.mContactsContainer.getCurrentContacts(),
                                MainService.this.mPlayMsgListener);
                    } while (i != 0);

                    MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
//                    return;
                } while ((i != 0) && (bool3));

                MainService.this.mAudioPlayer.putTailPlayable(new PlayableItem(R.string.tone_new_message));
            }
        });
    }

    private void sendUIBroadcast(Intent intent) {
        sendBroadcast(intent);
    }

    private void unregisterReceiver() {
        if (this.mWXMsgReceiver != null) {
            this.mWXMsgReceiver.unregister(this);
        }
//        if (this.mHeadsetConnectReceiver != null) {
//            this.mHeadsetConnectReceiver.unregister(this);
//        }
        if (this.mWXLuanchReceiver != null) {
            this.mWXLuanchReceiver.unregister(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            BaseAccessibilityService.getInstance().init(this);
//        BaseAccessibilityService.getInstance().goAccess();

//        startService(new Intent(MainService.this, MyAccesibilityService.class));
            createFloatView();

            IS_RUNNING = true;
            this.mSP = new SPFile(this, "FILE_CONFIG");
            this.mSP.put("KEY_SERVICE_STARTED", true);
            this.mAudioPlayer = new AudioPlayer(this, this.mAudioPlayerListener);
            this.mRecorder = new Recorder();
            this.mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));

            this.telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
            this.telephonyManager.listen(this.mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

            registerReceiver();

            this.mContactsContainer = new ContactsContainer(this);
            // TODO: 06/09/2017  
//        this.mWebManager = WebManager.getInstance(this);
//        SafeSco.getInstance().register(this);
//            new Intent("com.xpg.jabra.wechat.not.ready").putExtra("EXTRA_WECHAT_READY_STATE", false);

            MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(R.string.tone_new_message));

        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }

    }

    public void onDestroy() {
        super.onDestroy();
        ZLogger.d("MainService destroy");
        unregisterReceiver();
        IS_RUNNING = false;
        SafeSco.getInstance().unregister(this);
        this.mSP.put("KEY_SERVICE_STARTED", false);
//        HciSynthesizer.release();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("EXTRA_JABRA_SERVICE_IS_INSTALLED", false)) {
                BluetoothDevice localBluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("EXTRA_BLUETOOTH_DEVICE");
//                if ((this.mHeadsetBtnEventReceiver != null) && (localBluetoothDevice != null)) {
//                    this.mHeadsetBtnEventReceiver.bindJabraService(this, localBluetoothDevice.getName() + " " + localBluetoothDevice.getAddress());
//                }
            }

            if (intent.getBooleanExtra("EXTRA_GET_OPENID_SUCCEED", false)) {
                this.mContactsContainer.getAllContacts();
                if (this.mWXMsgReceiver != null) {
                    this.mWXMsgReceiver.stopRegisterThread();
                    this.mWXMsgReceiver.registerWX(this);
                    ZLogger.w("重新注册微信消息监听");
                }
            }
            if (intent.getBooleanExtra("EXTRA_UPDATE_CONTACTS_LIST", false)) {
                this.mContactsContainer.updateContactsList();
            }
//            if ((intent.getBooleanExtra("EXTRA_REBIND_JABRA_SERVICE_STATE", false)) && (this.mHeadsetBtnEventReceiver != null)) {
//                this.mHeadsetBtnEventReceiver.unbindJabraService(this);
//                new Handler().postDelayed(new Runnable() {
//                    public void run() {
//                        if ((MainService.this.mHeadsetConnectReceiver.getState() == 3) && (MainService.this.mHeadsetConnectReceiver != null)) {
//                            BluetoothDevice localBluetoothDevice = MainService.this.mHeadsetConnectReceiver.getConnectedDevice();
//                            if (localBluetoothDevice != null) {
//                                MainService.this.mHeadsetBtnEventReceiver.bindJabraService(MainService.this, localBluetoothDevice.getName() + " " + localBluetoothDevice.getAddress());
//                            }
//                        }
//                    }
//                }, REBIND_JABRA_SERVICE_DELAY);
//            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void onDouble() {
        MainService.this.mAudioPlayer.playPrompt(MainService.this.getString(R.string.tone_new_message));
    }

    private void onTap() {

    }

    WindowManager wm;
    View floatView;

    //创建悬浮按钮
    private void createFloatView() {
//        WindowManager.LayoutParams pl = new WindowManager.LayoutParams();
//        wm = (WindowManager) getSystemService(getApplication().WINDOW_SERVICE);
//        pl.type = WindowManager.LayoutParams.TYPE_PHONE;
//        pl.format = PixelFormat.RGBA_8888;
//        pl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        pl.gravity = Gravity.RIGHT | Gravity.BOTTOM;
//        pl.x = 0;
//        pl.y = 0;
//
//        pl.width = 200;
//        pl.height = 200;
//
//        LayoutInflater inflater = LayoutInflater.from(this);
//        floatView = inflater.inflate(R.layout.view_float, null);
//        wm.addView(floatView, pl);

//        floatbtn.setOnClickListener(this);

    }
}
