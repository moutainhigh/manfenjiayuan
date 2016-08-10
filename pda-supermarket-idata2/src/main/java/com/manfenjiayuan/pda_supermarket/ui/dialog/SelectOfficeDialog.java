package com.manfenjiayuan.pda_supermarket.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.adapter.OfficeAdapter;
import com.mfh.framework.login.entity.Office;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;


/**
 * 选择批发商
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SelectOfficeDialog extends CommonDialog {

    private View rootView;
    private RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private View emptyView;
    private ProgressBar progressBar;

    private OfficeAdapter productAdapter;


    public interface OnDialogListener {
        void onItemSelected(Office office);
    }

    private OnDialogListener listener;


    private SelectOfficeDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectOfficeDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.include_simple_recyclerview, null);
//        ButterKnife.bind(rootView);

        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.recyclerViewEmptySupport);
        emptyView = rootView.findViewById(R.id.empty_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgressBar);

        initRecyclerView();

//        btnClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//            }
//        });

        setContent(rootView, 0);
    }

    public SelectOfficeDialog(Context context) {
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
//////        p.width = d.getWidth() * 2 / 3;
//////        p.y = DensityUtil.dip2px(getContext(), 44);
//        p.height = d.getHeight();
////
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);


        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    public void init(List<Office> officeList, OnDialogListener listener) {
        this.listener = listener;
        productAdapter.setData(officeList);
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getContext(), LineItemDecoration.VERTICAL_LIST));

        productAdapter = new OfficeAdapter(getContext(), null);
        productAdapter.setOnItemClickLitener(new OfficeAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismiss();

                if (listener != null) {
                    listener.onItemSelected(productAdapter.getData().get(position));
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

}
