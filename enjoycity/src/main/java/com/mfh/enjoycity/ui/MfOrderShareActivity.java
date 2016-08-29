package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.utils.EnjoycityApi;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.wxapi.WXHelper;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.network.URLHelper;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;


import butterknife.Bind;
import butterknife.OnClick;


/**
 * 分享订单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class MfOrderShareActivity extends BaseActivity {
    private static final String TAG = MfOrderShareActivity.class.getSimpleName();
    public static final String EXTRA_KEY_ORDER_IDS = "orderIds";

    @Bind(R.id.tv_amount)
    TextView tvAmount;
    @Bind(R.id.tv_order_radio)
    TextView tvOrderRadio;
    @Bind(R.id.ll_share)
    View shareView;
    @Bind(R.id.button_wx_friend)
    Button btnWxFriend;
    @Bind(R.id.button_wx_circle)
    Button btnWxCircle;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;

    private String orderIds;//订单编号
    private String shareLink;//分享链接


    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, MfOrderShareActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_mf_order_share;
    }

    @Override
    protected void initToolBar() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            // Translucent status bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //注释该行，解决底部导航Tab在5.1.1 Nexus手机上和底部状态栏重叠问题。
            // Translucent navigation bar
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

//        EventBus.getDefault().register(this);

        if (StringUtils.isEmpty(orderIds)){
            setResult(RESULT_CANCELED);
            DialogUtil.showHint("订单号不能为空");
            finish();
        }

        //加载优惠券信息
        load(EnjoycityApi.BTYPE_STORE, orderIds);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
//        final MenuItem settings = menu.findItem(R.id.action_settings);
//        MenuItemCompat.setActionView(settings, R.layout.view_corner_button);
//        final Button btnSettings = (Button) settings.getActionView().findViewById(R.id.corner_button);
//        btnSettings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public voiredid onClick(View v) {
//                UIHelper.redirectToActivity(UserActivity.this, SettingsActivity.class);
//            }
//        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    /**
     * */
    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, -1);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if(animType == ANIM_TYPE_NEW_FLOW){
                this.setTheme(R.style.AppTheme_NewFlow);
            }

            orderIds = intent.getStringExtra(EXTRA_KEY_ORDER_IDS);
        }
    }

    @OnClick(R.id.button_close)
    public void close(){
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.button_wx_friend)
    public void shareToWxFriend(){
        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        WXHelper.getInstance(MfOrderShareActivity.this)
                .sendWebpageToWX(shareLink, "快来抢红包，下单更优惠~",
                        "超市代买 1小时速达 超市｜菜场｜水果店任性买！", thumb,
                        SendMessageToWX.Req.WXSceneTimeline);

        finish();
    }

    @OnClick(R.id.button_wx_circle)
    public void shareToWxCircle(){
        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        WXHelper.getInstance(MfOrderShareActivity.this)
                .sendWebpageToWX(shareLink, "快来抢红包，下单更优惠~",
                        "超市代买 1小时速达 超市｜菜场｜水果店任性买！", thumb,
                        SendMessageToWX.Req.WXSceneSession);
        finish();
    }

    /**
     * 预支付订单
     * @param orderIds 订单id,多个以英文,隔开(必填)
     * @param btype 业务类型, 3-商城(必填)
     * */
    private void load(final int btype, final String orderIds){
        animProgress.setVisibility(View.VISIBLE);
        btnWxCircle.setEnabled(false);
        btnWxFriend.setEnabled(false);

        if(!NetWorkUtil.isConnect(this)){
            animProgress.setVisibility(View.GONE);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        //回调
        NetCallBack.QueryRsCallBack queryResponseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<Long>(new PageInfo(1, 100)) {
                    // 处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<Long> rs) {//此处在主线程中执行。
                        try {
                            int retSize = rs.getReturnNum();
                            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

//                            List<Long> result = new ArrayList<>();
                            if(retSize > 0){
//                                for (int i = 0; i < retSize; i++) {
//                                    result.add(rs.getRowEntity(i));
//                                }

                                shareLink = URLHelper.append(H5Api.URL_MARKET_COUPON,
                                        String.format("id=%d&shareid=%d", rs.getRowEntity(0),
                                                MfhLoginService.get().getCurrentGuId()));
                                shareView.setVisibility(View.VISIBLE);
                                btnWxCircle.setEnabled(true);
                                btnWxFriend.setEnabled(true);

                            }else{
                                DialogUtil.showHint("加载红包失败");
                            }

                            animProgress.setVisibility(View.GONE);
                        }
                        catch(Throwable ex){
                            ZLogger.e(ex.toString());
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                        animProgress.setVisibility(View.GONE);
                    }
                }
                , Long.class
                , MfhApplication.getAppContext());

        EnjoycityApiProxy.findCoupons(orderIds, btype, queryResponseCallback);
    }

}
