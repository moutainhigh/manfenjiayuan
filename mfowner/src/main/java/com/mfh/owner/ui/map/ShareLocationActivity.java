package com.mfh.owner.ui.map;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.Projection;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.location.LocationClient;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.widget.LoadingImageView;
import com.mfh.owner.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;


/**
 * 位置分享
 * */
public class ShareLocationActivity extends BaseActivity
        implements AMapLocationListener, AMap.OnMapScreenShotListener, Runnable ,
        LocationSource, GeocodeSearch.OnGeocodeSearchListener , PoiSearch.OnPoiSearchListener {
    public static final String DATA_KEY_LATITUDE = "DATA_KEY_LATITUDE";
    public static final String DATA_KEY_LONGITUDE = "DATA_KEY_LONGITUDE";
    public static final String DATA_KEY_NAME = "DATA_KEY_NAME";
    public static final String DATA_KEY_ADDRESS = "DATA_KEY_ADDRESS";
    public static final long LOCATE_TIMEOUT = 12 * 1000;

    @Bind(R.id.topbar_title) TextView tvTopBarTitle;
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.btnMore) Button btnMore;

    private LocationManagerProxy aMapLocManager = null;
    private AMapLocation aMapLocation;// 用于判断定位超时
    private Handler handler = new Handler();

    @Bind(R.id.map_view) MapView mapView;
    private AMap aMap;
    private LocationSource.OnLocationChangedListener mListener;

    private Marker regeoMarker;//地图中心Marker
    private GeocodeSearch geocoderSearch;


    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch poiSearch;
    private PoiSearch.Query query;// Poi查询条件
    private PoiResult poiQueryResult; // poi返回的结果
    private List<PoiItem> poiQueryItems;// poi数据

    @Bind(R.id.location_list) ListView listView;
    private LocationHeaderView headerView;
    private LocationAdapter adapter;
    private Map<String, String> selectedMap = new HashMap<>();

    @Bind(R.id.loadingImageView)
    LoadingImageView loadingImageView;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_share_location;
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
        headerView.appendData(DATA_KEY_LATITUDE, String.valueOf(regeoMarker.getPosition().latitude));
        headerView.appendData(DATA_KEY_LONGITUDE, String.valueOf(regeoMarker.getPosition().longitude));
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMap = headerView.getData();
                headerView.setMarkerEnabled(true);
                adapter.setSelectId(-1);

                LatLng latLng = new LatLng(Double.valueOf(selectedMap.get(DATA_KEY_LATITUDE)),
                        Double.valueOf(selectedMap.get(DATA_KEY_LONGITUDE)));
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
            }
        });

        listView.addHeaderView(headerView, null, true);

        adapter = new LocationAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try{
                    //标记选中项
                    adapter.setSelectId(i - listView.getHeaderViewsCount());
                    headerView.setMarkerEnabled(false);

                    //listview 添加了headerview or footview后导致position不正确。使用adapterView.getAdapter()代替我们自己的adapter.
                    Map<String, String> entity = (Map<String, String>) adapterView.getAdapter().getItem(i);
                    if(selectedMap == null){
                        selectedMap = new HashMap<>();
                    }
                    selectedMap.clear();
                    selectedMap.put(DATA_KEY_LATITUDE, entity.get(DATA_KEY_LATITUDE));
                    selectedMap.put(DATA_KEY_LONGITUDE, entity.get(DATA_KEY_LONGITUDE));
                    selectedMap.put(DATA_KEY_ADDRESS, entity.get(DATA_KEY_ADDRESS));

                    LatLng latLng = new LatLng(Double.valueOf(entity.get(DATA_KEY_LATITUDE)),
                            Double.valueOf(entity.get(DATA_KEY_LONGITUDE)));
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                }
                catch (Exception e){
                    ZLogger.e(String.format("%d, detail:%s", i, e.toString()));
                }
            }
        });

        loadingImageView.setBackgroundResource(R.drawable.loading_anim);

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        //TODO:移动地图中心点坐标到最近一次位置
        if(!LocationClient.getLastLatitude(this).equalsIgnoreCase("0")
                && !LocationClient.getLastLongitude(this).equalsIgnoreCase("0")){
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
        mapView.onDestroy();
        deactivate();
    }


    /**
     * 初始化导航栏视图
     * */
    private void initTopBar(){
        tvTopBarTitle.setText(R.string.topbar_title_share_location);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnMore.setText(R.string.button_send);
        btnMore.setVisibility(View.VISIBLE);
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMapScreenShot();
            }
        });
    }

    /**
     * 初始化定位
     * */
    private void initLocation(){
        if(SharedPreferencesManager.getLocationAcceptEnabled()){
            aMapLocManager = LocationManagerProxy.getInstance(this);
            /*
             * mAMapLocManager.setGpsEnable(false);//
             * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
             * API定位采用GPS和网络混合定位方式，第一个参数是定位provider，第二个参数时间最短是2000毫秒，
             * 第三个参数距离间隔单位是米，第四个参数是定位监听者
             */
            aMapLocManager.requestLocationData(
                    LocationProviderProxy.AMapNetwork, 2 * 1000, 10, this);
        }else{
            stopLocation();
            DialogUtil.showHint("请在设置中启用定位功能");
        }
    }

    /**
     * 销毁定位
     */
    private void stopLocation() {
        if (aMapLocManager != null) {
            aMapLocManager.removeUpdates(this);
            aMapLocManager.destroy();
        }
        aMapLocManager = null;
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
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
//        aMap.setMyLocationStyle(myLocationStyle);

        aMap.setLocationSource(this);//设施定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));//默认显示地图缩放级别为16

        addMarkersToMap();// 往地图上添加marker
        //监听地图可视区域改变事件，移动marker
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(regeoMarker != null){
                    regeoMarker.setPosition(cameraPosition.target);
                }
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                jumpMarker(regeoMarker);

                selectedMap.clear();
                selectedMap.put(DATA_KEY_LATITUDE, String.valueOf(cameraPosition.target.latitude));
                selectedMap.put(DATA_KEY_LONGITUDE, String.valueOf(cameraPosition.target.longitude));

                headerView.appendData(DATA_KEY_LATITUDE, String.valueOf(cameraPosition.target.latitude));
                headerView.appendData(DATA_KEY_LONGITUDE, String.valueOf(cameraPosition.target.longitude));

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
    }
    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(){
        //TODO,获取地图标注点数据
        regeoMarker = aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
                .position(aMap.getCameraPosition().target));

        selectedMap.clear();
        selectedMap.put(DATA_KEY_LATITUDE, String.valueOf(regeoMarker.getPosition().latitude));
        selectedMap.put(DATA_KEY_LONGITUDE, String.valueOf(regeoMarker.getPosition().longitude));
    }

    /**
     * 此方法已经废弃
     */
    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * 混合定位回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aLocation) {
        if (aLocation != null && aLocation.getAMapException().getErrorCode() == 0) {
            this.aMapLocation = aLocation;// 判断超时机制

            //显示系统小蓝点
            if (mListener != null) {
                mListener.onLocationChanged(aLocation);// 显示系统小蓝点
            }
            ZLogger.d("onLocationChanged: " + aLocation.toString());
            LocationClient.saveLastLocationInfo(ShareLocationActivity.this, aLocation.getLatitude(), aLocation.getLongitude());
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
        aMap.invalidate();// 刷新地图
    }

    @Override
    public void onMapScreenShot(Bitmap bitmap) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if(null == bitmap){
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

            if (b){
//                DialogUtil.showHint("截屏成功");
                shareLocation(bitmap);
            }
            else {
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
     * */
    private void shareLocation(Bitmap bitmap){
        //TODO:发送图片并分享位置
        DialogUtil.showHint(String.format("发送图片并分享位置\n (%s, %s), %s",
                selectedMap.get(DATA_KEY_LATITUDE), selectedMap.get(DATA_KEY_LONGITUDE),
                selectedMap.get(DATA_KEY_ADDRESS)));

        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        if (rCode == 1000) {
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                if(headerView != null){
                    headerView.appendData(DATA_KEY_ADDRESS,
                            regeocodeResult.getRegeocodeAddress().getFormatAddress());
//                            + "附近");
                    selectedMap.put(DATA_KEY_ADDRESS,
                            regeocodeResult.getRegeocodeAddress().getFormatAddress());
                }
            } else {
                if(headerView != null){
                    headerView.appendData(DATA_KEY_ADDRESS, "对不起，没有搜索到相关数据！");
                }
            }
        } else {
            if(headerView != null){
                headerView.appendData(DATA_KEY_ADDRESS, "对不起，没有搜索到相关数据！");
            }
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(LatLonPoint lp) {
        loadingImageView.toggle(true);
        adapter.setData(new ArrayList<Map<String, String>>());

        aMap.setOnMapClickListener(null);// 进行poi搜索时清除掉地图点击事件
        currentPage = 0;
        query = new PoiSearch.Query("", "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        query.setLimitDiscount(false);
        query.setLimitGroupbuy(false);

        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new PoiSearch.SearchBound(lp, 2000, true));//
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
        loadingImageView.toggle(false);
        if (rCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(query)) {// 是否是同一条
                    poiQueryResult = poiResult;
                    // 取得第一页的poiitem数据，页数从数字0开始
                    poiQueryItems = poiResult.getPois();
                    // 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
//                    List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
                    if (poiQueryItems != null && poiQueryItems.size() > 0) {
                        List<Map<String, String>> list = new ArrayList<>();
                        for(PoiItem item : poiQueryItems){
//                            Log.d("Nat:", item.toString());
                            Map<String, String> map = new HashMap<>();
                            map.put(DATA_KEY_NAME, item.getTitle());
                            map.put(DATA_KEY_ADDRESS, item.getSnippet());
                            map.put(DATA_KEY_LATITUDE, String.valueOf(item.getLatLonPoint().getLatitude()));
                            map.put(DATA_KEY_LONGITUDE, String.valueOf(item.getLatLonPoint().getLongitude()));
                            list.add(map);
                        }
                        adapter.setData(list);
                    }
                }
            }
        }
    }

    @Override
    public void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int rCode) {
        //TODO,
    }

    /**
     * marker点击时跳动一下
     */
    public void jumpMarker(final Marker marker) {
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
                aMap.invalidate();// 刷新地图
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });

    }
}
