package com.mfh.litecashier.ui.fragment.cashier;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.CashierExpressInfo;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.ui.adapter.CashierExpressAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 寄快递
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ExpressFragment extends BaseFragment {

    @Bind(R.id.goods_list)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private CashierExpressAdapter adapter;


    public interface OnFragmentListener{
        void onClose();
    }
    private OnFragmentListener fragmentListener;
    public void setOnFragmentListener(OnFragmentListener fragmentListener){
        this.fragmentListener = fragmentListener;
    }

    public static ExpressFragment newInstance(Bundle args) {
        ExpressFragment fragment = new ExpressFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_cashier_express;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

        loadData();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick(R.id.btn_service_back)
    public void close() {
        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_HIDE_RIGHTSLIDE));
    }

    private void initRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 6);
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.5f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        adapter = new CashierExpressAdapter(CashierApp.getAppContext(), null);
        adapter.setOnAdapterLitener(new CashierExpressAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //TODO,记重
            }
        });
        menuRecyclerView.setAdapter(adapter);
    }

    private void loadData(){
        List<CashierExpressInfo> entityList = new ArrayList<>();
        entityList.add(CashierExpressInfo.newInstance(0L,"百世汇通", R.mipmap.ic_express_best));
        entityList.add(CashierExpressInfo.newInstance(0L, "联邦快递", R.mipmap.ic_express_fedex));
        entityList.add(CashierExpressInfo.newInstance(0L,"全峰", R.mipmap.ic_express_quanfeng));
        entityList.add(CashierExpressInfo.newInstance(0L,"申通", R.mipmap.ic_express_sto));
        entityList.add(CashierExpressInfo.newInstance(0L,"顺丰", R.mipmap.ic_express_sf));
        entityList.add(CashierExpressInfo.newInstance(0L,"圆通", R.mipmap.ic_express_yt));
        entityList.add(CashierExpressInfo.newInstance(0L,"韵达", R.mipmap.ic_express_yunda));
        entityList.add(CashierExpressInfo.newInstance(0L,"中通", R.mipmap.ic_express_zto));
        entityList.add(CashierExpressInfo.newInstance(0L,"EMS", R.mipmap.ic_express_ems));
        entityList.add(CashierExpressInfo.newInstance(0L,"UPS", R.mipmap.ic_express_ups));

        adapter.setEntityList(entityList);
    }

}