package com.mfh.litecashier.ui.fragment.inventory;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.OptionalLabel;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.dialog.SelectInvCompanyInfoDialog;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 商品建档
 * Created by Nat.ZZN(bingshanguxue) on 15/09/24.
 */
public class GreateScSkuGoodsFragment extends BaseProgressFragment {

    public static final String EXTRA_KEY_PURCHASE_GOODS = "purchase_goods";

    @Bind(R.id.tv_header_title)
    TextView tvTitle;

    @Bind(R.id.tv_barcode)
    TextView tvBarcode;
    @Bind(R.id.et_productName)
    EditText etProductName;
    @Bind(R.id.et_shortName)
    EditText etShortName;
    @Bind(R.id.spinner_price_type)
    Spinner spinnerPriceType;
    @Bind(R.id.spinner_unit)
    Spinner spinnerUnit;

    /*供应商*/
    private CompanyInfo curInvCompProvider = null;//当前私有供应商
    private SelectInvCompanyInfoDialog selectInvCompProviderDialog;
    @Bind(R.id.et_buyprice)
    EditText etBuyPrice;
    @Bind(R.id.et_quantity)
    EditText etQuantity;
    @Bind(R.id.et_costprice)
    EditText etCostPrice;

    private ScGoodsSku purchaseGoods;

    public static GreateScSkuGoodsFragment newInstance(Bundle args) {
        GreateScSkuGoodsFragment fragment = new GreateScSkuGoodsFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_purchase_create_goods;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            purchaseGoods = (ScGoodsSku)args.getSerializable(EXTRA_KEY_PURCHASE_GOODS);
        }
        tvTitle.setText("商品建档");

        ArrayAdapter<CharSequence> priceTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.pricetype_name, R.layout.mfh_spinner_item_text);
        priceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriceType.setAdapter(priceTypeAdapter);
        spinnerPriceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerPriceType.getSelectedItem().toString().equals("计重")) {
                    ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(getActivity(),
                            R.array.unit_name_1, R.layout.mfh_spinner_item_text);
                    unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerUnit.setAdapter(unitAdapter);
                    spinnerUnit.setSelection(0);
                    spinnerUnit.setEnabled(false);
                } else {
                    ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(getActivity(),
                            R.array.unit_name_0, R.layout.mfh_spinner_item_text);
                    unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerUnit.setAdapter(unitAdapter);
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

        labelInvcompProvider.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                curInvCompProvider = null;
            }
        });

        initTextEeditText(etProductName, etShortName);
        initTextEeditText(etShortName, etBuyPrice);
        initNumberEditText(etBuyPrice, etCostPrice);
        initNumberEditText(etCostPrice, etQuantity);
        initNumberEditText(etQuantity, null);

        refresh(purchaseGoods);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void initNumberEditText(final EditText editText, final View nextFocusView) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DeviceUtils.hideSoftInput(CashierApp.getAppContext(), editText);
                }
                editText.requestFocus();
                editText.setSelection(editText.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));
                //Press “esc”
                if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        finishActivity();
                    }
                    return true;
                }
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP && nextFocusView != null) {
                        nextFocusView.requestFocus();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    private void initTextEeditText(final EditText editText, final View nextFocusView) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);//不自动获取EditText的焦点
