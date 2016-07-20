package com.manfenjiayuan.cashierdisplay.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import com.manfenjiayuan.cashierdisplay.CashierOrderEvent;
import com.manfenjiayuan.cashierdisplay.R;
import com.manfenjiayuan.cashierdisplay.bean.CashierOrderInfoWrapper;
import com.manfenjiayuan.cashierdisplay.ui.dialog.CouponQRDialog;
import com.manfenjiayuan.cashierdisplay.ui.dialog.PhoneInputDialog;
import com.manfenjiayuan.cashierdisplay.ui.fragment.AdvFragment;
import com.manfenjiayuan.cashierdisplay.ui.fragment.OrderFragment;
import com.manfenjiayuan.cashierdisplay.ui.fragment.PayFragment;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.im.param.TextParam;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetProcessor;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {

//    @Bind(R.id.toolbar)
//    Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, MainActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
//        setSupportActionBar(toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        View decorView = getWindow().getDecorView();
//// Hide both the navigation bar and the status bar.
//// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//// a general rule, you should design your app to hide the status bar whenever you
//// hide the navigation bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);


//        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
//        }


        hideSystemUI();

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

//        StatusBarUtil.setTranslucent(this);

        showAdvFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //Any time the window receives focus, simply set the IMMERSIVE mode.
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @OnClick(R.id.fab)
    public void clickFab(){
        PhoneInputDialog dialog = new PhoneInputDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.init(new PhoneInputDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(Double quantity) {
                showQR();
            }
        });

        if (!dialog.isShowing()){
            dialog.show();
        }

        hideSystemUI();

        showAdvFragment();
//        Snackbar.make(fab, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show();
    }

    public void showQR(){
        CouponQRDialog dialog = new CouponQRDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        if (!dialog.isShowing()){
            dialog.show();
        }

        hideSystemUI();
    }

    private AdvFragment advFragment = null;
    private OrderFragment orderFragment = null;
    private PayFragment payFragment = null;
//    private Fragment curFragment = null;
    private void showAdvFragment(){
        if (advFragment == null){
            advFragment = new AdvFragment();
        }
        getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                .replace(R.id.fragment_container, advFragment)
                .commitAllowingStateLoss();
//                .commit();
//        curFragment = advFragment;
        hideSystemUI();
    }

    private void sendMessae(){
        //TODO 发送消息
//        DialogUtil.showHint("@开发君@ 失踪了...");
        NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>(){
            @Override
            protected void processOperResult(EmbMsg result){
//                doAfterSendSuccess(result);
                ZLogger.d("消息发送成功");
            }
        };
        EmbMsgService msgService = ServiceFactory.getService(EmbMsgService.class, this);
        msgService.sendMessageToPeople(MfhLoginService.get().getCurrentGuId(),
                132079L, new TextParam("test message from display"), processor);
    }

    public void onEventMainThread(CashierOrderEvent event) {
        CashierOrderInfoWrapper cashierOrderInfoWrapper = event.getCashierOrderInfoWrapper();

        if (cashierOrderInfoWrapper == null){
            ZLogger.d("cashierOrderInfo is null.");
            return;
        }

        try{

            switch (cashierOrderInfoWrapper.getCmdType()){
                case CashierOrderInfoWrapper.CMD_PAY_ORDER:{
                    ZLogger.d("准备跳转到支付页面");

                    Bundle args = new Bundle();
                    args.putSerializable("cashierOrderInfo", cashierOrderInfoWrapper.getCashierOrderInfo());
                    orderFragment = OrderFragment.newInstance(args);
                    getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                            .replace(R.id.fragment_container, orderFragment)
                            .commitAllowingStateLoss();
//                        .commit();
//            curFragment = orderFragment;
                    ZLogger.d("跳转到支付页面");
                    hideSystemUI();
                }
                break;
                case CashierOrderInfoWrapper.CMD_FINISH_ORDER:{

                    ZLogger.d("准备跳转到支付完成页面");
                    //支付完成
                    Bundle args = new Bundle();
                    args.putSerializable("cashierOrderInfo", cashierOrderInfoWrapper.getCashierOrderInfo());
                    payFragment = PayFragment.newInstance(args);
                    getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                            .replace(R.id.fragment_container, payFragment)
                            .commitAllowingStateLoss();
//                        .commit();
//            curFragment = payFragment;
                    ZLogger.d("跳转到支付完成页面");
                    hideSystemUI();
                }
                break;
                case CashierOrderInfoWrapper.CMD_CLEAR_ORDER:
                default:{
                    showAdvFragment();
                }
                break;
            }
        }
        catch (Exception e){
            ZLogger.e(String.format("显示订单失败：%s", e.toString()));
        }
    }
}
