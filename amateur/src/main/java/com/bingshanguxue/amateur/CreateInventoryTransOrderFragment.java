package com.mfh.litecashier.ui.fragment.inventory;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.manfenjiayuan.business.bean.wrapper.CreateOrderItemWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.OptionalLabel;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.InventoryTransEvent;
import com.mfh.litecashier.ui.adapter.CreateOrderItemAdapter;
import com.mfh.litecashier.ui.dialog.SelectCompanyInfoDialog;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.utils.ACacheHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 库存调拨--新建调拨单
 * Created by Nat.ZZN(bingshanguxue) on 15/09/24.
 */
public class CreateInventoryTransOrderFragment extends BaseFragment implements IInventoryView {

    public static final String EK_ENTERMODE = "enterMode";
    public static final String EXTRA_KEY_READCACHE_ENABLED = "EXTRA_KEY_READCACHE_ENABLED";//是否读取缓存

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @Bind(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;

    @Bind(R.id.goods_list)
    RecyclerView productRecyclerView;

    @Bind(R.id.tv_goods_quantity)
    TextView tvGoodsQuantity;
    @Bind(R.id.tv_total_amount)
    TextView tvTotalAmount;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    /*调拨*/
    @Bind(R.id.label_allocation_out)
    OptionalLabel labelAllocationOut;
    @Bind(R.id.label_allocation_in)
    OptionalLabel labelAllocationIn;
    /*备注*/
    @Bind(R.id.et_remark)
    EditText etRemark;

    private ItemTouchHelper itemTouchHelper;
    private CreateOrderItemAdapter productAdapter;
    private SelectCompanyInfoDialog selectTenantDialog;

    private InventoryGoodsPresenter inventoryGoodsPresenter;

    private int enterMode = 0;
    private CompanyInfo outCompanyInfo;
    private CompanyInfo inCompanyInfo;

    public static CreateInventoryTransOrderFragment newInstance(Bundle args) {
        CreateInventoryTransOrderFragment fragment = new CreateInventoryTransOrderFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_inventory_allocation_create;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        inventoryGoodsPresenter = new InventoryGoodsPresenter(this);

        tvHeaderTitle.setText("新建调拨单");
        labelAllocationIn.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                changeInCompany(null);
//                inlvBarcode.setVisibility(View.VISIBLE);
                inlvBarcode.requestFocus();
//                productAdapter.setEntityList(null);//清空商品

            }
        });
        initBarCodeInput();
        initGoodsRecyclerView();

        initData();
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
    public void finishAndSaveData() {
        List<CreateOrderItemWrapper> goodsList = productAdapter.getEntityList();
        if (goodsList != null && goodsList.size() > 0) {
            //保存缓存数据
            JSONArray cacheArrays = new JSONArray();
            cacheArrays.addAll(goodsList);
            ACacheHelper.put(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_GOODS_DATA, cacheArrays.toJSONString());
        }

        if (inCompanyInfo != null) {
            JSONArray cacheArrays = new JSONArray();
            cacheArrays.add(inCompanyInfo);
            ACacheHelper.put(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_TENANT_DATA, cacheArrays.toJSONString());
        }

        //TODO 通知刷新

        getActivity().finish();
    }

    /**
     * 选择调出方
     */
    @OnClick(R.id.label_allocation_out)
    public void selectOutCompany() {
        if (selectTenantDialog == null) {
            selectTenantDialog = new SelectCompanyInfoDialog(getActivity());
            selectTenantDialog.setCancelable(false);
            selectTenantDialog.setCanceledOnTouchOutside(false);
        }
        selectTenantDialog.init(AbilityItem.TENANT, new SelectCompanyInfoDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                outCompanyInfo = companyInfo;
                labelAllocationOut.setLabelText(outCompanyInfo != null ? outCompanyInfo.getName() : "");
            }
        });
        if (!selectTenantDialog.isShowing()) {
            selectTenantDialog.show();
        }
    }

    /**
     * 选择调入方
     */
    @OnClick(R.id.label_allocation_in)
    public void selectInCompany() {
        if (selectTenantDialog == null) {
            selectTenantDialog = new SelectCompanyInfoDialog(getActivity());
            selectTenantDialog.setCancelable(false);
            selectTenantDialog.setCanceledOnTouchOutside(false);
        }
        selectTenantDialog.init(AbilityItem.TENANT, new SelectCompanyInfoDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                if (outCompanyInfo != null && companyInfo != null && outCompanyInfo.getId().equals(companyInfo.getId())) {
                    DialogUtil.showHint("调入和调出门店不能相同");
                    return;
                }
                changeInCompany(companyInfo);
            }
        });
        if (!selectTenantDialog.isShowing()) {
            selectTenantDialog.show();
        }
    }

    /**
     * 切换发货方
     * */
    private void changeInCompany(CompanyInfo companyInfo){
        inCompanyInfo = companyInfo;
        labelAllocationIn.setLabelText(inCompanyInfo != null ? inCompanyInfo.getName() : "");
    }

    private void initData() {
        //默认调出方为当前登录用户
        outCompanyInfo = new CompanyInfo();
        outCompanyInfo.setId(MfhLoginService.get().getCurOfficeId());
        outCompanyInfo.setTenantId(MfhLoginService.get().getSpid());
        outCompanyInfo.setName(MfhLoginService.get().getCurOfficeName());
        labelAllocationOut.setLabelText(outCompanyInfo != null ? outCompanyInfo.getName() : "");
        //TODO, 只能调出不能调入。
        labelAllocationOut.setEnabled(false);

        Bundle args = getArguments();
        if (args != null) {
            enterMode = args.getInt(EK_ENTERMODE);

            if (enterMode == 2) {
                String cacheStr = ACacheHelper.getAsString(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_GOODS_DATA);
                List<CreateOrderItemWrapper> cacheData = JSONArray.parseArray(cacheStr, CreateOrderItemWrapper.class);

                String tenantCache = ACacheHelper.getAsString(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_TENANT_DATA);
                List<CompanyInfo> tenantData = JSONArray.parseArray(tenantCache,
                        CompanyInfo.class);
                if (tenantData != null && tenantData.size() > 0) {
                    inCompanyInfo = tenantData.get(0);
                } else {
                    inCompanyInfo = null;
                }

                clearCacheData();

                productAdapter.setEntityList(cacheData);
            }

            if (inCompanyInfo == null){
                labelAllocationIn.setLabelText("");
                selectInCompany();
            }
            else{
                labelAllocationIn.setLabelText(inCompanyInfo.getName());
            }
        }
    }

    /**
     * 清空缓存数据
     */
    private void clearCacheData() {
        ACacheHelper.remove(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_TENANT_DATA);
        ACacheHelper.remove(ACacheHelper.TCK_INVENTORY_CREATEALLOCATION_GOODS_DATA);
    }

    /**
     * 初始化条码输入
     */
    private void initBarCodeInput() {
        inlvBarcode.setEnterKeySubmitEnabled(true);
        inlvBarcode.setSoftKeyboardEnabled(false);
        inlvBarcode.requestFocus();
        inlvBarcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        queryByBarcode();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
    }

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
        //添加分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        productAdapter = new CreateOrderItemAdapter(getActivity(), null, false, true);
        productAdapter.setOnAdapterListener(new CreateOrderItemAdapter.OnAdapterListener() {

            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
                refreshBottomBar();
            }
        });
        productRecyclerView.setAdapter(productAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(productAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(productRecyclerView);
    }

    /**
     * 查询商品
     */
    private void queryByBarcode() {
        final String barcode = inlvBarcode.getInputString();
        if (StringUtils.isEmpty(barcode)) {
            DialogUtil.showHint("请输入商品条码");
            return;
        }
        inlvBarcode.clear();

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        inventoryGoodsPresenter.loadInventoryGoods(new PageInfo(-1, 10), null, barcode,
                null, getSortType(), null);
    }

    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        List<CreateOrderItemWrapper> goodsList = productAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            btnSubmit.setEnabled(false);
            DialogUtil.showHint("商品不能为空");
            return;
        }

