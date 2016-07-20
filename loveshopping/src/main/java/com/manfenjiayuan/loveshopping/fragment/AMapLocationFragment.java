package com.manfenjiayuan.loveshopping.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.manfenjiayuan.loveshopping.AMapUtil;
import com.manfenjiayuan.loveshopping.AppContext;

import butterknife.ButterKnife;

/**
 * 高德地图定位
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public abstract class AMapLocationFragment extends Fragment implements AMapLocationListener {

    private static final String TAG = "AMapFragment";
    protected View rootView;
    //声明AMapLocationClient类对象,持续定位
    public AMapLocationClient mLocationClient = null;
    //定位参数
    private AMapLocationClientOption mLocationOption = null;

    protected int getLayoutResId(){return 0;}

    protected abstract void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

//        ZLogger.d("onCreateView()");
        //Inflate the layout for this fragment
        rootView = inflater.inflate(getLayoutResId(), container, false);

        ButterKnife.bind(this, rootView);

        createViewInner(rootView, container, savedInstanceState);

        init();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ZLogger.d("onViewCreated()");
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        ZLogger.d("onDetach()");
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null){
//            mLocationClient.stopLocation();//停止定位
            mLocationClient.onDestroy();//销毁定位客户端
//            mLocationClient = null;
        }

//        mLocationOption = null;
    }

    private void init(){
        setUpLocation();
    }

    private void setUpLocation(){
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(5000);


        mLocationClient = new AMapLocationClient(AppContext.getAppContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        mLocationClient.startLocation();
    }


    protected void processAMapLocation(AMapLocation aMapLocation){

    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation == null){
            return;
        }
        Log.d(TAG, AMapUtil.getLocationStr(aMapLocation));

        if (aMapLocation.getErrorCode() == 0) {
            processAMapLocation(aMapLocation);
        }
    }
}
