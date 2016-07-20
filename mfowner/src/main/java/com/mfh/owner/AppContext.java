package com.mfh.owner;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.AppException;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.owner.utils.Constants;
import com.mfh.owner.utils.SensoroHelper;
import com.sensoro.beacon.kit.Beacon;
import com.sensoro.beacon.kit.BeaconManagerListener;
import com.sensoro.cloud.SensoroManager;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Administrator on 2015/7/10.
 */
public class AppContext extends MfhApplication {

    /**云子·摇一摇*/
    /*
	 * Sensoro Manager
	 */
    public SensoroManager sensoroManager;
    /*
	 * Beacon Manager lister,use it to listen the appearence, disappearence and
	 * updating of the beacons.
	 */
    private BeaconManagerListener beaconManagerListener;
    /*
    * store beacons in onUpdateBeacon
    */
    public CopyOnWriteArrayList<Beacon> mBeasons;
    private String beaconFilter;

    private static AppContext instance;

    @Override
    public void onCreate() {

        AppException.CRASH_FOLDER_PATH = getPackageName() + File.separator + "crash";

        super.onCreate();

        ZLogger.CRASH_FOLDER_PATH = getPackageName() + File.separator + "zlogger";

        instance = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            initSensoro();
        }
    }


    @Override
    public void onTerminate() {
        stopSensoro();
        super.onTerminate();
    }

    /**
     * 获得当前app运行的AppContext
     *
     * @return
     */
    public static AppContext getInstance() {
        return instance;
    }

    /**
     * initialize Sensoro SDK
     * */
    private void initSensoro() {
        sensoroManager = SensoroManager.getInstance(getApplicationContext());
        /**
         * Enable cloud service (upload sensor data, including battery status, UMM, etc.)。Without setup, it keeps in closed status as default.
         **/
        sensoroManager.setCloudServiceEnable(false);
        /**
         * 设置云子防蹭用密钥 (如果没有可以不设置)
         **/
//        sensoroManager.addBroadcastKey("7b4b5ff594fdaf8f9fc7f2b494e400016f461205");

        mBeasons = new CopyOnWriteArrayList<>();

        initSensoroListener();
        // Nat_20150421 如果在这里启动sensoro Service, 会报错。
//        startSensoroService();
    }

    /*
	 * Start sensoro service.
	 * SDK 是基于蓝牙 4.0 的服务，启动前请先检查蓝牙是否开启，否则 SDK 无法正常工作。
	 */
    public void startSensoroService() {
        // set a tBeaconManagerListener.
        sensoroManager.setBeaconManagerListener(beaconManagerListener);
        /**
         * Check whether the Bluetooth is on
         **/
        if (sensoroManager.isBluetoothEnabled()){
            /**
             * Enable SDK service
             **/
            try {
                sensoroManager.startService();
            } catch (Exception e) {// 捕获异常信息
                ZLogger.d("startSensoroService failed, " + e.toString());
//            e.printStackTrace();
            }
        }
    }

    /**
     * stop Sensoro SDK
     * */
    private void stopSensoro(){
        if (sensoroManager != null) {
            sensoroManager.stopService();
        }

        if(mBeasons != null){
            mBeasons.clear();
        }
    }

    /**
     * 传感器信息更新频率为 1 秒；发现一个新的传感器后，如果在 8 秒内没有再次扫描到这个设备，则会回调传感器消失。
     * serialNumber	SN，设备唯一标识
     major	iBeacon协议中的 major 信息
     minor	iBeacon协议中的 minor 信息
     proximityUUID	iBeacon协议中的 UUID 信息
     rssi	信号强度
     accuracy	距离（米）
     proximity	范围（很远，附近，很近，未知）
     temperature	芯片温度
     light	光线
     movingState	移动状态
     accelerometerCount	移动计数器
     batteryLevel	电池电量
     hardwareModelName	硬件版本
     firmwareVersion	固件版本
     measuredPower	1 米处测量 rssi
     transmitPower	广播功率
     advertisingInterval	广播间隔
     * */
    private void initSensoroListener() {
        beaconManagerListener = new BeaconManagerListener() {

            @Override
            public void onUpdateBeacon(final ArrayList<Beacon> beacons) {
                //Refresh sensor info
                StringBuilder sb = new StringBuilder();
                sb.append("Beacons Update:\n");
                int i = 0;

                //Add the update beacons into the grid.
                for (Beacon beacon : beacons) {
                    if (mBeasons.contains(beacon)) {
                        continue;
                    }

                    //filter
                    if (TextUtils.isEmpty(beaconFilter)) {
                        mBeasons.add(beacon);
                    } else {
                        String matchString = String.format(SensoroHelper.MATCH_FORMAT,
                                beacon.getSerialNumber(), beacon.getMajor(), beacon.getMinor());
                        if (matchString.contains(beaconFilter)) {
                            mBeasons.add(beacon);
                        }
                    }

                    sb.append(String.format("%d  %s\n", i++, beacon.toString()));
                }
//                MLog.d("onUpdateBeacon" + sb.toString());

                //TODO
                notifyBeaconsUpdate();
            }

            @Override
            public void onNewBeacon(Beacon beacon) {
                //New device found in range
                final String beanconInfo = beacon.toString();
//                MLog.d("onNewBeacon" + beanconInfo);
                //A new beacon appears.
//                String key = getKey(arg0);
//                boolean state = sharedPreferences.getBoolean(key, false);
//                if (state) {
//					//show notification
//                    showNotification(arg0, true);
//                }
//
//                runOnUiThread(new Runnable() {
//                    public void run() {
////                        DialogUtil.showHint(String.format("New Beacons<%s>", beanconInfo));
//                    }
//                });
            }

            @Override
            public void onGoneBeacon(Beacon beacon) {
                //A device has left the range
                final String beanconInfo = beacon.toString();
//                MLog.d("onGoneBeacon> " + beanconInfo);
				/*
				 * A beacon disappears.
				 */
                if (mBeasons.contains(beacon)) {
                    mBeasons.remove(beacon);
                }
                notifyBeaconsUpdate();

//                String key = getKey(arg0);
//                boolean state = sharedPreferences.getBoolean(key, false);
//                if (state) {
//					//show notification
//                    showNotification(arg0, false);
//                }

            }
        };
    }

    public String getKey(Beacon beacon) {
        if (beacon == null) {
            return null;
        }
        String key = beacon.getProximityUUID() + beacon.getMajor() + beacon.getMinor() + beacon.getSerialNumber();
        return key;
    }

    /**
     * 附近是否有云子
     * */
    public boolean existBeacons(){
        if(mBeasons != null && mBeasons.size() > 0){
            return true;
        }
        return false;
    }

    /**
     * 云子状态发生改变，通知改变摇一摇周边选项显示/隐藏
     * */
    private void notifyBeaconsUpdate(){
        Intent intent = new Intent(Constants.ACTION_BEACONS_UPDATE);
        intent.putExtra(Constants.KEY_BEACONS_EXIST, existBeacons());
        sendBroadcast(intent);

//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        for (OnBeaconChangeListener listener : beaconListeners) {
//                            if (listener == null) {
//                                continue;
//                            }
//                            listener.onBeaconChange(arg0);
//                        }
//                    }
//                });
    }

}