//        1:签收网点不能为空!
        if (inCompanyInfo == null) {
            btnSubmit.setEnabled(true);
            DialogUtil.showHint("请选择调入门店");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            return;
        }

        JSONObject jsonStrObject = new JSONObject();
        jsonStrObject.put("sendNetId", outCompanyInfo.getId());
        jsonStrObject.put("sendTenantId", outCompanyInfo.getTenantId());
        jsonStrObject.put("receiveNetId", inCompanyInfo.getId());
        jsonStrObject.put("remark", etRemark.getText().toString());
        jsonStrObject.put("bizType", InvOrderApi.BIZTYPE_PURCHASE);
        jsonStrObject.put("orderType", InvOrderApi.ORDERTYPE_RECEIPT);

        JSONArray itemsArray = new JSONArray();
        for (CreateOrderItemWrapper goods : goodsList) {
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("providerId", goods.getProviderId());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）
            item.put("proSkuId", goods.getProSkuId());
            item.put("productName", goods.getProductName());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("price", goods.getPrice());
            item.put("amount", goods.getAmount());

            itemsArray.add(item);
        }
        jsonStrObject.put("items", itemsArray);

        InvSendIoOrderApiImpl.createInvSendIoOrder(true, jsonStrObject.toJSONString(), responseCallback);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("新建调拨单失败: " + errMsg);
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
                    DialogUtil.showHint("新建调拨单失败" + errMsg);
                    btnSubmit.setEnabled(true);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}

