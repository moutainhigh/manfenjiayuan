package com.mfh.owner.ui.shake;


import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.param.TextParam;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetProcessor;
import com.mfh.owner.AppContext;
import com.mfh.owner.R;
import com.mfh.owner.ui.web.ComnJBH5Activity;
import com.mfh.owner.ui.map.LocationUtil;
import com.mfh.owner.ui.map.MyLocationListener;
import com.sensoro.beacon.kit.Beacon;

import butterknife.Bind;


/**
 * 摇一摇
 * */
public class ShakeActivity extends BaseActivity {
    @Bind(R.id.ib_back) ImageButton ibBack;
    @Bind(R.id.ib_more) ImageButton ibMore;

    private ImageView ivShakeIcon;

    private Vibrator mVibrator;
    private ShakeListener shakeListener;

    private ShakeResultDialog shakeResultDialog;

    private final static int MAX_TAB = 3;
    private int currentTabIndex;
    private LinearLayout[] mTabs;
    private ImageView[] ivTabIcons;
    private TextView[] tvTabTitles;


    private ShakeUtil.ShakeType currentShakeType;
    MyLocationListener locationListener = new MyLocationListener(ShakeActivity.this);


    @Override
    public int getLayoutResId() {
        return R.layout.activity_shake;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        initTopBar();
        initBottomTabBar();

        toggleStore(false);

        ivShakeIcon = (ImageView) findViewById(R.id.iv_shake_icon);

        mVibrator = (Vibrator) getApplication().getSystemService(
                VIBRATOR_SERVICE);
        shakeListener = new ShakeListener(this);
        shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
            @Override
            public void onShake() {
                //hide previous dialog
                if(shakeResultDialog != null){
                    shakeResultDialog.hide();
                }

//                llLoading.setVisibility(View.VISIBLE);

                startVibrato();
//                startAnimation();

//                Animation anim = ShakeUtil.shakeAnimation(ivShakeIcon.getLeft());
                Animation anim = ShakeUtil.shakeRotateAnimation();
                anim.setAnimationListener(new Animation.AnimationListener(){
                    @Override
                    public void onAnimationStart(Animation animation) {
                        shakeListener.stop();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        doWorkOnAnimationEnd();
                        shakeListener.start();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                ivShakeIcon.startAnimation(anim);

            }

        });
        // 检查设备是否有震动装置
        // mVibrator.hasVibrator();

        shakeResultDialog = new ShakeResultDialog(this, R.style.dialog_shake_result);
        shakeResultDialog.setDialogListener(new ShakeResultDialog.DialogListener() {
            @Override
            public void onRedirectTo(Object data) {
                ShakeHistoryEntity entity = (ShakeHistoryEntity)data;
//                UIHelper.redirectToNativeWeb(ShakeActivity.this, entity.getPageUrl(), true);
                ComnJBH5Activity.actionStart(ShakeActivity.this, entity.getPageUrl(), true, false, -1);
            }
        });

        changeTab(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationUtil.stopGPSMonitor(this, locationListener);

        if(shakeListener != null){
            shakeListener.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationUtil.startGPSMonitor(this, locationListener);

        if(shakeListener != null){
            shakeListener.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationUtil.stopGPSMonitor(this, locationListener);

        if(shakeListener != null){
            shakeListener.stop();
        }
    }

    /**
     * 初始化导航栏
     * */
    private void initTopBar(){
//        ibBack = (ImageButton) findViewById(R.id.ib_back);
        ibBack.setImageResource(R.drawable.navi_shake_back_normal);
        ibBack.setVisibility(View.VISIBLE);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        ibMore = (ImageButton) findViewById(R.id.ib_more);
        ibMore.setImageResource(R.drawable.navi_shake_more_normal);
        ibMore.setVisibility(View.VISIBLE);
        ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.mfh.framework.uikit.UIHelper.startActivity(ShakeActivity.this, ShakeHistoryActivity.class);
            }
        });
    }

    /**
     * 初始化底部视图
     * */
    private void initBottomTabBar(){
        mTabs = new LinearLayout[MAX_TAB];
        mTabs[0] = (LinearLayout) findViewById(R.id.ll_tab_store);
        mTabs[1] = (LinearLayout) findViewById(R.id.ll_tab_people);
        mTabs[2] = (LinearLayout) findViewById(R.id.ll_tab_redenvelope);

        ivTabIcons = new ImageView[MAX_TAB];
        ivTabIcons[0] = (ImageView) findViewById(R.id.img_store);
        ivTabIcons[1] = (ImageView) findViewById(R.id.img_people);
        ivTabIcons[2] = (ImageView) findViewById(R.id.img_redenvelope);

        tvTabTitles = new TextView[MAX_TAB];
        tvTabTitles[0] = (TextView) findViewById(R.id.tv_store);
        tvTabTitles[1] = (TextView) findViewById(R.id.tv_people);
        tvTabTitles[2] = (TextView) findViewById(R.id.tv_redenvelope);

        for(int i = 0; i < MAX_TAB; i++){
            mTabs[i].setOnClickListener(onTabClicked);
        }
    }

    private View.OnClickListener onTabClicked = new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            int index = 0;
            switch(view.getId()){
                case R.id.ll_tab_store:
                    index = 0;
                    break;
                case R.id.ll_tab_people:
                    index = 1;
                    break;
                case R.id.ll_tab_redenvelope:
                    index = 2;
                    break;
            }

            //TODO
            if(mTabs[0].getVisibility() == View.VISIBLE){
                toggleStore(false);
            }
            else{
                toggleStore(true);
            }

            final int newIndex = index;
//            Animation anim = ShakeUtil.jumpAnimation(view.getTop());
            Animation anim = ShakeUtil.clickAnimation(1.6f, 200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
//                    changeTab(newIndex);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
//            view.startAnimation(anim);

            changeTab(index);
        }
    };

