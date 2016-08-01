package com.manfenjiayuan.pda_supermarket.ui.invio;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.database.entity.InvIoGoodsEntity;
import com.bingshanguxue.pda.database.service.InvIoGoodsService;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.QueryBarcodeFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 出入库商品检查
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvIoGoodsInspectFragment extends QueryBarcodeFragment
        implements IChainGoodsSkuView {

    private static final String TAG = "InvReturnGoodsInspectFragment";
    public static final String EXTRA_KEY_COMPANYID = "companyId";
    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_price)
    EditLabelView labelPrice;
    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    private InvIoGoodsEntity curGoods = null;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvIoGoodsInspectFragment newInstance(Bundle args) {
        InvIoGoodsInspectFragment fragment = new InvIoGoodsInspectFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invio_goods;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ChainGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        labelPrice.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelPrice.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelSignQuantity.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
        labelSignQuantity.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelSignQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submit();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

            queryByBarcode(barcode);
        }

    }

    /**
     * 查询商品
     * */
    @Override
    public void sendQueryReq(String barcode) {
        super.sendQueryReq(barcode);

        if (StringUtils.isEmpty(barcode)){
            return;
        }

        QueryGoodsAsyncTask queryGoodsAsyncTask = new QueryGoodsAsyncTask(barcode);
        queryGoodsAsyncTask.execute();
    }


    @OnClick(R.id.button_submit)
    public void submit() {
        String price = labelPrice.getEtContent();
        if (StringUtils.isEmpty(price)){
            DialogUtil.showHint("请输入发货价格");
            return;
        }

        String quantityStr = labelSignQuantity.getEtContent();
        if (StringUtils.isEmpty(quantityStr)){
            DialogUtil.showHint("请输入签收数量");
            return;
        }
        Double quantityCheck = Double.valueOf(quantityStr);

        if (curGoods != null && curGoods.getQuantityCheck() > 0){
            quantityCheckConfirmDialog(curGoods, Double.valueOf(price), quantityCheck);
        }
        else{
            InvIoGoodsService.get().inspect(curGoods, Double.valueOf(price), quantityCheck);

            refreshPackage(null);
        }
    }


    /**
     * 刷新信息
     * */
    private void refreshPackage(InvIoGoodsEntity goods){
        curGoods = goods;
        if (curGoods == null){
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelPrice.setEtContent("");
            labelSignQuantity.setEtContent("");

            btnSubmit.setEnabled(false);
        }
        else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getProductName());
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            labelPrice.setEtContent(MUtils.formatDouble(curGoods.getPrice(), ""));
            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setEtContent("");

            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelSignQuantity);

        refresh();
    }

    @Override
    public void onChainGoodsSkuViewProcess() {

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onChainGoodsSkuViewError(String errorMsg) {
        hideProgressDialog();
        refreshPackage(null);
    }

    @Override
    public void onChainGoodsSkuViewSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        hideProgressDialog();
        if (dataList != null && dataList.size() > 0) {
            saveChainGoodsSku(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onChainGoodsSkuViewSuccess(ChainGoodsSku data) {
        hideProgressDialog();

        if (data != null) {
            saveChainGoodsSku(data);
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    class QueryGoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String barcode;
        private InvIoGoodsEntity goodsEntity;

        public QueryGoodsAsyncTask( String barcode) {
            this.barcode = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                goodsEntity = InvIoGoodsService.get().queryEntityBy(barcode);
                if (goodsEntity != null){
                    return true;
                }
            }
            catch (Exception e){
                ZLogger.d("查询本地收货商品失败, " + e.toString());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean){
                onQuerySuccess();
                saveInvIoGoodsEntity(goodsEntity);
            }
            else{
                queryNetGoods(barcode);
            }
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            onQueryProcess();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    private void queryNetGoods(String barcode){
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
            return;
        }

//        chainGoodsSkuPresenter.findTenantSku(new PageInfo(-1, 10),
//                null, barcode);

        chainGoodsSkuPresenter.getTenantSkuMust(null, barcode);
    }


    @Override
    public void onQuerySuccess() {
        super.onQuerySuccess();

        refreshPackage(null);
    }


    public void saveInvIoGoodsEntity(InvIoGoodsEntity goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvIoGoodsEntity entity = InvIoGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvIoGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setProductName(goods.getProductName());
            entity.setPrice(goods.getPrice());
            entity.setUnit(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setQuantityCheck(goods.getQuantityCheck());
            entity.setPosId(goods.getPosId());

            entity.setUpdatedDate(new Date());

            InvIoGoodsService.get().saveOrUpdate(entity);

        }
        refreshPackage(entity);
    }


    /**
     * 保存搜索商品
     * */
    private void saveChainGoodsSku(ChainGoodsSku goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        if (goods.getProSkuId() == null && StringUtils.isEmpty(goods.getBarcode())) {
            DialogUtil.showHint(" proSkuId和barcode不能同时为空");
            return;
        }

        InvIoGoodsEntity entity = InvIoGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvIoGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setProductName(goods.getSkuName());
            entity.setPrice(goods.getSingleCostPrice());
            entity.setUnit(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setQuantityCheck(0D);
            entity.setUpdatedDate(new Date());
            entity.setPosId(SharedPreferencesManager.getTerminalId());

            InvIoGoodsService.get().saveOrUpdate(entity);
        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;
    private void quantityCheckConfirmDialog(final InvIoGoodsEntity entity,
                                            final Double price, final Double quantity){
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(true);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经选择%.2f件，请选择[覆盖]还是[累加]",
                entity.getQuantityCheck()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvIoGoodsService.get().inspect(entity, price, quantity);

                refreshPackage(null);
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvIoGoodsService.get().inspect(entity, price, entity.getQuantityCheck() + quantity);

                refreshPackage(null);
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
