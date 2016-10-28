package com.bingshanguxue.pda.bizz.goods;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.DataSyncManager;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

/**
 * 自采商品--商品建档并入库
 * <ol>
 * <li>适用场景：门店收银扫描新条码，建档</li>
 * <li>
 * 采购数量默认为0，需要采购请创建收货单
 * </li>
 * </ol>
 * Created by Nat.ZZN(bingshanguxue) on 15/09/24.
 */
public class ScSkuGoodsStoreInFragment extends PDAScanFragment implements IScGoodsSkuView {

    public static final String EXTRA_STORE_TYPE = "storeType";


    //    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    //    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    //    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    //    @Bind(R.id.label_name)
    EditLabelView labelName;
    EditLabelView labelShortName;
    EditLabelView labelPackageNum;
    //    @Bind(R.id.spinner_price_type)
    Spinner spinnerPriceType;
    //    @Bind(R.id.spinner_unit)
    Spinner spinnerUnit;
    EditLabelView labelProdArea;
    EditLabelView labelGuaPeriod;
    EditLabelView labelProdLevel;
    //    @Bind(R.id.label_quantity)
    EditLabelView labelQuantity;
    //    @Bind(R.id.label_buyprice)
    EditLabelView labelBuyprice;
    //    @Bind(R.id.label_costprice)
    EditLabelView labelCostprice;
    EditLabelView labelHintPrice;

    //    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    private ArrayAdapter<CharSequence> unitAdapter0;
    private ArrayAdapter<CharSequence> unitAdapter1;

    private Integer storeType = StoreType.SUPERMARKET;
    private ScGoodsSku purchaseGoods;
    private ScGoodsSkuPresenter mScGoodsSkuPresenter;

    public static ScSkuGoodsStoreInFragment newInstance(Bundle args) {
        ScSkuGoodsStoreInFragment fragment = new ScSkuGoodsStoreInFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_scskugoods_storein;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mScanBar = (ScanBar) rootView.findViewById(R.id.scanBar);
        labelBarcode = (TextLabelView) rootView.findViewById(R.id.label_barcode);
        labelName = (EditLabelView) rootView.findViewById(R.id.label_name);
        labelShortName = (EditLabelView) rootView.findViewById(R.id.label_shortName);
        labelPackageNum = (EditLabelView) rootView.findViewById(R.id.label_packageNum);
        spinnerPriceType = (Spinner) rootView.findViewById(R.id.spinner_price_type);
        spinnerUnit = (Spinner) rootView.findViewById(R.id.spinner_unit);
        labelProdArea = (EditLabelView) rootView.findViewById(R.id.label_prodArea);
        labelProdLevel = (EditLabelView) rootView.findViewById(R.id.label_prodLevel);
        labelGuaPeriod = (EditLabelView) rootView.findViewById(R.id.labell_guaPeriod);
        labelQuantity = (EditLabelView) rootView.findViewById(R.id.label_quantity);
        labelBuyprice = (EditLabelView) rootView.findViewById(R.id.label_buyprice);
        labelCostprice = (EditLabelView) rootView.findViewById(R.id.label_costPrice);
        labelHintPrice = (EditLabelView) rootView.findViewById(R.id.label_hintPrice);
        btnSubmit = (FloatingActionButton) rootView.findViewById(R.id.fab_submit);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            storeType = args.getInt(EXTRA_STORE_TYPE, StoreType.SUPERMARKET);
        }

        try {
            mToolbar.setTitle("商品建档");
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
            mToolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().onBackPressed();
                        }
                    });

