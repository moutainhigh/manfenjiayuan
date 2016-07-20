package com.manfenjiayuan.pda_supermarket.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 签收订单
 * Created by bingshanguxue on 15/8/5.
 */
public class DistributionSignAdapter extends SwipAdapter<DistributionSignEntity, DistributionSignAdapter.ProductViewHolder> {

    public DistributionSignAdapter(Context context, List<DistributionSignEntity> entityList) {
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
                .inflate(R.layout.itemview_content_commodity_distributioin_good, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            DistributionSignEntity entity = entityList.get(position);

            holder.tvName.setText(String.format("商品名称：%s", entity.getProductName()));
            holder.tvBarcode.setText(String.format("商品条码：%s", entity.getBarcode()));
            holder.tvQuantity.setText(MUtils.formatDouble("签收数量:", "",
                    entity.getReceiveQuantity(), "无", "/", entity.getUnitSpec()));
            holder.tvAmount.setText(MUtils.formatDouble("实付金额:", "",
                    entity.getReceiveAmount(), "无", null, null));
            holder.tvPrice.setText(MUtils.formatDouble("收货价格:", "",
                    entity.getReceivePrice(), "无", "/", entity.getUnitSpec()));

            if (entity.getInspectStatus() == DistributionSignEntity.INSPECT_STATUS_OK) {
                holder.ivMarker.setImageResource(R.mipmap.ic_marker_ok);
                holder.tvQuantity.setTextColor(Color.parseColor("#2E7D32"));
            } else if (entity.getInspectStatus() == DistributionSignEntity.INSPECT_STATUS_CONFLICT) {
                holder.ivMarker.setImageResource(R.mipmap.ic_marker_warn);
                //冲突
                holder.tvQuantity.setTextColor(Color.parseColor("#FFC107"));
            } else if (entity.getInspectStatus() == DistributionSignEntity.INSPECT_STATUS_REJECT) {
                holder.ivMarker.setImageResource(R.mipmap.ic_marker_error);
                holder.tvQuantity.setTextColor(Color.parseColor("#F44336"));
            } else {
                holder.ivMarker.setImageResource(R.mipmap.ic_marker_uncheck);
                holder.tvQuantity.setTextColor(Color.parseColor("#000000"));
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_marker)
        ImageView ivMarker;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_price)
        TextView tvPrice;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return false;
                    }
//                    notifyDataSetChanged();//getAdapterPosition() return -1.
//
                    if (adapterListener != null) {
                        adapterListener.onItemLongClick(itemView, position);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setEntityList(List<DistributionSignEntity> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void removeEntity(int position) {
        DistributionSignEntity entity = getEntity(position);
        if (entity == null){
            return;
        }

        DistributionSignService.get().deleteById(String.valueOf(entity.getId()));

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void sortByInspectStatusDesc(){
        if (entityList == null || entityList.size() < 1){
            return;
        }

        Collections.sort(entityList, new Comparator<DistributionSignEntity>() {
            @Override
            public int compare(DistributionSignEntity order1, DistributionSignEntity order2) {
                return order2.getInspectStatus() - order1.getInspectStatus();
            }
        });
    }
    /**
     * 按时间排序
     * */
    private void sortByUpdateDate() {
        if (entityList == null || entityList.size() < 1){
            return;
        }

        Collections.sort(entityList, new Comparator<DistributionSignEntity>() {
            @Override
            public int compare(DistributionSignEntity order1, DistributionSignEntity order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }
}
