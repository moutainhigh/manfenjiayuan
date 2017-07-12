package com.mfh.litecashier.components.customer;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
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
import com.mfh.litecashier.BaseDialogFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.CustomerView;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <h1>积分兑换</h1>
 * Created by bingshanguxue on 15/12/15.
 */
public class ExchangeScoreDialogFragment extends BaseDialogFragment {
    public static final String EXTRA_KEY_HUMAN = "human";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.customer_view)
    CustomerView mCustomerView;
    @BindView(R.id.label_deductScore)
    InputNumberLabelView inlvScore;
    @BindView(R.id.button_exchange)
    Button btnExchange;

    private NumberInputDialog barcodeInputDialog = null;
    private Human mHuman;


    public interface OnDialogListener {
        void onSuccess();
        void onCancel();
    }

    private OnDialogListener mOnDialogListener;

    public void setOnDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }

    public static ExchangeScoreDialogFragment newInstance(Human human) {
        Bundle args = new Bundle();
        if (human != null) {
            args.putSerializable(EXTRA_KEY_HUMAN, human);
        }

        ExchangeScoreDialogFragment fragment = new ExchangeScoreDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ExchangeScoreDialogFragment newInstance(Bundle args) {
        ExchangeScoreDialogFragment fragment = new ExchangeScoreDialogFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getDialogType() {
        return DIALOG_TYPE_MIDDLE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_exchange_score;
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        ButterKnife.bind(this, rootView);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        try {
            ZLogger.d("打开积分兑换页面 开始");

            Bundle args = getArguments();
            if (args != null) {
                mHuman = (Human) args.getSerializable(EXTRA_KEY_HUMAN);
            }

            toolbar.setTitle(R.string.title_exchange_score);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Handle the menu item
                    int id = item.getItemId();
                    if (id == R.id.action_close) {
                        dismiss();
                    }
                    return true;
                }
            });
            // Inflate a menu to be displayed in the toolbar
            toolbar.inflateMenu(R.menu.menu_normal);

            initScoreInputView();

            mCustomerView.reload(mHuman);
            ZLogger.d("打开积分兑换页面结束");
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e("打开积分兑换页面异常" + e.toString());
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        setCancelable(true);
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

    /**
     * 积分兑换
     */
    @OnClick(R.id.button_exchange)
    public void submit() {
        onLoadProcess("请稍候...");

        if (mHuman == null) {
            onLoadError(getString(R.string.tip_cannot_find_customer));
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
                        onLoadError(e.getMessage());
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        onLoadFinished();
                        inlvScore.clear();
                        DialogUtil.showHint("积分兑换成功");
                        dismiss();
                        if (mOnDialogListener != null) {
                            mOnDialogListener.onSuccess();
                        }
                    }
                });
    }

//    @Override
    public void onLoadProcess(String description) {
        setCancelable(false);
        btnExchange.setEnabled(false);

//        super.onLoadProcess(description);
//        showProgressDialog(ProgressDialog.STATUS_PROCESSING, description, false);

    }

//    @Override
    public void onLoadFinished() {
//        super.onLoadFinished();
        btnExchange.setEnabled(true);
        setCancelable(true);
    }

//    @Override
    public void onLoadError(String errMessage) {
//        super.onLoadError(errMessage);
        btnExchange.setEnabled(true);
//        etPayCode.requestFocus();
        DialogUtil.showHint(errMessage);
        setCancelable(true);
    }
}
