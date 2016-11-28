package com.manfenjiayuan.mixicook_vip.ui.order;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.SettingsItem;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartService;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.InputTextFragment;
import com.manfenjiayuan.mixicook_vip.widget.LabelView2;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.commonuseraccount.CommonUserAccountApiImpl;
import com.mfh.framework.api.commonuseraccount.PayAmount;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.pmcstock.MarketRules;
import com.mfh.framework.api.pmcstock.PmcStockApi;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.api.scOrder.ScOrderApi;
import com.mfh.framework.api.shoppingCart.Cart;
import com.mfh.framework.api.shoppingCart.CartPack;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 下单页面
 * Created by bingshanguxue on 6/28/16.
 */
public class OrderCreateFragment extends BaseFragment {
    public static final String EXTRA_KEY_ORDERBRIEF = "orderBrief";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.address_view)
    LabelView2 mAddressView;
    @BindView(R.id.tv_shop_name)
    TextView tvShopName;
    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private OrderGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @BindView(R.id.empty_view)
    View emptyView;
    @BindView(R.id.item_serviceTime)
    SettingsItem serviceTiemItem;
    @BindView(R.id.item_remark)
    SettingsItem remarkItem;
    @BindView(R.id.item_coupon)
    SettingsItem marketRuleView;
    @BindView(R.id.goodsAmount_view)
    SettingsItem mGoodsAmountView;
    @BindView(R.id.transfee_view)
    LabelView2 mTransFeeView;
    @BindView(R.id.tv_brief)
    TextView tvBrief;
    @BindView(R.id.button_confirm)
    Button btnSubmit;

    private CreateOrderBrief mCreateOrderBrief;//
    private MarketRuleBrief mMarketRuleBrief = new MarketRuleBrief();//优惠券信息
    private PayAmount mPayAmount;
    private JSONArray productsInfo = new JSONArray();
    private SelectTimeDialog mSelectTimeDialog = null;

    public static OrderCreateFragment newInstance(Bundle args) {
        OrderCreateFragment fragment = new OrderCreateFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_order_create;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mCreateOrderBrief = (CreateOrderBrief) args.getSerializable(EXTRA_KEY_ORDERBRIEF);
        }
        toolbar.setTitle("下单");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        initGoodsRecyclerView();
        remarkItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_INPUT_TEXT);
                extras.putString(InputTextFragment.EXTRA_KEY_TITLE, "备注");
                extras.putString(InputTextFragment.EXTRA_KEY_HINT_TEXT, "请输入备注信息");
                extras.putString(InputTextFragment.EXTRA_KEY_RAW_TEXT, mCreateOrderBrief.getRemark());
                Intent intent = new Intent(getActivity(), FragmentActivity.class);
                intent.putExtras(extras);
                startActivityForResult(intent, ARCode.ARC_INUT_TEXT);
            }
        });
        serviceTiemItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDueDate();
            }
        });
        loadInit();
    }

    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
        mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                int lastVisibleItem = mRLayoutManager.findLastVisibleItemPosition();
//                int totalItemCount = mRLayoutManager.getItemCount();
//                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
//                // dy>0 表示向下滑动
////                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
//                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
//                    if (!isLoadingMore) {
//                        loadMore();
//                    }
//                } else if (dy < 0) {
//                    isLoadingMore = false;
//                }
            }
        });

        goodsListAdapter = new OrderGoodsAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListsner(new OrderGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onItemClick(View view, int position) {

                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
//                                                      onLoadFinished();

//                                                      refreshFabShopcart();
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_INUT_TEXT: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String remark = data.getStringExtra(InputTextFragment.EXTRA_KEY_RESULT);
                    if (mCreateOrderBrief != null) {
                        mCreateOrderBrief.setRemark(remark);

                        refreshRemark();
                    }
                }
            }
            break;
            case ARCode.ARC_ORDER_COUPONS: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // TODO: 13/10/2016 保存优惠券
                    MarketRuleBrief marketRuleBrief = (MarketRuleBrief) data.getSerializableExtra(OrderCouponsFragment.EXTRA_KEY_MARKETRULEBRIEF);
                    refreshMarketRule(marketRuleBrief);
                    // TODO: 13/10/2016 计算订单金额
                    getPayAmountByOrderInfo();
                }
            }
            break;
            case ARCode.ARC_ORDER_PAY: {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.button_confirm)
    public void submitOrder() {
        btnSubmit.setEnabled(false);
        if (StringUtils.isEmpty(mCreateOrderBrief.getDueDateSpan())) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, "请选择期望送达时间", true);
            btnSubmit.setEnabled(true);
            return;
        }

        //确认下单不需要出现提示框，此次应当确保购买主流程的快速完成，而不是安全性
        saveOrder();