//            mScanBar.setSoftKeyboardEnabled(true);
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

            ArrayAdapter<CharSequence> priceTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.pricetype_caption, R.layout.mfh_spinner_item_text);
            priceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            unitAdapter1 = ArrayAdapter.createFromResource(getActivity(),
                    R.array.unit_name_1, R.layout.mfh_spinner_item_text);
            unitAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            unitAdapter0 = ArrayAdapter.createFromResource(getActivity(),
                    R.array.unit_name_0, R.layout.mfh_spinner_item_text);
            unitAdapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


            spinnerPriceType.setAdapter(priceTypeAdapter);
            spinnerPriceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (spinnerPriceType.getSelectedItem().toString().equals("计重")) {
                        spinnerUnit.setAdapter(unitAdapter1);
                        spinnerUnit.setSelection(0);
                        spinnerUnit.setEnabled(false);
                    } else {

                        spinnerUnit.setAdapter(unitAdapter0);
                        spinnerUnit.setSelection(0);
                        spinnerUnit.setEnabled(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinnerPriceType.setSelection(0);

            spinnerUnit.setAdapter(unitAdapter0);
            spinnerUnit.setSelection(0);

            labelName.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelShortName.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelShortName.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelPackageNum.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelPackageNum.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelProdArea.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelProdArea.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelProdLevel.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelProdLevel.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelGuaPeriod.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelGuaPeriod.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelQuantity.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelQuantity.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelBuyprice.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelBuyprice.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelCostprice.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelCostprice.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    labelHintPrice.requestFocusEnd();
                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });
            labelHintPrice.setOnViewListener(new EditLabelView.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {

                }

                @Override
                public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
                }
            });

            if (StoreType.WHOLESALER.equals(storeType)) {
                labelCostprice.setStartText("批发价");
                labelHintPrice.setVisibility(View.VISIBLE);
            } else {
                labelCostprice.setStartText("零售价");
                labelHintPrice.setVisibility(View.GONE);
            }
            btnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    storeIn();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }

        reload();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
     */
    private void queryByBarcode(String barcode) {
        isAcceptBarcodeEnabled = false;
        labelBarcode.setEndText(barcode);
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.requestFocus();
            isAcceptBarcodeEnabled = true;
            return;
        }
        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            reload();
            return;
        }

        btnSubmit.setVisibility(View.GONE);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        mScGoodsSkuPresenter.getByBarcode(barcode);
    }

    public void onQueryError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            ZLogger.df(errorMsg);
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        } else {
            hideProgressDialog();
        }
        reload();
    }

    @Override
    public void onIScGoodsSkuViewProcess() {

    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        onQueryError(errorMsg);
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {

    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku data) {
        refresh(data);
    }

    private void reload() {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);
        hideProgressDialog();

        purchaseGoods = null;
        labelBarcode.setEndText("");
        labelName.setInput("");
        labelShortName.setInput("");
        labelPackageNum.setInput("");
        spinnerPriceType.setSelection(0);
        spinnerPriceType.setEnabled(false);
        spinnerUnit.setAdapter(unitAdapter0);
        spinnerUnit.setSelection(0);
        spinnerUnit.setEnabled(false);
        labelProdArea.setInput("");
        labelProdLevel.setInput("");
        labelGuaPeriod.setInput("");
        labelQuantity.setInput("");
        labelBuyprice.setInput("");
        labelCostprice.setInput("");
        labelHintPrice.setInput("");

        btnSubmit.setVisibility(View.GONE);
    }

    private void refresh(ScGoodsSku goodsSku) {
        hideProgressDialog();
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        purchaseGoods = goodsSku;
        if (goodsSku == null) {
            DialogUtil.showHint("未查到商品");
            labelName.setInput("");
            labelName.setEnabled(true);
            labelShortName.setInput("");
            labelPackageNum.setInput("");
            spinnerPriceType.setSelection(0);
            spinnerPriceType.setEnabled(true);
//            spinnerUnit.setAdapter(unitAdapter0);
//            spinnerUnit.setSelection(0);
//            spinnerUnit.setEnabled(true);
            labelProdArea.setInput("");
            labelProdLevel.setInput("");
            labelGuaPeriod.setInput("");
            labelQuantity.setInput("");
            labelBuyprice.setInput("");
            labelCostprice.setInput("");
            labelHintPrice.setInput("");


            labelName.requestFocusEnd();
        } else {
            labelName.setInput(goodsSku.getSkuName());
            labelName.setEnabled(false);
            labelShortName.setInput(goodsSku.getShortName());
            labelPackageNum.setInput(MUtils.formatDouble(goodsSku.getPackageNum(), ""));
            labelProdArea.setInput(goodsSku.getProdArea());
            labelProdLevel.setInput(goodsSku.getProdLevel());
            labelGuaPeriod.setInput("");
            labelQuantity.setInput(MUtils.formatDouble(goodsSku.getQuantity(), ""));
            labelBuyprice.setInput(MUtils.formatDouble(goodsSku.getBuyPrice(), ""));
            labelCostprice.setInput(MUtils.formatDouble(goodsSku.getCostPrice(), ""));

            if (goodsSku.getPriceType().equals(PriceType.WEIGHT)) {
                spinnerPriceType.setSelection(1);
            } else {
                spinnerPriceType.setSelection(0);
            }
            spinnerPriceType.setEnabled(false);

//            if (spinnerPriceType.getSelectedItem().toString().equals("计重")) {
//                spinnerUnit.setAdapter(unitAdapter1);
//                spinnerUnit.setSelection(0);
//                spinnerUnit.setEnabled(false);
//            } else {
//                spinnerUnit.setAdapter(unitAdapter0);
//                spinnerUnit.setSelection(0);
//                spinnerUnit.setEnabled(true);
//            }


            labelQuantity.requestFocusEnd();
        }

        btnSubmit.setEnabled(true);
        btnSubmit.setVisibility(View.VISIBLE);
    }

    /**
     * 商品建档入库
     */
    public void storeIn() {
        btnSubmit.setEnabled(false);
        String barcode = labelBarcode.getEndText();
        if (StringUtils.isEmpty(barcode)) {
            DialogUtil.showHint("商品条码不能为空");
            btnSubmit.setEnabled(true);
            return;
        }
        String productName = labelName.getInput();
        if (StringUtils.isEmpty(productName)) {
            DialogUtil.showHint("商品名称不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        String shortName = labelShortName.getInput();
        String packageNum = labelPackageNum.getInput();
        String prodArea = labelProdArea.getInput();
        String prodLevel = labelProdLevel.getInput();
        String guaPeriod = labelGuaPeriod.getInput();

        String quantity = labelQuantity.getInput();
        if (StringUtils.isEmpty(quantity)) {
            DialogUtil.showHint("初始库存不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        String buyprice = labelBuyprice.getInput();
        if (StringUtils.isEmpty(buyprice)) {
            DialogUtil.showHint("初始成本价不能为空");
            btnSubmit.setEnabled(true);
            return;
        }


        String costprice = labelCostprice.getInput();
        String hintPrice = labelHintPrice.getInput();
        if (StoreType.WHOLESALER.equals(storeType)) {
            if (StringUtils.isEmpty(costprice)) {
                DialogUtil.showHint("批发价不能为空");
                btnSubmit.setEnabled(true);
                return;
            }

            if (StringUtils.isEmpty(hintPrice)) {
                DialogUtil.showHint("建议零售价不能为空");
                btnSubmit.setEnabled(true);
                return;
            }
        } else {
            if (StringUtils.isEmpty(costprice)) {
                DialogUtil.showHint("零售价不能为空");
                btnSubmit.setEnabled(true);
                return;
            }
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        int priceType = PriceType.PIECE;
        if (spinnerPriceType.getSelectedItem().toString().equals("计重")) {
            priceType = PriceType.WEIGHT;
        }
        String unit = spinnerUnit.getSelectedItem().toString();

//        animProgress.setVisibility(View.VISIBLE);

        JSONArray jsonArray = new JSONArray();
        JSONObject item = new JSONObject();
        JSONObject product = new JSONObject();//产品本身信息
        JSONObject defaultSku = new JSONObject();//产品sku信息
        JSONObject tenantSku = new JSONObject();

        if (purchaseGoods != null) {
            product.put("id", purchaseGoods.getProductId());
            defaultSku.put("id", purchaseGoods.getProSkuId());
            defaultSku.put("skuMask", purchaseGoods.getSkuMask());
            tenantSku.put("id", purchaseGoods.getTenantSkuId());
        } else {
            defaultSku.put("skuMask", 0);
        }
        product.put("name", productName);
        product.put("unit", unit);
        product.put("priceType", priceType);
        product.put("shortName", shortName);
        product.put("prodArea", prodArea);
        if (!StringUtils.isEmpty(prodLevel)) {
            product.put("prodLevel", prodLevel);
        }
        if (!StringUtils.isEmpty(guaPeriod)) {
            product.put("guaPeriod", guaPeriod);
        }
        product.put("domain", CateApi.DOMAIN_TYPE_PROD);//商品业务域
//        product.put("shortName", shortName);
        defaultSku.put("barcode", barcode);
        if (!StringUtils.isEmpty(packageNum)) {
            defaultSku.put("packageNum", packageNum);
        }
        tenantSku.put("buyPrice", buyprice);
        //Column 'cost_price' cannot be null
        tenantSku.put("costPrice", costprice);//默认零售价等于采购价。
        if (!StringUtils.isEmpty(hintPrice)){
            tenantSku.put("hintPrice", hintPrice);//建议零售价,批发商才有
        }

        //入库数量不能为空或为0!
        item.put("quantity", quantity);
//        item.put("tenantId", MfhLoginService.get().getSpid());

        item.put("product", product);
        item.put("defaultSku", defaultSku);
        item.put("tenantSku", tenantSku);
        jsonArray.add(item);

        ScGoodsSkuApiImpl.storeIn(jsonArray.toJSONString(), storeType, responseCallback);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("商品建档失败: " + errMsg);
                    hideProgressDialog();
                    DialogUtil.showHint("建档失败");
                    btnSubmit.setEnabled(true);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
                    DialogUtil.showHint("建档成功");
                    reload();

                    //更新商品库
                    DataSyncManager.getInstance().notifyUpdateSku();
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


}
