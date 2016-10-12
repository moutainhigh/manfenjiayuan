package com.manfenjiayuan.mixicook_vip.ui.location;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.AoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.address.AddressBrief;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.location.LocationClient;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;

import static com.mfh.framework.core.utils.DensityUtil.dip2px;

/**
 * POI
 */
public class PoiActivity extends BaseActivity
        implements LocationSource, AMapLocationListener, AMap.OnMapScreenShotListener, Runnable,
        GeocodeSearch.OnGeocodeSearchListener, PoiSearch.OnPoiSearchListener {

    public static final long LOCATE_TIMEOUT = 12 * 1000;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private AMapLocation aMapLocation;// 用于判断定位超时
    private Handler handler = new Handler();

    @Bind(R.id.map_view)
    MapView mapView;
    private AMap aMap;
    private AMapLocationClient mAMapLocationClient;
    private AMapLocationClientOption mLocationOption;
    private LocationSource.OnLocationChangedListener mListener;

    private Marker regeoMarker;//地图中心Marker
    private GeocodeSearch geocoderSearch;


    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch poiSearch;
    private PoiSearch.Query query;// Poi查询条件

    @Bind(R.id.location_list)
    ListView listView;
    private LocationHeaderView headerView;
    private PoiAdapter adapter;
    private AddressBrief mAddressBrief = new AddressBrief();
    @Bind(R.id.animProgress)
    ProgressBar mProgressBar;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_poi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTopBar();

        initLocation();

        handler.postDelayed(this, LOCATE_TIMEOUT);// 设置超过12秒还没有定位到就停止定位

        mapView.onCreate(savedInstanceState);// 此方法必须重写
        initAMap();

        headerView = new LocationHeaderView(this);
        headerView.setMarkerEnabled(true);
        mAddressBrief = new AddressBrief();
        headerView.setAddressBrief(mAddressBrief);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headerView.setMarkerEnabled(true);
                adapter.setSelectId(-1);

                mAddressBrief = headerView.getAddressBrief();
                if (mAddressBrief != null){
                    Intent data = new Intent();
                    data.putExtra("addressBrief", mAddressBrief);
                    setResult(Activity.RESULT_OK, data);
                    finish();
//                    LatLng latLng = new LatLng(mAddressBrief.getLatitude(), mAddressBrief.getLongitude());
//                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
//                        DialogUtil.showHint(String.format("选中坐标:%f,%f", latLng.longitude, latLng.latitude));
                }
            }
        });

        listView.addHeaderView(headerView, null, true);

        adapter = new PoiAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    //标记选中项
                    adapter.setSelectId(i - listView.getHeaderViewsCount());
                    headerView.setMarkerEnabled(false);

                    //listview 添加了headerview or footview后导致position不正确。使用adapterView.getAdapter()代替我们自己的adapter.
                    mAddressBrief = (AddressBrief) adapterView.getAdapter().getItem(i);
                    headerView.setAddressBrief(mAddressBrief);

                    if (mAddressBrief != null){
                        Intent data = new Intent();
                        data.putExtra("addressBrief", mAddressBrief);
                        setResult(Activity.RESULT_OK, data);
                        finish();
//                        LatLng latLng = new LatLng(mAddressBrief.getLatitude(), mAddressBrief.getLongitude());
//                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
//                        DialogUtil.showHint(String.format("选中坐标:%f,%f", latLng.longitude, latLng.latitude));
                    }
                } catch (Exception e) {
                    ZLogger.e(String.format("%d, detail:%s", i, e.toString()));
                }
            }
        });
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        //TODO:移动地图中心点坐标到最近一次位置
        if (!LocationClient.getLastLatitude(this).equalsIgnoreCase("0")
                && !LocationClient.getLastLongitude(this).equalsIgnoreCase("0")) {
            LatLng latLng = new LatLng(Double.valueOf(LocationClient.getLastLatitude(this)),
                    Double.valueOf(LocationClient.getLastLongitude(this)));
            aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
//        deactivate();//放在onPause里面会导致应用进入后台后，重新进入应用导致定位已经停止，不能定位。
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        deactivate();
    }


    /**
     * 初始化导航栏视图
     */
    private void initTopBar() {
        toolbar.setTitle("定位");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_share) {
                    getMapScreenShot();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_poi);
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        if (SharedPreferencesManager.getLocationAcceptEnabled()) {
            if (mAMapLocationClient == null) {
                mAMapLocationClient = new AMapLocationClient(this);
                mLocationOption = new AMapLocationClientOption();
                //设置定位监听
                mAMapLocationClient.setLocationListener(this);
                //设置为高精度定位模式
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
                mLocationOption.setInterval(LOCATE_TIMEOUT);
                //设置定位参数
                mAMapLocationClient.setLocationOption(mLocationOption);
                // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
                // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
                // 在定位结束后，在合适的生命周期调用onDestroy()方法
                // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
                mAMapLocationClient.startLocation();
            }
        } else {
            stopLocation();
            DialogUtil.showHint("请在设置中启用定位功能");
        }
    }

    /**
     * 销毁定位
     */
    private void stopLocation() {
        if (mAMapLocationClient != null) {
            mAMapLocationClient.stopLocation();
            mAMapLocationClient.onDestroy();
        }
        mAMapLocationClient = null;
    }

    /**
     * 初始化AMap对象
     */
    private void initAMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.mipmap.amap_gps_point));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);//设施定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));//默认显示地图缩放级别为16
