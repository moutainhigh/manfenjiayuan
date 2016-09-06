package com.mfh.litecashier.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;

import com.bingshanguxue.cashier.hardware.scale.AHScaleAgent;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.event.SerialPortEvent;
import com.bingshanguxue.cashier.hardware.scale.DS781A;
import com.bingshanguxue.cashier.hardware.scale.SMScaleAgent;
import com.mfh.litecashier.utils.GlobalInstance;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android_serialport_api.ComBean;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;
import de.greenrobot.event.EventBus;


/**
 * 首页
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class CashierActivity extends BaseActivity {
    DispQueueThread DispQueue;//刷新显示线程
    private SerialPortFinder mSerialPortFinder;//串口设备搜索
    protected SerialControl comDisplay, comPrint, comScale, comAhScale;//串口

    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认发音人
    private String voicer = "xiaoyan";
    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        initCOM();

        initSpeechSynthesizer();

        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);

        mEngineType = SpeechConstant.TYPE_CLOUD;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        hideSystemUI();
    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        //Any time the window receives focus, simply set the IMMERSIVE mode.
//        if (hasFocus) {
//            hideSystemUI();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        //关闭串口
        CloseComPort(comDisplay);
        CloseComPort(comPrint);
        CloseComPort(comScale);
        CloseComPort(comAhScale);

        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        ZLogger.d("onConfigurationChanged" + newConfig.toString());
        CloseComPort(comDisplay);
        CloseComPort(comPrint);
        CloseComPort(comScale);
        CloseComPort(comAhScale);

        setControls();
    }

    private class SerialControl extends SerialHelper {

        public SerialControl(String sPort, String sBaudRate) {
            super(sPort, sBaudRate);
        }

        public SerialControl(String sPort, int iBaudRate) {
            super(sPort, iBaudRate);
        }

        public SerialControl() {
        }

        protected void onDataReceived(final ComBean ComRecData) {
            //数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
            //直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
            //用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
            //最终效果差不多-_-，线程定时刷新稍好一些。
            DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
        }
    }

    /**
     * 刷新显示线程
     */
    private class DispQueueThread extends Thread {
        private Queue<ComBean> mComBeanQueue = new LinkedList<>();

        @Override
        public void run() {
            super.run();
            // TODO: 8/9/16 java.util.NoSuchElementException 
            try {
                while (!isInterrupted()) {
                    final ComBean ComData;
                    while ((ComData = mComBeanQueue.poll()) != null) {
                        DispRecData(ComData);

                        Thread.sleep(50);//显示性能高的话，可以把此数值调小。
                        break;
                    }
                }
            } catch (Exception e) {
//                        e.printStackTrace();
                ZLogger.ef(e.toString());
            }
        }

        public synchronized void AddQueue(ComBean ComData) {
            mComBeanQueue.add(ComData);
        }
    }

    /**
     * 显示接收数据
     */
    private void DispRecData(ComBean comBean) {
        String port = comBean.sComPort;
        if (StringUtils.isEmpty(port)){
            return;
        }

        if (!BizConfig.RELEASE){
            String sMsg = String.format("时间：<%s>\n串口：<%s>\n数据1：<%s>\n数据2:<%s>",
                    comBean.sRecTime, port,
                    new String(comBean.bRec), DataConvertUtil.ByteArrToHex(comBean.bRec));
            ZLogger.d("COM RECV:" + sMsg);
        }

        if (port.equals(AHScaleAgent.getPort()) && AHScaleAgent.isEnabled()) {
            GlobalInstance.getInstance().setNetWeight(AHScaleAgent.parseACSP215(comBean.bRec));
        }
        else if (port.equals(SMScaleAgent.PORT_SCALE_DS781) && SMScaleAgent.isEnabled()) {
            DS781A ds781A = SMScaleAgent.parseData(comBean.bRec);
            if (ds781A != null) {
                GlobalInstance.getInstance().setNetWeight(ds781A.getNetWeight());
            }
        }
    }

    /**
     * 初始化串口
     */
    private void initCOM() {
        comDisplay = new SerialControl(SerialManager.getLedPort(), SerialManager.getLedBaudrate());
        comPrint = new SerialControl(SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate());
        comScale = new SerialControl(SMScaleAgent.PORT_SCALE_DS781, SMScaleAgent.getBaudrate());
        comAhScale = new SerialControl(AHScaleAgent.getPort(),
                AHScaleAgent.getBaudrate());

        DispQueue = new DispQueueThread();
        DispQueue.start();

        setControls();
    }

    private class OpenPortRunnable implements Runnable {
        private SerialHelper serialHelper;

        public OpenPortRunnable(SerialHelper serialHelper) {
            this.serialHelper = serialHelper;
        }

        @Override
        public void run() {
            try {
                if (serialHelper == null || serialHelper.isOpen()) {
                    return;
                }

                serialHelper.open();
            } catch (SecurityException e) {
                ZLogger.e("打开串口失败:没有串口读/写权限!" + serialHelper.getPort());
//            DialogUtil.showHint("打开串口失败:没有串口读/写权限!" + ComPort.getPort());
            } catch (IOException e) {
                ZLogger.e("打开串口失败:未知错误!" + serialHelper.getPort());
//            DialogUtil.showHint("打开串口失败:未知错误!" + ComPort.getPort());
            } catch (InvalidParameterException e) {
                ZLogger.e("打开串口失败:参数错误!");
//            DialogUtil.showHint("打开串口失败:参数错误!" + ComPort.getPort());
            }
        }
    }

    /**
     * 打开串口
     */
    public void OpenComPort(SerialHelper serialHelper) {
        //使用线程，避免界面卡顿
        new Thread(new OpenPortRunnable(serialHelper)).start();
    }

    /**
     * 关闭串口
     */
    private void CloseComPort(SerialHelper serialHelper) {
        if (serialHelper != null && serialHelper.isOpen()) {
            serialHelper.stopSend();
            serialHelper.close();
        }
    }

    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, String sOut, boolean bHex) {
        if (ComPort != null && ComPort.isOpen()) {
//            DialogUtil.showHint("发送串口数据!" + sOut);
            if (bHex) {
                ComPort.sendHex(sOut);
            } else {
                ComPort.sendTxt(sOut);
            }
        }
    }

    /**
     * 串口发送
     */
    private void sendPortData(SerialHelper ComPort, byte[] bOutArray) {
        if (ComPort != null && ComPort.isOpen()) {
            ComPort.send(bOutArray);
        }
    }

    /**
     * poslab: devices [/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttymxc4, /dev/ttymxc3, /dev/ttymxc2, /dev/ttymxc1, /dev/ttymxc0]
     * JOOYTEC: devices:[/dev/ttyGS3, /dev/ttyGS2, /dev/ttyGS1, /dev/ttyGS0, /dev/ttyS3, /dev/ttyS1, /dev/ttyS0, /dev/ttyFIQ0]
     */
    public void setControls() {
        mSerialPortFinder = new SerialPortFinder();

        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        List<String> allDevices = new ArrayList<>();
        if (entryValues != null) {
            Collections.addAll(allDevices, entryValues);
        }
        ZLogger.d("devicePath:" + allDevices.toString());
        SerialManager.getInstance().setComDevicesPath(allDevices);//保存devices

        if (!BizConfig.RELEASE) {
            String[] entryValues2 = mSerialPortFinder.getAllDevices();
            List<String> allDevices2 = new ArrayList<>();
            if (entryValues2 != null) {
                Collections.addAll(allDevices2, entryValues2);
            }
            ZLogger.d("devices:" + allDevices2.toString());
        }

        if (allDevices.contains(SerialManager.getPrinterPort())) {
            OpenComPort(comPrint);
        }
        if (allDevices.contains(SerialManager.getLedPort())) {
            OpenComPort(comDisplay);
        }
        if (allDevices.contains(SMScaleAgent.getBaudrate()) && SMScaleAgent.isEnabled()) {
            OpenComPort(comScale);
        }
        if (allDevices.contains(AHScaleAgent.getBaudrate()) && AHScaleAgent.isEnabled()){
            OpenComPort(comAhScale);
        }
    }

    /**
     * 串口
     */
    public void onEventMainThread(SerialPortEvent event) {
        ZLogger.d(String.format("SerialPortEvent(%d)", event.getType()));
        //客显
        if (event.getType() == SerialPortEvent.SERIAL_TYPE_DISPLAY) {
            //打开
            OpenComPort(comDisplay);
            sendPortData(comDisplay, event.getCmd(), true);
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_PRINTER) {
            OpenComPort(comPrint);
            sendPortData(comPrint, event.getCmdBytes());
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_PRINTER_INIT) {
            CloseComPort(comPrint);

            comPrint = new SerialControl(SerialManager.getPrinterPort(), SerialManager.getPrinterBaudrate());
            OpenComPort(comPrint);
        } else if (event.getType() == SerialPortEvent.UPDATE_PORT_SMSCALE) {
            try {
                //清空数据
                GlobalInstance.getInstance().setNetWeight(0D);
                CloseComPort(comScale);

                if (SMScaleAgent.isEnabled()){
                    comScale = new SerialControl(SMScaleAgent.PORT_SCALE_DS781, SMScaleAgent.getBaudrate());
                    OpenComPort(comScale);
                }
            } catch (Exception e) {
                ZLogger.ef(e.toString());
            }
        } else if (event.getType() == SerialPortEvent.UPDATE_PORT_AHSCALE) {
            try {
                //清空数据
                GlobalInstance.getInstance().setNetWeight(0D);
                CloseComPort(comAhScale);

                if (AHScaleAgent.isEnabled()){
                    comAhScale = new SerialControl(AHScaleAgent.getPort(),
                            AHScaleAgent.getBaudrate());
                    OpenComPort(comAhScale);
                }
            } catch (Exception e) {
                ZLogger.ef(e.toString());
            }
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_INIT) {
            CloseComPort(comDisplay);

            comDisplay = new SerialControl(SerialManager.getLedPort(), SerialManager.getLedBaudrate());
            OpenComPort(comDisplay);
            sendPortData(comDisplay, SerialManager.VFD("12.306"));
        }else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD) {
            OpenComPort(comDisplay);
            sendPortData(comDisplay, SerialManager.VFD(event.getCmd()));
        } else if (event.getType() == SerialPortEvent.SERIAL_TYPE_VFD_BYTE) {
            OpenComPort(comDisplay);
            sendPortData(comDisplay, event.getCmdBytes());
        }
    }

    /**
     * 初始化合成对象
     */
    private void initSpeechSynthesizer() {
        try {
//1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
            mTts = SpeechSynthesizer.createSynthesizer(getApplicationContext(),
                    mTtsInitListener);
            //2.合成参数设置,详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
            // 设置发音人(更多在线发音人,用户可参见 附录12.2
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
            mTts.setParameter(SpeechConstant.SPEED, "50");
            //设置语速
            mTts.setParameter(SpeechConstant.VOLUME, "80");
            //设置音量,范围 0~100
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //设置云端
            // 设置合成音频保存位置(可自定义保存位置),保存在“./sdcard/iflytek.pcm”
//保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
// 仅支持保存为 pcm 格式,如果不需要保存合成音频,注释该行代码
            mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
//3.开始合成
//            mTts.startSpeaking("科大讯飞,让世界聆听我们的声音", mTtsListener);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            ZLogger.d("InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                DialogUtil.showHint("初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };


    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
//            DialogUtil.showHint("开始播放");
        }

        @Override
        public void onSpeakPaused() {
//            DialogUtil.showHint("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
//            DialogUtil.showHint("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
//            DialogUtil.showHint(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
//            DialogUtil.showHint(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
//                DialogUtil.showHint("播放完成");
            } else {
                ZLogger.ef(error.getPlainDescription(true));
//                DialogUtil.showHint(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 参数设置
     *
     */
    private void setParam() {
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "50");
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "50");
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "50");
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH,
                Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }


    public void cloudSpeak(String text) {
        if (!SharedPreferencesManager.getBoolean(SharedPreferencesManager.PREF_NAME_CONFIG,
                SharedPreferencesManager.PK_B_TTS_ENABLED, true)) {
            ZLogger.d("请在设置中开启语音播报功能");
            return;
        }

        if (StringUtils.isEmpty(text)) {
            return;
        }

        if (!BizConfig.RELEASE) {
            text = "1";
        }

        if (mTts == null) {
            initSpeechSynthesizer();
        }

        // 设置参数
        setParam();
        ZLogger.d("准备播放语音:" + text);
        int code = mTts.startSpeaking(text, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                //未安装则跳转到提示安装页面
//                mInstaller.install();
            } else {
                DialogUtil.showHint("语音合成失败,错误码: " + code);
            }
        }
    }
//    private int selectedNum = 0;
//    public void selectVoicer(){
//        new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
//                .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
//                        selectedNum, // 默认的选项
//                        new DialogInterface.OnClickListener() { // 点击单选框后的处理
//                            public void onClick(DialogInterface dialog,
//                                                int which) { // 点击了哪一项
//                                voicer = mCloudVoicersValue[which];
//                                if ("catherine".equals(voicer) || "henry".equals(voicer) || "vimary".equals(voicer)) {
//                                    ((EditText) findViewById(R.id.tts_text)).setText(R.string.text_tts_source_en);
//                                }else {
//                                    ((EditText) findViewById(R.id.tts_text)).setText(R.string.text_tts_source);
//                                }
//                                selectedNum = which;
//                                dialog.dismiss();
//                            }
//                        }).show();
//    }

}
