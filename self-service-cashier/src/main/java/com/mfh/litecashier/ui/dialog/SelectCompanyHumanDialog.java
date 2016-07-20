package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.CompanyHumanAdapter;
import com.mfh.litecashier.database.entity.CompanyHumanEntity;
import com.mfh.litecashier.database.logic.CompanyHumanService;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration2;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.login.logic.MfhLoginService;


/**
 * 交接班－－选择用户
 * 
 * @author NAT.ZZN
 * 
 */
public class SelectCompanyHumanDialog extends CommonDialog {

    private View rootView;

    private TextView tvTitle;
    private ImageButton ibClose;

    private RecyclerView mRecyclerView;
    private CompanyHumanAdapter mAdapter;
    private GridLayoutManager mRLayoutManager;

    public interface DialogClickListener {
        void onSelectHuman(CompanyHumanEntity entity);
    }
    private DialogClickListener mListener;

    private SelectCompanyHumanDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectCompanyHumanDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_select_companyhuman, null);
//        ButterKnife.bind(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        tvTitle.setText("选择用户");
        ibClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
//        ibClose.setVisibility(View.GONE);
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.human_list);

        initRecyclerView();

        setContent(rootView, 0);
    }

    public SelectCompanyHumanDialog(Context context) {
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

        DeviceUtils.hideSoftInput(getOwnerActivity());

        mAdapter.setEntityList(CompanyHumanService.get().queryAllBy(String.format("userName != '%s'", MfhLoginService.get().getLoginName())));
    }

    public void setOnDialogClickListener(DialogClickListener listener){
        this.mListener = listener;
    }

    /**
     * 初始化快捷菜单
     */
    private void initRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getContext(), 5);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration2(getContext(), 1,
                getContext().getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
                getContext().getResources().getColor(R.color.transparent), 0.1f,
                getContext().getResources().getColor(R.color.transparent), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        mAdapter = new CompanyHumanAdapter(getContext(), null);
        mAdapter.setOnAdapterLitener(new CompanyHumanAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismiss();
                CompanyHumanEntity entity = mAdapter.getEntity(position);
                if (mListener != null){
                    mListener.onSelectHuman(entity);
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }


}
