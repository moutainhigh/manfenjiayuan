package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.model.wrapper.OrderPayInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.uikit.widget.BadgeDrawable;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.fragment.pay.PayHistoryFragment;
import com.mfh.litecashier.utils.CashierHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 异常订单
 * Created by Nat.ZZN on 15/8/5.
 */
public class ExceptionOrderflowAdapter
        extends RegularAdapter<PosOrderEntity, ExceptionOrderflowAdapter.ProductViewHolder> {

    private PosOrderEntity curPosOrder = null;
    private CommonDialog confirmDialog = null;

    public ExceptionOrderflowAdapter(Context context, List<PosOrderEntity> entityList) {
        super(context, entityList);
    }


    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onProcessClick(PosOrderEntity entity);

        void onPrintPreview(PosOrderEntity entity);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.cardview_orderflow_pos_exception, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrderEntity entity = entityList.get(position);
        BadgeDrawable drawableBizType =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(0xff666666)
                        .text1(BizType.name(entity.getBizType()))
                        .build();
        BadgeDrawable drawableOrderStatus =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(0xff666666)
                        .text1("")
                        .build();
        BadgeDrawable drawableSyncStatus =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(0xff666666)
                        .text1("")
                        .build();
        BadgeDrawable drawableActive =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(0xff666666)
                        .text1("")
                        .build();
        BadgeDrawable drawableWaytype =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.material_purple_500))
                        .text1("")
                        .build();

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvId.setText(String.format("订单编号：%d", entity.getId()));
        holder.tvBarcode.setText(String.format("流水编号：%s", entity.getBarCode()));
        holder.tvCreateDate.setText(String.format("下单时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        holder.tvUpdateDate.setText(String.format("下单时间：%s",
                TimeUtil.format(entity.getUpdatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        switch (entity.getStatus()) {
            case PosOrderEntity.ORDER_STATUS_INIT: {
                drawableOrderStatus.setText1(String.format("待确认(%d)", entity.getStatus()));
                drawableOrderStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_HANGUP: {
                drawableOrderStatus.setText1(String.format("挂单(%d)", entity.getStatus()));
                drawableOrderStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.orange));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_STAY_PAY: {
                drawableOrderStatus.setText1(String.format("等待支付(%d)", entity.getStatus()));
                drawableOrderStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.material_cyan_500));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_PROCESS: {
                drawableOrderStatus.setText1(String.format("支付处理中(%d)", entity.getStatus()));
                drawableOrderStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.orangered));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_EXCEPTION: {
                drawableOrderStatus.setText1(String.format("支付异常(%d)", entity.getStatus()));
                drawableOrderStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.red));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_FINISH: {
                drawableOrderStatus.setText1(String.format("支付完成(%d)", entity.getStatus()));
                drawableOrderStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.mfh_colorPrimary));
            }
            break;
            default: {
                drawableOrderStatus.setText1(String.format("未知(%d)", entity.getStatus()));
                drawableOrderStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.material_black));
            }
            break;
        }
        //同步状态
        if (entity.getSyncStatus() == PosOrderEntity.SYNC_STATUS_SYNCED) {
            drawableSyncStatus.setText1(String.format("已同步(%d)", entity.getSyncStatus()));
            drawableSyncStatus.setBadgeColor(ContextCompat.getColor(mContext, R.color.mfh_colorPrimary));
        } else {
            drawableSyncStatus.setText1(String.format("(%d)", entity.getSyncStatus()));
        }
        if (PosOrderEntity.ACTIVE.equals(entity.getIsActive())) {
            drawableActive.setText1(String.format("开启(%d)", entity.getIsActive()));
            drawableActive.setBadgeColor(ContextCompat.getColor(mContext, R.color.mfh_colorPrimary));
        } else {
            drawableActive.setText1(String.format("关闭(%d)", entity.getIsActive()));
        }

        OrderPayInfo payWrapper = OrderPayInfo.deSerialize(entity.getId());
        drawableWaytype.setText1(WayType.name(payWrapper.getPayType()));
        drawableWaytype.setText2(String.valueOf(payWrapper.getPayType()));

        SpannableString badgeBrief = new SpannableString(TextUtils.concat(drawableActive.toSpannable(),
                " ", drawableBizType.toSpannable(),
                " ", drawableOrderStatus.toSpannable(),
                " ", drawableSyncStatus.toSpannable(),
                " ", drawableWaytype.toSpannable()));
        holder.tvBadge.setText(badgeBrief);
        holder.tvOffice.setText(String.format("网点：%d", entity.getSellOffice()));
        holder.tvTenant.setText(String.format("租户：%d", entity.getSellerId()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_badge)
        TextView tvBadge;
        @Bind(R.id.tv_id)
        TextView tvId;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
        @Bind(R.id.tv_updatedate)
        TextView tvUpdateDate;
        @Bind(R.id.tv_office)
        TextView tvOffice;
        @Bind(R.id.tv_tenant)
        TextView tvTenant;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }
                    curPosOrder = entityList.get(position);
                    notifyDataSetChanged();
