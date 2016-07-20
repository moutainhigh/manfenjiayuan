package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.PayHistoryAdapter;
import com.bingshanguxue.cashier.database.dao.PosOrderDao;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;

import java.util.List;


/**
 * 对话框 -- 订单支付记录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PayHistoryDialog extends CommonDialog {

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private RecyclerView mRecyclerView;
    private PayHistoryAdapter productAdapter;

    private PayHistoryDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private PayHistoryDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_pay_history, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.product_list);

        tvTitle.setText("支付记录");
        initRecyclerView();

//        btnSubmit.setVisibility(View.VISIBLE);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public PayHistoryDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);

//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
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
    }

    public void init(Long orderId){
        List<PosOrderPayEntity> orderEntityList = PosOrderPayService.get()
                .queryAllBy(String.format("orderId = '%d'", orderId),
                        PosOrderDao.ORDER_BY_UPDATEDATE_DESC);

        productAdapter.setEntityList(orderEntityList);
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getContext(), LineItemDecoration.VERTICAL_LIST));
        productAdapter = new PayHistoryAdapter(getContext(), null);

        mRecyclerView.setAdapter(productAdapter);
    }

}
