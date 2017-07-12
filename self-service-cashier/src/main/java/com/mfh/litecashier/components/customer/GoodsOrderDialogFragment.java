package com.mfh.litecashier.components.customer;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mfh.framework.api.account.Human;
import com.mfh.framework.rxapi.bean.GoodsOrder;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.order.PosOrderItemsAdapter;


/**
 * Created by bingshanguxue on 30/06/2017.
 */

public class GoodsOrderDialogFragment extends DialogFragment {

    private View rootView;
    private TextView tvTitle;
    private RecyclerView goodsRecyclerView;
    private PosOrderItemsAdapter goodsAdapter;
    private GoodsOrder mPosOrder;

    public interface OnCustomerQueryListener {
        void onQuerySuccess(int target, int type, Human human);

        void onCancelOrDismiss(int target);
    }

    private OnCustomerQueryListener mOnCustomerQueryListener;

    public void setOnCustomerQueryListener(OnCustomerQueryListener listener) {
        mOnCustomerQueryListener = listener;
    }

    public void setPosOrder(GoodsOrder order) {
        this.mPosOrder = order;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //1 通过样式定义
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Light_Dialog);
        //2代码设置 无标题 无边框
//        setStyle(DialogFragment.STYLE_NO_TITLE|DialogFragment.STYLE_NO_FRAME,0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
//        return super.onCreateView(inflater, container, savedInstanceState);
//        getDialog().setTitle("查询会员");

        //3 在此处设置 无标题 对话框背景色
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // //对话框背景色
//        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.RED));
//        getDialog().getWindow().setDimAmount(0.5f);//背景黑暗度

        //不能在此处设置style
        // setStyle(DialogFragment.STYLE_NORMAL,R.style.Mdialog);//在此处设置主题样式不

        try {
            rootView = inflater.inflate(R.layout.dialog_googsorder_items, container, false);

            goodsRecyclerView = (RecyclerView) rootView.findViewById(R.id.order_goods_list);
            tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
            tvTitle.setText("订单详情");

            rootView.findViewById(R.id.button_header_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (mOnCustomerQueryListener != null) {
//                        mOnCustomerQueryListener.onCancelOrDismiss(mTarget);
//                    }
                    dismiss();
                }
            });

            initGoodsRecyclerView();

            if (goodsAdapter != null) {
                goodsAdapter.setEntityList(mPosOrder != null ? mPosOrder.getItems() : null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
//        if (mOnCustomerQueryListener != null) {
//            mOnCustomerQueryListener.onCancelOrDismiss(mTarget);
//        }
    }

//    @Override
//    public void onDismiss(DialogInterface dialog) {
//        super.onDismiss(dialog);
//        if (mOnCustomerQueryListener != null) {
//            mOnCustomerQueryListener.onCancelOrDismiss();
//        }
//    }


    @Override
    public int show(FragmentTransaction transaction, String tag) {
        int ret = super.show(transaction, tag);
        try {
            //hide soft input
            DeviceUtils.hideSoftInput(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));

        goodsAdapter = new PosOrderItemsAdapter(CashierApp.getAppContext(), null);
        this.goodsRecyclerView.setAdapter(goodsAdapter);
    }

}