//                        animProgress.setVisibility(View.GONE);
                    /**
                     * 新建调拨单，更新采购单列表
                     * */
                    ZLogger.d("新建调拨单成功: ");
                    getActivity().finish();

                    SharedPreferencesHelper.set(SharedPreferencesHelper.PK_SYNC_INVTRANSORDER_OUT_ENABLED, true);
                    if (enterMode == 1) {
                        EventBus.getDefault().post(new InventoryTransEvent(InventoryTransEvent.EVENT_ID_RELOAD_DATA));
                    }
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    private void refreshBottomBar() {
        if (productAdapter != null && productAdapter.getItemCount() > 0) {
            Double quantityCheck = 0D;
            Double amount = 0D;
            List<CreateOrderItemWrapper> entityList = productAdapter.getEntityList();
            for (CreateOrderItemWrapper entity : entityList) {
                quantityCheck += entity.getQuantityCheck();
                amount += entity.getAmount();
            }
            tvGoodsQuantity.setText(String.format("商品数：%.2f", quantityCheck));
            tvTotalAmount.setText(String.format("商品金额：%.2f", amount));
            btnSubmit.setVisibility(View.VISIBLE);
        } else {
            tvGoodsQuantity.setText(String.format("商品数：%d", 0));
            tvTotalAmount.setText(String.format("商品金额：%.2f", 0D));
            btnSubmit.setVisibility(View.INVISIBLE);
        }
    }


    public Long getOtherTenantId() {
        return null;
    }

    public String getBarcode() {
        return null;
    }

    public int getSortType() {
        return 0;
    }

    @Override
    public void onProcess() {

    }

    @Override
    public void onError(String errorMsg) {
        DialogUtil.showHint("未找到商品");
    }

    @Override
    public void onData(ScGoodsSku data) {

    }

    @Override
    public void onList(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        if (dataList != null && dataList.size() > 0) {
            productAdapter.appendStockGoods(dataList.get(0));
        } else {
            DialogUtil.showHint("未找到商品");
        }
    }
}
