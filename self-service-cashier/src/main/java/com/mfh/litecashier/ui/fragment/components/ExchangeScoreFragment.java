package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import butterknife.BindView;
import butterknife.OnClick;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
//        if (args != null) {
//            dailySettleDatetime = args.getString(EXTRA_KEY_DATETIME);
//        }

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
//                return true;
                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
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

        CommonUserAccountApi.payDirectBySweepCode(humanId3, curScore, payRC);
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

    NetCallBack.NetTaskCallBack payRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"操作成功!","version":"1","data":""}
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    ZLogger.d("积分兑换成功:" + retValue.getValue());
                    DialogUtil.showHint("积分兑换成功");
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"指定网点已经日结过：132079","version":"1","data":null}
                    etPayCode.getText().clear();
                    onLoadError("积分兑换失败：" + errMsg);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };


}
