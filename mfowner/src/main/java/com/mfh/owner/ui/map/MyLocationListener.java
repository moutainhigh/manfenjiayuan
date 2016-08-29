package com.mfh.owner.ui.map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.mfh.framework.anlaysis.logger.ZLogger;


/**
 * Created by Administrator on 2015/5/8.
 */
public class MyLocationListener implements LocationListener {

    private Context context;
    public MyLocationListener(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        ZLogger.d(LocationUtil.getLocationInfo(location));
        //TODO,保存位置信息
        LocationUtil.saveLocationInfo(context, location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Provider状态在可用、暂不可用、无服务三个状态之间直接切换时触发此函数
        ZLogger.d(String.format("%s, status:%d", provider, status));
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Provider被enable时触发此函数,比如GPS被打开
        ZLogger.d(provider + " enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Provider被disable时触发此函数,比如GPS被关闭
        ZLogger.d(provider + " disabled");
    }

}
