package com.mfh.litecashier.ui.fragment.goods;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.presenter.ScGoodsSkuPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IScGoodsSkuView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 商品建档并入库
 * <ul>
 * 适用场景：
 * <li>门店收银扫描新条码，建档</li>
 * <li>
 * 采购数量默认为0，需要采购请创建收货单
 * </li>
 * 修改历史：
 * <li>2016-09-13,新增规格/箱规/产地/等级/保质期/初始库存/初始成本价字段</li>
 * </ul>
 * Created by Nat.ZZN(bingshanguxue) on 15/09/24.
 */
public class ScSkuGoodsStoreInFragment extends BaseProgressFragment implements IScGoodsSkuView {

    public static final String EXTRY_KEY_BARCODE = "barcode";

    @Bind(R.id.tv_header_title)
    TextView tvTitle;
    @Bind(R.id.button_footer_positive)
    Button btnSubmit;

    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_name)
    EditLabelView labelName;
    @Bind(R.id.label_shortName)
    EditLabelView labelShortName;
    @Bind(R.id.label_packageNum)
    EditLabelView labelPackageNum;
    @Bind(R.id.spinner_price_type)
    Spinner spinnerPriceType;
    @Bind(R.id.spinner_unit)
    Spinner spinnerUnit;
    @Bind(R.id.label_prodArea)
    EditLabelView labelProdArea;
    @Bind(R.id.label_prodLevel)
    EditLabelView labelProdLevel;
    @Bind(R.id.label_guaPeriod)
    EditLabelView labelGuaPeriod;
    @Bind(R.id.label_quantity)
    EditLabelView labelQuantity;
    @Bind(R.id.label_buyprice)
    EditLabelView labelBuyprice;
    @Bind(R.id.label_costprice)
    EditLabelView labelCostprice;

    private String barcode;
    private ScGoodsSku purchaseGoods;
    private ScGoodsSkuPresenter mScGoodsSkuPresenter;
    private ArrayAdapter<CharSequence> unitAdapter0;
    private ArrayAdapter<CharSequence> unitAdapter1;

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
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            barcode = args.getString(EXTRY_KEY_BARCODE, "");
        }
        tvTitle.setText("商品建档");
        labelBarcode.setEndText(barcode);

        ArrayAdapter<CharSequence> priceTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.pricetype_name, R.layout.mfh_spinner_item_text);
        priceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitAdapter0 = ArrayAdapter.createFromResource(getActivity(),
                R.array.unit_name_0, R.layout.mfh_spinner_item_text);
        unitAdapter0.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitAdapter1 = ArrayAdapter.createFromResource(getActivity(),
                R.array.unit_name_1, R.layout.mfh_spinner_item_text);
        unitAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.unit_name_0, R.layout.mfh_spinner_item_text);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
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

            }

            @Override
            public void onScan() {
//                refreshPackage(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
        loadInit();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 初始化数据
     */
    private void loadInit() {
        btnSubmit.setEnabled(false);
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(false);
            return;
        }

//        animProgress.setVisibility(View.VISIBLE);

        mScGoodsSkuPresenter.getByBarcode(barcode);
    }

    @Override
    public void onIScGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
