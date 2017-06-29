package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bingshanguxue.cashier.model.wrapper.HangupOrder;
import com.bingshanguxue.cashier.CashierProvider;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.HangupDialogAdapter;


/**
 * 调单/挂单
 * 
 * @author NAT.ZZN
 * 
 */
public class HangupOrderDialog extends CommonDialog {

    public interface OnResponseCallback {
        void onResumeOrder(String orderBarCode);
    }

    private View rootView;

    private TextView tvTitle;
    private ImageButton btnClose;
    private RecyclerView mRecyclerView;

    private HangupDialogAdapter productAdapter;
    private OnResponseCallback mListener;

    private HangupOrderDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private HangupOrderDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(R.layout.dialogview_hanguporder, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.product_list);

        tvTitle.setText("调单");

        initRecyclerView();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContent(rootView, 0);
    }

    public HangupOrderDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (getWindow() != null) {
            getWindow().setGravity(Gravity.CENTER);
        }
//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);
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
        productAdapter = new HangupDialogAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new HangupDialogAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismiss();
                if (mListener != null) {
                    HangupOrder entity = productAdapter.getEntity(position);
                    mListener.onResumeOrder(entity.getOrderTradeNo());
                }
            }

            @Override
            public void onDataSetChanged() {
            }
        });

        mRecyclerView.setAdapter(productAdapter);
    }

    @Override
    public void show() {
        super.show();
    }

    public void init(OnResponseCallback callback) {
//        this.dialogType = type;
        this.mListener = callback;

        productAdapter.setEntityList(CashierProvider.fetchHangupOrders());
    }
}
