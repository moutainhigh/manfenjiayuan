package com.jabra.media;

import android.content.Context;
import android.os.Handler;


import com.jabra.bean.Contacts;
import com.jabra.bean.Message;
import com.jabra.bean.PlayableItem;
import com.jabra.data.MsgSortList;
import com.jabra.listener.AudioPlayerListener;
import com.jabra.listener.PlayListener;
import com.jabra.listener.PlayMsgListener;
import com.jabra.utils.EmptyUtil;
import com.jabra.utils.SPFile;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.io.File;
import java.util.LinkedList;

public class AudioPlayer {
    private static final int MAX_AUTO_PLAY_MSG_TIME = 300000;
    private static final int MSG_PLAY_DELAY = 100;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PLAYING_MSG = 3;
    public static final int STATE_PLAYING_PROMPT = 1;
    public static final int STATE_PLAYING_TAIL = 4;
    public static final int STATE_PLAYING_TONE = 2;
    private static String TAG = "AudioPlayer";
    private Context context;
    private Contacts currentContacts;
    private boolean isAutoPlayMsg;
    private boolean isPlayNicknameNextTime;
    private boolean isStopBySystem;
    private PlayMsgListener listener;
    private AudioPlayerListener mAudioPlayerListener;
    private Handler mHandler = new Handler();
    private MultifunctionPlayer mMPlayer;
    private SPFile mSP;
    private Runnable playNicknameRunnable = new Runnable() {
        public void run() {
            AudioPlayer.this.isPlayNicknameNextTime = true;
        }
    };
    private String preUserId;
    private int state;
    private LinkedList<PlayableItem> tailPlayables = new LinkedList();

    public AudioPlayer(Context paramContext, AudioPlayerListener paramAudioPlayerListener) {
        this.context = paramContext;
        this.mAudioPlayerListener = paramAudioPlayerListener;
        this.mMPlayer = new MultifunctionPlayer(paramContext);
        this.mSP = new SPFile(paramContext, "FILE_CONFIG");
    }

    private void addTailPlayable(PlayableItem paramPlayableItem) {
        this.tailPlayables.add(paramPlayableItem);
        if (this.state == 0) {
            playTails();
        }
    }

    private void playEndTone() {
        ZLogger.d(TAG, "���������������");
        this.state = 4;
        this.mMPlayer.play(new PlayableItem(2131230766), new PlayListener() {
            public void onComplete() {
                AudioPlayer.this.playTails();
            }

            public void onError() {
                AudioPlayer.this.playTails();
            }
        });
    }

    private void playMsg(final MsgSortList paramMsgSortList, final PlayMsgListener paramPlayMsgListener) {
        if (EmptyUtil.isEmpty(paramMsgSortList)) {
            this.currentContacts = null;
            return;
        }
        this.state = 3;
        this.mHandler.removeCallbacks(this.playNicknameRunnable);
        if (this.isAutoPlayMsg) {
            this.mHandler.postDelayed(this.playNicknameRunnable, 300000L);
        }
        final Message localMessage = (Message) paramMsgSortList.pollFirst();
        if (paramPlayMsgListener != null) {
            paramPlayMsgListener.onPlayStart(localMessage);
        }
        ZLogger.d(TAG, "播放联系消息 " + localMessage.getContent());
        ZLogger.d(TAG, "播放联系消息 msgId: " + localMessage.getMsgId());
        switch (localMessage.getContentType()) {
            default:
                playMsg(paramMsgSortList, paramPlayMsgListener);
        }

        PlayableItem localPlayableItem = new PlayableItem(localMessage.getContent());
        this.mMPlayer.play(localPlayableItem, new PlayListener() {
            public void onComplete() {
                AudioPlayer.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        ZLogger.d(AudioPlayer.TAG, "playMsg   isStopBySystem:" + AudioPlayer.this.isStopBySystem);
                        if (AudioPlayer.this.isStopBySystem) {
                            AudioPlayer.this.state = 0;
                            paramMsgSortList.put(localMessage);
                            return;
                        }
                        if (paramPlayMsgListener != null) {
                            paramPlayMsgListener.onPlayEnd(localMessage);
                        }
                        AudioPlayer.this.playStartTone(paramMsgSortList, paramPlayMsgListener);
                    }
                }, 100L);
            }

