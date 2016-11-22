package com.mfh.owner.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.qrcode.ScanActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.compound.LoadingTextView;
import com.mfh.owner.AppContext;
import com.mfh.owner.R;
import com.mfh.owner.bean.FunctionCell;
import com.mfh.owner.ui.CategoryTab;
import com.mfh.owner.ui.life.LifeFragmentPagerAdapter;
import com.mfh.framework.core.location.LocationClient;
import com.mfh.framework.core.location.MyLocationListener;
import com.mfh.owner.ui.shake.ShakeBeepManager;
import com.mfh.owner.ui.shake.ShakeHelper;
import com.mfh.owner.ui.shake.ShakeHistoryActivity;
import com.mfh.owner.ui.shake.ShakeHistoryEntity;
import com.mfh.owner.ui.shake.ShakeHistoryService;
import com.mfh.owner.ui.shake.ShakeListener;
import com.mfh.owner.ui.shake.ShakeResultDialog;
import com.mfh.owner.ui.shake.ShakeUtil;
import com.mfh.owner.ui.shake.WXShopDevicePage;
import com.mfh.owner.ui.web.BrowserFragment;
import com.mfh.owner.ui.web.ComnJBH5Activity;
import com.mfh.owner.utils.Constants;
import com.mfh.owner.utils.MobileURLConf;
import com.mfh.owner.utils.NetProxy;
import com.mfh.owner.utils.UIHelper;
import com.mfh.owner.view.FunctionButton;
import com.sensoro.beacon.kit.Beacon;

import java.util.Date;
import java.util.Random;

import butterknife.Bind;


/**
 * 生活 + 摇一摇
 *
 * @author zhangzn created on 2015-04-13
 * @since Framework 1.0
 */
public class LifeFragment2 extends BaseFragment{

    @Bind(R.id.button_sweep) FunctionButton btnSweep;
    @Bind(R.id.button_shake) FunctionButton btnShake;
    @Bind(R.id.tabHost) TabHost tabHost;
    @Bind(R.id.tab_viewpager) ViewPager tabViewPager;
    private LifeFragmentPagerAdapter viewPagerAdapter;

    @Bind(R.id.fragment_life) RelativeLayout rlMain;
    @Bind(R.id.top_function) LinearLayout llTopFunction;

