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
import com.mfh.litecashier.database.entity.DailysettleEntity;
import com.mfh.litecashier.ui.dialog.PayHistoryDialog;
import com.mfh.litecashier.utils.AnalysisHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 日结
 * Created by Nat.ZZN on 15/8/5.
 */
public class SettingsDailysettleAdapter
        extends RegularAdapter<DailysettleEntity, SettingsDailysettleAdapter.ProductViewHolder> {

    private DailysettleEntity curPosOrder = null;
    private CommonDialog confirmDialog = null;
    private PayHistoryDialog payHistoryDialog = null;

    public SettingsDailysettleAdapter(Context context, List<DailysettleEntity> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_settings_dailysettle, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        DailysettleEntity entity = entityList.get(position);

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvId.setText(String.format("订单编号：%d", entity.getId()));
        holder.tvBarcode.setText(String.format("条码：%s", entity.getBarCode()));

        holder.tvPayStatus.setText(String.format("%s(%d)",
                DailysettleEntity.getPayStatusDesc(entity.getPaystatus()), entity.getPaystatus()));

        if (entity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_YES){
            holder.tvConfirmStatus.setText(String.format("%s(%d)", "已确认", entity.getConfirmStatus()));
        }
        else{
            holder.tvConfirmStatus.setText(String.format("%s(%d)", "未确认", entity.getConfirmStatus()));
        }

        holder.tvOffice.setText(String.format("网点：%d", entity.getOfficeId()));
        holder.tvUpdateDate.setText(String.format("更新日期：%s",
                TimeCursor.InnerFormat.format(entity.getUpdatedDate())));
        holder.tvCreateDate.setText(String.format("创建日期：%s",
                TimeCursor.InnerFormat.format(entity.getCreatedDate())));
    }

    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    @Override
    public void onViewRecycled(ProductViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_id)
        TextView tvId;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_paystatus)
        TextView tvPayStatus;
        @Bind(R.id.tv_confirmStatus)
        TextView tvConfirmStatus;
        @Bind(R.id.tv_office)
        TextView tvOffice;
        @Bind(R.id.tv_createdate)
        TextView tvCreateDate;
        @Bind(R.id.tv_updatedate)
        TextView tvUpdateDate;

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
//                    List<PosOrderPayEntity> payEntityList = PosOrderPayService.get()
//                            .queryAllBy(String.format("orderBarCode = '%s'", curPosOrder.getBarCode()));
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

            DailysettleEntity entity = entityList.get(position);
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
        if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
            return;
        }

        final DailysettleEntity entity = entityList.get(position);
        if (entity == null) {
            return;
        }

        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(mContext);
        }

        confirmDialog.setMessage(String.format("<p>订单编号：%s\n</p>", entity.getBarCode()) + "<p>[删除订单]: 删除本地日结记录，同时删除该日结对应的支付记录。\n</p>");
        confirmDialog.setPositiveButton("删除订单", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                entityList.remove(position);
                notifyItemRemoved(position);

                AnalysisHelper.deleteDailysettle(entity);
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
    public void setEntityList(List<DailysettleEntity> entityList) {
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

    public void appendEntityList(List<DailysettleEntity> entityList) {
        if (entityList == null) {
            return;
        }

        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        }

        for (DailysettleEntity order : entityList) {
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

    public DailysettleEntity getCurPosOrder() {
        return curPosOrder;
    }

}
