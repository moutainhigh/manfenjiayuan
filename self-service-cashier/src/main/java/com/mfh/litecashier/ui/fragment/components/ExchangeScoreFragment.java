package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.http.CommonUserAccountHttpManager;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * <h1>积分兑换</h1>
 * Created by bingshanguxue on 15/12/15.
 */
public class ExchangeScoreFragment extends BaseProgressFragment {
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.tv_score)
    TextView tvScore;
    @BindView(R.id.et_pay_code)
    EditText etPayCode;
    @BindView(R.id.button_exchange)
    Button btnExchange;

    private NumberInputDialog scoreDialog = null;
    private Double curScore = null;

    public static ExchangeScoreFragment newInstance(Bundle args) {
        ExchangeScoreFragment fragment = new ExchangeScoreFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_exchange_score;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        tvHeaderTitle.setText("积分兑换");
        etPayCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));

                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        exchangeScore();
                    }
                    return true;
                }
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etPayCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etPayCode);
                }
                etPayCode.requestFocus();
                etPayCode.setSelection(etPayCode.length());
                //返回true,不再继续传递事件
                return true;
            }
        });


        if (curScore == null){
            inputScore();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }


    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    @OnClick(R.id.tv_score)
    public void inputScore(){
        if (scoreDialog == null) {
            scoreDialog = new NumberInputDialog(getActivity());
            scoreDialog.setCancelable(false);
            scoreDialog.setCanceledOnTouchOutside(false);
        }

        scoreDialog.initializeDecimalNumber(EditInputType.NUMBER_DECIMAL, "积分",
                MUtils.formatDouble(curScore, "输入积分值"),
                0, "",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {

                    }

                    @Override
                    public void onNext(Double value) {
                        curScore = value;
                        tvScore.setText(String.valueOf(value));
                    }

                    @Override
                    public void onCancel() {
                        if (curScore == null){
                            getActivity().setResult(Activity.RESULT_CANCELED);
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                });

        if (!scoreDialog.isShowing()) {
            scoreDialog.show();
        }
    }

    /**
     * 积分兑换
     */
    @OnClick(R.id.button_exchange)
    public void exchangeScore(){
        onLoadProcess("请稍候...");

        String cardNo = etPayCode.getText().toString();
        String humanId3 = MUtils.parseMfPaycode(cardNo);
        if (StringUtils.isEmpty(humanId3)) {
            onLoadError("付款码无效");
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，请重新尝试。");
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("humanId", humanId3);
        if (curScore != null) {
            options.put("score", String.format("%.0f", curScore));
        }
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        CommonUserAccountHttpManager.getInstance().payDirect(options,
                new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        etPayCode.getText().clear();
                        onLoadError("积分兑换失败：" + e.toString());
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.df("积分兑换成功:" + s);
                        DialogUtil.showHint("积分兑换成功");
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
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
        etPayCode.requestFocus();
    }

}
