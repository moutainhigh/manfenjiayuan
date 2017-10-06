package com.manfenjiayuan.pda_supermarket.ui.store.invIo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.bizz.invio.ProductStructureAdapter;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.ProductStructure;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.httpmgr.InvIoOrderHttpManager;
import com.mfh.framework.rxapi.httpmgr.ScProductStructureHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;


/**
 * 库存转换
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvConvertToFragment extends BaseFragment{

    public static final String EXTRA_KEY_INV_SKU_GOODS = "invSkuGoods";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private ProductStructureAdapter goodsAdapter;
    @BindView(R.id.empty_view)
    View emptyView;

    private InvSkuGoods mInvSkuGoods;

    public static InvConvertToFragment newInstance(Bundle args) {
        InvConvertToFragment fragment = new InvConvertToFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invconvert_to;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (mToolbar != null) {
            mToolbar.setTitle("商品配方");
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
            mToolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().onBackPressed();
                        }
                    });
        } else {
            ZLogger.d("mToolbar is null");
        }

        initRecyclerView();

        Bundle args = getArguments();
        if (args != null) {
            mInvSkuGoods = (InvSkuGoods) args.getSerializable(EXTRA_KEY_INV_SKU_GOODS);
        }

        if (mInvSkuGoods == null) {
            DialogUtil.showHint("转换商品无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        loadProductStructure();
    }

    private void initRecyclerView() {
        goodsAdapter = new ProductStructureAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new ProductStructureAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                InvIoGoodsEntity entity = goodsAdapter.getEntity(position);
//                if (entity != null) {
//                    inspect(entity.getBarcode());
//                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {
//                final InvIoGoodsEntity entity = goodsAdapter.getEntity(position);
//                if (operateDialog == null) {
//                    operateDialog = new CommonDialog(getActivity());
//                    operateDialog.setCancelable(true);
//                }
//                operateDialog.setMessage(String.format("%s\n%s", entity.getBarcode(), entity.getProductName()));
//                operateDialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        goodsAdapter.removeEntity(position);
//                    }
//                });
//                operateDialog.setNegativeButton("点错了", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                if (!operateDialog.isShowing()) {
//                    operateDialog.show();
//                }
            }

            @Override
            public void onDataSetChanged() {
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
//        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);

        goodsRecyclerView.setAdapter(goodsAdapter);
    }

    /**
     * 加载商品配方
     * */
    private void loadProductStructure() {
        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            goodsAdapter.setEntityList(null);
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("masterSkuId", String.valueOf(mInvSkuGoods.getProSkuId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        ScProductStructureHttpManager.getInstance().list(options,
                new MQuerySubscriber<ProductStructure>(new PageInfo(-1, 20)){
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<ProductStructure> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        goodsAdapter.setEntityList(dataList);
                    }
                });
    }

    @OnClick(R.id.fab_submit)
    public void submit() {
        if (!NetworkUtils.isConnect(getActivity())) {
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        createIoOrder1();
    }


    /**
     * 源商品
     * */
    private void createIoOrder1() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        JSONObject jsonStr = new JSONObject();
        jsonStr.put("orderType", InvIoOrderApi.ORDER_TYPE_IN);
        jsonStr.put("storeType", InvIoOrderApi.STORE_TYPE_RETAIL);
        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("proSkuId", mInvSkuGoods.getProSkuId());
        item.put("productName", mInvSkuGoods.getName());
        item.put("barcode", mInvSkuGoods.getBarcode());
        item.put("quantityCheck", mInvSkuGoods.getQuantityCheck());
//        item.put("quantityPack", mInvSkuGoods.getQuantityPack());
        item.put("price", mInvSkuGoods.getCostPrice());
        item.put("posId", SharedPrefesManagerFactory.getTerminalId());
        items.add(item);
        jsonStr.put("items", items);

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonStr.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        InvIoOrderHttpManager.getInstance().createIoOrder(options,
                new MValueSubscriber<String>() {

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.df("新建出入库订单失败: " + e.toString());
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        commitIoOrder1(data);
                    }

                });
    }

    private void commitIoOrder1(String orderId) {
        Map<String, String> options = new HashMap<>();
        options.put("orderId", orderId);
//        if (transHumanId != null) {
//            params.put("transHumanId", String.valueOf(transHumanId));
//        }
//        params.put("vehicle", vehicle);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        InvIoOrderHttpManager.getInstance().commitOrder(options,
                new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.df("商品入库失败:" + e.toString());

                        createIoOrder2();
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.df("商品入库成功:" + s);
                        createIoOrder2();
                    }

                });
    }
    /**
     * 配方入库
     * */
    private void createIoOrder2() {
        JSONObject jsonStr = new JSONObject();
        jsonStr.put("orderType", InvIoOrderApi.ORDER_TYPE_OUT);
        jsonStr.put("storeType", InvIoOrderApi.STORE_TYPE_RETAIL);
        JSONArray items = new JSONArray();

        List<ProductStructure> entities = goodsAdapter.getEntityList();
        if (entities != null) {
            for (ProductStructure entity : entities) {
                JSONObject item = new JSONObject();
                item.put("proSkuId", entity.getPartSkuId());
                item.put("productName", entity.getPartSkuName());
//                item.put("barcode", entity.getpa());
                item.put("quantityCheck", MathCompact.mult(entity.getPartNum(), mInvSkuGoods.getQuantityCheck()));
//        item.put("quantityPack", mInvSkuGoods.getQuantityPack());
//                item.put("price", entity.getCostPrice());
                item.put("posId", SharedPrefesManagerFactory.getTerminalId());
                items.add(item);
            }
        }
        jsonStr.put("items", items);

        Map<String, String> options = new HashMap<>();
        options.put("jsonStr", jsonStr.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        InvIoOrderHttpManager.getInstance().createIoOrder(options,
                new MValueSubscriber<String>() {

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.df("新建出入库订单失败: " + e.toString());
                        showProgressDialog(ProgressDialog.STATUS_ERROR, e.getMessage(), true);
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        commitIoOrder2(data);
                    }

                });
    }
    private void commitIoOrder2(String orderId) {
        Map<String, String> options = new HashMap<>();
        options.put("orderId", orderId);
//        if (transHumanId != null) {
//            params.put("transHumanId", String.valueOf(transHumanId));
//        }
//        params.put("vehicle", vehicle);
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        InvIoOrderHttpManager.getInstance().commitOrder(options,
                new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        DialogUtil.showHint(e.getMessage());
                        hideProgressDialog();
//                        getActivity().setResult(Activity.RESULT_OK);
//                        getActivity().finish();
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.df("配方出库成功:" + s);
                        DialogUtil.showHint("库存转换成功");
                        hideProgressDialog();
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }

                });
    }

}
