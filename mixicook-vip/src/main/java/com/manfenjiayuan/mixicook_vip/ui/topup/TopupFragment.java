package com.manfenjiayuan.mixicook_vip.ui.topup;


import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.bingshanguxue.vector_uikit.ProfileView;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 充值
 *
 * @author bingshanguxue created on 2015-04-13
 * @since Framework 1.0
 */
public class TopupFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.profileView)
    ProfileView mProfileView;
    @Bind(R.id.amountRecyclerView)
    RecyclerView mRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private TopupAdapter mTopupAdapter;

    public TopupFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_topup;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("充值");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        mProfileView.setAvatarUrl(MfhLoginService.get().getHeadimage());
        mProfileView.setPrimaryText(MfhLoginService.get().getHumanName());
        mProfileView.setSecondaryText(MfhLoginService.get().getTelephone());
        initRecyclerView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(this, 1,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f,
//                getResources().getColor(R.color.gray), 1f));

        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(3, 2, false));

        List<Double> entities = new ArrayList<>();
        entities.add(0.01D);
        entities.add(1D);
        entities.add(20D);
        entities.add(50D);
        entities.add(100D);
        entities.add(200D);
        entities.add(300D);
        entities.add(500D);
        entities.add(1000D);
        mTopupAdapter = new TopupAdapter(getActivity(), entities);
        mTopupAdapter.setOnAdapterLitener(new TopupAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO: 9/27/16 跳转页面
                Double amount = mTopupAdapter.getEntity(position);
                if (amount != null){
                    DialogUtil.showHint(String.format("准备充值 %.2f 元", amount));
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mRecyclerView.setAdapter(mTopupAdapter);
    }

}
