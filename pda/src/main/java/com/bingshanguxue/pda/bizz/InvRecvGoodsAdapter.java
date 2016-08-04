package com.bingshanguxue.pda.bizz;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.entity.InvRecvGoodsEntity;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 签收订单
 * Created by bingshanguxue on 15/8/5.
 */
public class InvRecvGoodsAdapter extends SwipAdapter<InvRecvGoodsEntity,
        InvRecvGoodsAdapter.ProductViewHolder> {

    public InvRecvGoodsAdapter(Context context, List<InvRecvGoodsEntity> entityList) {
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
                .inflate(R.layout.itemview_inv_recv_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            InvRecvGoodsEntity entity = entityList.get(position);

            holder.tvName.setText(String.format("商品名称：%s", entity.getProductName()));
            holder.tvBarcode.setText(String.format("商品条码：%s", entity.getBarcode()));
            holder.tvQuantity.setText(MUtils.formatDouble("签收数量:", "",
                    entity.getReceiveQuantity(), "无", "/", entity.getUnitSpec()));
            holder.tvAmount.setText(MUtils.formatDouble("实付金额:", "",
                    entity.getReceiveAmount(), "无", null, null));
            holder.tvPrice.setText(MUtils.formatDouble("收货价格:", "",
                    entity.getReceivePrice(), "无", "/", entity.getUnitSpec()));

            if (entity.getInspectStatus() == InvRecvGoodsEntity.INSPECT_STATUS_OK) {
                holder.tvQuantity.setTextColor(Color.parseColor("#2E7D32"));
            } else if (entity.getInspectStatus() == InvRecvGoodsEntity.INSPECT_STATUS_CONFLICT) {
                //冲突
                holder.tvQuantity.setTextColor(Color.parseColor("#FFC107"));
            } else if (entity.getInspectStatus() == InvRecvGoodsEntity.INSPECT_STATUS_REJECT) {
                holder.tvQuantity.setTextColor(Color.parseColor("#F44336"));
            } else {
                holder.tvQuantity.setTextColor(Color.parseColor("#000000"));
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
//        @BindView(R2.id.tv_name)
        private TextView tvName;
//        @BindView(R2.id.tv_barcode)
private TextView tvBarcode;
//        @BindView(R2.id.tv_quantity)
private TextView tvQuantity;
//        @BindView(R2.id.tv_price)
private TextView tvPrice;
//        @BindView(R2.id.tv_amount)
private TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvName = (TextView)itemView.findViewById(R.id.tv_name);
            tvBarcode = (TextView)itemView.findViewById(R.id.tv_barcode);
            tvQuantity = (TextView)itemView.findViewById(R.id.tv_quantity);
            tvPrice = (TextView)itemView.findViewById(R.id.tv_price);
            tvAmount = (TextView)itemView.findViewById(R.id.tv_amount);

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
    public void setEntityList(List<InvRecvGoodsEntity> entityList) {
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
        InvRecvGoodsEntity entity = getEntity(position);
        if (entity == null){
            return;
        }

        InvRecvGoodsService.get().deleteById(String.valueOf(entity.getId()));

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

        Collections.sort(entityList, new Comparator<InvRecvGoodsEntity>() {
            @Override
            public int compare(InvRecvGoodsEntity order1, InvRecvGoodsEntity order2) {
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

        Collections.sort(entityList, new Comparator<InvRecvGoodsEntity>() {
            @Override
            public int compare(InvRecvGoodsEntity order1, InvRecvGoodsEntity order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }
}
