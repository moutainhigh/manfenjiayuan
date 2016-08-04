package com.manfenjiayuan.pda_wholesaler.ui.fragment.invsendio;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.database.entity.InvIoPickGoodsEntity;
import com.manfenjiayuan.pda_wholesaler.database.logic.InvIoPickGoodsService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 拣货单发货－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvIoGoodsFragment extends BaseReceiveOrderFragment
        implements IChainGoodsSkuView {

    private static final String TAG = "DistributionInspectFragment";
    public static final String EXTRA_KEY_INSPECTMODE = "inspectMode";
//    public static final String EXTRA_KEY_COMPANYID = "companyId";
    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;

    @Bind(R.id.label_productName)
    TextLabelView labelName;
    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_price)
    EditLabelView labelPrice;
    @Bind(R.id.label_pickedQuantity)
    TextLabelView labelPickedQuantity;
    @Bind(R.id.label_sign_quantity)
    EditLabelView labelSignQuantity;

    @Bind(R.id.button_reject)
    Button btnReject;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    private int inspectMode = 0;
    private InvIoPickGoodsEntity curGoods = null;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;
    private boolean isQueryProcessing;


    public static InvIoGoodsFragment newInstance(Bundle args) {
        InvIoGoodsFragment fragment = new InvIoGoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invio_goods_inspect;
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
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

//            eqvBarcode.setInputString(barcode);
            query(barcode);
        }
    }

    /**
     * 查询包裹信息
     */
    public void query(String barcode) {
        eqvBarcode.clear();

        if (isQueryProcessing || StringUtils.isEmpty(barcode)) {
            return;
        }

        QueryGoodsAsyncTask queryGoodsAsyncTask = new QueryGoodsAsyncTask(barcode);
        queryGoodsAsyncTask.execute();
    }

    @OnClick(R.id.button_reject)
    public void reject() {
        InvIoPickGoodsService.get().reject(curGoods);

        refreshPackage(null);
    }

    @OnClick(R.id.button_submit)
    public void submit() {
        String price = labelPrice.getEtContent();
        if (StringUtils.isEmpty(price)) {
            DialogUtil.showHint("请输入收货价格");
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
            InvIoPickGoodsService.get().inspect(curGoods, Double.valueOf(price), quantityCheck);
            refreshPackage(null);
        }
    }


    /**
     * 刷新信息
     */
    private void refreshPackage(InvIoPickGoodsEntity goods) {
        curGoods = goods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelName.setTvSubTitle("");
            labelPrice.setEtContent("");
            labelPickedQuantity.setTvSubTitle("");
            labelSignQuantity.setEtContent("");

            btnReject.setEnabled(false);
            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();
        }
        else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelName.setTvSubTitle(curGoods.getProductName());
            labelPickedQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantityCheck(), ""));
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
        private InvIoPickGoodsEntity goodsEntity;

        public QueryGoodsAsyncTask( String barcode) {
            this.barcode = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try{
                goodsEntity = InvIoPickGoodsService.get().queryEntityBy(barcode);
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
                saveInvIoPickGoodsEntity(goodsEntity);
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

    private void queryNetGoods(String barcode) {
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            onError(getString(R.string.toast_network_error));
            return;
        }

        // TODO: 5/31/16 这里的companyId使用null,查询所有供应商的商品，如果要查询指定批发商的商品，需要传入companyId的值
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


    public void saveInvIoPickGoodsEntity(InvIoPickGoodsEntity goods) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

        InvIoPickGoodsEntity entity = InvIoPickGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null) {
            entity = new InvIoPickGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getChainSkuId());
            entity.setProductName(goods.getProductName());
            entity.setPrice(goods.getPrice());
            entity.setUnitSpec(goods.getUnitSpec());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getProviderId());
            entity.setIsPrivate(goods.getIsPrivate());

            entity.setQuantityCheck(1D);

            //设置金额
            if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
                entity.setAmount(0D);
            } else {
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setUpdatedDate(new Date());

            InvIoPickGoodsService.get().saveOrUpdate(entity);
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

        InvIoPickGoodsEntity entity = InvIoPickGoodsService.get().queryEntityBy(goods.getBarcode());
        if (entity == null) {
            entity = new InvIoPickGoodsEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getId());
            entity.setProductName(goods.getSkuName());
            entity.setPrice(goods.getSingleCostPrice());
            entity.setUnitSpec(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getTenantId());
            entity.setIsPrivate(IsPrivate.PLATFORM);

            entity.setQuantityCheck(0D);

            //设置金额
            if (entity.getQuantityCheck() == null || entity.getPrice() == null) {
                entity.setAmount(0D);
            } else {
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setUpdatedDate(new Date());

            InvIoPickGoodsService.get().saveOrUpdate(entity);

        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;
    private void quantityCheckConfirmDialog(final InvIoPickGoodsEntity entity,
                                            final Double price, final Double quantity){
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(true);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经拣货%.2f件，请选择[覆盖]还是[累加]",
                entity.getQuantityCheck()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvIoPickGoodsService.get().inspect(entity, price, quantity);
                refreshPackage(null);
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                InvIoPickGoodsService.get().inspect(entity, price,
                        entity.getQuantityCheck() + quantity);
                refreshPackage(null);
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }


}
