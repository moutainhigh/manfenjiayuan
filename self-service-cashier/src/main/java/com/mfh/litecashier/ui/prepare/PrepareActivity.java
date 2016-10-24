package com.mfh.litecashier.ui.prepare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;

import de.greenrobot.event.EventBus;


/**
 * 拣货页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrepareActivity extends BaseActivity {

    public static final String EXTRA_KEY_TRADENO = "tradeNo";

    private PickStep1Fragment mPickStep1Fragment;
    private PickStep2Fragment mPickStep2Fragment;

    private String tradeNo = null;

    private int curStep = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, PrepareActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_cashierpay;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        EventBus.getDefault().register(this);

        showStep1();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }

            tradeNo = intent.getStringExtra(EXTRA_KEY_TRADENO);
        }

//        cashierOrderInfo = GlobalInstance.getInstance().getCashierOrderInfo();
    }
    /**
     * 显示
     */
    public void showStep1() {
        curStep = 0;
        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_KEY_TRADENO, tradeNo);
        if (mPickStep1Fragment == null) {
            mPickStep1Fragment = PickStep1Fragment.newInstance(intent.getExtras());
        }
        else{
            mPickStep1Fragment.setArguments(intent.getExtras());
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mPickStep1Fragment)
                .commit();
    }

    public void showStep2(ScOrder scOrder) {
        if (curStep == 1){
            return;
        }
        ZLogger.df("准备跳转到组货页面");
        curStep = 1;

        try{
            Bundle args = new Bundle();
            args.putSerializable(PickStep2Fragment.EXTRA_KEY_SCORDER, scOrder);
            if (mPickStep2Fragment == null) {
                mPickStep2Fragment = PickStep2Fragment.newInstance(args);
            }
            else{
                mPickStep2Fragment.setArguments(args);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mPickStep2Fragment)
                    .commit();
        }
        catch (Exception e){
            e.printStackTrace();
            ZLogger.ef(e.toString());
        }
        ZLogger.df("跳转到组货页面end");
    }

    public void onEventMainThread(PickEvent event) {
        Bundle args = event.getArgs();
        ZLogger.df(String.format("PickEvent:%d\n%s",
                event.getAction(), StringUtils.decodeBundle(args)));
        switch (event.getAction()){
            case PickEvent.ACTION_PREPARE:{
                if (args != null){
                    ScOrder scOrder = (ScOrder) args.getSerializable(PickEvent.KEY_SCORDER);
                    if (scOrder == null){
                        return;
                    }

                    showStep2(scOrder);
                }
            }
            break;
        }
    }



}
