package com.mfh.enjoycity.ui.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.ShopCartAdapter;
import com.mfh.enjoycity.bean.ShopProductBean;
import com.mfh.enjoycity.bean.SubdisBean;
import com.mfh.enjoycity.database.AnonymousAddressService;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.CreateOrderActivity;
import com.mfh.enjoycity.ui.ProductDetailActivity;
import com.mfh.enjoycity.ui.activity.MainActivity;
import com.mfh.enjoycity.ui.activity.SearchCommunityActivity;
import com.mfh.enjoycity.ui.activity.SelectAddressActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.OrderHelper;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 购物车
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class ShoppingCartFragment extends BaseFragment {

    @Bind(R.id.view_empty)
    View mEmptyView;

    @Bind(R.id.view_footer)
    View mFooterView;
    @Bind(R.id.tv_total_price)
    TextView tvTotalPrice;

    @Bind(R.id.listview_procucts)
    RecyclerView mRecyclerView;
    private ShopCartAdapter mShoppingCartAdapter;

    public ShoppingCartFragment() {
        super();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_shopping_cart;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        initRecyclerView();

        setAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.d(String.format("ShoppingcartFragment.onActivityResult.requestCode=%d, resultCode=%d", requestCode, resultCode));

        switch (requestCode){
            case Constants.ACTIVITY_REQUEST_CODE_CHANGE_RECEIVE_ADDR:{
                if(resultCode == Activity.RESULT_OK){
                    mShoppingCartAdapter.notifyDataSetChanged();
                }
                else{

                }
            }
            break;
            case Constants.ACTIVITY_REQUEST_CODE_SEARCH_COMMUNITY:{
                if(resultCode == Activity.RESULT_OK){
                    SubdisBean bean = (SubdisBean)data.getSerializableExtra(Constants.INTENT_KEY_ADDRESS_DATA);

                    AnonymousAddressService dbService = AnonymousAddressService.get();
                    dbService.saveOrUpdate(bean);

                    ShopcartHelper.getInstance().refreshAnonymousOrderAddr(String.valueOf(bean.getId()));

                    mShoppingCartAdapter.notifyDataSetChanged();
                }
                else{

                }
            }
            break;
            case Constants.ACTIVITY_REQUEST_CODE_SHOW_PRODUCT_DETAIL:{
                if(resultCode == Activity.RESULT_OK){
                    ShoppingCartService dbService = ShoppingCartService.get();
                    mShoppingCartAdapter.setData(dbService.queryAllForAdapter());
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 跳转至登录页面
     * */
    @OnClick(R.id.button_buy)
    public void redirectToMain(){
        UIHelper.startActivity(getContext(), MainActivity.class);

        getActivity().finish();
    }

    /**
     * 确认下单
     * */
    @OnClick(R.id.btn_confirm_order)
    public void confirmOrder(){
        //检查是否选择收货地址
        if (!ShopcartHelper.getInstance().bSelectAddress()){
            DialogUtil.showHint("请选择收货地址");
            return;
        }

        //TODO,确认下单
//        ShopcartHelper.getInstance().preCreateOrder();
        ShoppingCartService dbService = ShoppingCartService.get();
        List<ShopProductBean> beanList = dbService.queryAllForAdapter();
        //TODO
        List<ShopProductBean> beanList1 = new ArrayList<>();//未满起送价格
        List<ShopProductBean> beanListFinal = new ArrayList<>();
        for (ShopProductBean bean : beanList){
            if (bean.getTotalAmount() < ShopcartHelper.MIN_DELIVER_PRICE){
                beanList1.add(bean);
            }
            else{
                beanListFinal.add(bean);
            }
        }

        StringBuilder sb = new StringBuilder();
        if (beanListFinal.size() > 0){

            if (beanList1.size() > 0){
                //未满起送额店铺信息
                for (int i = 0 ; i < beanList1.size(); i++){
                    Long shopId = beanList1.get(i).getShopId();
                    ShopEntity entity = ShopService.get().getEntityById(shopId);

                    if (i > 0){
                        sb.append(",");
                    }
                    sb.append(entity.getShopName());
                    if (i == beanList1.size() - 1){
                        sb.append(String.format("的商品未满起送额(￥%.2f)，会保留在购物车中。", ShopcartHelper.MIN_DELIVER_PRICE));
                    }
                }

                sb.append("当前只能下单 ");
                for (int i = 0 ; i < beanListFinal.size(); i++){
                    Long shopId = beanListFinal.get(i).getShopId();
                    ShopEntity entity = ShopService.get().getEntityById(shopId);

                    if (i > 0){
                        sb.append(",");
                    }
                    sb.append(entity.getShopName());
                    if (i == beanList1.size()){
                        sb.append(" 的商品。");
                    }
                }

                showConfirmOrderDialog(sb.toString(), beanListFinal);
            }
            else{
                OrderHelper.getInstance().restore();
                OrderHelper.getInstance().saveOrderProducts(beanList);
               UIHelper.startActivity(getContext(), CreateOrderActivity.class);
            }
        }
        else {
            if (beanList1.size() > 0){
                //未满起送额店铺信息
                for (int i = 0 ; i < beanList1.size(); i++){
                    Long shopId = beanList1.get(i).getShopId();
                    ShopEntity entity = ShopService.get().getEntityById(shopId);

                    if (i > 0){
                        sb.append(",");
                    }
                    sb.append(String.format("[%s]最低起送价为￥%.2f", entity.getShopName(), ShopcartHelper.MIN_DELIVER_PRICE));
                }
            }

            showConfirmOrderDialog(sb.toString(), beanListFinal);
        }

    }

    private void showConfirmOrderDialog(String message, final List<ShopProductBean> beanList){
        showConfirmDialog("确定要下单吗？",
                "下单", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        OrderHelper.getInstance().restore();
                        OrderHelper.getInstance().saveOrderProducts(beanList);
                        UIHelper.startActivity(getContext(), CreateOrderActivity.class);
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }


    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 16, 16));
//添加分割线
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(
//                getActivity(), DividerItemDecoration.HORIZONTAL_LIST));
//        mRecyclerView.setOnTouchListener(
//                new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        return mIsRefreshing;
//                    }
//                }
//        );
//        mRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                int position = mRecyclerView.getChildPosition(v);
//                DialogUtil.showHint("click " + position);
//                return false;
//            }
//        });
    }

    private void setAdapter() {
        //获取购物车数据
        ShoppingCartService shoppingCartService = ShoppingCartService.get();
        mShoppingCartAdapter = new ShopCartAdapter(getActivity(), shoppingCartService.queryAllForAdapter());
        mShoppingCartAdapter.setOnItemClickLitener(new ShopCartAdapter.OnAdapterListener() {

            @Override
            public void onChangeAddress() {
                if (MfhLoginService.get().haveLogined()){
                    Intent intent = new Intent(getContext(), SelectAddressActivity.class);
                    startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_CHANGE_RECEIVE_ADDR);
                }else{
                    Intent intent = new Intent(getContext(), SearchCommunityActivity.class);
                    startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SEARCH_COMMUNITY);
                }
            }

            @Override
            public void onItemClick(View view, final int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onDataSetChanged() {
                showTotalPrice();
            }

            @Override
            public void showProductDetail(ShoppingCartEntity entity) {
                Bundle extras = new Bundle();
                extras.putInt(ProductDetailActivity.EXTRA_KEY_ANIM_TYPE, 0);
                extras.putLong(ProductDetailActivity.EXTRA_KEY_PRODUCT_ID, entity.getProductId());
                extras.putLong(ProductDetailActivity.EXTRA_KEY_SHOP_ID, entity.getShopId());

                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtras(extras);
                startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SHOW_PRODUCT_DETAIL);
            }

        });
        mRecyclerView.setAdapter(mShoppingCartAdapter);

        showTotalPrice();
    }

    private void reload(){
        //获取购物车数据
        ShoppingCartService dbService = ShoppingCartService.get();
        mShoppingCartAdapter.setData(dbService.queryAllForAdapter());

        showTotalPrice();
    }


    private void showTotalPrice(){
        double totalPrice = 0;

        if (mShoppingCartAdapter != null){
            List<ShopProductBean> dataSet = mShoppingCartAdapter.getData();
            if (dataSet != null && dataSet.size() > 0){
                mFooterView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                for (ShopProductBean adapterData : dataSet){
                    totalPrice += adapterData.getTotalAmount();
                }
            }
            else{
                mFooterView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
        else{
            mFooterView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }

        tvTotalPrice.setText(String.format("￥ %.2f", totalPrice));
    }


}