    private void changeTab(int index){
        for(int i = 0; i < MAX_TAB; i++){
            ivTabIcons[i].setSelected(false);
            tvTabTitles[i].setSelected(false);
        }
        ivTabIcons[index].setSelected(true);
        tvTabTitles[index].setSelected(true);
        currentTabIndex = index;
        currentShakeType = ShakeUtil.ShakeType.values()[index];
    }

    /**
     * 播放声音和震动
     * */
    private void startVibrato(){
        ShakeUtil.playSound(this);

        //第一个｛｝里面是节奏数组， 第二个参数是重复次数，-1为不重复
        mVibrator.vibrate( new long[]{500,200,500,200}, -1);
//                mVibrator.vibrate(500);
    }



    /**
     * 动画结束执行操作
     * */
    private void doWorkOnAnimationEnd(){

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                AppContext app = (AppContext) getApplication();
                if (app.mBeasons == null || app.mBeasons.size() < 1){
                    DialogUtil.showHint("无结果，请重新再试一次");
                    return;
                }

                //TODO,向服务器请求推送消息,这里暂时模拟显示搜索结果
                //测试阶段，先模拟向服务器发送一个消息。
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("查询到 %s 条信息\n", app.mBeasons.size()));
                int i = 0;

                for(Beacon beacon : app.mBeasons){
                    sb.append(String.format("%d  %s\n", i++, app.getKey(beacon)));
                    String id = String.format("%s-%04x-%04x", beacon.getSerialNumber(), beacon.getMajor(), beacon.getMinor());
                    //"Beacon [major=%d, minor=%d, proximityUUID=%s, serialNumber=%s, macAddress=%s, rssi=%d, batteryLevel=%d, remainingLifetime=" + this.r + ", hardwareModelName=%s, firmwareVersion=" + this.t + ", temperature=" + this.u + ", light=" + this.v + ", accelerometerCount=" + this.w + ", accuracy=" + this.x + ", proximity=" + this.y + ", measuredPower=" + this.d + ", movingState=" + this.z + ", runningAverageRssi=" + this.A + ", baseSettings=" + this.B + ", sensorSettings=" + this.C + ", secureBroadcastInterval=" + this.D + ", isIBeaconEnabled=" + this.E + ", isSecretEnabled=" + this.F + ", isPasswordEnabled=" + this.G + "]"
                    String description = beacon.toString();

                    TextParam textParam = new TextParam(description);

                    NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>(){
                        @Override
                        protected void processOperResult(EmbMsg result){
                            //TODO,显示搜索结果，这里返回的有可能一个也有可能是多个。
//                                showShakeResult(null, "", "");
                        }
                    };
                    //TODO,发送网络请求，获取推送消息
//                        msgService.sendMessage(messageFragment.getSessionId(), wxParam, this, processor);

//                        ShakeHistoryService historyService = ServiceFactory.getService(ShakeHistoryService.class.getName());
//                        ShakeHistoryEntity entity = new ShakeHistoryEntity();
//                        //shake_history.id may not be NULL (code 19)
//                        entity.setId(NetProxy.getRandomString(12));
//                        entity.setGuid(getSharedPreferences("login", Activity.MODE_PRIVATE).getString("app.user.guid", null));
//                        entity.setHeaderUrl("");
//                        entity.setTitle(id + "--" + new Random().nextInt(100));
//                        entity.setDescription(description + "--" + NetProxy.getRandomString(100));
//                        entity.setRedirectUrl(String.format("%s?shelfid=%d&shopid=%d", "http://devmobile.manfenjiayuan.com/m/market/shop.html", 25, 114991));
//                        entity.setCreatedDate(new Date());
//
//                        historyService.save(entity);

                    //TODO,这里暂时直接显示模拟数据
                    if(shakeResultDialog != null){
                        shakeResultDialog.hide();
//                            shakeResultDialog.show(entity);
                    }
                    break;
                }
                //这里暂时模拟显示设备信息，正式版本应该显示从后台获取到的推送信息。
            }
        }, 600);

    }


    private void toggleStore(boolean enabled){
        if(enabled){
            Animation anim = ShakeUtil.getAlphaAnimation(0, 1, 200);
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
        }
    }


//    /**
//     * 显示摇一摇结果
//     * */
//    private void showShakeResult(String imagePath, String name, String description){
//        try {
//            View view = View.inflate(this, R.layout.shake_result, null);
//            ((ImageView)view.findViewById(R.id.iv_bitmap)).setImageResource(R.drawable.material_complaint);
//            ((TextView)view.findViewById(R.id.tv_name)).setText(name);//显示扫描到的内容
//            ((TextView)view.findViewById(R.id.tv_description)).setText(description);//显示扫描到的内容
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //TODO
//                    DialogUtil.showHint("TODO: 进入店铺详情");
//                }
//            });
//
//            new AlertDialog.Builder(this)
////                    .setTitle(this.getString(R.string.dialog_title_scanning_result))
//                    .setView(view)
////                    .setPositiveButton(this.getString(com.mfh.comna.R.string.ok), new DialogInterface.OnClickListener() {
////                        @Override
////                        public void onClick(DialogInterface dialog, int which) {
////                            //TODO,点击链接跳转页面
////                        }
////                    })
//                    .setCancelable(true)
//                    .show();
//        }
//        catch (Throwable ex) {
//            Log.d("Nat: QRCode Scanning result", ex.toString());
//        }
//    }

}
