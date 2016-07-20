package com.manfenjiayuan.mixicook_vip.ui.shopcart;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_user.UserApiImpl;
import com.manfenjiayuan.business.bean.NetInfo;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.DateTimePickerDialog;
import com.manfenjiayuan.mixicook_vip.InputTextFragment;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartEntity;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartService;
import com.manfenjiayuan.mixicook_vip.utils.ACacheHelper;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.CompanyApi;
import com.mfh.framework.api.impl.InvSendOrderApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class ConfirmOrderFragment extends BaseFragment {


    @Bind(R.id.tv_net_name)
    TextView tvNetName;
    @Bind(R.id.tv_net_phonenumber)
    TextView tvNetPhonenumber;
    @Bind(R.id.tv_net_address)
    TextView tvNetAddress;

    @Bind(R.id.item_serviceTime)
    SettingsItem serviceTiemItem;
    @Bind(R.id.item_remark)
    SettingsItem remarkItem;

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private OrderGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.tv_brief)
    TextView tvBrief;
    @Bind(R.id.button_confirm)
    Button btnConfirm;

    private OrderFormInfo mOrderFormInfo;
    private DateTimePickerDialog dateTimePickerDialog = null;

    private Long TENANT_ID_MIXICOOK = 135799L;//135266L;//

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_confirm_order;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        initGoodsRecyclerView();
        remarkItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString(SimpleActivity.EXTRA_TITLE, "备注");
                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                extras.putInt(SimpleActivity.EXTRA_KEY_FRAGMENT_TYPE, SimpleActivity.FT_INPUT_TEXT);
                extras.putString(InputTextFragment.EXTRA_HINT_TEXT, "请输入备注信息");
                extras.putString(InputTextFragment.EXTRA_RAW_TEXT, mOrderFormInfo.getRemark());
                Intent intent = new Intent(getActivity(), SimpleActivity.class);
                intent.putExtras(extras);
                startActivityForResult(intent, 0);
            }
        });
        serviceTiemItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeServiceTime();
            }
        });
        mOrderFormInfo = new OrderFormInfo();
        refresh(true);
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
            case 0: {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String remark = data.getStringExtra(InputTextFragment.EXTRA_RESULT);
                    if (mOrderFormInfo != null){
                        mOrderFormInfo.setRemark(remark);
                    }
                    refresh(false);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.button_confirm)
    public void submitOrder() {
        showConfirmDialog("是否确认预定？",
                "预定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doSubmitStuff();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    private void refresh(boolean isAutoReload) {
        if (mOrderFormInfo == null) {
            mOrderFormInfo = new OrderFormInfo();
        }
        String netInfoCache = ACacheHelper.getAsString(ACacheHelper.CK_DEFAULT_NET);
        if (!StringUtils.isEmpty(netInfoCache)) {
            mOrderFormInfo.setNetInfo(JSONObject.toJavaObject(JSONObject.parseObject(netInfoCache),
                    NetInfo.class));
        }
        mOrderFormInfo.setShopcartEntities(PurchaseShopcartService.getInstance()
                .fetchFreshEntites());

        NetInfo netInfo = mOrderFormInfo.getNetInfo();
        if (netInfo != null) {
            tvNetName.setText(netInfo.getContact());
            tvNetPhonenumber.setText(netInfo.getMobilenumber());
            tvNetAddress.setText(String.format("%s(%s)", netInfo.getMobilenumber(), netInfo.getAddr()));
        } else {
            tvNetName.setText("");
            tvNetPhonenumber.setText("");
            tvNetAddress.setText("");
        }

        String remark = mOrderFormInfo.getRemark();
        if (StringUtils.isEmpty(remark)) {
            remarkItem.setSubTitle("无备注信息");
        } else {
            remarkItem.setSubTitle(remark);
        }
        serviceTiemItem.setSubTitle(TimeUtil.format(mOrderFormInfo.getServiceTime(),
                TimeCursor.FORMAT_YYYYMMDDHHMM));
        goodsListAdapter.setEntityList(mOrderFormInfo.getShopcartEntities());

        if (isAutoReload) {
            loadAddress();
        }
    }

    private void changeServiceTime() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (dateTimePickerDialog == null) {
            dateTimePickerDialog = new DateTimePickerDialog(getActivity());
            dateTimePickerDialog.setCancelable(true);
            dateTimePickerDialog.setCanceledOnTouchOutside(true);
        }
        dateTimePickerDialog.init(calendar, new DateTimePickerDialog.OnDateTimeSetListener() {
            @Override
            public void onDateTimeSet(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                if (mOrderFormInfo != null) {
                    mOrderFormInfo.setServiceTime(calendar.getTime());
                }
                refresh(false);
            }
        });
        if (!dateTimePickerDialog.isShowing()) {
            dateTimePickerDialog.show();
        }
    }

    private void doSubmitStuff() {
        btnConfirm.setEnabled(false);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        NetInfo netInfo = mOrderFormInfo.getNetInfo();
        if (netInfo == null || netInfo.getId() == null) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, "收货地址不能为空", true);
            btnConfirm.setEnabled(true);
            return;
        }

        JSONArray items = new JSONArray();
        List<PurchaseShopcartEntity> entities = goodsListAdapter.getEntityList();
        if (entities != null && entities.size() > 0) {
            for (PurchaseShopcartEntity entity : entities) {
                JSONObject item = new JSONObject();
                item.put("chainSkuId", entity.getChainSkuId());
                item.put("proSkuId", entity.getProSkuId());
                item.put("barcode", entity.getBarcode());
                item.put("productName", entity.getName());
                item.put("totalCount", entity.getQuantity());
                item.put("price", entity.getPrice());
                item.put("amount", entity.getPrice() * entity.getQuantity());

                items.add(item);
            }
        }

        InvSendOrderApiImpl.createPlanOrder(TENANT_ID_MIXICOOK,
                TimeUtil.format(mOrderFormInfo.getServiceTime(),
                        TimeCursor.FORMAT_YYYYMMDDHHMMSS), mOrderFormInfo.getRemark(),
                netInfo.getId(), items, submitRC);
    }

    NetCallBack.NetTaskCallBack submitRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    btnConfirm.setEnabled(true);
                    PurchaseShopcartService.getInstance().clearFreshGoodsList();
