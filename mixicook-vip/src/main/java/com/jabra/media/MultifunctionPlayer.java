package com.jabra.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Handler;

import com.jabra.bean.PlayableItem;
import com.jabra.listener.PlayListener;
import com.jabra.listener.TTSListener;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.tencent.mm.sdk.ext.MMOpenApiCaller;

import net.sourceforge.simcpux.wxapi.Constants;

import java.io.File;
import java.util.LinkedList;

public class MultifunctionPlayer {
    private static final String TAG = "MultifunctionPlayer";
    private Context context;
    private Handler handler = new Handler();
    private boolean isTrackPlaying;
    private MediaPlayer mMediaPlayer;
//    private TtsPlayer mTTSPlayer;
    private AudioTrack mTrackPlayer;

    public MultifunctionPlayer(Context paramContext) {
        this.context = paramContext;
//        this.mTTSPlayer = new TtsPlayer(paramContext);
    }

    private MMOpenApiCaller.VoiceData decodePCM(File paramFile) {
        try {
            Context localContext = this.context;
            String[] arrayOfString = new String[1];
            arrayOfString[0] = paramFile.getAbsolutePath();
            MMOpenApiCaller.MMResult localMMResult = MMOpenApiCaller.decodeVoice(localContext, Constants.APP_ID, arrayOfString);
            ZLogger.d("MultifunctionPlayer", "decodePCM: " + localMMResult.retCode);
            MMOpenApiCaller.VoiceData localVoiceData = (MMOpenApiCaller.VoiceData) ((LinkedList) localMMResult.data).peekLast();
            return localVoiceData;
        } catch (Exception localException) {
            localException.printStackTrace();
            ZLogger.e("MultifunctionPlayer", "decodePCM error: " + paramFile.getAbsolutePath());
        }
        return null;
    }