    private boolean bShakeFragmentVisible = false;
    @Bind(R.id.life_shake) RelativeLayout rlShake;
    @Bind(R.id.shake_top) RelativeLayout rlShakeTop;
    @Bind(R.id.shake_bottom) LinearLayout rlShakeBottom;
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.ib_more) ImageButton ibMore;
    @Bind(R.id.loadingTextView)
    LoadingTextView loadingTextView;
    @Bind(R.id.iv_shake_icon) ImageView ivShakeIcon;
    private ShakeBeepManager beepManager;
    private ShakeListener shakeListener;
    private Animation shakeAnim;
    private ShakeResultDialog shakeResultDialog;

    @Bind(R.id.shake_ad_title) TextView tvAdTitle;
    @Bind(R.id.shake_ad_author) TextView tvAdAuthor;
    private final static int MAX_TAB = 3;
    private int currentTabIndex;
    private TextView[] mTabs;
    private ShakeUtil.ShakeType currentShakeType;
    private MyLocationListener locationListener;
    private BroadcastReceiver lifeReceiver;

    public LifeFragment2() {
        super();
    }

    @Override
    public int getLayoutResId() {
            return R.layout.fragment_life;
        }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        locationListener = new MyLocationListener(getContext());

        initViews();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.ACTIVITY_REQUEST_CODE_ZXING_QRCODE)
        {
            if(resultCode == Activity.RESULT_OK){
                try{
                    Bundle bundle = data.getExtras();
                    String resultText = bundle.getString("result", "");
//                Bitmap barcode =  (Bitmap)bundle.getParcelable("bitmap");//扫描截图

                    if(StringUtils.isUrl(resultText)){
                        UIHelper.showUrlOption(getActivity(), resultText);
                    }
                    else{
                        com.mfh.owner.utils.UIHelper.showCopyTextOption(getActivity(), resultText);
                    }
                }catch(Exception ex){
                    //TransactionTooLargeException
                    ZLogger.e(ex.toString());
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (lifeReceiver != null) {
            getActivity().unregisterReceiver(lifeReceiver);
        }
        setShakeEnabled(false);

        beepManager.close();
    }

    @Override
    public void onResume() {
        super.onResume();

//        ComnApplication app = (ComnApplication) getActivity().getApplication();
//        app.startSensoroService();

        registerReceiver();

        if(bShakeFragmentVisible){
            setShakeEnabled(true);
        }else{
            //todo,加载数据
            refresh();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (lifeReceiver != null) {
//            getActivity().unregisterReceiver(lifeReceiver);
        }
        setShakeEnabled(false);
    }

    /**
     * 初始化主要功能视图
     * */
    private void initViews(){
        btnSweep.setData(new FunctionCell(getString(R.string.button_scanning), R.drawable.button_scanning_normal, ""));
        btnShake.setData(new FunctionCell(getString(R.string.button_shake), R.drawable.button_shake_normal, ""));

        //设置字体颜色
        btnSweep.getTvDescription().setTextColor(Color.parseColor("#ccffffff"));
        btnShake.getTvDescription().setTextColor(Color.parseColor("#ccffffff"));

        btnSweep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //需要处理扫描结果
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_ZXING_QRCODE);
            }
        });

        btnShake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShakeFragment(true);
            }
        });

        initTabHost();

        initShakeFragment();
    }

    private void initTabHost(){
//        tabHost.setup();
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
//                if (viewPagerAdapter != null) {
//                    viewPagerAdapter.notifyDataSetChanged();
//                }

                int position = tabHost.getCurrentTab();
                tabViewPager.setCurrentItem(position, true);
            }
        });

        tabViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int nextPagePosition,
                                       float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int i) {
                tabHost.setCurrentTab(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        viewPagerAdapter = new LifeFragmentPagerAdapter(getChildFragmentManager(), tabHost, tabViewPager);
        tabViewPager.setOffscreenPageLimit(3);

        String[] title = getResources().getStringArray(
                R.array.catetory_viewpage_arrays);
        String[] urls = new String[]{MobileURLConf.URL_ME_SERVER, MobileURLConf.URL_ME_SERVER,
                MobileURLConf.URL_ME_SERVER, MobileURLConf.URL_ME_SERVER, MobileURLConf.URL_ME_SERVER};
        CategoryTab[] tabs = CategoryTab.values();
        final int size = tabs.length;
        for(int i = 0; i< size; i++){
            CategoryTab tab = tabs[i];

//            Bundle bundle1 = new Bundle();
//            bundle1.putString(BrowserFragment.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_ME_SERVER);
//            viewPagerAdapter.addTab(title[0], "news", BrowserFragment.class,
//                    bundle1);
        }
        Bundle bundle1 = new Bundle();
        bundle1.putString(BrowserFragment.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_ME_SERVER);
        viewPagerAdapter.addTab(title[0], "news", BrowserFragment.class,
                bundle1);

        Bundle bundle2 = new Bundle();
        bundle2.putString(BrowserFragment.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_ME_SERVER);
        viewPagerAdapter.addTab(title[1], "news_week", BrowserFragment.class,
                bundle2);
        Bundle bundle3 = new Bundle();
        bundle3.putString(BrowserFragment.EXTRA_KEY_REDIRECT_URL, MobileURLConf.URL_ME_SERVER);
        viewPagerAdapter.addTab(title[2], "latest_blog", BrowserFragment.class,
                bundle3);


        //TODO
        tabHost.setCurrentTab(1);
        tabViewPager.setCurrentItem(0, true);
        tabHost.setCurrentTab(0);
    }

    public void refresh(){
         if (viewPagerAdapter != null) {
             viewPagerAdapter.notifyDataSetChanged();
         }
        tabViewPager.setCurrentItem(tabHost.getCurrentTab(), true);
    }

    private void initShakeFragment(){
        ibBack.setVisibility(View.VISIBLE);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleShakeFragment(false);
            }
        });

        ibMore.setVisibility(View.VISIBLE);
        ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.mfh.framework.uikit.UIHelper.startActivity(getContext(), ShakeHistoryActivity.class);
            }
        });

        mTabs = new TextView[MAX_TAB];
        mTabs[0] = (TextView) rootView.findViewById(R.id.tab_store);
        mTabs[1] = (TextView) rootView.findViewById(R.id.tab_people);
        mTabs[2] = (TextView) rootView.findViewById(R.id.tab_redenvelope);

        for(int i = 0; i < MAX_TAB; i++){
            mTabs[i].setOnClickListener(onTabClicked);
        }

        mTabs[0].setVisibility(View.GONE);

        beepManager = new ShakeBeepManager(getActivity());
        shakeAnim = ShakeUtil.shakeRotateAnimation();
        shakeAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {
                shakeListener.stop();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doWorkOnAnimationEnd();

                if(bShakeFragmentVisible){
                    shakeListener.start();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        shakeListener = new ShakeListener(getContext());
        shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
            @Override
            public void onShake() {
                //hide previous dialog
                if(shakeResultDialog != null){
                    shakeResultDialog.hide();
                }

                beepManager.playBeepSoundAndVibrate();
//                ShakeUtil.playSoundAndVibrate(getActivity());

                ivShakeIcon.startAnimation(shakeAnim);
            }
        });

        setShakeEnabled(bShakeFragmentVisible);//默认不开启摇一摇和位置定位功能

        shakeResultDialog = new ShakeResultDialog(getContext(), R.style.dialog_shake_result);
        shakeResultDialog.setDialogListener(new ShakeResultDialog.DialogListener() {
            @Override
            public void onRedirectTo(Object data) {
                ShakeHistoryEntity entity = (ShakeHistoryEntity)data;
//                UIHelper.redirectToNativeWeb(getActivity(), entity.getPageUrl(), true);
                ComnJBH5Activity.actionStart(getActivity(), entity.getPageUrl(), true, false, -1);
            }
        });

        //默认选择第2项
        changeTab(1);
    }

    private View.OnClickListener onTabClicked = new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            int index = 0;
            switch(view.getId()){
                case R.id.tab_store:
                    index = 0;
                    break;
                case R.id.tab_people:
                    index = 1;
                    break;
                case R.id.tab_redenvelope:
                    index = 2;
                    break;
            }

            changeTab(index);
        }
    };

    private void changeTab(int index){
        for(int i = 0; i < MAX_TAB; i++){
            mTabs[i].setSelected(false);
            mTabs[i].setSelected(false);
        }
        mTabs[index].setSelected(true);
        mTabs[index].setSelected(true);
        currentTabIndex = index;
        currentShakeType = ShakeUtil.ShakeType.values()[index];
    }

    /**
     * 动画结束执行操作
     * */
    private void doWorkOnAnimationEnd(){
        if(!NetworkUtils.isConnect(getContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        loadingTextView.show("正在搜索周边信息...");

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                try{
                    if(AppContext.getInstance().existBeacons()){
                        //TODO,向服务器请求推送消息,这里暂时模拟显示搜索结果
                        //测试阶段，先模拟向服务器发送一个消息。
//                        StringBuilder sb = new StringBuilder();
//                        sb.append(String.format("查询到 %s 条信息\n", app.beacons.size()));

                        String serialNo = "";
                        double minAccurancy = 0.0f;
                        for(Beacon beacon : AppContext.getInstance().mBeasons){
//                            String id = String.format("%s-%04x-%04x", beacon.getSerialNumber(), beacon.getMajor(), beacon.getMinor());
                            //"Beacon [major=%d, minor=%d, proximityUUID=%s, serialNumber=%s, macAddress=%s, rssi=%d, batteryLevel=%d, remainingLifetime=" + this.r + ", hardwareModelName=%s, firmwareVersion=" + this.t + ", temperature=" + this.u + ", light=" + this.v + ", accelerometerCount=" + this.w + ", accuracy=" + this.x + ", proximity=" + this.y + ", measuredPower=" + this.d + ", movingState=" + this.z + ", runningAverageRssi=" + this.A + ", baseSettings=" + this.B + ", sensorSettings=" + this.C + ", secureBroadcastInterval=" + this.D + ", isIBeaconEnabled=" + this.E + ", isSecretEnabled=" + this.F + ", isPasswordEnabled=" + this.G + "]"
//                            String description = beacon.toString();

                            //get the nearest beacon
                            if(minAccurancy > beacon.getAccuracy()){
                                minAccurancy = beacon.getAccuracy();
                                serialNo = beacon.getSerialNumber();
                            }else{
                                if (serialNo == null || serialNo.isEmpty()){
                                    serialNo = beacon.getSerialNumber();
                                }
                            }
                        }

                        if(NetworkUtils.isConnect(getContext())){
                            NetProxy proxy = new NetProxy();
                            proxy.getWXShopDevicePage(serialNo, responseCallback);
                        }else{
                            DialogUtil.showHint(getContext().getString(R.string.toast_network_error));

                            Message message = new Message();
                            message.what = MSG_SHAKE_ERROR;
                            shakeHandler.sendMessage(message);
                        }
                    }else{
                        Message message = new Message();
                        message.what = MSG_SHAKE_NONE;
                        shakeHandler.sendMessage(message);
                    }
                }catch(Exception e){
                    ZLogger.e(e.toString());
                    Message message = new Message();
                    message.what = MSG_SHAKE_NONE;
                    shakeHandler.sendMessage(message);
                }
            }
        }, 300);
    }

    //回调
    PageInfo pageInfo = new PageInfo(1, 100);
    //回调
    NetCallBack.QueryRsCallBack responseCallback =new NetCallBack.QueryRsCallBack<>(
            new NetProcessor.QueryRsProcessor<WXShopDevicePage>(pageInfo) {
//                处理查询结果集，子类必须继承
                @Override
                public void processQueryResult(RspQueryResult<WXShopDevicePage> rs) {//此处在主线程中执行。
                    saveQueryResult(rs);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    ZLogger.d("processFailure: " + errMsg);
                    super.processFailure(t, errMsg);
                    Message message = new Message();
                    message.what =MSG_SHAKE_ERROR;
                    shakeHandler.sendMessage(message);
                }
            }
            , WXShopDevicePage.class
            , MfhApplication.getAppContext());

    /**
     * 将后台返回的结果集保存到本地,同步执行
     * @param rs 结果集
     */
    private void saveQueryResult(RspQueryResult<WXShopDevicePage> rs) {
        try {
            //保存下来
            int retSize = rs.getReturnNum();
//            Log.d("Nat: saveQueryResult", String.format("%d result, content:%s", retSize, rs.toString()));

            if(retSize < 1){
                //TODO,未搜索到结果
                Message message = new Message();
                message.what = MSG_SHAKE_NONE;
                shakeHandler.sendMessage(message);
                return;
            }

            ShakeHistoryService historyService = ServiceFactory.getService(ShakeHistoryService.class.getName());
            ShakeHistoryEntity entity = new ShakeHistoryEntity();

            WXShopDevicePage devicePage;

            int randomIndex = new Random().nextInt(retSize);
//            for (int i = 0; i < retSize; i++) {
                devicePage = rs.getRowEntity(randomIndex);
                if (devicePage == null) {
//                    continue;
                    return;
                }

                entity = new ShakeHistoryEntity();

                //shake_history.id may not be NULL (code 19)
                entity.setId(String.valueOf(devicePage.getDeviceId()));
                entity.setCreatedDate(new Date());
                entity.setGuid(String.valueOf(MfhLoginService.get().getCurrentGuId()));

                entity.setDeviceId(devicePage.getDeviceId());
                entity.setIconUrl(devicePage.getIconUrl());
                entity.setTitle(devicePage.getTitle());
                entity.setDescription(devicePage.getDescription());
                entity.setPageUrl(devicePage.getPageUrl());
                entity.setPageId(devicePage.getPageId());
                entity.setRemark(devicePage.getRemark());

                //保存或更新
                historyService.saveOrUpdate(entity);

                //TODO,保存成功，显示搜索结果
                Message message = new Message();
                message.what = MSG_SHAKE_SUCCESS;
                message.obj = entity;
                shakeHandler.sendMessage(message);

//                break;//只保存一条记录
//            }
        }
        catch(Throwable ex){
            ZLogger.e(ex.toString());
            Message message = new Message();
            message.what = MSG_SHAKE_ERROR;
            shakeHandler.sendMessage(message);
//            throw new RuntimeException(ex);
        }
    }

    private final static int MSG_SHAKE_NONE = 0;
    private final static int MSG_SHAKE_ERROR = 1;
    private final static int MSG_SHAKE_SUCCESS = 2;
    private Handler shakeHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHAKE_NONE:
                    DialogUtil.showHint("无结果，请重新再试一次");
                    loadingTextView.hide();
                    break;
                case MSG_SHAKE_ERROR:
                    loadingTextView.hide();
                    break;
                case MSG_SHAKE_SUCCESS:
                    loadingTextView.hide();
                    //避免摇一摇后返回上一页面后仍弹出对话框。
                    if(bShakeFragmentVisible){
                        ShakeHistoryEntity entity = (ShakeHistoryEntity)msg.obj;
                        if(shakeResultDialog != null){
                            shakeResultDialog.hide();
                            shakeResultDialog.show(entity);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void toggleShakeFragment(final boolean enabled){
        final Animation anim1,anim2,anim3,anim4,anim5;

        setShakeEnabled(enabled);

        //进入摇一摇
        if(enabled){
            anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.shake_top_out);
            anim2 = AnimationUtils.loadAnimation(getContext(), R.anim.shake_bottom_out);
            anim3 = AnimationUtils.loadAnimation(getContext(), R.anim.shake_top_in);
//            anim4 = AnimationUtils.loadAnimation(getContext(), R.anim.shake_bottom_in);
            anim4 = AnimationUtils.loadAnimation(getContext(), R.anim.push_left_in);
            anim5 = AnimationUtils.loadAnimation(getContext(), R.anim.push_right_in);

            anim1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //显示摇一摇页面(开始)
                    rlShake.setVisibility(View.VISIBLE);
                    rlShakeTop.setVisibility(View.GONE);
                    rlShakeBottom.setVisibility(View.GONE);
                    //更新摇一摇内容
                    tvAdTitle.setText(ShakeHelper.getInstance().getAdTitle());
                    tvAdAuthor.setText(ShakeHelper.getInstance().getAdAuthor());
//                    ibBack.setVisibility(View.GONE);
//                    ibMore.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //隐藏生活首页（结束）
                    rlMain.setVisibility(View.GONE);

                    bShakeFragmentVisible = true;
                    com.mfh.owner.utils.UIHelper.sendToggleTabbarBroadcast(getContext(), !bShakeFragmentVisible);//隐藏首页tab

                    //twice, to ensure shake service open/close correctly.
                    setShakeEnabled(bShakeFragmentVisible);

                    rlShakeTop.startAnimation(anim3);
//                    rlShakeBottom.startAnimation(anim3);
//                    ibBack.startAnimation(anim4);
//                    ibMore.startAnimation(anim5);
//                    rlShake.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            anim3.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    rlShakeTop.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rlShakeBottom.setVisibility(View.VISIBLE);
                    ibBack.startAnimation(anim4);
                    ibMore.startAnimation(anim5);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
//            anim4.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    ibBack.setVisibility(View.VISIBLE);
//                    ibMore.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });

            llTopFunction.startAnimation(anim1);
            tabHost.startAnimation(anim2);

            //TODO,监听蓝牙开关状态启动服务
            AppContext app = (AppContext) getActivity().getApplication();
            app.startSensoroService();
        }else{
            anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.shake_top_in);
            anim2 = AnimationUtils.loadAnimation(getContext(), R.anim.shake_bottom_in);
            anim3 = AnimationUtils.loadAnimation(getContext(), R.anim.shake_bottom_out);
            anim4 = AnimationUtils.loadAnimation(getContext(), R.anim.push_left_out);
            anim5 = AnimationUtils.loadAnimation(getContext(), R.anim.push_right_out);

//            anim3.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//                    rlMain.setVisibility(View.VISIBLE);
//                    isTop = true;
//                    if(listener != null){
//                        listener.onStackChanged();
//                    }
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
////                    ibBack.startAnimation(anim4);
////                    ibMore.startAnimation(anim5);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//
//                }
//            });
            anim4.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
//                    setShakeEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
//                    ibBack.setVisibility(View.GONE);
//                    ibMore.setVisibility(View.GONE);
//                    rlShakeTop.setVisibility(View.GONE);
                    rlShake.setVisibility(View.GONE);

                    bShakeFragmentVisible = false;

                    rlMain.setVisibility(View.VISIBLE);
                    llTopFunction.startAnimation(anim1);
                    tabHost.startAnimation(anim2);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            anim1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //twice, to ensure shake service open/close correctly.
                    setShakeEnabled(bShakeFragmentVisible);

                    com.mfh.owner.utils.UIHelper.sendToggleTabbarBroadcast(getContext(), !bShakeFragmentVisible);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

//            rlShakeBottom.startAnimation(anim3);
            ibBack.startAnimation(anim4);
            ibMore.startAnimation(anim5);
        }
    }

    /**
     * 设置是否监听位置信息
     * */
    private void setShakeEnabled(boolean enabled){
        if(enabled){
            if(SharedPrefesManagerFactory.getLocationAcceptEnabled()){
                LocationClient.startGPSMonitor(getContext(), locationListener);
            }

            if(shakeListener != null){
                shakeListener.start();
            }
        }
        else{
            LocationClient.stopGPSMonitor(getContext(), locationListener);

            if(shakeListener != null){
                shakeListener.stop();
            }
        }
    }

    public boolean isShakeFragmentVisible() {
        return bShakeFragmentVisible;
    }

    /**
     * 注册监听器
     */
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_BEACONS_UPDATE);
        lifeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
//                MLog.d("LifeFragment.onReceive.action" + intent.getAction());
                if (intent.getAction().equals(Constants.ACTION_BEACONS_UPDATE)) {
                    Bundle bundle = intent.getExtras();
                    if(bundle != null){
                        toggleStoreItem(bundle.getBoolean(Constants.KEY_BEACONS_EXIST, false));
                    }else{
                        toggleStoreItem(false);
                    }
                }
            }
        };
        //使用LocalBroadcastManager.getInstance(getActivity())反而不行，接收不到。
        getActivity().registerReceiver(lifeReceiver, intentFilter);
    }

    /**
     * 设置摇一摇·店铺(周边，基于云子)
     * */
    private void toggleStoreItem(boolean enabled){
        if(enabled){
//            Animation anim = ShakeUtil.getAlphaAnimation(0, 1, 200);
            Animation anim = ShakeUtil.clickAnimation(0.5f, 200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
//            mTabs[0].startAnimation(anim);
            mTabs[0].setVisibility(View.VISIBLE);
        }else{
            mTabs[0].setVisibility(View.GONE);

            changeTab(1);
        }
    }

}
