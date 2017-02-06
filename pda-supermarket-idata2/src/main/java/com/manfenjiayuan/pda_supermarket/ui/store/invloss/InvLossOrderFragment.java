package com.manfenjiayuan.pda_supermarket.ui.store.invloss;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.bizz.invloss.InvLossOrderGoodsAdapter;
import com.bingshanguxue.pda.database.entity.InvLossGoodsEntity;
import com.bingshanguxue.pda.database.service.InvLossGoodsService;
import com.bingshanguxue.vector_uikit.widget.NaviAddressView;
import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.common.SecondaryActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.invOrder.InvOrderApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 新建报损 or 盘点报损
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvLossOrderFragment extends BaseFragment {

    public static final String EXTRA_INV_LOSSORDER = "invLossOrder";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.providerView)
    NaviAddressView mProviderView;
    @BindView(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private InvLossOrderGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;

    @BindView(R.id.empty_view)
    View emptyView;


    //当前正在报损的单据
    private InvLossOrder invLossOrder = null;

    public static InvLossOrderFragment newInstance(Bundle args) {
        InvLossOrderFragment fragment = new InvLossOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected boolean isResponseBackPressed() {
        return true;
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_create_inv_lossorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        InvLossGoodsService.get().clear();
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            invLossOrder = (InvLossOrder) args.getSerializable(EXTRA_INV_LOSSORDER);
        }
        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_submit) {
                    submit();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_inv_io);

        initRecyclerView();

        if (invLossOrder != null) {
            mProviderView.setText(invLossOrder.getOrderName());
        }
    }

    @Override
    public boolean onBackPressed() {
//        DialogUtil.showHint("onBackPressed");
        if (goodsAdapter.getItemCount() > 0) {
            showConfirmDialog("退出后商品列表将会清空，确定要退出吗？",
                    "退出", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            getActivity().setResult(Activity.RESULT_CANCELED);
                            getActivity().finish();
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }


    /**
     * 加载正在报损的订单
     * */
    private void loadLossOrder() {
        invLossOrder = null;

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            getActivity().finish();
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在请求报损单号...", false);

        InvOrderApiImpl.invLossOrderGetCurrentOrder(MfhLoginService.get().getCurOfficeId(),
                StoreType.SUPERMARKET, queryOrderRC);
    }

    private NetCallBack.NetTaskCallBack queryOrderRC = new NetCallBack.NetTaskCallBack<InvLossOrder,
            NetProcessor.Processor<InvLossOrder>>(
            new NetProcessor.Processor<InvLossOrder>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"查询成功!","version":"1","data":null}

                    hideProgressDialog();

                    if (rspData != null) {
                        RspBean<InvLossOrder> retValue = (RspBean<InvLossOrder>) rspData;
                        invLossOrder = retValue.getValue();
                    }

                    if (invLossOrder == null) {
                        DialogUtil.showHint("获取报损单号失败");
                        getActivity().finish();
                    } else {
                        mProviderView.setText(invLossOrder.getOrderName());
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);

                    hideProgressDialog();
                    DialogUtil.showHint("获取报损单号失败");
                    getActivity().finish();
                }
            }
            , InvLossOrder.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 签收
     */
    public void submit() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在报损...", false);

        List<InvLossGoodsEntity> goodsList = goodsAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, "您还没有添加商品", true);
            return;
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, getString(R.string.toast_network_error), true);
            return;
        }

        JSONArray items = new JSONArray();
        for (InvLossGoodsEntity goods : goodsList) {
            JSONObject item = new JSONObject();
            item.put("proSkuId", goods.getProSkuId());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("updateHint", 1);
            items.add(item);
        }

        if (invLossOrder == null) {
            // TODO: 07/02/2017 创建报损单
            DialogUtil.showHint("TODO:创建报损单");
        }
        else{
            InvOrderApiImpl.invLossOrderItemBatchCommit(invLossOrder.getId(),
                    items.toJSONString(), submitCallback);
        }
    }

    private NetCallBack.NetTaskCallBack submitCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("新建报损单失败: " + errMsg);
//                    {"code":"1","msg":"132079网点有仓储单正在处理中...","version":"1","data":null}
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
//                    DialogUtil.showHint("新建退货单失败" + errMsg);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);
                    /**
                     * 新建退货单成功，更新采购单列表
                     * */
                    ZLogger.d("新建报损单成功: ");
                    hideProgressDialog();
                    getActivity().finish();
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        addressRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        addressRecyclerView.setHasFixedSize(true);
        //添加分割线
//        addressRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        addressRecyclerView.setEmptyView(emptyView);

        goodsAdapter = new InvLossOrderGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new InvLossOrderGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                InvLossGoodsEntity entity = goodsAdapter.getEntity(position);
                if (entity != null) {
                    inspect(entity.getBarcode());
                }
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
            }
        });

        addressRecyclerView.setAdapter(goodsAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(goodsAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(addressRecyclerView);
    }

    /**
     * 验货
     */
    @OnClick(R.id.fab_add)
    public void inspect() {
        inspect(null);
    }

    /**盘点验货*/
    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INVLOSS_INSPECTGOODS);
        extras.putString(InvLossInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_DISTRIBUTION_INSPECT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_DISTRIBUTION_INSPECT: {
                goodsAdapter.setEntityList(InvLossGoodsService.get().queryAll());
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {
        if (invLossOrder == null) {
            loadLossOrder();
        }
    }


}