//                    showProgressDialog(ProgressDialog.STATUS_DONE, "预定成功", true);

//                    refresh(false);
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("取消订单失败：" + errMsg);
                    btnConfirm.setEnabled(true);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    private void loadAddress() {
        NetCallBack.NetTaskCallBack submitRC = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        RspValue<String> retValue = (RspValue<String>) rspData;

                        String companyId = retValue.getValue();
                        if (!StringUtils.isEmpty(companyId)) {
                            loadAddress2(Long.valueOf(companyId));
                        }
//                        btnConfirm.setEnabled(true);
//                        PurchaseShopcartService.getInstance().clearFreshGoodsList();
//                        showProgressDialog(ProgressDialog.STATUS_DONE, "预定成功", true);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        ZLogger.d("取消订单失败：" + errMsg);
//                        btnConfirm.setEnabled(true);
//                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }
                }
                , String.class
                , MfhApplication.getAppContext()) {
        };

        UserApiImpl.getMyPramValue("defaultNet", submitRC);
    }

    private void loadAddress2(Long companyId) {
        NetCallBack.NetTaskCallBack submitRC = new NetCallBack.NetTaskCallBack<NetInfo,
                NetProcessor.Processor<NetInfo>>(
                new NetProcessor.Processor<NetInfo>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        RspBean<NetInfo> retValue = (RspBean<NetInfo>) rspData;
                        ACacheHelper.put(ACacheHelper.CK_DEFAULT_NET,
                                JSONObject.toJSONString(retValue.getValue()));
                        refresh(false);
//                        btnConfirm.setEnabled(true);
//                        PurchaseShopcartService.getInstance().clearFreshGoodsList();
//                        showProgressDialog(ProgressDialog.STATUS_DONE, "预定成功", true);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
//                        ZLogger.d("取消订单失败：" + errMsg);
//                        btnConfirm.setEnabled(true);
//                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    }
                }
                , NetInfo.class
                , MfhApplication.getAppContext()) {
        };

        CompanyApi.getById(companyId, submitRC);
    }


}
