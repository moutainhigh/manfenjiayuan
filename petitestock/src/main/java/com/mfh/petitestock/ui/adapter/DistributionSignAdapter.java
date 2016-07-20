package com.mfh.petitestock.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.entity.DistributionSignEntity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 签收订单
 * Created by bingshanguxue on 15/8/5.
 */
public class DistributionSignAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_PRODUCT = 1;

    private final LayoutInflater mLayoutInflater;
    private List<DistributionSignEntity> entityList;


    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public DistributionSignAdapter(Context context, List<DistributionSignEntity> entityList) {
        this.entityList = entityList;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_PRODUCT) {
            return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_commodity_distributioin_good, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE_PRODUCT) {
            DistributionSignEntity entity = entityList.get(position);

            ((ProductViewHolder) holder).tvName.setText(entity.getProductName());
            ((ProductViewHolder) holder).tvBarcode.setText(entity.getBarcode());
            if (entity.getInspectStatus() == DistributionSignEntity.INSPECT_STATUS_OK){
                ((ProductViewHolder) holder).ivMarker.setImageResource(R.mipmap.ic_marker_ok);

                ((ProductViewHolder) holder).tvQuantity.setText(MUtils.formatDouble(null, null, entity.getTotalCount(), "无", null, entity.getUnitSpec()));
            }
            else if (entity.getInspectStatus() == DistributionSignEntity.INSPECT_STATUS_CONFLICT){
                ((ProductViewHolder) holder).ivMarker.setImageResource(R.mipmap.ic_marker_warn);

                //冲突
                ((ProductViewHolder) holder).tvQuantity
                        .setText(Html.fromHtml(String.format("<font color=#000000>%s / </font><font color=#FE0000>%s %s</font>",
                                MUtils.formatDouble(entity.getTotalCount(), "0"),
                                MUtils.formatDouble(entity.getQuantityCheck(), "0"), entity.getUnitSpec())));
            }
            else if (entity.getInspectStatus() == DistributionSignEntity.INSPECT_STATUS_REJECT){
                ((ProductViewHolder) holder).ivMarker.setImageResource(R.mipmap.ic_marker_error);
                ((ProductViewHolder) holder).tvQuantity.setText(MUtils.formatDouble(null, null, entity.getTotalCount(), "无", null, entity.getUnitSpec()));
            }
            else {
                ((ProductViewHolder) holder).ivMarker.setImageResource(R.mipmap.ic_marker_uncheck);
                ((ProductViewHolder) holder).tvQuantity.setText(MUtils.formatDouble(null, null, entity.getTotalCount(), "无", null, entity.getUnitSpec()));
            }

            ((ProductViewHolder) holder).tvTotalFee.setText(MUtils.formatDouble(entity.getAmount(), "无"));
        }
    }

    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    //根据这个类型判断去创建不同item的ViewHolder
    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE_PRODUCT;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
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
        @Bind(R.id.tv_totalFee)
        TextView tvTotalFee;

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

    public void setEntityList(List<DistributionSignEntity> entityList) {
        this.entityList = entityList;
//        sortByInspectStatusDesc();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<DistributionSignEntity> getEntityList() {
        return entityList;
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
}
