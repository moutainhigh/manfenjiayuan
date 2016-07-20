package com.mfh.petitestock.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration2;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.R;
import com.mfh.petitestock.bean.Shelfnumber;
import com.mfh.petitestock.ui.adapter.ShelfnumberAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * 选择批发商
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SelectShelvesDialog extends CommonDialog  {

    private View rootView;
    private RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TextView emptyView;
    private ProgressBar progressBar;

    private ShelfnumberAdapter shelfnumberAdapter;

    public interface OnDialogListener {
        void onItemSelected(Shelfnumber entity);
    }

    private OnDialogListener listener;


    private SelectShelvesDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectShelvesDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_select_platform_provider, null);
//        ButterKnife.bind(rootView);

        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.company_list);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        initRecyclerView();

//
//        btnClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//            }
//        });


        setContent(rootView, 0);
    }

    public SelectShelvesDialog(Context context) {
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


    public void init(int maxNumber, OnDialogListener listener) {
        this.listener = listener;

        List<Shelfnumber> localList = new ArrayList<>();
        for (int i = 1; i < maxNumber; i++) {
            localList.add(Shelfnumber.newInstance((long) i));
        }
        if (shelfnumberAdapter != null) {
            shelfnumberAdapter.setEntityList(localList);
        }
    }

    private void initRecyclerView() {
        //        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        shelfnumberRecyclerView.setLayoutManager(linearLayoutManager);
        GridLayoutManager mRLayoutManager = new GridLayoutManager(getContext(), 4);
//        mRLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
//        shelfnumberRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.HORIZONTAL_LIST));
        //添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration2(getContext(), 1,
                getContext().getResources().getColor(R.color.mf_dividerColorPrimary), 0.0f,
                getContext().getResources().getColor(R.color.mf_dividerColorPrimary), 0.05f,
                getContext().getResources().getColor(R.color.mf_dividerColorPrimary), 0.0f));

        shelfnumberAdapter = new ShelfnumberAdapter(AppContext.getAppContext(), null);
        shelfnumberAdapter.setOnAdapterLitener(new ShelfnumberAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Shelfnumber entity = shelfnumberAdapter.getCurShelfnumber();

                dismiss();
                if (entity == null) {
                    return;
                }

                if (listener != null) {
                    listener.onItemSelected(entity);
                }

            }
        });

        mRecyclerView.setAdapter(shelfnumberAdapter);
    }


}