//        showConfirmDialog("是否确认下单？",
//                "下单", new DialogInterface.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                        saveOrder();
//                    }
//                }, "点错了", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
    }

    /**
     * 下单
     */
    private void saveOrder() {
        btnSubmit.setEnabled(false);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        String couponIds = mMarketRuleBrief != null ?
                ObjectsCompact.splitLong(mMarketRuleBrief.getCouponIds(), ",") : null;
        String ruleIds = mMarketRuleBrief != null ?
                ObjectsCompact.splitLong(mMarketRuleBrief.getRuleIds(), ",") : null;

        StringBuilder cartIds = new StringBuilder();
        JSONArray items = new JSONArray();
        Double bcount = 0D, amount = 0D;
        List<CartPack> productList = mCreateOrderBrief.getPacks();
        if (productList != null && productList.size() > 0) {
            for (CartPack entity : productList) {
                JSONObject item = new JSONObject();
                Cart cart = entity.getCart();

                item.put("shopId", cart.getShopId());//订单项店铺编号（NumberFormat）
                item.put("productId", cart.getProductId());//订单项商品编号（NumberFormat）
                item.put("goodsId", cart.getGoodsId());//订单项商品编号（NumberFormat）
                item.put("skuId", cart.getProSkuId());//订单项商品编号（NumberFormat）
                item.put("bcount", cart.getBcount());//订单项件数
                item.put("price", cart.getPrice());//商品单价
                item.put("amount", cart.getAmount());//订单项总价
                item.put("remark", entity.getProductName());//备注
                items.add(item);

                //购物车编号
                if (cartIds.length() > 0) {
                    cartIds.append(",");
                }
                cartIds.append(cart.getId());

                bcount += cart.getBcount();
                amount += cart.getAmount();
            }
        }

        JSONObject order = new JSONObject();
        Reciaddr reciaddr = mCreateOrderBrief.getReciaddr();
        if (reciaddr != null) {
            order.put("addressId", reciaddr.getId());//收件地址编号,若为空采用业主默认收件地址
            order.put("addrvalId", reciaddr.getAddrvalid());//收件公寓地址编号,若为空采用业主默认绑定楼幢
            order.put("subdisId", reciaddr.getSubdisId());//小区编号
        }
//        order.put("guideHumanid", humanId);//导购员编号
        order.put("humanId", mCreateOrderBrief.getHumanId());//购物业主编号
        order.put("payType", 1);///支付类型:0货到付款，1 预先支付
        order.put("bcount", bcount);//总件数
        //注意：总价，这里使用后台接口返回的实际支付金额，而不是商品总金额
        order.put("amount", mPayAmount.getPayAmount());
        order.put("dueDate", mCreateOrderBrief.getDueDate());//期望送达时间：开始
        order.put("dueDateEnd", mCreateOrderBrief.getDueDateEnd());//期望送达时间：结束
        order.put("subType", 1);//商超
        order.put("needAmount", "true");//是否需要返回订单价格
        order.put("couponIds", couponIds);

        ScOrderApi.saveOrder(order.toJSONString(), items.toJSONString(),
                cartIds.toString(), ruleIds.toString(), saveOrderRC);
    }

    NetCallBack.NetTaskCallBack saveOrderRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                    {"code":"0","msg":"新增成功!","version":"1","data":{"val":"1090753;17.0"}}
                    try {
                        String retStr = null;
                        if (rspData != null) {
                            RspValue<String> retValue = (RspValue<String>) rspData;
                            retStr = retValue.getValue();
                        }
                        if (!StringUtils.isEmpty(retStr)) {
                            String[] retA = retStr.split(";");
                            if (retA.length > 1) {
                                PayOrderBrief payOrderBrief = new PayOrderBrief();
                                payOrderBrief.setOrderIds(retA[0]);
                                payOrderBrief.setAmount(Double.valueOf(retA[1]));
                                payOrderBrief.setBizType(mCreateOrderBrief.getBizType());
                                redirect2PayOrder(payOrderBrief);
                                return;
                            }
                        }

                        btnSubmit.setEnabled(true);
                        PurchaseShopcartService.getInstance().clearFreshGoodsList();
//                    showProgressDialog(ProgressDialog.STATUS_DONE, "预定成功", true);
                    } catch (Exception e) {
                        ZLogger.ef(e.toString());
                    }

                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("下单失败：" + errMsg);
                    btnSubmit.setEnabled(true);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    private void loadInit() {
        if (mCreateOrderBrief == null) {
//            mCreateOrderBrief = new CreateOrderBrief();

            DialogUtil.showHint("下单失败");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
            return;
        }
        ZLogger.d("orderBrief:\n" + JSON.toJSONString(mCreateOrderBrief));

        //收货地址
        refreshRecvAddr();
        //店铺名
        refreshShop();
        //商品列表
        goodsListAdapter.setEntityList(mCreateOrderBrief.getPacks());

        //期望送达时间
        refreshDueDateSpan();
        //备注信息
        refreshRemark();
        //商品金额
        Double goodsAmount = 0D;
        List<CartPack> packs = mCreateOrderBrief.getPacks();
        if (packs != null && packs.size() > 0) {
            for (CartPack pack : packs) {
                Cart cart = pack.getCart();
                goodsAmount += cart.getAmount();

                JSONObject item = new JSONObject();
                item.put("goodsId", cart.getGoodsId());
                item.put("skuId", cart.getProSkuId());
                item.put("bcount", cart.getBcount());
                item.put("price", cart.getPrice());
                item.put("whereId", cart.getShopId());//网点ID,netid,
                productsInfo.add(item);
            }
        }
        mGoodsAmountView.setSubTitle(getString(R.string.mf_format_price_1, goodsAmount));
        //优惠券
        refreshMarketRule(mMarketRuleBrief);
        //配送费
        mTransFeeView.setEndText(getString(R.string.mf_format_price_1, mCreateOrderBrief.getTransFee()));
        //实付金额
//        Spanned amountBrief = toSpanned(getString(R.string.create_order_amount,
//                goodsAmount - mCreateOrderBrief.getTransFee(),
//                0D));
//        tvBrief.setText(amountBrief);
        refreshOrderPayAmout(mPayAmount);

        findMarketRulesByOrderInfo();
    }

    /**
     * 刷新收货地址
     */
    private void refreshRecvAddr() {
        Reciaddr reciaddr = mCreateOrderBrief.getReciaddr();
        if (reciaddr != null) {
            mAddressView.setTitle(String.format("%s/%s",
                    reciaddr.getReceiveName(), reciaddr.getReceivePhone()));
            mAddressView.setSubTitle(reciaddr.getSubName());
        } else {
            mAddressView.setTitle("");
            mAddressView.setSubTitle("");
        }
    }

    /**
     * 刷新店铺信息
     */
    private void refreshShop() {
        CompanyInfo companyInfo = mCreateOrderBrief.getCompanyInfo();
        if (companyInfo != null) {
            tvShopName.setText(companyInfo.getName());
        } else {
            tvShopName.setText("");
        }
    }

    /**
     * 期望送达时间
     */
    private void refreshDueDateSpan() {
        String dueDateSpan = mCreateOrderBrief.getDueDateSpan();
        if (StringUtils.isEmpty(dueDateSpan)) {
            serviceTiemItem.setSubTitle("未选择时间",
                    ContextCompat.getColor(getContext(), R.color.material_pink_400));
        } else {
            serviceTiemItem.setSubTitle(dueDateSpan,
                    ContextCompat.getColor(getContext(), R.color.material_light_blue_400));
        }
    }

    /**
     * 刷新备注信息
     */
    private void refreshRemark() {
        String remark = mCreateOrderBrief.getRemark();
        if (StringUtils.isEmpty(remark)) {
            remarkItem.setSubTitle("无备注信息");
        } else {
            remarkItem.setSubTitle(remark);
        }
    }

    /**
     * 刷新优惠券&规则信息
     */
    private void refreshMarketRule(MarketRuleBrief marketRuleBrief) {
        mMarketRuleBrief = marketRuleBrief;
        if (marketRuleBrief == null) {
            marketRuleView.setSubTitle("无优惠券可用");
        } else {
            List<Long> ruleIds = marketRuleBrief.getRuleIds();//选中
            List<Long> couponIds = marketRuleBrief.getCouponIds();//选中

            marketRuleView.setSubTitle(String.format("已选择%d张优惠券，%d 个营销规则",
                    couponIds != null ? couponIds.size() : 0,
                    ruleIds != null ? ruleIds.size() : 0));
        }
    }

    /**
     * 刷新订单支付信息
     */
    private void refreshOrderPayAmout(PayAmount payAmount) {
        mPayAmount = payAmount;

        Spanned spanned;
        if (payAmount == null) {
            spanned = StringUtils.toSpanned("<font color=#FE5000>[点击刷新]</font>");

            btnSubmit.setEnabled(false);
        } else {
            spanned = StringUtils.toSpanned(getString(R.string.create_order_amount,
                    payAmount.getPayAmount(),
                    payAmount.getCoupAmount() + payAmount.getRuleAmount()));

            btnSubmit.setEnabled(true);
        }
        tvBrief.setText(spanned);

    }

    /**
     * 设置期望送达时间
     */
    private void setDueDate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (mSelectTimeDialog == null) {
            mSelectTimeDialog = new SelectTimeDialog(getContext());
            mSelectTimeDialog.setResponseCallback(new SelectTimeDialog.OnResponseCallback() {
                @Override
                public void onSelectTime(String text) {
                    setDueDate(text);
                    refreshDueDateSpan();
                }
            });
        }

        if (!mSelectTimeDialog.isShowing()) {
            mSelectTimeDialog.show();
        }
    }

    /**
     * 保存期望送达时间
     *
     * @param timeDisplay 显示时间：今天09:00-10:00
     */
    public void setDueDate(String timeDisplay) {
        if (StringUtils.isEmpty(timeDisplay)) {
            mCreateOrderBrief.setDueDateSpan(timeDisplay);
            mCreateOrderBrief.setDueDate(null);
            mCreateOrderBrief.setDueDateEnd(null);
            return;
        }

        try {
            ZLogger.d("timeDisplay:" + timeDisplay);

            String dateStr = timeDisplay.substring(0, 2);
            String timeStr = timeDisplay.substring(2, timeDisplay.length());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);
            SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);//12小时制
            SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);//24小时制

            Calendar calendar = Calendar.getInstance();
            if (dateStr.contains("今天")) {
                dateStr = sdf.format(calendar.getTime());
            } else if (dateStr.contains("明天")) {
                calendar.add(Calendar.DATE, 1);
                dateStr = sdf.format(calendar.getTime());
            } else {
                return;
            }

            String[] timeA = timeStr.split("-");
            if (timeA.length >= 2) {
                String startTime = String.format("%s %s", dateStr, timeA[0]);
                String endTime = String.format("%s %s", dateStr, timeA[1]);


                Date startDate = sdf3.parse(startTime);
                Date endDate = sdf3.parse(endTime);

                mCreateOrderBrief.setDueDate(sdf5.format(startDate));
                mCreateOrderBrief.setDueDateEnd(sdf5.format(endDate));
                mCreateOrderBrief.setDueDateSpan(timeDisplay);
            }
        } catch (Exception ex) {
            ZLogger.e(ex.toString());
        }
    }


    /**
     * 跳转至订单支付页面
     */
    public void redirect2PayOrder(PayOrderBrief payOrderBrief) {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_ORDER_PAY);
        extras.putSerializable(OrderPayFragment.EXTRA_KEY_ORDERBRIEF, payOrderBrief);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ORDER_PAY);