            public void onError() {
                AudioPlayer.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        ZLogger.d(AudioPlayer.TAG, "playMsg   isStopBySystem:" + AudioPlayer.this.isStopBySystem);
                        if (AudioPlayer.this.isStopBySystem) {
                            AudioPlayer.this.state = 0;
                            paramMsgSortList.put(localMessage);
                            return;
                        }
                        if (paramPlayMsgListener != null) {
                            paramPlayMsgListener.onPlayEnd(localMessage);
                        }
                        AudioPlayer.this.playStartTone(paramMsgSortList, paramPlayMsgListener);
                    }
                }, 100L);
            }
        });

        //            localPlayableItem = new PlayableItem(new File(localMessage.getContent()));
    }

    private void playStartTone(final MsgSortList paramMsgSortList, final PlayMsgListener paramPlayMsgListener) {
        if (EmptyUtil.isEmpty(paramMsgSortList)) {
            this.currentContacts = null;
            if (paramPlayMsgListener != null) {
                paramPlayMsgListener.onCompleted();
            }
            playEndTone();
            return;
        }
        this.state = 3;
        ZLogger.d(TAG, "���������������������");
        this.mMPlayer.play(new PlayableItem(2131230767), new PlayListener() {
            public void onComplete() {
                AudioPlayer.this.mHandler.post(new Runnable() {
                    public void run() {
                        AudioPlayer.this.playMsg(paramMsgSortList, paramPlayMsgListener);
                    }
                });
            }

            public void onError() {
                AudioPlayer.this.mHandler.post(new Runnable() {
                    public void run() {
                        AudioPlayer.this.playMsg(paramMsgSortList, paramPlayMsgListener);
                    }
                });
            }
        });
    }

    private void playTails() {
        if (this.tailPlayables.isEmpty()) {
            this.state = 0;
            if ((this.currentContacts != null) && (EmptyUtil.notEmpty(this.currentContacts.getMsgSortList()))) {
                ZLogger.d(TAG, "playTails   currentContacts not null");
                this.mHandler.post(new Runnable() {
                    public void run() {
                        AudioPlayer.this.playContacts(AudioPlayer.this.currentContacts, AudioPlayer.this.listener);
                    }
                });
                return;
            }
            if (this.mAudioPlayerListener != null) {
                this.mAudioPlayerListener.onComplete();
            }
            ZLogger.d(TAG, "playTails   currentContacts is null");
            return;
        }
        this.state = 4;
        PlayableItem localPlayableItem = (PlayableItem) this.tailPlayables.pollFirst();
        if (localPlayableItem == null) {
            playTails();
        }
        ZLogger.d(TAG, "������������: " + localPlayableItem.toString());
        this.mMPlayer.play(localPlayableItem, new PlayListener() {
            public void onComplete() {
                AudioPlayer.this.playTails();
            }

            public void onError() {
                AudioPlayer.this.playTails();
            }
        });
    }

    public void clearPreUserId() {
        this.preUserId = null;
    }

    public int getState() {
        return this.state;
    }

    public boolean isPlaying() {
        return this.state > 0;
    }

    public boolean isPlayingMsg() {
        return this.state == 3;
    }

    public boolean isPlayingSys() {
        int i = 1;
        if ((this.state != i) && (this.state != 2)) {
            i = 0;
        }
        return i == 1;
    }

    public boolean isPlayingTone() {
        return this.state == 2;
    }

    public void playContacts(final Contacts paramContacts, final PlayMsgListener paramPlayMsgListener) {
        this.currentContacts = paramContacts;
        ZLogger.d(TAG, "playContacts");
        stop();
        this.state = 3;
        this.isAutoPlayMsg = this.mSP.getBoolean("KEY_AUTO_PLAY_MESSAGE", false);
        ZLogger.d(TAG, "playContacts   isAutoPlayMsg:" + this.isAutoPlayMsg);
        if (this.isAutoPlayMsg) {
            if ((EmptyUtil.notEmpty(this.preUserId)) && (this.preUserId.equals(paramContacts.getUserId())) && (!this.isPlayNicknameNextTime)) {
                playStartTone(paramContacts.getMsgSortList(), paramPlayMsgListener);
                return;
            }
            this.isPlayNicknameNextTime = false;
            this.preUserId = paramContacts.getUserId();
            ZLogger.d(TAG, "���������������������: " + paramContacts.getNickName());
            this.mMPlayer.play(new PlayableItem(paramContacts.getNickName() + "this.context.getString(2131230764)"), new PlayListener() {
                public void onComplete() {
                    AudioPlayer.this.mHandler.post(new Runnable() {
                        public void run() {
                            if (AudioPlayer.this.isStopBySystem) {
                                AudioPlayer.this.state = 0;
                                return;
                            }
                            AudioPlayer.this.playMsg(paramContacts.getMsgSortList(), paramPlayMsgListener);
                        }
                    });
                }

                public void onError() {
                    AudioPlayer.this.mHandler.post(new Runnable() {
                        public void run() {
                            if (AudioPlayer.this.isStopBySystem) {
                                AudioPlayer.this.state = 0;
                                return;
                            }
                            AudioPlayer.this.playMsg(paramContacts.getMsgSortList(), paramPlayMsgListener);
                        }
                    });
                }
            });
            return;
        }
        this.isPlayNicknameNextTime = true;
        this.preUserId = paramContacts.getUserId();
        ZLogger.d(TAG, "播放联系人名字: " + paramContacts.getNickName());
        this.mMPlayer.play(new PlayableItem(paramContacts.getNickName() + "this.context.getString(2131230764)"), new PlayListener() {
            public void onComplete() {
                AudioPlayer.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (AudioPlayer.this.isStopBySystem) {
                            AudioPlayer.this.state = 0;
                            return;
                        }
                        AudioPlayer.this.playMsg(paramContacts.getMsgSortList(), paramPlayMsgListener);
                    }
                });
            }

            public void onError() {
                AudioPlayer.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (AudioPlayer.this.isStopBySystem) {
                            AudioPlayer.this.state = 0;
                            return;
                        }
                        AudioPlayer.this.playMsg(paramContacts.getMsgSortList(), paramPlayMsgListener);
                    }
                });
            }
        });
    }

    public void playPrompt(String paramString) {
        ZLogger.d(TAG, "播放系统提示语: " + paramString);
        stop();
        this.state = 1;
        this.mMPlayer.play(new PlayableItem(paramString), new PlayListener() {
            public void onComplete() {
                AudioPlayer.this.playTails();
            }

            public void onError() {
                AudioPlayer.this.playTails();
            }
        });
    }

    public void playTone(int paramInt) {
        ZLogger.d(TAG, "播放系统音效: " + paramInt);
        stop();
        this.state = 2;
        this.mMPlayer.play(new PlayableItem(paramInt), new PlayListener() {
            public void onComplete() {
                AudioPlayer.this.playTails();
            }

            public void onError() {
                AudioPlayer.this.playTails();
            }
        });
    }

    public void putTailPlayable(PlayableItem paramPlayableItem) {
        this.tailPlayables.remove(paramPlayableItem);
        addTailPlayable(paramPlayableItem);
    }

    public void removeTailPlayable(PlayableItem paramPlayableItem) {
        this.tailPlayables.remove(paramPlayableItem);
    }

    public void setCurrentContacts(Contacts paramContacts, PlayMsgListener paramPlayMsgListener) {
        ZLogger.d(TAG, "setCurrentContacts");
        if (paramContacts != null) {
            ZLogger.d(TAG, "setCurrentContacts   nickname:" + paramContacts.getNickName());
        }
        this.currentContacts = paramContacts;
        this.listener = paramPlayMsgListener;
    }

    public void setPlayNickname(boolean paramBoolean) {
        this.isPlayNicknameNextTime = paramBoolean;
    }

    public void setStopBySystem(boolean paramBoolean) {
        this.isStopBySystem = paramBoolean;
    }

    public void stop() {
        ZLogger.d(TAG, "stop");
        this.mMPlayer.stop();
        this.isStopBySystem = false;
        this.state = 0;
    }
}
