package com.manfenjiayuan.pda_supermarket.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.StockOutItem;
import com.manfenjiayuan.pda_supermarket.ui.QueryBarcodeFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;


/**
 * 包裹
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PackageFragment extends QueryBarcodeFragment {

    @Bind({R.id.label_receiveName, R.id.label_receivePhone, R.id.label_itemTypeName,
            R.id.label_createdDate, R.id.label_transportName, R.id.label_transHumanInfo})
    List<TextLabelView> labelViews;

    private StockOutItem curPackage = null;

    public static PackageFragment newInstance(Bundle args) {
        PackageFragment fragment = new PackageFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_package;
    }


    @Override
    public boolean isRootFlow() {
        return true;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);
        btnSubmit.setEnabled(false);
    }

    /**
     * 查询包裹信息
     */
    @Override
    public void sendQueryReq(String barcode) {
        super.sendQueryReq(barcode);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
        }
        else{
            // TODO: 7/30/16 执行查询动作
            curPackage = null;
//        animProgress.setVisibility(View.VISIBLE);
//        btnStockOut.setEnabled(false);
            //查询出库列表
            StockApiImpl.findStockOutByCode(barcode,
                    new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<StockOutItem>(new PageInfo(1, 20)) {
                        @Override
                        public void processQueryResult(RspQueryResult<StockOutItem> rs) {
                            //此处在主线程中执行。
                            int retSize = rs.getReturnNum();
                            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

                            if (retSize > 0) {
                                refreshPackage(rs.getRowEntity(0));
                            } else {
                                DialogUtil.showHint("未查询到结果");
                                refreshPackage(null);
                            }
                            onQuerySuccess();
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);

                            onQueryError(errMsg);
                        }
                    }, StockOutItem.class, AppContext.getAppContext()));
        }
    }

    @Override
    public void onQuerySuccess() {
        super.onQuerySuccess();
    }

    @Override
    public void onQueryError(String errorMsg) {
        super.onQueryError(errorMsg);

        refreshPackage(null);
    }

    @Override
    public void submit() {
        btnSubmit.setEnabled(false);
        if (curPackage == null) {
            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            btnSubmit.setEnabled(true);
            return;
        }


        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        //TODO,加载完成后立即准备
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        //orderId、items、btype、curStatus等通过findStockOut查询获取的待出库列表信息中有。
        jsonObject.put("orderId", curPackage.getGoodsId());//包裹物件Id
        jsonObject.put("items", curPackage.getItems());//物件内部明细id(可空)
        jsonObject.put("stockId", curPackage.getStockId());//仓库编号
        jsonObject.put("tokentype", 0);//自提(0)或代取(1)
        jsonObject.put("transHumanId", curPackage.getTransHumanId());//物流承担者人或车辆Id（可空)
        jsonObject.put("btype", curPackage.getItemType());//包裹业务类型
        jsonObject.put("curStatus", curPackage.getStatus());//包裹当前状态
        jsonArray.add(jsonObject);

//        animProgress.setVisibility(View.VISIBLE);

        StockApiImpl.stockOut(jsonArray.toJSONString(), stockoutResponseCallback);
    }

    //回调
    NetCallBack.NetTaskCallBack stockoutResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                    RspValue<String> retValue = (RspValue<String>) rspData;
//                    String retStr = retValue.getValue();

                    //出库成功:1-556637
                    showProgressDialog(ProgressDialog.STATUS_ERROR, "出库成功", true);
                    refreshPackage(null);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("出库失败：" + errMsg);

                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
//                        animProgress.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };

    /**
     * */
    private void refreshPackage(StockOutItem stockOutItem) {
        refresh();
        curPackage = stockOutItem;
        if (curPackage == null) {
            labelViews.get(0).setTvSubTitle("");
            labelViews.get(1).setTvSubTitle("");
            labelViews.get(2).setTvSubTitle("");
            labelViews.get(3).setTvSubTitle("");
            labelViews.get(4).setTvSubTitle("");
            labelViews.get(5).setTvSubTitle("");

            btnSubmit.setEnabled(false);
        } else {
            labelViews.get(0).setTvSubTitle(curPackage.getReceiveName());
            labelViews.get(1).setTvSubTitle(curPackage.getReceivePhone());
            labelViews.get(2).setTvSubTitle(curPackage.getItemTypeName());
            labelViews.get(3).setTvSubTitle(curPackage.getCreatedDate());
            labelViews.get(4).setTvSubTitle(curPackage.getTransportName());
            labelViews.get(5).setTvSubTitle(String.format("%d", curPackage.getTransHumanId()));

            btnSubmit.setEnabled(true);
        }
    }

}
