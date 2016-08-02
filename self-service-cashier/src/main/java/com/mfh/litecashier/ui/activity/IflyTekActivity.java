package com.mfh.litecashier.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;


/**
 * 科大讯飞
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class IflyTekActivity extends SerialPortActivity {
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

        mTts.stopSpeaking();
        // 退出时释放连接
        mTts.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ZLogger.d("onConfigurationChanged" + newConfig.toString());
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
     * @param
     * @return
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
        if (StringUtils.isEmpty(text)) {
            return;
        }
        // 设置参数
        setParam();
        ZLogger.df("准备播放语音:" + text);
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
