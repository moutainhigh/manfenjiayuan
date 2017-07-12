package com.mfh.litecashier.components;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.pmcstock.PmcStockApiImpl;
import com.mfh.framework.api.pmcstock.StockOutItem;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.StockOutAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 服务－－取包裹（物品明细）
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class StockDetailFragment extends BaseFragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.et_query_content)
    EditText etQueryContent;
    @BindView(R.id.button_query)
    Button btnQuery;
    @BindView(R.id.button_stockOut)
    Button btnStockOut;

    @BindView(R.id.button_toggle)
    ImageButton btnToggleAll;
    @BindView(R.id.product_list)
    RecyclerViewEmptySupport productRecyclerView;
    private LinearLayoutManager linearLayoutManager;

    @BindView(R.id.animProgress)
    ProgressBar animProgress;
    @BindView(R.id.empty_view)
    TextView emptyView;



    private StockOutAdapter productAdapter;

    public static StockDetailFragment newInstance(Bundle args){
        StockDetailFragment fragment = new StockDetailFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_stockout_packagedetail;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("包裹明细");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        etQueryContent.setInputType(InputType.TYPE_CLASS_TEXT);
        etQueryContent.setHint("手机号/面单号/楼幢号/取货码");
        etQueryContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    btnQuery.setEnabled(true);
                } else {
                    btnQuery.setEnabled(false);
                }
            }
        });
        etQueryContent.setOnKeyListener(new EditText.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d(String.format("setOnKeyListener(etQuery):keyCode=%d, action=%d", keyCode, event.getAction()));
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    //按下回车键后会执行两次，
                    // 猜测一，输入框会自动捕获回车按键，自动切换焦点到下一个控件；
                    // 猜测二，通过打印日志观察发现，每次按下按键，都会监听到两次键盘事件，重复导致。
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        loadStockoutList();
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etQueryContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()){
                        DeviceUtils.showSoftInput(CashierApp.getAppContext(), etQueryContent);
                    }
                    else{
                        DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etQueryContent);
                    }
                }
                etQueryContent.requestFocus();
//                etInput.setSelection(etInput.length());
                //返回true,不再继续传递事件
                return true;
            }
        });

        btnStockOut.setEnabled(false);
        initRecyclerView();
    }

    @OnClick(R.id.button_toggle)
    public void toggleAll(){
        if (btnToggleAll.isSelected()){
            btnToggleAll.setSelected(false);
            productAdapter.toggleAll(false);
        }else{
            btnToggleAll.setSelected(true);
            productAdapter.toggleAll(true);
        }
    }

    @OnClick(R.id.button_query)
    public void loadStockoutList(){
        String queryText = etQueryContent.getText().toString();
        if (StringUtils.isEmpty(queryText)){
            return;
        }

        DeviceUtils.hideSoftInput(CashierApp.getAppContext(), etQueryContent);
        etQueryContent.getText().clear();
        animProgress.setVisibility(View.VISIBLE);
//        btnStockOut.setEnabled(false);
        //查询出库列表
        PmcStockApiImpl.findStockOutByCode(queryText, queryRsCallBack);
    }

    @OnClick(R.id.button_stockOut)
    public void stockOut() {
        animProgress.setVisibility(View.VISIBLE);
        btnStockOut.setEnabled(false);
        List<StockOutItem> items = productAdapter.getSelectedPaidEntityList();
        if (items == null || items.size() < 1){
            animProgress.setVisibility(View.GONE);
            btnStockOut.setEnabled(true);
            return;
        }

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            animProgress.setVisibility(View.GONE);
            btnStockOut.setEnabled(true);
            return;
        }

        final List<StockOutItem> stockOutItems = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        for (StockOutItem item : items){
            JSONObject jsonObject = new JSONObject();
            //orderId、items、btype、curStatus等通过findStockOut查询获取的待出库列表信息中有。
            jsonObject.put("orderId", item.getGoodsId());//包裹物件Id
            jsonObject.put("items", item.getItems());//物件内部明细id(可空)
            jsonObject.put("stockId", item.getStockId());//仓库编号
            jsonObject.put("tokentype", 0);//自提(0)或代取(1)
            jsonObject.put("transHumanId", item.getTransHumanId());//物流承担者人或车辆Id（可空)
            jsonObject.put("btype", item.getItemType());//包裹业务类型
            jsonObject.put("curStatus", item.getStatus());//包裹当前状态

            jsonArray.add(jsonObject);

            stockOutItems.add(item);
        }


        //回调
        NetCallBack.NetTaskCallBack stockoutResponseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        try{
                            //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            String retStr = retValue.getValue();

                            //出库成功:1-556637
                            ZLogger.d("出库成功:" + retStr);
//                            if(humanCompany != null && humanCompany.getOptionList() != null && humanCompany.getOptionList().size() > 0){
//                                HumanCompanyOption option = humanCompany.getOptionList().get(0);
//                                tvMemberCompany.setText(option.getValue());
//                            }
                            PrinterFactory.getPrinterManager().printStockOutOrder(stockOutItems);

                            animProgress.setVisibility(View.GONE);
                            btnStockOut.setEnabled(true);
                            loadStockoutList();
                        }catch(Exception ex){
                            ZLogger.e("stockoutResponseCallback, " + ex.toString());

                            animProgress.setVisibility(View.GONE);
                            btnStockOut.setEnabled(true);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("出库失败：" + errMsg);

                        animProgress.setVisibility(View.GONE);
                        btnStockOut.setEnabled(true);
//                        loadStockoutList();
                    }
                }
                , String.class
                , CashierApp.getAppContext())
        {
        };
        PmcStockApiImpl.stockOut(jsonArray.toJSONString(), stockoutResponseCallback);
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        productRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        productRecyclerView.setHasFixedSize(true);
        //添加分割线
        productRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        productRecyclerView.setEmptyView(emptyView);

        productAdapter = new StockOutAdapter(CashierApp.getAppContext(), null);
        productAdapter.setOnAdapterListener(new StockOutAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
                //显示 总计－－金额
//                SerialDisplayHelper.show(2, productAdapter.getProductAmount());

                List<StockOutItem> items = productAdapter.getSelectedEntityList();
                if (items == null || items.size() < 1){
                    btnStockOut.setEnabled(false);
                }else{
                    btnStockOut.setEnabled(true);
                }

                btnToggleAll.setSelected(productAdapter.isSelectAll());
            }
        });
        productRecyclerView.setAdapter(productAdapter);
    }


    private NetCallBack.QueryRsCallBack queryRsCallBack =  new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<StockOutItem>(new PageInfo(1, 50)) {
        @Override
        public void processQueryResult(RspQueryResult<StockOutItem> rs) {
            //此处在主线程中执行。
            int retSize = rs.getReturnNum();

            List<StockOutItem> result = new ArrayList<>();
            if (retSize > 0) {
                for (int i = 0; i < retSize; i++) {
                    result.add(rs.getRowEntity(i));
                }
            }
            productAdapter.setEntityList(result);

            animProgress.setVisibility(View.GONE);
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);

            animProgress.setVisibility(View.GONE);
        }
    }, StockOutItem.class, CashierApp.getAppContext());

}
