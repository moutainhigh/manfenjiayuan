package com.manfenjiayuan.pda_supermarket.ui.fragment.receipt;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.manfenjiayuan.business.presenter.ChainGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IChainGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.core.logger.ZLogger;
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
 * 商品配送－－验货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class DistributionInspectFragment extends PDAScanFragment implements IChainGoodsSkuView {

    private static final String TAG = "DistributionInspectFragment";
    public static final String EXTRA_KEY_INSPECTMODE = "inspectMode";
    public static final String EXTRA_KEY_BARCODE = "barcode";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;

    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_send_price)
    TextLabelView labelSendPrice;
    @Bind(R.id.label_send_quantity)
    TextLabelView labelSendQuantity;
    @Bind(R.id.label_receive_quantity)
    EditLabelView labelReceiveQuantity;
    @Bind(R.id.label_receive_amount)
    EditLabelView labelReceiveAmount;
    @Bind(R.id.label_receive_price)
    TextLabelView labelReceivePrice;

    @Bind(R.id.button_reject)
    Button btnReject;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    private int inspectMode = 0;
    private DistributionSignEntity curGoods = null;
    private ChainGoodsSkuPresenter chainGoodsSkuPresenter;
    private boolean isQueryProcessing;


    public static DistributionInspectFragment newInstance(Bundle args) {
        DistributionInspectFragment fragment = new DistributionInspectFragment();

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
        return R.layout.fragment_distribution_inspect;
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
        labelReceiveQuantity.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelReceiveQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelReceiveAmount.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
        labelReceiveQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                labelReceivePrice.setTvSubTitle(MUtils.formatDouble(calculateReceivePrice(), ""));

            }
        });
        labelReceiveAmount.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
//        labelSignQuantity.setSoftKeyboardEnabled(false);
        labelReceiveAmount.setOnViewListener(new EditLabelView.OnViewListener() {
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
        labelReceiveAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                labelReceivePrice.setTvSubTitle(MUtils.formatDouble(calculateReceivePrice(), ""));

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

//        labelReceivePrice.setVisibility(View.INVISIBLE);

        Bundle args = getArguments();
        if (args != null) {
            inspectMode = args.getInt(EXTRA_KEY_INSPECTMODE, 0);
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

//            eqvBarcode.setInputString(barcode);
            query(barcode);
        }
    }

    @Override
    public boolean onBackPressed() {
//        if (curGoods != null){
//            // TODO: 6/10/16 删除当前商品信息
//        }
//
//        showConfirmDialog("退出后商品列表将会清空，确定要退出吗？",
//                "退出", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                        getActivity().setResult(Activity.RESULT_CANCELED);
//                        getActivity().finish();
//                    }
//                }, "点错了", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//
////        DialogUtil.showHint("onBackPressed");
//        if (officeAdapter.getItemCount() > 0) {
//
//        } else {
//            getActivity().setResult(Activity.RESULT_CANCELED);
//            getActivity().finish();
//        }

        return false;
    }


    private Double calculateReceivePrice(){
        String quantityStr = labelReceiveQuantity.getEtContent();
        if (StringUtils.isEmpty(quantityStr)) {
            return 0D;
        }

        String amount = labelReceiveAmount.getEtContent();
        if (StringUtils.isEmpty(amount)) {
            return 0D;
        }

        Double quantityVal = Double.valueOf(quantityStr);
        Double amountVal = Double.valueOf(amount);
        if (quantityVal == 0){
            return 0D;
        }
        else{
            return amountVal / quantityVal;
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
        DistributionSignService.get().reject(curGoods);

        refreshPackage(null);
    }

    @OnClick(R.id.button_submit)
    public void submit() {
        String quantityStr = labelReceiveQuantity.getEtContent();
        if (StringUtils.isEmpty(quantityStr)) {
            DialogUtil.showHint("请输入签收数量");
            return;
        }

        String amount = labelReceiveAmount.getEtContent();
        if (StringUtils.isEmpty(amount)) {
            DialogUtil.showHint("请输入收货金额");
            return;
        }

        Double quantityCheck = Double.valueOf(quantityStr);
        if (curGoods != null && curGoods.getReceiveQuantity() > 0) {
            quantityCheckConfirmDialog(curGoods, Double.valueOf(amount), quantityCheck);
        } else {
            DistributionSignService.get().inspect(curGoods, Double.valueOf(amount), quantityCheck);

            DialogUtil.showHint("添加成功");
            refreshPackage(null);
        }
    }


    /**
     * 刷新信息
     */
    private void refreshPackage(DistributionSignEntity goods) {
        curGoods = goods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelSendQuantity.setTvSubTitle("");
            labelSendPrice.setTvSubTitle("");
            labelReceiveQuantity.setEtContent("");
            labelReceiveAmount.setEtContent("");
            labelReceivePrice.setTvSubTitle("");

            btnReject.setEnabled(false);
            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getProductName());
            labelSendQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getSendQuantity(), ""));
