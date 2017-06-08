package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bingshanguxue.cashier.pay.PayActionEvent;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.presenter.CustomerPresenter;
import com.manfenjiayuan.business.view.ICustomerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.pay.PayStep2Fragment;
import com.mfh.litecashier.ui.widget.CustomerView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * <h1>积分兑换</h1>
 * Created by bingshanguxue on 15/12/15.
 */
public class ExchangeScoreFragment extends BaseProgressFragment implements ICustomerView {
    public static final String EXTRA_HUMAN = "human";
    public static final String EXTRA_IS_PAY_ACTION = "isPayAction";


    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.customer_view)
    CustomerView mCustomerView;
    @BindView(R.id.label_deductScore)
    InputNumberLabelView inlvScore;
    @BindView(R.id.button_exchange)
    Button btnExchange;

    private NumberInputDialog barcodeInputDialog = null;
    private NumberInputDialog customerDialog = null;
    private Double curScore = null;
    private Human mHuman;
    private boolean isPayAction;

    private CustomerPresenter mCustomerPresenter;


    public static ExchangeScoreFragment newInstance(Bundle args) {
        ExchangeScoreFragment fragment = new ExchangeScoreFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCustomerPresenter = new CustomerPresenter(this);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_exchange_score;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            ZLogger.d("打开积分兑换页面 开始");

            Bundle args = getArguments();
            if (args != null) {
                mHuman = (Human) args.getSerializable(EXTRA_HUMAN);
                isPayAction = args.getBoolean(EXTRA_IS_PAY_ACTION);
            }
            tvHeaderTitle.setText("积分兑换");

            initScoreInputView();

            mCustomerView.reload(mHuman);
            if (mHuman == null) {
                queryCustomer();
            }
            ZLogger.d("打开积分兑换页面结束");
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e("打开积分兑换页面异常" + e.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initScoreInputView() {
        inlvScore.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            submit();
                        }
                    }
                });
        inlvScore.registerOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onClickAction1(String text) {
//                submit();
                inlvScore.clear();
            }

            @Override
            public void onLongClickAction1(String text) {

            }
        });
        inlvScore.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                inlvScore.requestFocusEnd();

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvScore.isSoftKeyboardEnabled()) {
                        showBarcodeKeyboard();
                    }
                }

                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    /**
     * 显示条码输入界面
     * 相当于扫描条码
     */
    private void showBarcodeKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getActivity());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeDecimalNumber(EditInputType.NUMBER, "积分",
                inlvScore.getInputString(), 2, "积分",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        inlvScore.setInputString(value);

                        submit();
                    }

                    @Override
                    public void onNext(Double value) {
                        if (value != null) {
                            inlvScore.setInputString(String.valueOf(value));

                            submit();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }

                });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        if (isPayAction) {
            Bundle args = new Bundle();
            EventBus.getDefault().post(new PayActionEvent(PayActionEvent.PAY_ACTION_VIP_UPDATED, args));
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }
    }


    /**
     * 查询用户
     */
    @OnClick(R.id.customer_view)
    public void queryCustomer() {
        if (customerDialog == null) {
            customerDialog = new NumberInputDialog(getActivity());
            customerDialog.setCancelable(false);
            customerDialog.setCanceledOnTouchOutside(false);
        }

        customerDialog.initializeBarcode(EditInputType.TEXT, "搜索会员", "会员帐号", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        mCustomerPresenter.getCustomerByOther(value);
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {
                        if (mHuman == null) {
                            DialogUtil.showHint("取消积分兑换");
                            if (isPayAction) {
                                Bundle args = new Bundle();
                                args.putBoolean(PayStep2Fragment.EXTRA_KEY_IS_RELOAD_VIP, true);
                                EventBus.getDefault().post(new PayActionEvent(PayActionEvent.PAY_ACTION_VIP_UPDATED, args));
                            } else {
                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        if (!customerDialog.isShowing()) {
            customerDialog.show();
        }
    }

    /**
     * 积分兑换
     */
    @OnClick(R.id.button_exchange)
    public void submit() {
        onLoadProcess("请稍候...");

        if (mHuman == null) {
            queryCustomer();
            return;
        }

        String score = inlvScore.getInputString();
        if (StringUtils.isEmpty(score)) {
            onLoadError("请输入积分值");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，请重新尝试。");
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("humanId", String.valueOf(mHuman.getId()));
        //score参数不能为空!
        options.put("score", score);
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        CommonUserAccountHttpManager.getInstance().payDirect(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
//                        etPayCode.getText().clear();
                        onLoadError("积分兑换失败：" + e.getMessage());
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        DialogUtil.showHint("积分兑换成功");
                        if (isPayAction) {
                            Bundle args = new Bundle();
                            args.putBoolean(PayStep2Fragment.EXTRA_KEY_IS_RELOAD_VIP, true);
                            EventBus.getDefault().post(new PayActionEvent(PayActionEvent.PAY_ACTION_VIP_UPDATED, args));
                        } else {
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    }

                });
    }

    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        btnExchange.setEnabled(true);
    }

    @Override
    public void onLoadError(String errMessage) {
        super.onLoadError(errMessage);
        btnExchange.setEnabled(true);
//        etPayCode.requestFocus();
    }

    @Override
    public void onICustomerViewLoading() {

        onLoadProcess("正在加载会员信息");
    }

    @Override
    public void onICustomerViewError(int type, String content, String errorMsg) {
        mHuman = null;
        mCustomerView.reload(mHuman);
        onLoadError(errorMsg);
    }

    @Override
    public void onICustomerViewSuccess(int type, String content, Human human) {
        mHuman = human;
        mCustomerView.reload(mHuman);
        onLoadFinished();
    }

}
