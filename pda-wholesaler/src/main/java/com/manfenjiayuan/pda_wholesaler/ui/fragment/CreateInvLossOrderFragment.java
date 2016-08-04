package com.manfenjiayuan.pda_wholesaler.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.bizz.invloss.InvLossOrderGoodsAdapter;
import com.bingshanguxue.pda.database.entity.InvLossGoodsEntity;
import com.manfenjiayuan.business.bean.InvLossOrder;
import com.manfenjiayuan.pda_wholesaler.R;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;




/**
 * 新建报损单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateInvLossOrderFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private InvLossOrderGoodsAdapter officeAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view)
    View emptyView;

    private InvLossOrder invLossOrder = null;

    public static CreateInvLossOrderFragment newInstance(Bundle args) {
        CreateInvLossOrderFragment fragment = new CreateInvLossOrderFragment();

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
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
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

//        Bundle args = getArguments();
//        if (args != null) {
////            invSendOrder = (InvSendOrder)args.getSerializable("sendOrder");
//        }


        loadLossOrder();
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (companyInfo == null){
//            selectInvCompProvider();
//        }
//        else{
//            eqvBarcode.requestFocus();
//            eqvBarcode.clear();
//        }

    }

    @Override
    public boolean onBackPressed() {
//        DialogUtil.showHint("onBackPressed");
        if (officeAdapter.getItemCount() > 0) {
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
        }
        else{
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }


    private void loadLossOrder(){
        invLossOrder = null;

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            getActivity().finish();
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在请求报损单号...", false);

        InvOrderApiImpl.invLossOrderGetCurrentOrder(MfhLoginService.get().getCurOfficeId(),
                StoreType.WHOLESALER, queryOrderRC);
    }

    private NetCallBack.NetTaskCallBack queryOrderRC = new NetCallBack.NetTaskCallBack<InvLossOrder,
            NetProcessor.Processor<InvLossOrder>>(
            new NetProcessor.Processor<InvLossOrder>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"查询成功!","version":"1","data":null}

                    hideProgressDialog();

                    if (rspData != null){
                        RspBean<InvLossOrder> retValue = (RspBean<InvLossOrder>) rspData;
                        invLossOrder = retValue.getValue();
                    }

                    if (invLossOrder == null){
                        DialogUtil.showHint("获取报损单号失败");
                        getActivity().finish();
                    }
                    else{
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
        showProgressDialog(ProgressDialog.STATUS_PROCESSING);

        List<InvLossGoodsEntity> goodsList = officeAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            DialogUtil.showHint("商品不能为空");
            hideProgressDialog();
            return;
        }

        if (invLossOrder == null){
//            DialogUtil.showHint("请点击屏幕右上角的'+'号新建盘点");
            DialogUtil.showHint("报损单号不能为空，请退出重试");

            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            hideProgressDialog();
            return;
        }

        JSONArray items = new JSONArray();
        for (InvLossGoodsEntity goods : goodsList){
            JSONObject item = new JSONObject();
            item.put("proSkuId", goods.getProSkuId());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("updateHint", 1);
            items.add(item);
        }

        InvOrderApiImpl.invLossOrderItemBatchCommit(invLossOrder.getId(),
                items.toJSONString(), submitCallback);
    }

    private NetCallBack.NetTaskCallBack submitCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("新建退货单失败: " + errMsg);
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
                    ZLogger.d("新建退货单成功: ");
                    hideProgressDialog();
                    getActivity().finish();

//                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


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

        officeAdapter = new InvLossOrderGoodsAdapter(getActivity(), null);
        officeAdapter.setOnAdapterListener(new InvLossOrderGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                CreateOrderItemWrapper entity = officeAdapter.getEntity(position);
//                inspect(entity.getBarcode());
//                changeQuantityCheck();
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
            }
        });

        addressRecyclerView.setAdapter(officeAdapter);
    }


    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {
        if (invLossOrder == null){
            loadLossOrder();
        }
    }



}