//        etBarCode.setCursorVisible(false);//隐藏光标
//        editText.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
////                if (event.getAction() == MotionEvent.ACTION_UP) {
////                    DeviceUtils.showSoftInput(CashierApp.getAppContext(), editText);
////                }
//                editText.requestFocus();
//                editText.setSelection(editText.length());
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etBarCode): keyCode=%d, action=%d", keyCode, event.getAction()));
                //Press “esc”
                if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        finishActivity();
                    }
                    return true;
                }
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP && nextFocusView != null) {
                        nextFocusView.requestFocus();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    private void refresh(ScGoodsSku stockGoods) {
        if (stockGoods == null) {
            DialogUtil.showHint("商品无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
            return;
        }

        purchaseGoods = stockGoods;
        tvBarcode.setText(stockGoods.getBarcode());
        etProductName.setText(stockGoods.getSkuName());
        etProductName.setEnabled(false);
        etShortName.setText(stockGoods.getShortName());
        etShortName.setEnabled(false);
        etBuyPrice.setText(String.format("%.2f", stockGoods.getBuyPrice()));
//        etBuyPrice.setEnabled(false);
        etCostPrice.setText(String.format("%.2f", stockGoods.getCostPrice()));
        //显示当前库存
//        etQuantity.setText(String.format("%.2f", stockGoods.getQuantity()));

        etBuyPrice.requestFocus();
        etBuyPrice.setSelection(etBuyPrice.length());
        etQuantity.getText().clear();
//        etCostPrice.setEnabled(false);
//        etQuantity.setEnabled(false);

        if (stockGoods.getPriceType().equals(PriceType.WEIGHT)) {
            spinnerPriceType.setSelection(1);
        } else {
            spinnerPriceType.setSelection(0);
        }
        spinnerPriceType.setEnabled(false);

        if (spinnerPriceType.getSelectedItem().toString().equals("计重")) {
            ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.unit_name_1, R.layout.mfh_spinner_item_text);
            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUnit.setAdapter(unitAdapter);
            spinnerUnit.setSelection(0);
            spinnerUnit.setEnabled(false);
        } else {
            ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.unit_name_0, R.layout.mfh_spinner_item_text);
            unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerUnit.setAdapter(unitAdapter);
            spinnerUnit.setSelection(0);
            spinnerUnit.setEnabled(true);
        }
    }

    /**
     * 选择供应商
     */
    @Bind(R.id.label_invcomp_provider)
    OptionalLabel labelInvcompProvider;

    @OnClick(R.id.label_invcomp_provider)
    public void selectInvCompProvider() {
        //TODO,判断商品是否存在多个供应链，若存在多个，则提示选择供应链
        if (selectInvCompProviderDialog == null) {
            selectInvCompProviderDialog = new SelectInvCompanyInfoDialog(getActivity());
            selectInvCompProviderDialog.setCancelable(false);
            selectInvCompProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectInvCompProviderDialog.init(new SelectInvCompanyInfoDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                curInvCompProvider = companyInfo;
                labelInvcompProvider.setLabelText(curInvCompProvider != null ? curInvCompProvider.getName() : "");
            }

            @Override
            public void onCancel() {

            }
        });
        if (!selectInvCompProviderDialog.isShowing()) {
            selectInvCompProviderDialog.show();
        }
    }

    @Bind(R.id.button_footer_positive)
    Button btnSubmit;

    /**
     * 商品建档入库
     */
    @OnClick(R.id.button_footer_positive)
    public void saveTenantSku() {
        btnSubmit.setEnabled(false);

        if (curInvCompProvider == null){
            DialogUtil.showHint("请选择供应商");
            btnSubmit.setEnabled(true);
            return;
        }

        String buyprice = etBuyPrice.getText().toString();
        if (StringUtils.isEmpty(buyprice)) {
            DialogUtil.showHint("采购价不能为空");
            btnSubmit.setEnabled(true);
            return;
        }
        Double buypriceVal = Double.valueOf(buyprice);
        if (buypriceVal <= 0) {
            DialogUtil.showHint("采购价不能为零");
            btnSubmit.setEnabled(true);
            return;
        }

        String quantity = etQuantity.getText().toString();
        if (StringUtils.isEmpty(quantity)) {
            DialogUtil.showHint("采购数量不能为空");
            btnSubmit.setEnabled(true);
            return;
        }
        Double quantityVal = Double.valueOf(quantity);
//        if (quantityVal <= 0){
//            DialogUtil.showHint("采购数量不能为零");
//            btnSubmit.setEnabled(true);
//            return;
//        }

        String costprice = etCostPrice.getText().toString();
        if (StringUtils.isEmpty(costprice)) {
            DialogUtil.showHint("零售价不能为空");
            btnSubmit.setEnabled(true);
            return;
        }
        Double costpriceVal = Double.valueOf(costprice);
        if (costpriceVal <= 0) {
            DialogUtil.showHint("零售价不能为零");
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

        purchaseGoods.setBuyPrice(buypriceVal);
        purchaseGoods.setCostPrice(costpriceVal);
        purchaseGoods.setQuantity(quantityVal);

        JSONObject jsonObject = new JSONObject();
        JSONObject product = new JSONObject();//产品本身信息
        JSONObject defaultSku = new JSONObject();//产品sku信息
        JSONObject tenantSku = new JSONObject();

        product.put("id", purchaseGoods.getProductId());
        defaultSku.put("id", purchaseGoods.getProSkuId());
        defaultSku.put("skuMask", purchaseGoods.getSkuMask());
        tenantSku.put("id", purchaseGoods.getTenantSkuId());
        product.put("name", purchaseGoods.getSkuName());
        product.put("unit", purchaseGoods.getUnit());
        product.put("priceType", purchaseGoods.getPriceType());
//        product.put("brand", brand);

        product.put("shortName", purchaseGoods.getShortName());
        defaultSku.put("barcode", purchaseGoods.getBarcode());
        tenantSku.put("buyPrice", purchaseGoods.getBuyPrice());
        //Column 'cost_price' cannot be null
        tenantSku.put("costPrice", purchaseGoods.getCostPrice());//默认零售价等于采购价。//
        tenantSku.put("providerId", curInvCompProvider.getId());

        //入库数量不能为空或为0!
        jsonObject.put("quantity", purchaseGoods.getQuantity());
//        item.put("tenantId", MfhLoginService.get().getSpid());

        jsonObject.put("product", product);
        jsonObject.put("defaultSku", defaultSku);
        jsonObject.put("tenantSku", tenantSku);

        CashierApiImpl.saveTenantSku(jsonObject.toJSONString(), responseCallback);
//        animProgress.setVisibility(View.VISIBLE);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("processFailure: " + errMsg);
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
                    onLoadError(errMsg);
                    btnSubmit.setEnabled(true);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}

//                        animProgress.setVisibility(View.GONE);
                    onLoadFinished();
                    /**
                     * 新增商品成功，更新商品库
                     * */
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };
}
