package com.manfenjiayuan.pda_wholesaler.ui.fragment;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.database.entity.InvLossGoodsEntity;
import com.bingshanguxue.pda.database.service.InvLossGoodsService;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_wholesaler.R;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 报损－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvLossInspectFragment extends PDAScanFragment implements IInvSkuGoodsView {

    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";
    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelName;
    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    private InvLossGoodsEntity curGoods = null;
    private InvSkuGoodsPresenter chainGoodsSkuPresenter;


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

        chainGoodsSkuPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (mToolbar != null) {
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

        if (mScanBar != null) {
            mScanBar.setSoftKeyboardEnabled(true);
            mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }

                @Override
                public void onAction1Click(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }
            });
        } else {
            ZLogger.d("mScanBar is null");
        }
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

//            eqvBarcode.setInputString(barcode);
            queryByBarcode(barcode);
        }

    }
    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
        mScanBar.reset();
        queryByBarcode(code);
    }
    /**
     * 查询商品
     * */
    public void queryByBarcode(String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.reset();
            isAcceptBarcodeEnabled = true;
            return;
        }

        QueryGoodsAsyncTask queryGoodsAsyncTask = new QueryGoodsAsyncTask(barcode);
        queryGoodsAsyncTask.execute();
    }

    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        onSubmitProcess();

        if (curGoods == null){
            onSubmitError("商品无效");
            return;
        }
        String quantityStr = labelSignQuantity.getInput();
        if (StringUtils.isEmpty(quantityStr)){
            onSubmitError("请输入报损数量");
            return;
        }
        Double quantityCheck = Double.valueOf(quantityStr);

        if (curGoods.getQuantityCheck() > 0){
            hideProgressDialog();
            quantityCheckConfirmDialog(curGoods, quantityCheck);
        }
        else{
            InvLossGoodsService.get().inspect(curGoods, quantityCheck);
            onSubmitSuccess();
        }
    }

    /**
     * 提交处理中
     */
    public void onSubmitProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 提交失败
     */
    public void onSubmitError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
            ZLogger.df(errorMsg);
        } else {
            hideProgressDialog();
        }
        isAcceptBarcodeEnabled = true;
        btnSubmit.setEnabled(true);
    }

    /**
     * 提交成功
     */
    public void onSubmitSuccess() {
//        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
        hideProgressDialog();
        refreshPackage(null);
    }

    /**
     * 刷新信息
     * */
    private void refreshPackage(InvLossGoodsEntity goods){
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = goods;
        if (curGoods == null){
            labelBarcode.setTvSubTitle("");
            labelName.setTvSubTitle("");
            labelSignQuantity.setInput("");

            btnSubmit.setEnabled(false);
        }
        else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelName.setTvSubTitle(curGoods.getProductName());

            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setInput("");

            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }
    }


    @Override
    public void onIInvSkuGoodsViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onIInvSkuGoodsViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        refreshPackage(null);
    }


    @Override
    public void onIInvSkuGoodsViewSuccess(InvSkuGoods data) {
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
                hideProgressDialog();

                saveInvLossGoodsEntity(goodsEntity);
            }
            else{
                queryNetGoods(barcode);
            }
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            onIInvSkuGoodsViewProcess();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    private void queryNetGoods(String barcode){
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onIInvSkuGoodsViewError(getString(R.string.toast_network_error));
            return;
        }

        chainGoodsSkuPresenter.getByBarcodeMust(barcode);
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
    private void saveScGoodsSku(InvSkuGoods goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvLossGoodsEntity entity = InvLossGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvLossGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProSkuId(goods.getProSkuId());
            entity.setProductName(goods.getName());
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
            quantityCheckConfirmDialog.setCancelable(false);
            quantityCheckConfirmDialog.setCanceledOnTouchOutside(false);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经签收 %.2f 件，请选择 [覆盖] or [累加]",
                entity.getQuantityCheck()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvLossGoodsService.get().inspect(entity, quantity);

                refreshPackage(null);
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvLossGoodsService.get().inspect(entity, entity.getQuantityCheck() + quantity);

                refreshPackage(null);
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