//            labelSignQuantity.setEtContent(String.format("%.2f", curGoods.getSignQuantity()));
            labelSendPrice.setTvSubTitle(MUtils.formatDouble(curGoods.getSendPrice(), ""));
            //默认签收数量为空，根据实际情况填写
            labelReceiveQuantity.setEtContent("");
            labelReceiveAmount.setEtContent("");
            labelReceivePrice.setTvSubTitle(MUtils.formatDouble(calculateReceivePrice(), ""));

            btnReject.setEnabled(true);
            btnSubmit.setEnabled(true);

            labelReceiveQuantity.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelReceiveQuantity);
    }

    class QueryGoodsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private String barcode;
        private DistributionSignEntity goodsEntity = null;

        public QueryGoodsAsyncTask(String barcode) {
            this.barcode = barcode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                goodsEntity = DistributionSignService.get().queryEntityBy(barcode);
                if (goodsEntity != null) {
                    return true;
                }
            } catch (Exception e) {
                ZLogger.d("查询本地收货商品失败, " + e.toString());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean && goodsEntity != null) {
                saveDistributionSignEntity(goodsEntity);
            } else {
                if (inspectMode == 0) {
                    queryNetGoods(barcode);
                } else {
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

    public void saveDistributionSignEntity(DistributionSignEntity goods) {
        hideProgressDialog();
        isQueryProcessing = false;
        refreshPackage(goods);
    }


    /**
     * 保存搜索商品
     */
    private void saveChainGoodsSku(ChainGoodsSku goods) {
        if (goods == null) {
            DialogUtil.showHint("商品无效");
            return;
        }

//        if (goods.getSingleCostPrice() == null) {
//            //“如果singleCostPrice值为null，说明缺少箱规数，信息不完整，这种情况你不允许进行采购或收货
//            DialogUtil.showHint("商品未设置单件批发价，无法采购货收货");
//            return;
//        }

        DistributionSignEntity entity = DistributionSignService.get().queryEntityBy(goods.getBarcode());
        if (entity == null) {
            entity = new DistributionSignEntity();
            entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息

//        entity.setOrderId(productEntity.getOrderId());
            entity.setProductId(goods.getId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setChainSkuId(goods.getId());
            entity.setProductName(goods.getSkuName());
            entity.setUnitSpec(goods.getUnit());
            entity.setBarcode(goods.getBarcode());
            entity.setProviderId(goods.getTenantId());
            entity.setIsPrivate(IsPrivate.PLATFORM);

            entity.setSendPrice(goods.getSingleCostPrice());
            entity.setSendAmount(0D);
            entity.setSendPrice(0D);
            entity.setReceiveQuantity(0D);
            entity.setReceiveAmount(0D);
            entity.setInspectStatus(DistributionSignEntity.INSPECT_STATUS_NONE);
            entity.setUpdatedDate(new Date());

            DistributionSignService.get().saveOrUpdate(entity);
        }
        refreshPackage(entity);
    }

    private CommonDialog quantityCheckConfirmDialog = null;

    private void quantityCheckConfirmDialog(final DistributionSignEntity entity,
                                            final Double amount, final Double quantity) {
        if (quantityCheckConfirmDialog == null) {
            quantityCheckConfirmDialog = new CommonDialog(getActivity());
            quantityCheckConfirmDialog.setCancelable(true);
        }
        quantityCheckConfirmDialog.setMessage(String.format("已经签收%.2f件，请选择[覆盖]还是[累加]",
                entity.getReceiveQuantity()));
        quantityCheckConfirmDialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                DistributionSignService.get().inspect(entity, amount, quantity);
                DialogUtil.showHint("添加成功");
                refreshPackage(null);
            }
        });
        quantityCheckConfirmDialog.setNegativeButton("累加", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                DistributionSignService.get().inspect(entity,
                        amount + entity.getReceiveAmount(),
                        entity.getReceiveQuantity() + quantity);
                DialogUtil.showHint("添加成功");
                refreshPackage(null);
            }
        });
        if (!quantityCheckConfirmDialog.isShowing()) {
            quantityCheckConfirmDialog.show();
        }
    }
}