//监听地图可视区域改变事件，移动marker
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
//                if(regeoMarker != null){
//                    regeoMarker.setPosition(cameraPosition.target);
//                }
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                jumpMarker(regeoMarker);
                mAddressBrief = null;
                headerView.setAddressBrief(null);
                headerView.setMarkerEnabled(true);
                adapter.setSelectId(-1);

                //逆地理编码，获取地图中心点地址
                LatLonPoint lanLonPoint = AMapUtil.convertToLatLonPoint(cameraPosition.target);
                // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                RegeocodeQuery query = new RegeocodeQuery(lanLonPoint, 200,
                        GeocodeSearch.AMAP);
                geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求

                //周边搜索，获取位置信息
                doSearchQuery(lanLonPoint);
            }
        });
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                addMarkersToMap();// 往地图上添加marker
            }
        });
        addMarkersToMap();// 往地图上添加marker
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap() {
        try {
            //TODO,获取地图标注点数据
            LatLng latLng = aMap.getCameraPosition().target;
            Point screenPosition = aMap.getProjection().toScreenLocation(latLng);

            regeoMarker = aMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.purple_pin)));
            //设置Marker在屏幕上,不跟随地图移动
            regeoMarker.setPositionByPixels(screenPosition.x, screenPosition.y);

