package com.manfenjiayuan.pda_supermarket.ui.invreturn;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.manfenjiayuan.business.mvp.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.mvp.view.IChainGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.InvReturnGoodsEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.InvReturnGoodsService;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditLabelView;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditQueryView;
import com.manfenjiayuan.pda_supermarket.widget.compound.TextLabelView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品退货－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvReturnGoodsInspectFragment extends PDAScanFragment implements IChainGoodsSkuView {

    private static final String TAG = "InvReturnGoodsInspectFragment";
    public static final String EXTRA_KEY_INSPECTMODE = "inspectMode";
    public static final String EXTRA_KEY_COMPANYID = "companyId";
    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;

    private final static int LABELVIEW_INDEX_BARCODE = 0;
    private final static int LABELVIEW_INDEX_NAME = 1;
    private final static int LABELVIEW_INDEX_TOTALCOUNT = 2;
    @Bind({R.id.label_barcode, R.id.label_productName, R.id.label_totalcount })
    List<TextLabelView> labelViews;
    @Bind(R.id.label_price)
    EditLabelView labelPrice;
    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    @Bind(R.id.button_reject) Button btnReject;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    private int inspectMode = 0;
    private Long companyId;
    private InvReturnGoodsEntity curGoods = null;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;
    private boolean isQueryProcessing;


    public static InvReturnGoodsInspectFragment newInstance(Bundle args) {
        InvReturnGoodsInspectFragment fragment = new InvReturnGoodsInspectFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inspect_invreturn_goods;
    }

    @Override
    protected void onScanCode(String code) {
//        eqvBarcode.setInputString(code);
        query(code);
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

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            inspectMode = args.getInt(EXTRA_KEY_INSPECTMODE, 0);
            companyId = args.getLong(EXTRA_KEY_COMPANYID);
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

//            eqvBarcode.setInputString(barcode);
            query(barcode);
        }

    }

    /**
     * 查询包裹信息
     * */
    public void query(String barcode){
        eqvBarcode.clear();

        if (isQueryProcessing || StringUtils.isEmpty(barcode)){
            return;
        }

        QueryGoodsAsyncTask queryGoodsAsyncTask = new QueryGoodsAsyncTask(barcode);
        queryGoodsAsyncTask.execute();
    }

    @OnClick(R.id.button_reject)
    public void reject(){
        InvReturnGoodsService.get().reject(curGoods);

        refreshPackage(null);
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
            InvReturnGoodsService.get().inspect(curGoods, Double.valueOf(price), quantityCheck);

            refreshPackage(null);
        }
    }


    /**
     * 刷新信息
     * */
    private void refreshPackage(InvReturnGoodsEntity goods){
        curGoods = goods;
        if (curGoods == null){
            labelViews.get(LABELVIEW_INDEX_BARCODE).setTvSubTitle("");
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle("");
            labelViews.get(LABELVIEW_INDEX_TOTALCOUNT).setTvSubTitle("");
            labelPrice.setEtContent("");
            labelSignQuantity.setEtContent("");

            btnReject.setEnabled(false);
            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();
        }
        else {
            labelViews.get(LABELVIEW_INDEX_BARCODE).setTvSubTitle(curGoods.getBarcode());
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle(curGoods.getProductName());
            labelViews.get(LABELVIEW_INDEX_TOTALCOUNT).setTvSubTitle(MUtils.formatDouble(curGoods.getTotalCount(), ""));
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            labelPrice.setEtContent(MUtils.formatDouble(curGoods.getPrice(), ""));
            //默认签收数量为空，根据实际情况填写
            labelSignQuantity.setEtContent("");

            btnReject.setEnabled(true);
            btnSubmit.setEnabled(true);

            labelSignQuantity.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelSignQuantity);
    }

    class QueryGoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String barcode;
        private InvReturnGoodsEntity goodsEntity;

        public QueryGoodsAsyncTask( String barcode) {
            this.barcode = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                goodsEntity = InvReturnGoodsService.get().queryEntityBy(barcode);
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
                onError("");
                saveInvReturnGoodsEntity(goodsEntity);
            }
            else{
                if (inspectMode == 0){
                    queryNetGoods(barcode);
                }
                else{
                    DialogUtil.showHint("未找到商品");
                    onError("");
                }
            }
        }

        @Override
        protected void onPreExecute() {
            ZLogger.d("onPreExecute");
            onProcess();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            ZLogger.d("onProgressUpdate");
        }
    }

    private void queryNetGoods(String barcode){
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            onError(getString(R.string.toast_network_error));
            return;
        }

//        chainGoodsSkuPresenter.findTenantSku(new PageInfo(-1, 10),
//                null, barcode);

        chainGoodsSkuPresenter.getTenantSkuMust(null, barcode);
    }

    @Override
    public void onProcess() {
        isQueryProcessing = true;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onError(String errorMsg) {
        hideProgressDialog();
        isQueryProcessing = false;
        refreshPackage(null);
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<ChainGoodsSku> dataList) {
        hideProgressDialog();
        isQueryProcessing = false;

        if (dataList != null && dataList.size() > 0) {
            saveChainGoodsSku(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    @Override
    public void onQueryChainGoodsSku(ChainGoodsSku chainGoodsSku) {
        hideProgressDialog();
        isQueryProcessing = false;

        if (chainGoodsSku != null) {
            saveChainGoodsSku(chainGoodsSku);
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }

    public void saveInvReturnGoodsEntity(InvReturnGoodsEntity goods){
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvReturnGoodsEntity entity = InvReturnGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvReturnGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProductId(goods.getProductId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getChainSkuId());
            entity.setProductName(goods.getProductName());
            entity.setPrice(goods.getPrice());
            entity.setUnitSpec(goods.getUnitSpec());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getProviderId());
            entity.setIsPrivate(goods.getIsPrivate());

            entity.setTotalCount(1D);
            entity.setQuantityCheck(entity.getTotalCount());
            entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_NONE);

            //设置金额
            if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
                entity.setAmount(0D);
            } else {
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setUpdatedDate(new Date());

            InvReturnGoodsService.get().saveOrUpdate(entity);

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

//        if (goods.getSingleCostPrice() == null) {
//            //“如果singleCostPrice值为null，说明缺少箱规数，信息不完整，这种情况你不允许进行采购或收货
//            DialogUtil.showHint("商品未设置单件批发价，无法采购货收货");
//            return;
//        }

        InvReturnGoodsEntity entity = InvReturnGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null){
            entity = new InvReturnGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProductId(goods.getId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getId());
            entity.setProductName(goods.getSkuName());
            entity.setPrice(goods.getSingleCostPrice());
            entity.setUnitSpec(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getTenantId());
            entity.setIsPrivate(IsPrivate.PLATFORM);

            entity.setTotalCount(0D);
            entity.setQuantityCheck(0D);
            entity.setInspectStatus(InvReturnGoodsEntity.INSPECT_STATUS_NONE);

            //设置金额
            if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
                entity.setAmount(0D);
            } else {
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setUpdatedDate(new Date());

            InvReturnGoodsService.get().saveOrUpdate(entity);
        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;
    private void quantityCheckConfirmDialog(final InvReturnGoodsEntity entity,
                                            final Double price, final Double quantity){
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(true);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经签收%.2f件，请选择[覆盖]还是[累加]",
                entity.getQuantityCheck()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvReturnGoodsService.get().inspect(entity, price, quantity);

                refreshPackage(null);
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvReturnGoodsService.get().inspect(entity, price, entity.getQuantityCheck() + quantity);

                refreshPackage(null);
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
