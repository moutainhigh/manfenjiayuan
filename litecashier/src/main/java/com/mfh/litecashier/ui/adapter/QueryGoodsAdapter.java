package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.StockTakeGoods;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 查询商品
 * Created by Nat.ZZN on 15/8/5.
 */
public class QueryGoodsAdapter
        extends SwipAdapter<StockTakeGoods, QueryGoodsAdapter.ProductViewHolder> {

    public interface OnAdapterListener {
        void onDataSetChanged(boolean needScroll);
    }

    private OnAdapterListener adapterListener;


    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public QueryGoodsAdapter(Context context, List<StockTakeGoods> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_return_product, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        StockTakeGoods entity = entityList.get(position);

        holder.tvName.setText(entity.getSkuName());

        //退货时单个扫描商品需要
        holder.tvPrice.setText(String.format("%.2f", entity.getCostPrice()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_price)
        TextView tvPrice;
        @Bind(R.id.tv_quantity)
        TextView tvCount;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }
    }

    @Override
    public void setEntityList(List<StockTakeGoods> entityList) {
        super.setEntityList(entityList);
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(true);
        }
    }

    @Override
    public void removeEntity(int position) {
        if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
            return;
        }

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged(false);
        }
    }

    /**
     * 按时间排序
     */
    private void sortByUpdateDate() {
        if (entityList == null || entityList.size() < 1) {
            return;
        }

        Collections.sort(entityList, new Comparator<StockTakeGoods>() {
            @Override
            public int compare(StockTakeGoods order1, StockTakeGoods order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }

    public void append(StockTakeGoods goods) {
        if (goods == null) {
            ZLogger.d("商品无效");
            return;
        }

        StockTakeGoods entity = query(goods.getBarcode());
        if (entity == null) {
            entityList.add(goods);
        }

        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(true);
        }
    }

    private StockTakeGoods query(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        if (entityList != null && entityList.size() > 0) {
            for (StockTakeGoods entity : entityList) {
                if (entity.getBarcode().equals(barcode)) {
                    return entity;
                }
            }
        }

        return null;
    }

}