//        refresh(null);
        hideProgressDialog();
        DialogUtil.showHint("加载商品信息失败");
        btnSubmit.setEnabled(false);
    }

    @Override
    public void onIScGoodsSkuViewSuccess(PageInfo pageInfo, List<ScGoodsSku> dataList) {

    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku data) {
        refresh(data);
    }

    private void refresh(ScGoodsSku stockGoods) {
        hideProgressDialog();

        purchaseGoods = stockGoods;

        if (stockGoods == null) {
            labelName.setInput("");
            labelName.setEnabled(true);

            labelShortName.setInput("");
            labelPackageNum.setInput("");
            spinnerPriceType.setSelection(0);
            spinnerPriceType.setEnabled(true);
            spinnerUnit.setEnabled(true);
            labelProdArea.setInput("");
            labelProdLevel.setInput("");
            labelGuaPeriod.setInput("");
            labelQuantity.setInput("");
            labelBuyprice.setInput("");
            labelCostprice.setInput("");

            labelName.requestFocusEnd();
        } else {
            labelName.setInput(stockGoods.getSkuName());
            labelName.setEnabled(false);

            labelShortName.setInput(stockGoods.getShortName());
            labelPackageNum.setInput(MUtils.formatDouble(stockGoods.getPackageNum(), ""));
            labelProdArea.setInput(stockGoods.getProdArea());
            labelProdLevel.setInput(stockGoods.getProdLevel());
            labelGuaPeriod.setInput("");
            labelQuantity.setInput(MUtils.formatDouble(stockGoods.getQuantity(), ""));
            labelBuyprice.setInput(MUtils.formatDouble(stockGoods.getBuyPrice(), ""));
            labelCostprice.setInput(MUtils.formatDouble(stockGoods.getCostPrice(), ""));

            if (stockGoods.getPriceType().equals(PriceType.WEIGHT)) {
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
    }

    /**
     * 商品建档入库
     */
    @OnClick(R.id.button_footer_positive)
    public void stockIn() {
        btnSubmit.setEnabled(false);
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
//        if (StringUtils.isEmpty(shortName)) {
//            DialogUtil.showHint("规格不能为空");
//            btnSubmit.setEnabled(true);
//            return;
//        }
        String packageNum = labelPackageNum.getInput();
//        if (StringUtils.isEmpty(packageNum)) {
//            DialogUtil.showHint("箱规不能为空");
//            btnSubmit.setEnabled(true);
//            return;
//        }
        String prodArea = labelProdArea.getInput();
//        if (StringUtils.isEmpty(prodArea)) {
//            DialogUtil.showHint("产地不能为空");
//            btnSubmit.setEnabled(true);
//            return;
//        }
        String prodLevel = labelProdLevel.getInput();
//        if (StringUtils.isEmpty(prodLevel)) {
//            DialogUtil.showHint("等级不能为空");
//            btnSubmit.setEnabled(true);
//            return;
//        }
        String guaPeriod = labelGuaPeriod.getInput();
//        if (StringUtils.isEmpty(guaPeriod)) {
//            DialogUtil.showHint("保质期不能为空");
//            btnSubmit.setEnabled(true);
//            return;
//        }
        String buyprice = labelBuyprice.getInput();
        if (StringUtils.isEmpty(buyprice)) {
            DialogUtil.showHint("采购价不能为空");
            btnSubmit.setEnabled(true);
            return;
        }
        String costprice = labelCostprice.getInput();
        if (StringUtils.isEmpty(costprice)) {
            DialogUtil.showHint("零售价不能为空");
            btnSubmit.setEnabled(true);
            return;
        }
        String quantity = labelQuantity.getInput();
        if (StringUtils.isEmpty(quantity)) {
            DialogUtil.showHint("初始库存不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            return;
        }

        onLoadProcess("正在发送请求...");
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
        if (!StringUtils.isEmpty(prodArea)){
            product.put("prodArea", prodArea);
        }
        if (!StringUtils.isEmpty(prodArea)){
            product.put("prodLevel", prodLevel);
        }
        if (!StringUtils.isEmpty(prodArea)){
            product.put("guaPeriod", guaPeriod);
        }
        product.put("domain", CateApi.DOMAIN_TYPE_PROD);//商品业务域
//        product.put("shortName", shortName);
        defaultSku.put("barcode", barcode);
        if (!StringUtils.isEmpty(packageNum)){
            defaultSku.put("packageNum", packageNum);
        }
        tenantSku.put("buyPrice", buyprice);
        //Column 'cost_price' cannot be null
        tenantSku.put("costPrice", costprice);//默认零售价等于采购价。

        //入库数量不能为空或为0!
        item.put("quantity", quantity);
//        item.put("tenantId", MfhLoginService.get().getSpid());

        item.put("product", product);
        item.put("defaultSku", defaultSku);
        item.put("tenantSku", tenantSku);
        jsonArray.add(item);

        ScGoodsSkuApiImpl.storeIn(jsonArray.toJSONString(), StoreType.SUPERMARKET, responseCallback);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
//                    DialogUtil.showHint("新增商品失败");
                    onLoadError(errMsg);
                    btnSubmit.setEnabled(true);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}

                    onLoadFinished();
                    /**
                     * 新增商品成功，更新商品库
                     * */
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
//
//                        //更新商品库
//                        DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };


}
