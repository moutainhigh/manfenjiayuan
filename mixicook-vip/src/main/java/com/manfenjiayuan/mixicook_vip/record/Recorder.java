package com.manfenjiayuan.mixicook_vip.record;

import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;


import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.FileUtil;

import java.io.File;


public class Recorder {
    private static final String TAG = "Recorder";
    private static final long TIME_NOMAL = 1300;
    private static final long RECORD_TIME_OUT = 10000;
    private String filePath;
    private boolean isRecordNormal;
    private boolean isRecording;
    private Handler mHandler = new Handler();
    private MediaRecorder mediaRecorder;


    private Runnable recordRunnable = new Runnable() {
        public void run() {
            Recorder.this.isRecordNormal = true;
        }
    };

    private void postRecordTime() {
        this.isRecordNormal = false;
        removeRecordTimer();
        this.mHandler.postDelayed(this.recordRunnable, 1300L);
    }

    private void removeRecordTimer() {
        try {
            this.mHandler.removeCallbacks(this.recordRunnable);
            return;
        } catch (Exception localException) {
        }
    }

    private Runnable timeOutRunnable = new Runnable() {
        public void run() {
            ZLogger.w("录音超时");
            complete(mRecordCompleteListener);
        }
    };

    private void postTimeOut() {
        removeTimeOut();
        this.mHandler.postDelayed(this.timeOutRunnable, RECORD_TIME_OUT);
    }

    private void removeTimeOut() {
        try {
            this.mHandler.removeCallbacks(this.timeOutRunnable);
            return;
        } catch (Exception localException) {
        }
    }

    private void stop(RecordCompleteListener paramRecordCompleteListener) {
        try {
            if (this.mediaRecorder != null) {
                this.mediaRecorder.stop();
                this.mediaRecorder.release();
                this.mediaRecorder = null;
            }
            ZLogger.w("停止录音");

//            SafeSco.getInstance().disconnect(new ScoDisconnectListener(paramRecordCompleteListener) {
//                private String tempRecordFilePath = Recorder.this.filePath;
//
//                public void onDisconnected() {
//                    ZLogger.e("Recorder", "sco关闭成功");
//                    if (this.tempRecordFilePath == null)
//                        if (this.val$listener != null)
//                            this.val$listener.onError(2131230755);
//                    while (true) {
//                        Recorder.this.isRecording = false;
//                        return;
//                        if (this.val$listener == null)
//                            continue;
//                        if (Recorder.this.isRecordNormal)
//                            this.val$listener.onComplete(this.tempRecordFilePath);
//                        try {
//                            new File(this.tempRecordFilePath).delete();
//                            label96:
//                            this.val$listener.onError(2131230756);
//                        } catch (Exception localException) {
//                            break label96:
//                        }
//                    }
//                }
//            });

            if (mRecordCompleteListener != null) {
                mRecordCompleteListener.onComplete(this.filePath);
            }
            removeRecordTimer();
            removeTimeOut();
            return;
        } catch (Exception localException) {
            localException.printStackTrace();
            ZLogger.e("停止录音失败:" + localException.toString());
        }
    }

    public void cancel() {
        stop(null);
        try {
            if (filePath != null) {
                File file = new File(this.filePath);
                if (file.exists()) {
                    file.delete();
                }
                this.filePath = null;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            ZLogger.e("取消录音失败:" + localException.toString());
        }
    }

    public String complete(RecordCompleteListener paramRecordCompleteListener) {
        stop(paramRecordCompleteListener);
        String str = this.filePath;
        this.filePath = null;
        return str;
    }

    public void completeDelayed(long paramLong, final RecordCompleteListener paramRecordCompleteListener) {
        new Handler().postDelayed(new Runnable() {
                                      public void run() {
                                          Recorder.this.stop(paramRecordCompleteListener);
                                          Recorder.this.filePath = null;
                                      }
                                  }
                , paramLong);
    }

    public boolean isRecording() {
        return this.isRecording;
    }

    private RecordCompleteListener mRecordCompleteListener;
    public void start(RecordCompleteListener recordCompleteListener) {
        if (!FileUtil.checkExternalStorageExists()) {//checkExternalSDExists
            ZLogger.w("ExternalSD not exist");
            return;
        }
        cancel();
        this.isRecording = true;
//        SafeSco.getInstance().connect(new ScoConnectListener(paramRecordErrorListener) {
//            public void onConnected() {
//                ZLogger.w("sco连接成功");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        String fileName = System.currentTimeMillis() + ".amr";;
        Recorder.this.filePath = Environment.getExternalStorageDirectory() + "/bingshanguxue/record" + File.separator + fileName;
        FileUtil.getSaveFile("/bingshanguxue/record", fileName);

        Recorder.this.mediaRecorder.setOutputFile(Recorder.this.filePath);
        try {
            ZLogger.i("准备录音: " + Recorder.this.filePath);
            Recorder.this.mRecordCompleteListener = recordCompleteListener;
            Recorder.this.mediaRecorder.prepare();
            Recorder.this.mediaRecorder.start();
            Recorder.this.postTimeOut();
            Recorder.this.postRecordTime();
            ZLogger.i("开始录音");
            return;
        } catch (Exception localException) {
            localException.printStackTrace();
            Recorder.this.cancel();
//                    if (this.val$listener != null)
//                        this.val$listener.onError();
            ZLogger.e("开始录音失败:" + localException.toString());
        }
//            }
//
//            public void onError() {
//                onConnected();
//            }
//        });
    }
}
