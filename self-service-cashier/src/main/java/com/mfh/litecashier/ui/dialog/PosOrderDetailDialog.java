package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bingshanguxue.cashier.v1.CashierAgent;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;
import com.mfh.litecashier.hardware.SerialManager;


/**
 * 异常订单处理，修改订单信息并提交。
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PosOrderDetailDialog extends CommonDialog {

    private View rootView;
    private TextView tvTitle, tvOutTradeNo;
    private Spinner mPaytypeSpinner;
    private EditText etPaidMoney;
    private Button btnSubmit;
    private ImageButton btnClose;
    private ProgressBar progressBar;

    private ArrayAdapter<CharSequence> payTypeAdapter;

    public interface onDialogClickListener {
        void onDatasetChanged();
    }
    private onDialogClickListener mListener;

    private CashierOrderInfo mCashierOrderInfo;

    private PosOrderDetailDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private PosOrderDetailDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_posorder_detail, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvOutTradeNo = (TextView) rootView.findViewById(R.id.tv_outTradeNo);
        mPaytypeSpinner = (Spinner) rootView.findViewById(R.id.spinner_paytype);
        etPaidMoney = (EditText) rootView.findViewById(R.id.et_paidmoney);
        btnSubmit = (Button) rootView.findViewById(R.id.button_footer_positive);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        payTypeAdapter = ArrayAdapter.createFromResource(context,
                R.array.order_paytype, R.layout.mfh_spinner_item_text);
        payTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPaytypeSpinner.setAdapter(payTypeAdapter);
        tvTitle.setText("处理订单");

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public PosOrderDetailDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.height = d.getHeight();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();

        DeviceUtils.hideSoftInput(getOwnerActivity());
    }

    public void init(CashierOrderInfo cashierOrderInfo, onDialogClickListener listener){
        this.mCashierOrderInfo = cashierOrderInfo;
        this.mListener = listener;
        refresh();
    }

    /**
     * 刷新会员信息
     */
    private void refresh(){
        if (mCashierOrderInfo != null){
            tvOutTradeNo.setText(mCashierOrderInfo.getPosTradeNo());
        }
        mPaytypeSpinner.setSelection(payTypeAdapter.getPosition(SerialManager.getUmsipsPort()));
        etPaidMoney.getText().clear();
    }

    private void submit() {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        String amount = etPaidMoney.getText().toString();
        if (StringUtils.isEmpty(amount)) {
            DialogUtil.showHint("请输入支付金额");
            btnSubmit.setEnabled(true);
            etPaidMoney.requestFocus();
            progressBar.setVisibility(View.GONE);
            return;
        }

        Double amoutVal = Double.valueOf(amount);
        Integer wayType = WayType.NA;

        String payTypeCaption = mPaytypeSpinner.getSelectedItem().toString();
        switch (payTypeCaption){
            case "现金":{
                wayType = WayType.CASH;
            }
            break;
            case "会员":{
                wayType = WayType.VIP;
            }
            break;
            case "支付宝":{
                wayType = WayType.ALI_F2F;
            }
            break;
            case "微信":{
                wayType = WayType.WX_F2F;
            }
            break;
            case "银联":{
                wayType = WayType.BANKCARD;
            }
            break;
        }

        mCashierOrderInfo.paid(wayType, amoutVal);
        CashierAgent.updateCashierOrder(mCashierOrderInfo, mCashierOrderInfo.getPayAmount(), PosOrderEntity.ORDER_STATUS_FINISH);

        // TODO: 6/20/16
        btnSubmit.setEnabled(true);
        progressBar.setVisibility(View.GONE);

        if (mListener != null){
            mListener.onDatasetChanged();
        }

        dismiss();
    }

}
