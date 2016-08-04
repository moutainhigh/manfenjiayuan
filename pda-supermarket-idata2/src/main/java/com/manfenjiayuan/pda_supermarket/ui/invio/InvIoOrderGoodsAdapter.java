package com.manfenjiayuan.pda_supermarket.ui.invio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.database.entity.InvIoGoodsEntity;
import com.bingshanguxue.pda.database.service.InvIoGoodsService;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 出入库
 * Created by bingshanguxue on 15/8/5.
 */
public class InvIoOrderGoodsAdapter extends RegularAdapter<InvIoGoodsEntity,
        InvIoOrderGoodsAdapter.ProductViewHolder> {

    public InvIoOrderGoodsAdapter(Context context, List<InvIoGoodsEntity> entityList) {
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
                .inflate(R.layout.itemview_invreturnorder_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvIoGoodsEntity entity = entityList.get(position);

        holder.tvName.setText(String.format("商品名称：%s", entity.getProductName()));
        holder.tvBarcode.setText(String.format("商品条码：%s", entity.getBarcode()));
        holder.tvPrice.setText(MUtils.formatDouble("价格:", "",
                entity.getPrice(), "无", null, null));
        holder.tvQuantity.setText(MUtils.formatDouble("数量:", "",
                entity.getQuantityCheck(), "无", "/", entity.getUnit()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_price)
        TextView tvPrice;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;

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
////                    notifyDataSetChanged();//getAdapterPosition() return -1.
////
                    if (adapterListener != null) {
                        adapterListener.onItemLongClick(itemView, position);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setEntityList(List<InvIoGoodsEntity> entityList) {
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
//        super.removeEntity(position);

        InvIoGoodsEntity entity = getEntity(position);
        if (entity == null){
            return;
        }

        InvIoGoodsService.get().deleteById(String.valueOf(entity.getId()));

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * 按时间排序
     */
    private void sortByUpdateDate() {
        if (entityList == null || entityList.size() < 1) {
            return;
        }

        Collections.sort(entityList, new Comparator<InvIoGoodsEntity>() {
            @Override
            public int compare(InvIoGoodsEntity order1, InvIoGoodsEntity order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }

}
