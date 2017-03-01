package com.mfh.litecashier.ui.prepare;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.bingshanguxue.vector_uikit.EditInputType;
import com.bingshanguxue.vector_uikit.dialog.NumberInputDialog;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.presenter.ScOrderPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IScOrderView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 首页－－采购
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class PrepareStep1Fragment extends BaseFragment implements IScOrderView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.label_quantity)
    MultiLayerLabel labelQuantity;
    @BindView(R.id.label_amount)
    MultiLayerLabel labelAmount;
    @BindView(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;
    @BindView(R.id.recyclerView)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private PrepareTypeAdapter menuAdapter;

    private String tradeNo;
    private BizSubTypeWrapper mBizSubTypeWrapper = null;
    private ScOrderPresenter mScOrderPresenter;
    private List<CashierShopcartEntity> mShopcartEntities;


    public static PrepareStep1Fragment newInstance(Bundle args) {
        PrepareStep1Fragment fragment = new PrepareStep1Fragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_pick_step1;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
        mScOrderPresenter = new ScOrderPresenter(this);
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        ZLogger.df(String.format("拣货，%s", StringUtils.decodeBundle(args)));

        if (args != null) {
            tradeNo = args.getString(PrepareActivity.EXTRA_KEY_TRADENO);
        }

        toolbar.setTitle("拣货");
//        setSupportActionBar(toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_close) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
                return true;
            }
        });

        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_normal);

        initInlvBarcode();
        initMenuRecyclerView();
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (inlvBarcode != null){
            inlvBarcode.clear();
//        inlvBarcode.setInputString("cest");
            inlvBarcode.requestFocusEnd();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_normal, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initInlvBarcode() {
//        inlvBarcode.setSoftKeyboardEnabled(false);
//        inlvBarcode.setDigits(2);
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_MULTIPLY, KeyEvent.KEYCODE_NUMPAD_ADD}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    submitOrder(inlvBarcode.getInputString());
                }
            }
        });
        inlvBarcode.registerOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onClickAction1(String text) {
                submitOrder(text);
            }

            @Override
            public void onLongClickAction1(String text) {

            }
        });
        inlvBarcode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
                            || inlvBarcode.isSoftKeyboardEnabled()) {
                        showBarcodeKeyboard();
                    }
                }

                inlvBarcode.requestFocusEnd();
                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    private void initMenuRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getActivity(), 6);
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.5f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        menuAdapter = new PrepareTypeAdapter(CashierApp.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new PrepareTypeAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                responseMenu(menuAdapter.getEntity(position));
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
        menuAdapter.setEntityList(getSubTypes());
    }

    private NumberInputDialog barcodeInputDialog = null;
    /**
     * 显示条码输入界面
     * 相当于扫描条码
     */
    private void showBarcodeKeyboard() {
        if (barcodeInputDialog == null) {
            barcodeInputDialog = new NumberInputDialog(getActivity());
            barcodeInputDialog.setCancelable(true);
            barcodeInputDialog.setCanceledOnTouchOutside(true);
        }
        barcodeInputDialog.initializeBarcode(EditInputType.BARCODE, "订单编号", "订单编号", "确定",
                new NumberInputDialog.OnResponseCallback() {
                    @Override
                    public void onNext(String value) {
                        if (StringUtils.isEmpty(value)){
                            return;
                        }

                        if (mBizSubTypeWrapper == null){
                            submitOrder(value);
                        }
                        else{
                            Bundle args = new Bundle();
                            args.putSerializable(PrepareTakeoutFragment.EXTRA_KEY_SUBTYPE,
                                    mBizSubTypeWrapper);
                            args.putString(PrepareEvent.KEY_OUTERNO, value);
                            EventBus.getDefault().post(new PrepareEvent(PrepareEvent.ACTION_PREPARE_TAKEOUT, args));
                        }
                    }

                    @Override
                    public void onNext(Double value) {

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
        barcodeInputDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mBizSubTypeWrapper = null;
            }
        });
//        barcodeInputDialog.setMinimumDoubleCheck(0.01D, true);
        if (!barcodeInputDialog.isShowing()) {
            barcodeInputDialog.show();
        }
    }

    /**
     * 获取菜单
     */
    private synchronized List<BizSubTypeWrapper> getSubTypes() {
        List<BizSubTypeWrapper> functionalList = new ArrayList<>();
        functionalList.add(new BizSubTypeWrapper(PosType.POS_TYPE_MEITUAN,
                "美团", R.mipmap.ic_plat_meituan));
        functionalList.add(new BizSubTypeWrapper(PosType.POS_TYPE_ELEM,
                "饿了么", R.mipmap.ic_plat_eleme));
        functionalList.add(new BizSubTypeWrapper(PosType.POS_TYPE_BAIDU,
                "百度外卖", R.mipmap.ic_plat_baiduwaimai));
        functionalList.add(new BizSubTypeWrapper(PosType.POS_TYPE_KOUBEI,
                "口碑", R.mipmap.ic_plat_koubei));
        functionalList.add(new BizSubTypeWrapper(PosType.POS_TYPE_DIANPING,
                "大众点评", R.mipmap.ic_plat_dianping));
        functionalList.add(new BizSubTypeWrapper(PosType.POS_TYPE_TELPHONE,
                "电话", R.mipmap.ic_plat_telephone));

        return functionalList;
    }

    private void responseMenu(BizSubTypeWrapper bizSubTypeWrapper){
        List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                .queryAllBy(String.format("posTradeNo = '%s'", tradeNo));
        if (shopcartEntities != null && shopcartEntities.size() > 0){
            mBizSubTypeWrapper = bizSubTypeWrapper;

            showBarcodeKeyboard();
        }
        else{
            DialogUtil.showHint("未添加商品");
        }
    }

    /**
     * 查询订单
     */
    private void submitOrder(String barcode) {
        inlvBarcode.clear();
        inlvBarcode.requestFocusEnd();
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        // TODO: 17/10/2016 查询订单
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        mScOrderPresenter.getByBarcode(barcode, ScOrder.MFHORDER_STATUS_BUYING, true);
    }

    private void refresh(){
        inlvBarcode.clear();
//        inlvBarcode.setInputString("cest");
        inlvBarcode.requestFocusEnd();

        if (!StringUtils.isEmpty(tradeNo)){
            mShopcartEntities = CashierShopcartService.getInstance()
                    .queryAllBy(String.format("posTradeNo = '%s'", tradeNo));
        }

        Double bcount = 0D, amount = 0D;
        if (mShopcartEntities != null && mShopcartEntities.size() > 0){
            for (CashierShopcartEntity entity : mShopcartEntities){
                bcount += entity.getBcount();
                amount += entity.getFinalAmount();
            }
        }

        labelQuantity.setTopText(MUtils.formatDouble(bcount, ""));
        labelAmount.setTopText(MUtils.formatDouble(amount, ""));
    }
    /**
     * 刷新订单
     * */
    private void refreshScOrder(ScOrder scOrder) {
        if (scOrder == null){
            hideProgressDialog();
            DialogUtil.showHint("未找到订单");
            return;
        }

        List<ScOrderItem> items = scOrder.getItems();
        if (!StringUtils.isEmpty(tradeNo) && items != null && items.size() > 0){
            for (ScOrderItem item : items){
                String sqlWhere = String.format("posTradeNo = '%s' and proSkuId = '%d'",
                        tradeNo, item.getSkuId());
                CashierShopcartEntity entity = CashierShopcartService
                        .getInstance().getEntityBy(sqlWhere);
                if (entity != null){
                    item.setQuantityCheck(entity.getBcount());
                }
                else{
                    item.setQuantityCheck(item.getBcount());
                }
            }
        }
        hideProgressDialog();

        Bundle args = new Bundle();
        args.putSerializable(PrepareEvent.KEY_SCORDER, scOrder);
        EventBus.getDefault().post(new PrepareEvent(PrepareEvent.ACTION_PREPARE, args));
    }

    @Override
    public void onIScOrderViewProcess() {

    }

    @Override
    public void onIScOrderViewError(String errorMsg) {
        refreshScOrder(null);
    }

    @Override
    public void onIScOrderViewSuccess(PageInfo pageInfo, List<ScOrder> dataList) {

    }

    @Override
    public void onIScOrderViewSuccess(ScOrder data) {
        refreshScOrder(data);
    }

}
