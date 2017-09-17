package com.jabra.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import com.jabra.listener.ScoConnectListener;
import com.jabra.listener.ScoDisconnectListener;
import com.mfh.framework.anlaysis.logger.ZLogger;

public class SafeSco {
    public static final int NEED_CONNECT = 1;
    public static final int NEED_DISCONNECT = 2;
    public static final int NEED_NULL = 0;
    public static final int STATE_CONNECTED = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_DISCONNECTED = 4;
    public static final int STATE_DISCONNECTING = 3;
    public static final int STATE_UNKNOWN = 0;
    private static final String TAG = "SafeSco";
    private static SafeSco instance;
    private AudioManager audioManager;
    private Context context;
    private IntentFilter filter = new IntentFilter("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
    private int need = 0;
    private ScoConnectListener scoConnectListener;
    private ScoDisconnectListener scoDisconnectListener;
    private BroadcastReceiver scoReceiver = new BroadcastReceiver() {
        public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent) {
            switch (paramAnonymousIntent.getIntExtra("android.media.extra.SCO_AUDIO_STATE", -1)) {
                default:
                case 1:
                case 2:
                case 0:
                    do {
                        do {
                            do {
//              return;
                                ZLogger.d("SCO_AUDIO_STATE_CONNECTED");
                                SafeSco.this.state = 1;
                                if (SafeSco.this.need == 2) {
                                    SafeSco.this.audioManager.stopBluetoothSco();
                                    SafeSco.this.state = 3;
                                    SafeSco.this.need = 0;
                                    return;
                                }
                            } while (SafeSco.this.scoConnectListener == null);
                            SafeSco.this.scoConnectListener.onConnected();
                            SafeSco.this.scoConnectListener = null;
                            paramAnonymousContext.unregisterReceiver(SafeSco.this.scoReceiver);
//            return;
                            ZLogger.v("SafeSco", "SCO_AUDIO_STATE_CONNECTING");
                            SafeSco.this.state = 2;
//            return;
                            ZLogger.v("SafeSco", "SCO_AUDIO_STATE_DISCONNECTED");
                            int i = SafeSco.this.state;
                            SafeSco.this.state = 4;
                            if (i != 2) {
                                break;
                            }
                            SafeSco.this.state = 1;
                        } while (SafeSco.this.scoConnectListener == null);
                        SafeSco.this.scoConnectListener.onConnected();
                        SafeSco.this.scoConnectListener = null;
                        paramAnonymousContext.unregisterReceiver(SafeSco.this.scoReceiver);
//          return;
                        if (SafeSco.this.need == 1) {
                            SafeSco.this.audioManager.startBluetoothSco();
                            SafeSco.this.state = 2;
                            SafeSco.this.need = 0;
                            return;
                        }
                    } while (SafeSco.this.scoDisconnectListener == null);
                    SafeSco.this.scoDisconnectListener.onDisconnected();
                    SafeSco.this.scoDisconnectListener = null;
                    paramAnonymousContext.unregisterReceiver(SafeSco.this.scoReceiver);
                    return;
            }
//      Log.v("SafeSco", "SCO_AUDIO_STATE_ERROR");
//      Log.d("/Jabra_Social/record", "SCO_AUDIO_STATE_ERROR");
        }
    };
    private int state = 0;

    public static SafeSco getInstance() {
        if (instance == null) {
        }
        try {
            if (instance == null) {
                instance = new SafeSco();
            }
            return instance;
        } finally {
        }
    }

    public void connect(ScoConnectListener paramScoConnectListener) {
        this.need = 0;
        this.scoConnectListener = null;
        this.scoDisconnectListener = null;
        ZLogger.e("connect state: " + this.state);
        switch (this.state) {
        }
        for (; ; ) {
            this.context.registerReceiver(this.scoReceiver, this.filter);
//      return;
            if (paramScoConnectListener != null) {
                paramScoConnectListener.onConnected();
//        return;
                this.scoConnectListener = paramScoConnectListener;
//        continue;
                this.scoConnectListener = paramScoConnectListener;
                this.audioManager.startBluetoothSco();
                this.state = 2;
//        continue;
                this.scoConnectListener = paramScoConnectListener;
                this.need = 1;
            }
        }
    }

    public void disconnect(ScoDisconnectListener paramScoDisconnectListener) {
        this.need = 0;
        this.scoConnectListener = null;
        this.scoDisconnectListener = null;
        ZLogger.e("disconnect state: " + this.state);
        switch (this.state) {
        }
        for (; ; ) {
            this.context.registerReceiver(this.scoReceiver, this.filter);
//      return;
            this.scoDisconnectListener = paramScoDisconnectListener;
            for (int i = 0; ; i++) {
                if (i >= 10) {
                    this.state = 3;
                    break;
                }
                this.audioManager.stopBluetoothSco();
            }
            this.scoDisconnectListener = paramScoDisconnectListener;
            this.need = 2;
//      continue;
            this.audioManager.stopBluetoothSco();
            if (paramScoDisconnectListener != null) {
                paramScoDisconnectListener.onDisconnected();
//        return;
                this.scoDisconnectListener = paramScoDisconnectListener;
            }
        }
    }

    public void register(Context paramContext) {
        this.context = paramContext;
        if (this.audioManager == null) {
            this.audioManager = ((AudioManager) paramContext.getSystemService(Context.AUDIO_SERVICE));
            if (!this.audioManager.isBluetoothScoOn()) {
//        break;
            }
        }

        for (int i = 1; ; i = 4) {
            this.state = i;
            return;
        }
    }

    public void unregister(Context paramContext) {
        this.audioManager = null;
        try {
            paramContext.unregisterReceiver(this.scoReceiver);
            return;
        } catch (Exception localException) {
        }
    }
}
