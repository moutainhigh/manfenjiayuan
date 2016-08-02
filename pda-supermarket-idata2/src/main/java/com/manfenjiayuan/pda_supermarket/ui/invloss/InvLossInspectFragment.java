package com.manfenjiayuan.pda_supermarket.ui.invloss;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.database.entity.InvLossGoodsEntity;
import com.bingshanguxue.pda.database.service.InvLossGoodsService;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.QueryBarcodeFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;
import java.util.List;

import butterknife.Bind;


/**
 * 报损－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvLossInspectFragment extends QueryBarcodeFragment implements IScGoodsSkuView {

    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelName;
    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    private InvLossGoodsEntity curGoods = null;
    private ScGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvLossInspectFragment newInstance(Bundle args) {
        InvLossInspectFragment fragment = new InvLossInspectFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invloss_goods;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);
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

        fabCancel.setVisibility(View.VISIBLE);

        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

//            eqvBarcode.setInputString(barcode);
            queryByBarcode(barcode);
        }

    }

    /**
     * 查询商品
     * */
    @Override
    public void sendQueryReq(String barcode) {
        super.sendQueryReq(barcode);
        QueryGoodsAsyncTask queryGoodsAsyncTask = new QueryGoodsAsyncTask(barcode);
        queryGoodsAsyncTask.execute();
    }

    @Override
    public void cancel() {
        super.cancel();
        InvLossGoodsService.get().reject(curGoods);

        refreshPackage(null);
    }

    @Override
    public void submit() {
        super.submit();

        String quantityStr = labelSignQuantity.getEtContent();
        if (StringUtils.isEmpty(quantityStr)){
            onSubmitError("请输入报损数量");
            return;
        }
        Double quantityCheck = Double.valueOf(quantityStr);

        if (curGoods != null && curGoods.getQuantityCheck() > 0){
            quantityCheckConfirmDialog(curGoods, quantityCheck);
        }
        else{
            InvLossGoodsService.get().inspect(curGoods, quantityCheck);
            onSubmitSuccess();

        }
    }

    @Override
    public void onSubmitSuccess() {
        super.onSubmitSuccess();
        refreshPackage(null);
    }

    /**
     * 刷新信息
     * */
    private void refreshPackage(InvLossGoodsEntity goods){
        refresh();
        curGoods = goods;
        if (curGoods == null){
            labelBarcode.setTvSubTitle("");
            labelName.setTvSubTitle("");
            labelSignQuantity.setEtContent("");

            fabCancel.setEnabled(false);
            btnSubmit.setEnabled(false);
        }
        else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelName.setTvSubTitle(curGoods.getProductName());

            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setEtContent("");

            fabCancel.setEnabled(true);
            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }
    }


    @Override
    public void onIScGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        refreshPackage(null);
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        hideProgressDialog();

        if (dataList != null && dataList.size() > 0) {
            saveScGoodsSku(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku data) {
        hideProgressDialog();

        if (data != null) {
            saveScGoodsSku(data);
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    class QueryGoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String barcode;
        private InvLossGoodsEntity goodsEntity;

        public QueryGoodsAsyncTask( String barcode) {
            this.barcode = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                goodsEntity = InvLossGoodsService.get().queryEntityBy(barcode);
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
                saveInvLossGoodsEntity(goodsEntity);
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

        chainGoodsSkuPresenter.getGoodsByBarCode(barcode);
    }


    public void saveInvLossGoodsEntity(InvLossGoodsEntity goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvLossGoodsEntity entity = InvLossGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvLossGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProSkuId(goods.getProSkuId());
            entity.setProductName(goods.getProductName());
            entity.setBarcode(goods.getBarcode());

            entity.setQuantityCheck(1D);
            entity.setUpdatedDate(new Date());

            InvLossGoodsService.get().saveOrUpdate(entity);

        }
        refreshPackage(entity);
    }


    /**
     * 保存搜索商品
     * */
    private void saveScGoodsSku(ScGoodsSku goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvLossGoodsEntity entity = InvLossGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvLossGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProSkuId(goods.getProSkuId());
            entity.setProductName(goods.getSkuName());
            entity.setBarcode(goods.getBarcode());

            //默认数量是0
            entity.setQuantityCheck(0D);

            entity.setUpdatedDate(new Date());

            InvLossGoodsService.get().saveOrUpdate(entity);
        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;
    private void quantityCheckConfirmDialog(final InvLossGoodsEntity entity, final Double quantity){
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(true);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经签收 %.2f 件，请选择 [覆盖] or [累加]",
                entity.getQuantityCheck()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvLossGoodsService.get().inspect(entity, quantity);

                onSubmitSuccess();
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvLossGoodsService.get().inspect(entity, entity.getQuantityCheck() + quantity);
                onSubmitSuccess();
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