    private void playAMR(File paramFile, final PlayListener paramPlayListener) {
        ZLogger.d("MultifunctionPlayer", "playAMR: " + paramFile.getAbsolutePath());
        this.mMediaPlayer = new MediaPlayer();
        this.mMediaPlayer.setAudioStreamType(3);
        try {
            this.mMediaPlayer.setDataSource(paramFile.getAbsolutePath());
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer paramAnonymousMediaPlayer) {
                    ZLogger.d("MultifunctionPlayer", "AMR������������������");
                    if (MultifunctionPlayer.this.mMediaPlayer != null) {
                        MultifunctionPlayer.this.mMediaPlayer.release();
                        MultifunctionPlayer.this.mMediaPlayer = null;
                    }
                    if (paramPlayListener != null) {
                        paramPlayListener.onComplete();
                    }
                }
            });
            this.mMediaPlayer.start();
            return;
        } catch (Exception localException) {
            do {
                localException.printStackTrace();
                ZLogger.e("MultifunctionPlayer", "AMR������������������");
                ZLogger.d("/Jabra_Social/api_log", "AMR������������������");
                this.mMediaPlayer.release();
                this.mMediaPlayer = null;
            } while (paramPlayListener == null);
            paramPlayListener.onError();
        }
    }

    private void playPCM(File paramFile, final PlayListener paramPlayListener) {
        ZLogger.d("MultifunctionPlayer", "playPCM: " + paramFile.getAbsolutePath());
        final MMOpenApiCaller.VoiceData localVoiceData = decodePCM(paramFile);
        if ((localVoiceData == null) || (localVoiceData.voiceType != 1)) {
            if (paramPlayListener != null) {
                paramPlayListener.onError();
            }
            return;
        }
        ZLogger.d("MultifunctionPlayer", " voiceType:" + localVoiceData.voiceType + "  || sampleRateInHz:" + localVoiceData.sampleRateInHz + "    ||channelConfig:" + localVoiceData.channelConfig + "  || audioFormat:" + localVoiceData.audioFormat);
        int i = AudioTrack.getMinBufferSize(localVoiceData.sampleRateInHz, localVoiceData.channelConfig, localVoiceData.audioFormat);
        this.mTrackPlayer = new AudioTrack(3, localVoiceData.sampleRateInHz, localVoiceData.channelConfig, localVoiceData.audioFormat, i * 2, 1);
        this.mTrackPlayer.play();
        this.isTrackPlaying = true;
        new Thread(new Runnable() {
            private AudioTrack tempTrackPlayer = MultifunctionPlayer.this.mTrackPlayer;

            /* Error */
            public void run() {
                // Byte code:
                //   0: aconst_null
                //   1: astore_1
                //   2: new 43	java/io/FileInputStream
                //   5: dup
                //   6: aload_0
                //   7: getfield 25	com/xpg/jabra/proto/media/MultifunctionPlayer$4:val$voiceData	Lcom/tencent/mm/sdk/ext/MMOpenApiCaller$VoiceData;
                //   10: getfield 49	com/tencent/mm/sdk/ext/MMOpenApiCaller$VoiceData:filePath	Ljava/lang/String;
                //   13: invokespecial 52	java/io/FileInputStream:<init>	(Ljava/lang/String;)V
                //   16: astore_2
                //   17: sipush 4096
                //   20: newarray <illegal type>
                //   22: astore 9
                //   24: lconst_0
                //   25: lstore 10
                //   27: aload_0
                //   28: getfield 23	com/xpg/jabra/proto/media/MultifunctionPlayer$4:this$0	Lcom/xpg/jabra/proto/media/MultifunctionPlayer;
                //   31: invokestatic 56	com/xpg/jabra/proto/media/MultifunctionPlayer:access$3	(Lcom/xpg/jabra/proto/media/MultifunctionPlayer;)Z
                //   34: ifeq +17 -> 51
                //   37: aload_2
                //   38: aload 9
                //   40: invokevirtual 60	java/io/FileInputStream:read	([B)I
                //   43: istore 13
                //   45: iload 13
                //   47: iconst_m1
                //   48: if_icmpne +42 -> 90
                //   51: aload_2
                //   52: ifnull +185 -> 237
                //   55: aload_2
                //   56: invokevirtual 63	java/io/FileInputStream:close	()V
                //   59: ldc2_w 64
                //   62: invokestatic 71	java/lang/Thread:sleep	(J)V
                //   65: aload_0
                //   66: getfield 36	com/xpg/jabra/proto/media/MultifunctionPlayer$4:tempTrackPlayer	Landroid/media/AudioTrack;
                //   69: invokevirtual 76	android/media/AudioTrack:stop	()V
                //   72: aload_0
                //   73: getfield 36	com/xpg/jabra/proto/media/MultifunctionPlayer$4:tempTrackPlayer	Landroid/media/AudioTrack;
                //   76: invokevirtual 79	android/media/AudioTrack:release	()V
                //   79: aload_0
                //   80: getfield 23	com/xpg/jabra/proto/media/MultifunctionPlayer$4:this$0	Lcom/xpg/jabra/proto/media/MultifunctionPlayer;
                //   83: invokestatic 56	com/xpg/jabra/proto/media/MultifunctionPlayer:access$3	(Lcom/xpg/jabra/proto/media/MultifunctionPlayer;)Z
                //   86: ifne +101 -> 187
                //   89: return
                //   90: aload_0
                //   91: getfield 36	com/xpg/jabra/proto/media/MultifunctionPlayer$4:tempTrackPlayer	Landroid/media/AudioTrack;
                //   94: aload 9
                //   96: iconst_0
                //   97: iload 13
                //   99: invokevirtual 83	android/media/AudioTrack:write	([BII)I
                //   102: pop
                //   103: lload 10
                //   105: iload 13
                //   107: i2l
                //   108: ladd
                //   109: lstore 10
                //   111: ldc 85
                //   113: new 87	java/lang/StringBuilder
                //   116: dup
                //   117: ldc 89
                //   119: invokespecial 90	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
                //   122: lload 10
                //   124: invokevirtual 94	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
                //   127: invokevirtual 98	java/lang/StringBuilder:toString	()Ljava/lang/String;
                //   130: invokestatic 104	com/easy/util/LogUtil:v	(Ljava/lang/String;Ljava/lang/Object;)V
                //   133: goto -106 -> 27
                //   136: astore 5
                //   138: aload_2
                //   139: astore_1
                //   140: aload 5
                //   142: invokevirtual 107	java/lang/Exception:printStackTrace	()V
                //   145: aload_1
                //   146: ifnull -87 -> 59
                //   149: aload_1
                //   150: invokevirtual 63	java/io/FileInputStream:close	()V
                //   153: goto -94 -> 59
                //   156: astore 6
                //   158: goto -99 -> 59
                //   161: astore_3
                //   162: aload_1
                //   163: ifnull +7 -> 170
                //   166: aload_1
                //   167: invokevirtual 63	java/io/FileInputStream:close	()V
                //   170: aload_3
                //   171: athrow
                //   172: astore 12
                //   174: goto -115 -> 59
                //   177: astore 7
                //   179: aload 7
                //   181: invokevirtual 108	java/lang/InterruptedException:printStackTrace	()V
                //   184: goto -119 -> 65
                //   187: aload_0
                //   188: getfield 23	com/xpg/jabra/proto/media/MultifunctionPlayer$4:this$0	Lcom/xpg/jabra/proto/media/MultifunctionPlayer;
                //   191: invokestatic 112	com/xpg/jabra/proto/media/MultifunctionPlayer:access$4	(Lcom/xpg/jabra/proto/media/MultifunctionPlayer;)Landroid/os/Handler;
                //   194: new 114	com/xpg/jabra/proto/media/MultifunctionPlayer$4$1
                //   197: dup
                //   198: aload_0
                //   199: aload_0
                //   200: getfield 27	com/xpg/jabra/proto/media/MultifunctionPlayer$4:val$listener	Lcom/xpg/jabra/proto/listener/PlayListener;
                //   203: invokespecial 117	com/xpg/jabra/proto/media/MultifunctionPlayer$4$1:<init>	(Lcom/xpg/jabra/proto/media/MultifunctionPlayer$4;Lcom/xpg/jabra/proto/listener/PlayListener;)V
                //   206: invokevirtual 123	android/os/Handler:post	(Ljava/lang/Runnable;)Z
                //   209: pop
                //   210: aload_0
                //   211: getfield 23	com/xpg/jabra/proto/media/MultifunctionPlayer$4:this$0	Lcom/xpg/jabra/proto/media/MultifunctionPlayer;
                //   214: iconst_0
                //   215: invokestatic 127	com/xpg/jabra/proto/media/MultifunctionPlayer:access$5	(Lcom/xpg/jabra/proto/media/MultifunctionPlayer;Z)V
                //   218: return
                //   219: astore 4
                //   221: goto -51 -> 170
                //   224: astore_3
                //   225: aload_2
                //   226: astore_1
                //   227: goto -65 -> 162
                //   230: astore 5
                //   232: aconst_null
                //   233: astore_1
                //   234: goto -94 -> 140
                //   237: goto -178 -> 59
                // Local variable table:
                //   start	length	slot	name	signature
                //   0	240	0	this	4
                //   1	233	1	localObject1	Object
                //   16	210	2	localFileInputStream	java.io.FileInputStream
                //   161	10	3	localObject2	Object
                //   224	1	3	localObject3	Object
                //   219	1	4	localException1	Exception
                //   136	5	5	localException2	Exception
                //   230	1	5	localException3	Exception
                //   156	1	6	localException4	Exception
                //   177	3	7	localInterruptedException	InterruptedException
                //   22	73	9	arrayOfByte	byte[]
                //   25	98	10	l	long
                //   172	1	12	localException5	Exception
                //   43	63	13	i	int
                // Exception table:
                //   from	to	target	type
                //   17	24	136	java/lang/Exception
                //   27	45	136	java/lang/Exception
                //   90	103	136	java/lang/Exception
                //   111	133	136	java/lang/Exception
                //   149	153	156	java/lang/Exception
                //   2	17	161	finally
                //   140	145	161	finally
                //   55	59	172	java/lang/Exception
                //   59	65	177	java/lang/InterruptedException
                //   166	170	219	java/lang/Exception
                //   17	24	224	finally
                //   27	45	224	finally
                //   90	103	224	finally
                //   111	133	224	finally
                //   2	17	230	java/lang/Exception
            }
        }).start();
    }

    private void playRes(int paramInt, final PlayListener paramPlayListener) {
        String str = this.context.getString(paramInt);
        ZLogger.d("MultifunctionPlayer", "playRes: " + str);
        this.mMediaPlayer = new MediaPlayer();
        this.mMediaPlayer.setAudioStreamType(3);
        try {
            AssetFileDescriptor localAssetFileDescriptor = this.context.getAssets().openFd(str);
            this.mMediaPlayer.setDataSource(localAssetFileDescriptor.getFileDescriptor(), localAssetFileDescriptor.getStartOffset(), localAssetFileDescriptor.getLength());
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer paramAnonymousMediaPlayer) {
                    ZLogger.e("MultifunctionPlayer", "Res������������������");
                    if (MultifunctionPlayer.this.mMediaPlayer != null) {
                        MultifunctionPlayer.this.mMediaPlayer.release();
                        MultifunctionPlayer.this.mMediaPlayer = null;
                    }
                    if (paramPlayListener != null) {
                        paramPlayListener.onComplete();
                    }
                }
            });
            this.mMediaPlayer.start();
        } catch (Exception localException) {
            do {
                localException.printStackTrace();
                ZLogger.v("MultifunctionPlayer Res������������������");
                this.mMediaPlayer.release();
                this.mMediaPlayer = null;
            } while (paramPlayListener == null);
            paramPlayListener.onError();
        }
    }

    private void playTTS(String paramString, final PlayListener paramPlayListener) {
        ZLogger.d("MultifunctionPlayer", "playTTS: " + paramString);
//        this.mTTSPlayer.speak(paramString, new TTSListener() {
//            public void onComplete() {
//                if (paramPlayListener != null) {
//                    paramPlayListener.onComplete();
//                }
//            }
//
//            public void onError() {
//                if (paramPlayListener != null) {
//                    paramPlayListener.onError();
//                }
//            }
//        });
    }

    public void play(PlayableItem playableItem, PlayListener paramPlayListener) {
        switch (playableItem.getType()) {
            case 1:
                playAMR(playableItem.getFile(), paramPlayListener);
                return;
            case 2:
                playPCM(playableItem.getFile(), paramPlayListener);
                return;
            case 4:
                playRes(playableItem.getResId(), paramPlayListener);
                return;
            default:
                if (paramPlayListener != null) {
                    paramPlayListener.onError();
                }
                return;
        }
//        playTTS(paramPlayableItem.getText(), paramPlayListener);
    }

    public void stop() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.setOnCompletionListener(null);
            this.mMediaPlayer.setOnErrorListener(null);
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
        if (this.mTrackPlayer != null) {
            this.isTrackPlaying = false;
            this.mTrackPlayer = null;
        }
        try {
            Thread.sleep(100L);
//            if (this.mTTSPlayer != null) {
//                this.mTTSPlayer.stop();
//            }
            return;
        } catch (InterruptedException localInterruptedException) {
            for (; ; ) {
                localInterruptedException.printStackTrace();
            }
        }
    }
}
