package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.ui.dialog.PayHistoryDialog;
import com.mfh.litecashier.utils.CashierHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 线下门店订单流水
 * Created by Nat.ZZN on 15/8/5.
 */
public class SettingsOrderflowAdapter
        extends RegularAdapter<PosOrderEntity, SettingsOrderflowAdapter.ProductViewHolder> {

    private PosOrderEntity curPosOrder = null;
    private CommonDialog confirmDialog = null;
    private PayHistoryDialog payHistoryDialog = null;

    public SettingsOrderflowAdapter(Context context, List<PosOrderEntity> entityList) {
        super(context, entityList);
    }


    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_orderflow_local_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrderEntity entity = entityList.get(position);

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvId.setText(String.format("订单编号：%d", entity.getId()));
        holder.tvCreateDate.setText(String.format("下单时间：%s",
                (entity.getCreatedDate() != null ? TimeCursor.InnerFormat.format(entity.getCreatedDate()) : "")));
        switch (entity.getStatus()) {
            case PosOrderEntity.ORDER_STATUS_INIT: {
                holder.tvOrderStatus.setText(String.format("状态：待确认(%d)", entity.getStatus()));
                holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.lightskyblue));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_HANGUP: {
                holder.tvOrderStatus.setText(String.format("状态：挂单(%d)", entity.getStatus()));
                holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.orange));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_STAY_PAY: {
                holder.tvOrderStatus.setText(String.format("状态：等待支付(%d)", entity.getStatus()));
                holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.limegreen));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_PROCESS: {
                holder.tvOrderStatus.setText(String.format("状态：支付处理中(%d)", entity.getStatus()));
                holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.orangered));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_EXCEPTION: {
                holder.tvOrderStatus.setText(String.format("状态：支付异常(%d)", entity.getStatus()));
                holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.red));
            }
            break;
            case PosOrderEntity.ORDER_STATUS_FINISH: {
                holder.tvOrderStatus.setText(String.format("状态：支付完成(%d)", entity.getStatus()));
                holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.mfh_colorPrimary));
            }
            break;
            default: {
                holder.tvOrderStatus.setText(String.format("状态：(%d)", entity.getStatus()));
            }
            break;
        }
        //同步状态
        if (entity.getSyncStatus() == PosOrderEntity.SYNC_STATUS_SYNCED){
            holder.tvSyncStatus.setText("同步状态：已同步");
        }
        else{
            holder.tvSyncStatus.setText(String.format("同步状态：%d", entity.getSyncStatus()));
        }

        holder.tvPayType.setText(String.format("支付方式：%s(%d)",
                WayType.name(entity.getPayType()), entity.getPayType()));
        holder.tvBizType.setText(String.format("%s(%d)",
                BizType.name(entity.getBizType()), entity.getBizType()));

        holder.tvOffice.setText(String.format("网点：%d", entity.getSellOffice()));
        holder.tvTenant.setText(String.format("租户：%d", entity.getSellerId()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_id)
        TextView tvId;
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
        @Bind(R.id.tv_order_status)
        TextView tvOrderStatus;
        @Bind(R.id.tv_sync_status)
        TextView tvSyncStatus;
        @Bind(R.id.tv_biztype)
        TextView tvBizType;
        @Bind(R.id.tv_pay_type)
        TextView tvPayType;
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

        @OnClick(R.id.button_pay_history)
        public void showPayHistory(){
            final int position = getAdapterPosition();

            PosOrderEntity entity = getEntity(position);
            if (entity == null) {
                return;
            }

            if (payHistoryDialog == null) {
                payHistoryDialog = new PayHistoryDialog(mContext);
            }

            payHistoryDialog.init(entity.getId());
            payHistoryDialog.show();
        }
    }

    @Override
    public void removeEntity(final int position) {

        final PosOrderEntity entity = getEntity(position);
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
