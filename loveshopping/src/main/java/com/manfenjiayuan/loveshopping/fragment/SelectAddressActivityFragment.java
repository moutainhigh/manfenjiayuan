package com.manfenjiayuan.loveshopping.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.manfenjiayuan.loveshopping.AppContext;
import com.manfenjiayuan.loveshopping.R;
import com.manfenjiayuan.loveshopping.SearchCommunityAdapter;
import com.manfenjiayuan.loveshopping.eventbus.CommunityEvent;
import com.manfenjiayuan.loveshopping.mvp.ISubdisListView;
import com.manfenjiayuan.loveshopping.mvp.SubdisListPresenter;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.compound.CustomSearchView;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.login.entity.Subdis;
import com.mfh.framework.mvp.MvpFragment;

import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;

/**
 * A placeholder fragment containing a simple view.
 */
public class SelectAddressActivityFragment extends MvpFragment<ISubdisListView, SubdisListPresenter>
        implements ISubdisListView {

    @Bind(R.id.search_view)
    CustomSearchView searchView;
    @Bind(R.id.address_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    private SearchCommunityAdapter mAdapter;

    public SelectAddressActivityFragment() {
    }

    @Override
    public SubdisListPresenter createPresenter() {
        return new SubdisListPresenter();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_select_address;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        initSearchView();
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
//enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));

        mAdapter = new SearchCommunityAdapter(getActivity(), null);
        mAdapter.setOnItemClickLitener(new SearchCommunityAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                EventBus.getDefault().post(new CommunityEvent(CommunityEvent.EVENT_ID_UPDATED, mAdapter.getEntity(position)));
                getActivity().finish();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 初始化搜索框
     */
    private void initSearchView() {
//        searchView.setHint(R.string.search_bar_hint_conversation);
//        searchView.setTextColor(this.getResources().getColor(R.color.material_black));
        searchView.setListener(new CustomSearchView.CustomSearchViewListener() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                load(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void doSearch(String queryText) {
                //TODO
                load(queryText);
            }

        });
    }

    private void load(String queryText) {
        if (bSyncInProgress){
            ZLogger.d("正在加载数据。");
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            onLoadFinished();
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        PageInfo pageInfo = new PageInfo(0, 50);
//        ScApi.listSubdis("", queryText, responseCallback);
        if (presenter != null) {
            presenter.listSubdis(pageInfo, "", queryText);
        } else {
            ZLogger.d("presenter is null");
        }
    }

    protected boolean bSyncInProgress = false;//是否正在同步

    /**
     * 开始加载
     */
    public void onLoadStart() {
        bSyncInProgress = true;
        animProgress.setVisibility(View.VISIBLE);
    }

    /**
     * 加载完成
     */
    public void onLoadFinished() {
        bSyncInProgress = false;
        animProgress.setVisibility(View.GONE);
    }


    @Override
    public void onQuerySubdisProcess() {
        onLoadStart();
    }

    @Override
    public void onQuerySubdisError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onQuerySubdisSuccess(PageInfo pageInfo, List<Subdis> dataList) {
        // TODO: 4/19/16
        mAdapter.setEntityList(dataList);
        onLoadFinished();
    }
}
