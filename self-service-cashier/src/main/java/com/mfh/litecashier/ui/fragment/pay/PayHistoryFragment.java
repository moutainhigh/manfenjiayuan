package com.mfh.litecashier.ui.fragment.pay;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.database.dao.PosOrderDao;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;
import com.bingshanguxue.cashier.database.service.PosOrderPayService;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.v1.CashierAgent;
import com.bingshanguxue.cashier.v1.CashierOrderInfo;
import com.bingshanguxue.cashier.v1.CashierProvider;
import com.mfh.comn.net.ResponseBody;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.pay.PayApi;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.EmptyEntity;
import com.mfh.litecashier.service.DataUploadManager;
import com.mfh.litecashier.ui.adapter.PayHistoryAdapter;

import java.util.List;

import butterknife.BindView;


/**
 * 对话框 -- 订单支付记录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PayHistoryFragment extends BaseListFragment<PosOrderPayEntity> {

    public static final String EXTRA_KEY_ORDER_ID = "orderId";
    public static final String EXTRA_KEY_EDITABLE = "editable";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.product_list)
    RecyclerView mRecyclerView;
    private PayHistoryAdapter productAdapter;

    private Long orderId;
    private boolean editable = false;

    public static PayHistoryFragment newInstance(Bundle args) {
        PayHistoryFragment fragment = new PayHistoryFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialogview_pay_history;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            orderId = args.getLong(EXTRA_KEY_ORDER_ID);
            editable = args.getBoolean(EXTRA_KEY_EDITABLE);
        }
        ZLogger.d(String.format("orderId=%d, editable=%b", orderId, editable));
        mToolbar.setTitle("支付记录");
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    getActivity().onBackPressed();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_normal);

        initRecyclerView();

        reload();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void reload() {
        super.reload();
        List<PosOrderPayEntity> orderEntityList = PosOrderPayService.get()
                .queryAllBy(String.format("orderId = '%d'", orderId),
                        PosOrderDao.ORDER_BY_UPDATEDATE_DESC);

        productAdapter.setEntityList(orderEntityList);
    }

    private void finishActivity() {
        getActivity().finish();
        getActivity().setResult(Activity.RESULT_OK);
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getContext(), LineItemDecoration.VERTICAL_LIST));
        productAdapter = new PayHistoryAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new PayHistoryAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
                final PosOrderPayEntity entity = productAdapter.getEntity(position);
                if (entity == null) {
                    return;
                }

                if (!editable){
                    ZLogger.d("item can not be edit");
                    return;
                }

                if (PosOrderPayEntity.PAY_STATUS_FINISH != entity.getPaystatus()){
                    if (WayType.ALI_F2F.equals(entity.getPayType())) {
                        // TODO: 9/14/16  查询订单支付状态
                        showConfirmDialog(String.format("订单支付状态：%s",
                                PosOrderPayEntity.getPayStatusDesc(entity.getPaystatus())),
                                "查询支付状态", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        queryAlipayOrder(entity);
                                        dialog.dismiss();
                                    }
                                }, "点错了", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                    }
                    else if (WayType.WX_F2F.equals(entity.getPayType())) {
                        // TODO: 9/14/16  查询订单支付状态
                        showConfirmDialog(String.format("订单支付状态：%s",
                                PosOrderPayEntity.getPayStatusDesc(entity.getPaystatus())),
                                "查询支付状态", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        queryWxpayOrder(entity);
                                        dialog.dismiss();
                                    }
                                }, "点错了", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                    }
                }

            }

            @Override
            public void onDataSetChanged() {

            }
        });

        mRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 支付宝支付--轮询查询订单状态
     * <b>应用场景实例：</b>本接口提供支付宝支付订单的查询的功能，商户可以通过本接口主动查询订单状态，完成下一步的业务逻辑。<br>
     * 需要调用查询接口的情况：<br>
     * 1. 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；<br>
     * 2. 调用扫码支付支付接口后，返回系统错误或未知交易状态情况；<br>
     * 3. 调用扫码支付请求后，如果结果返回处理中（返回结果中的code等于10003）的状态；<br>
     * 4. 调用撤销接口API之前，需确认该笔交易目前支付状态。<br>
     */
    private void queryAlipayOrder(final PosOrderPayEntity entity) {
        NetCallBack.RawNetTaskCallBack payRespCallback = new NetCallBack.RawNetTaskCallBack<EmptyEntity,
                NetProcessor.RawProcessor<EmptyEntity>>(
                new NetProcessor.RawProcessor<EmptyEntity>() {

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("支付宝条码支付状态查询:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));

                        switch (rspBody.getRetCode()) {
                            //业务处理成功
                            // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                            case "0":
                                showProgressDialog(ProgressDialog.STATUS_DONE, "支付成功", true);

                                entity.setPaystatus(PosOrderPayEntity.PAY_STATUS_FINISH);
                                PosOrderPayService.get().saveOrUpdate(entity);

                                autoFixOrder(entity.getOrderId());
                                break;
                            //{"code":"-1","msg":"Success","version":"1","data":""}
                            // 支付结果不明确，需要收银员继续查询或撤单
                            case "-1":
                                showProgressDialog(ProgressDialog.STATUS_DONE, "支付异常", true);

                                entity.setPaystatus(PosOrderPayEntity.PAY_STATUS_FINISH);
                                PosOrderPayService.get().saveOrUpdate(entity);

                                autoFixOrder(entity.getOrderId());
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default:
                                showProgressDialog(ProgressDialog.STATUS_ERROR, "支付失败", true);

                                entity.setPaystatus(PosOrderPayEntity.PAY_STATUS_FINISH);
                                PosOrderPayService.get().saveOrUpdate(entity);

                                autoFixOrder(entity.getOrderId());
                                break;
                        }
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询订单状态失败:" + errMsg);
                        //TODO 调用支付宝支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }
                }
                , EmptyEntity.class
                , CashierApp.getAppContext()) {
        };

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        PayApi.queryAliBarpayStatus(entity.getOutTradeNo(), payRespCallback);
    }

    /**
     * 微信支付--轮询查询订单状态
     * <b>应用场景实例：</b>本接口提供微信支付订单的查询的功能，商户可以通过本接口主动查询订单状态，完成下一步的业务逻辑。<br>
     * 需要调用查询接口的情况：<br>
     * 1. 当商户后台、网络、服务器等出现异常，商户系统最终未接收到支付通知；<br>
     * 2. 调用扫码支付支付接口后，返回系统错误或未知交易状态情况；<br>
     * 3. 调用扫码支付请求后，如果结果返回处理中（返回结果中的code等于10003）的状态；<br>
     * 4. 调用撤销接口API之前，需确认该笔交易目前支付状态。<br>
     */
    private void queryWxpayOrder(final PosOrderPayEntity entity) {
        NetCallBack.RawNetTaskCallBack payRespCallback = new NetCallBack.RawNetTaskCallBack<EmptyEntity,
                NetProcessor.RawProcessor<EmptyEntity>>(
                new NetProcessor.RawProcessor<EmptyEntity>() {

                    @Override
                    public void processResult(ResponseBody rspBody) {
                        ZLogger.df(String.format("微信条码支付状态查询:%s--%s", rspBody.getRetCode(), rspBody.getReturnInfo()));

                        //业务处理成功
                        // 10000--"trade_status": "TRADE_SUCCESS",交易支付成功
                        switch (rspBody.getRetCode()) {
                            case "0":
                                showProgressDialog(ProgressDialog.STATUS_DONE, "支付成功", true);

                                entity.setPaystatus(PosOrderPayEntity.PAY_STATUS_FINISH);
                                PosOrderPayService.get().saveOrUpdate(entity);

                                autoFixOrder(entity.getOrderId());
                                break;
                            //{"code":"-1","msg":"继续查询","version":"1","data":""}
                            // 支付结果不明确，需要收银员继续查询或撤单
                            case "-1": //-1
                                showProgressDialog(ProgressDialog.STATUS_DONE, "支付异常", true);

                                entity.setPaystatus(PosOrderPayEntity.PAY_STATUS_EXCEPTION);
                                PosOrderPayService.get().saveOrUpdate(entity);
                                break;
                            //10000--"trade_status": "WAIT_BUYER_PAY",交易创建，等待买家付款
                            //10000--"trade_status": "TRADE_CLOSED",未付款交易超时关闭，支付完成后全额退款
                            //10000--"trade_status": "TRADE_FINISHED",交易结束，不可退款
                            // 处理失败,交易不存在
                            //40004--"sub_code": "ACQ.TRADE_NOT_EXIST",
                            default: //-2
                                showProgressDialog(ProgressDialog.STATUS_ERROR, "支付失败", true);

                                entity.setPaystatus(PosOrderPayEntity.PAY_STATUS_FAILED);
                                PosOrderPayService.get().saveOrUpdate(entity);
                                break;
                        }
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.df("查询订单状态失败:" + errMsg);
                        //TODO 调用微信支付接口时未返回明确的返回结果(如由于系统错误或网络异常导致无返回结果)，需要将交易进行撤销。
                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }
                }
                , EmptyEntity.class
                , CashierApp.getAppContext()) {
        };

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        PayApi.queryWxBarpayStatus(entity.getOutTradeNo(), payRespCallback);
    }


    /**
     * 自动把已经支付完成的订单状态修改成已支付
     * */
    private void autoFixOrder(Long orderId){
        PosOrderEntity orderEntity = PosOrderService.get().getEntityById(String.valueOf(orderId));
        if (orderEntity == null) {
            ZLogger.df("订单不存在");
        }
        CashierOrderInfo cashierOrderInfo = CashierAgent.makeCashierOrderInfo(orderEntity, null);
        Double handleAmount = CashierProvider.getHandleAmount(cashierOrderInfo);
        ZLogger.df("handleAmount=" + handleAmount);
        if (handleAmount < 0.01) {
            //修改订单支付信息（支付金额，支付状态）
            CashierAgent.updateCashierOrder(cashierOrderInfo, null, PosOrderEntity.ORDER_STATUS_FINISH);

            DataUploadManager.getInstance().stepUploadPosOrder(orderEntity);
        }

        productAdapter.notifyDataSetChanged();
    }
}