//                    notifyItemChanged(position);

//                    //加载支付记录
//                    JSONArray payInfoArray = new JSONArray();
//                    List<PosOrderPayEntity> payEntityList = PosOrderPayService.get().queryAllBy(String.format("orderBarCode = '%s'", curPosOrder.getBarCode()));
//                    for (PosOrderPayEntity payEntity : payEntityList){
//                        payInfoArray.add(payEntity);
//                    }
//                    ZLogger.d(String.format("{payInfo:%s}", payInfoArray.toJSONString()));

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();

                    removeEntity(position);

//                    if (adapterListener != null) {
//                        adapterListener.onItemLongClick(itemView, getPosition());
//                    }
                    return false;
                }
            });
        }

        @OnClick(R.id.ib_payhistory)
        public void showPayHistory() {
            final int position = getAdapterPosition();
            PosOrderEntity entity = entityList.get(position);
            if (entity == null) {
                return;
            }

            ZLogger.d(JSONObject.toJSONString(entity));
            Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
            extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FT_PAY_HISTORY);
            extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
            extras.putLong(PayHistoryFragment.EXTRA_KEY_ORDER_ID, entity.getId());
            if (entity.getStatus() == PosOrderEntity.ORDER_STATUS_EXCEPTION){
                extras.putBoolean(PayHistoryFragment.EXTRA_KEY_EDITABLE, true);
            }
            UIHelper.startActivity(mContext, SimpleDialogActivity.class, extras);
        }

        @OnClick(R.id.ib_process)
        public void processOrder() {
            final int position = getAdapterPosition();
            PosOrderEntity entity = entityList.get(position);
            if (entity == null) {
                return;
            }

            if (adapterListener != null) {
                adapterListener.onProcessClick(entity);
            }
        }

        /**
         * 打印预览
         */
        @OnClick(R.id.ib_print_preview)
        public void printPreview() {
            final int position = getAdapterPosition();
            PosOrderEntity entity = entityList.get(position);
            if (entity == null) {
                return;
            }

            if (adapterListener != null) {
                adapterListener.onPrintPreview(entity);
            }
        }
    }

    @Override
    public void removeEntity(final int position) {
        if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
            return;
        }

        final PosOrderEntity entity = entityList.get(position);
        if (entity == null) {
            return;
        }

        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(mContext);
        }

        confirmDialog.setMessage(String.format("<p>订单流水编号：%s\n</p>", entity.getBarCode())
                + "<p>[删除订单]: 删除本地订单流水记录，同时删除该订单对应的商品明细和支付记录。\n</p>");
        confirmDialog.setPositiveButton("删除订单", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                entityList.remove(position);
                notifyItemRemoved(position);

                CashierHelper.deleteCashierOrder(entity);
//                //删除订单同时，需要删除对应订单的商品明细和支付记录
//                PosOrderService.get().deleteById(String.valueOf(entity.getId()));
//                CashierHelper.clearOrderItems(entity.getBarCode());
//                PosOrderPayService.get().deleteBy(String.format("orderBarCode = '%s'", entity.getBarCode()));
            }
        });
        confirmDialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        confirmDialog.show();
    }


    @Override
    public void setEntityList(List<PosOrderEntity> entityList) {
        this.entityList = entityList;
        if (this.entityList != null && this.entityList.size() > 0) {
            this.curPosOrder = this.entityList.get(0);
        } else {
            this.curPosOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void appendEntityList(List<PosOrderEntity> entityList) {
        if (entityList == null) {
            return;
        }

        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        }

        for (PosOrderEntity order : entityList) {
            if (!this.entityList.contains(order)) {
                this.entityList.add(order);
            }
        }

        if (this.curPosOrder == null && this.entityList.size() > 0) {
            this.curPosOrder = this.entityList.get(0);
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public PosOrderEntity getCurPosOrder() {
        return curPosOrder;
    }

}
