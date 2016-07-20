package com.mfh.owner.sensoro;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Administrator on 2015/4/20.
 */
public class SensoroServie extends Service {
    private static final String TAG = SensoroServie.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
