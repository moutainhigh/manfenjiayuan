package com.mfh.enjoycity.ui;


import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.events.WxPayEvent;
import com.mfh.enjoycity.ui.fragments.CreateOrderFragment;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.base.BaseActivity;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 下单
 * @author Nat.ZZN(bingshanguxue) created on 2015-08-13
 */
public class CreateOrderActivity extends BaseActivity {
    @Bind(R.id.tool_bar)
    Toolbar toolbar;


    private BroadcastReceiver receiver;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_order);
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CreateOrderActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EventBus.getDefault().register(this);
//        registerReceiver();

        CreateOrderFragment orderFragment = new CreateOrderFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, orderFragment)
                .show(orderFragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //android.app.IntentReceiverLeaked: Activity com.mfh.enjoycity.ui.activity.UserActivity has leaked IntentReceiver com.mfh.enjoycity.ui.activity.UserActivity$3@443b09b8 that was originally registered here. Are you missing a call to unregisterReceiver()?
//        if (receiver != null) {
//            unregisterReceiver(receiver);
//        }

        EventBus.getDefault().unregister(this);
    }

//    private void registerReceiver(){
//        IntentFilter filter = new IntentFilter();//接收者只有在activity才起作用。
//        filter.addAction(Constants.BROADCAST_ACTION_WXPAY_RESP);
//        receiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                switch (action){
//                    case Constants.BROADCAST_ACTION_WXPAY_RESP:{
//                        int errCode = intent.getIntExtra(Constants.BROADCAST_KEY_WXPAY_RESP_ERRCODE, 0);
//                        String errStr = intent.getStringExtra(Constants.BROADCAST_KEY_WXPAY_RESP_ERRSTR);
//                        ZLogger.d(String.format("errCode:%d, errStr:%s", errCode, errStr));
//                    }
//                    break;
//                }
//            }
//        };
//        registerReceiver(receiver, filter);
//
//    }

    public void onEventMainThread(WxPayEvent event) {
        ZLogger.d(String.format("onEventMainThread: %d-%s", event.getErrCode(), event.getErrStr()));
        try{
//            animProgress.setVisibility(View.GONE);
//            emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);

            switch(event.getErrCode()){
                //成功，展示成功页面
                case 0:{
                    //如果支付成功则去后台查询支付结果再展示用户实际支付结果。注意一定不能以客户端
                    // 返回作为用户支付的结果，应以服务器端的接收的支付通知或查询API返回的结果为准。
//                    processOrder(EnjoycityApiProxy.WAYTYPE_WXPAY);
                }
                break;
                //错误，可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
                case -1:{
//                    notifyPayResult(-1);
                    DialogUtil.showHint(String.format("微信充值失败:code=%d, %s", event.getErrCode(), event.getErrStr()));

                }
                break;
                //用户取消，无需处理。发生场景：用户不支付了，点击取消，返回APP。
                case -2:{
//                    notifyPayResult(-2);
                    DialogUtil.showHint("取消微信充值");
                }
            }
        }
        catch (Exception e){
            ZLogger.e("parseWxpayResp failed, " + e.toString());
        }
    }

}
