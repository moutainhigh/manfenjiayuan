package com.mfh.petitestock.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.petitestock.R;
import com.mfh.petitestock.ui.adapter.OfficeAdapter;
import com.mfh.petitestock.utils.DataCacheHelper;

import butterknife.Bind;


/**
 * 网店列表
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class OfficeListFragment extends BaseFragment {

    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private OfficeAdapter officeAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view) View emptyView;

    public static OfficeListFragment newInstance(Bundle args) {
        OfficeListFragment fragment = new OfficeListFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_office_list;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        addressRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        addressRecyclerView.setHasFixedSize(true);
        //添加分割线
        addressRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        addressRecyclerView.setEmptyView(emptyView);

        officeAdapter = new OfficeAdapter(getActivity(), MfhLoginService.get().getOffices());
        officeAdapter.setOnItemClickLitener(new OfficeAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                DataCacheHelper.getInstance().setCurrentOffice(officeAdapter.getData().get(position));
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        addressRecyclerView.setAdapter(officeAdapter);
    }

}