//        startActivity(intent);
    }

    /**
     * 跳转至订单优惠券页面
     */
    @OnClick(R.id.item_coupon)
    public void redirect2PayCoupons() {
        if (mMarketRuleBrief == null || mMarketRuleBrief.getMarketRules() == null) {
            findMarketRulesByOrderInfo();
            return;
        }

        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_ORDER_COUPONS);
        extras.putSerializable(OrderCouponsFragment.EXTRA_KEY_MARKETRULEBRIEF, mMarketRuleBrief);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ORDER_COUPONS);
//        startActivity(intent);
    }


    /**
     * 加载订单可用卡券
     */
    public void findMarketRulesByOrderInfo() {
        JSONObject jsonObject = new JSONObject();
        //默认按会员支付方式查询
        jsonObject.put("payType", WayType.VIP);
        jsonObject.put("humanId", mCreateOrderBrief.getHumanId());
        jsonObject.put("btype", mCreateOrderBrief.getBizType());
        jsonObject.put("discount", 1);
        jsonObject.put("createdDate", TimeUtil.format(new Date(), TimeCursor.FORMAT_YYYYMMDDHHMMSS));
//        jsonObject.put("subdisId", new Date());//会员所属小区
        jsonObject.put("items", productsInfo);

        PmcStockApi.findMarketRulesByOrderInfo(jsonObject.toJSONString(), marketRulesRC);
    }

    NetCallBack.QueryRsCallBack marketRulesRC = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<MarketRules>(new PageInfo(1, 20)) {
        @Override
        public void processQueryResult(RspQueryResult<MarketRules> rs) {
            //此处在主线程中执行。
            MarketRuleBrief marketRuleBrief = new MarketRuleBrief();
            //不考虑订单拆分，取第一个就可以了
            if (rs != null && rs.getReturnNum() > 0) {
                marketRuleBrief.setMarketRules(rs.getRowEntity(0));

            }
            refreshMarketRule(marketRuleBrief);
            hideProgressDialog();

            getPayAmountByOrderInfo();
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
            mMarketRuleBrief.setMarketRules(null);
            mMarketRuleBrief.setRuleIds(null);
            mMarketRuleBrief.setCouponIds(null);//            DialogUtil.showHint("加载卡券失败");
            hideProgressDialog();
        }
    }, MarketRules.class, AppContext.getAppContext());


    @OnClick(R.id.tv_brief)
    public void retryGetPayAmountByOrderInfo(){
        if (mPayAmount != null){
            //订单实际支付金额不为空，忽略
            return;
        }

        getPayAmountByOrderInfo();
    }

    /**
     * 计算会员/优惠券优惠金额
     */
    private void getPayAmountByOrderInfo() {
//        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在计算优惠金额...", true);
//
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            refreshMarketRule(null);
            refreshOrderPayAmout(null);
            return;
        }

        //保存
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<PayAmount,
                NetProcessor.Processor<PayAmount>>(
                new NetProcessor.Processor<PayAmount>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                        java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
//                        {"code":"0","msg":"查询成功!","version":"1","data":{"val":"14.0"}}
//                        {"code":"0","msg":"查询成功!","version":"1","data":[6.0,6.0]}

                        RspBean<PayAmount> retValue = (RspBean<PayAmount>) rspData;
                        refreshOrderPayAmout(retValue.getValue());
                        hideProgressDialog();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.ef(errMsg);
                        refreshMarketRule(null);
                        refreshOrderPayAmout(null);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }
                }
                , PayAmount.class
                , AppContext.getAppContext()) {
        };
//

        String couponIds = mMarketRuleBrief != null ?
                ObjectsCompact.splitLong(mMarketRuleBrief.getCouponIds(), ",") : null;
        String ruleIds = mMarketRuleBrief != null ?
                ObjectsCompact.splitLong(mMarketRuleBrief.getRuleIds(), ",") : null;

        JSONObject jsonstr = new JSONObject();
        jsonstr.put("humanId", mCreateOrderBrief.getHumanId());
        jsonstr.put("btype", mCreateOrderBrief.getBizType());
        jsonstr.put("discount", 1);
        jsonstr.put("items", productsInfo);

        CommonUserAccountApiImpl.getPayAmountByOrderInfo(2, jsonstr.toJSONString(),
                couponIds, ruleIds, responseCallback);
    }

}