//            selectedMap.clear();
//            selectedMap.put(DATA_KEY_LATITUDE, String.valueOf(regeoMarker.getPosition().latitude));
//            selectedMap.put(DATA_KEY_LONGITUDE, String.valueOf(regeoMarker.getPosition().longitude));
        } catch (Exception e) {
            ZLogger.ef(e.toString());
            e.printStackTrace();
        }

    }

    private void addMarkersToMap2() {
        try {
            //TODO,获取地图标注点数据
            LatLng latLng = aMap.getCameraPosition().target;

//        regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
//                .position(latLng));

            Point screenPosition = aMap.getProjection().toScreenLocation(latLng);
            regeoMarker = aMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.purple_pin)));
            //设置Marker在屏幕上,不跟随地图移动
            regeoMarker.setPositionByPixels(screenPosition.x, screenPosition.y);

            mAddressBrief = null;
            headerView.setAddressBrief(null);
        } catch (Exception e) {
            ZLogger.ef(e.toString());
            e.printStackTrace();
        }

    }


    /**
     * 混合定位回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        ZLogger.d("混合定位回调函数" + aLocation.toString());
        if (mListener != null && aLocation != null) {
            if (aLocation.getErrorCode() == 0) {
                this.aMapLocation = aLocation;// 判断超时机制
                mListener.onLocationChanged(aLocation);// 显示系统小蓝点
//                aMap.moveCamera(CameraUpdateFactory.zoomTo(24));
            } else {
                ZLogger.d(String.format("定位失败: %d-%s",
                        aLocation.getErrorCode(), aLocation.getErrorInfo()));
            }

            LocationClient.saveLastLocationInfo(AppContext.getAppContext(),
                    aLocation.getLatitude(), aLocation.getLongitude());
        }
    }

    @Override
    public void run() {
        if (aMapLocation == null) {
            stopLocation();// 销毁掉定位

            //定位失败，返回上一层
            DialogUtil.showHint("12秒内还没有定位成功，停止定位");
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    /**
     * 对地图进行截屏
     */
    public void getMapScreenShot() {
        DialogUtil.showHint("正在截图...");
        aMap.getMapScreenShot(this);
    }

    @Override
    public void onMapScreenShot(Bitmap bitmap) {

    }

    @Override
    public void onMapScreenShot(Bitmap bitmap, int i) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if (null == bitmap) {
            return;
        }
        //保存图片
        try {
            File destDir = new File(BizConfig.DEFAULT_SCRESHOOT_PATH);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(BizConfig.DEFAULT_SCRESHOOT_PATH + "share_"
                    + sdf.format(new Date()) + ".png");
            boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            try {
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (b) {
//                DialogUtil.showHint("截屏成功");
                shareLocation(bitmap);
            } else {
                DialogUtil.showHint("截屏失败");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        initLocation();
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        stopLocation();
    }


    /**
     * 分享位置并发送图片
     * TODO:将位置信息以水印的方式放到图片上
     */
    private void shareLocation(Bitmap bitmap) {
        //TODO:发送图片并分享位置
        DialogUtil.showHint(String.format("发送图片并分享位置\n (%f, %f), %s",
                mAddressBrief.getLatitude(), mAddressBrief.getLongitude(),
                mAddressBrief.getAddress()));

        setResult(Activity.RESULT_OK);
        finish();
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        if (rCode == 1000 && regeocodeResult != null) {

            RegeocodeQuery regeocodeQuery = regeocodeResult.getRegeocodeQuery();
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            StreetNumber streetNumber = regeocodeAddress.getStreetNumber();
            List<AoiItem> aoiItems = regeocodeAddress.getAois();
//            if (aoiItems != null && aoiItems.size() > 0){
//                for ()
//            }
            ZLogger.d("逆地理编码回调"
                    + regeocodeAddress.getAdCode() + ","
                    + regeocodeAddress.getBuilding() + ","
                    + regeocodeAddress.getCity() + ","
                    + regeocodeAddress.getCityCode() + ","
                    + regeocodeAddress.getDistrict() + ","
                    + regeocodeAddress.getFormatAddress() + ","
                    + regeocodeAddress.getNeighborhood() + ","
                    + regeocodeAddress.getProvince() + ","
                    + regeocodeAddress.getTowncode() + ","
                    + regeocodeAddress.getTownship() + ","
                    + streetNumber.getDirection() + ","
                    + streetNumber.getNumber() + ","
                    + streetNumber.getStreet() + ","
                    + streetNumber.getDistance() + ","
                    + streetNumber.getLatLonPoint().toString() + ",");
            if (mAddressBrief == null){
                mAddressBrief = new AddressBrief();
            }
            mAddressBrief.setName(regeocodeAddress.getFormatAddress());
            mAddressBrief.setAreaID(regeocodeAddress.getAdCode());
            mAddressBrief.setAddress(regeocodeAddress.getFormatAddress());
            mAddressBrief.setLongitude(streetNumber.getLatLonPoint().getLongitude());
            mAddressBrief.setLatitude(streetNumber.getLatLonPoint().getLatitude());
            headerView.setAddressBrief(mAddressBrief);
        } else {
            headerView.setAddressBrief(null);
        }
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(LatLonPoint lp) {
        mProgressBar.setVisibility(View.VISIBLE);
        adapter.setData(null);

        aMap.setOnMapClickListener(null);// 进行poi搜索时清除掉地图点击事件
        currentPage = 0;
        query = new PoiSearch.Query("", "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new PoiSearch.SearchBound(lp, 5000, true));//
            // 设置搜索区域为以lp点为圆心，其周围2000米范围
            /*
			 * List<LatLonPoint> list = new ArrayList<LatLonPoint>();
			 * list.add(lp);
			 * list.add(AMapUtil.convertToLatLonPoint(IMConstants.BEIJING));
			 * poiSearch.setBound(new SearchBound(list));// 设置多边形poi搜索范围
			 */
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        mProgressBar.setVisibility(View.GONE);
        if (rCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(query)) {// 是否是同一条
                    // poi返回的结果
//                    PoiResult poiQueryResult = poiResult;
                    // 取得第一页的poiitem数据，页数从数字0开始
                    List<PoiItem> poiQueryItems = poiResult.getPois();
                    // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
//                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
                    if (poiQueryItems != null && poiQueryItems.size() > 0) {
                        List<AddressBrief> list = new ArrayList<>();
                        for (PoiItem item : poiQueryItems) {
//                            Log.d("Nat:", item.toString());
                            AddressBrief brief = new AddressBrief();
                            brief.setName(item.getTitle());
                            brief.setAddress(item.getSnippet());
                            brief.setAreaID(item.getAdCode());
                            brief.setLatitude(item.getLatLonPoint().getLatitude());
                            brief.setLongitude(item.getLatLonPoint().getLongitude());
                            list.add(brief);
                        }
                        adapter.setData(list);
                    }
                }
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * marker点击时跳动一下
     */
    public void jumpMarker(final Marker marker) {
        if (marker == null) {
            return;
        }
        //根据屏幕距离计算需要移动的目标点
        final LatLng latLng = marker.getPosition();
        Point point = aMap.getProjection().toScreenLocation(latLng);
        point.y -= dip2px(this, 125);
        LatLng target = aMap.getProjection()
                .fromScreenLocation(point);
        //使用TranslateAnimation,填写一个需要移动的目标点
        Animation animation = new TranslateAnimation(target);
        animation.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                // 模拟重加速度的interpolator
                if (input <= 0.5) {
                    return (float) (0.5f - 2 * (0.5 - input) * (0.5 - input));
                } else {
                    return (float) (0.5f - Math.sqrt((input - 0.5f) * (1.5f - input)));
                }
            }
        });
        //整个移动所需要的时间
        animation.setDuration(600);
        //设置动画
        marker.setAnimation(animation);
        //开始动画
        marker.startAnimation();

    }

    public void jumpMarker2(final Marker marker) {
        if (marker == null) {
            return;
        }
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        final LatLng originalLatLng = marker.getPosition();
        Point startPoint = proj.toScreenLocation(originalLatLng);
        startPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * originalLatLng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * originalLatLng.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
//                aMap.invalidate();// 刷新地图
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });

    }
}
