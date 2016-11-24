package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 查询商品
 * Created by Nat.ZZN on 15/8/5.
 */
public class QueryGoodsAdapter
        extends SwipAdapter<ScGoodsSku, QueryGoodsAdapter.ProductViewHolder> {

    public interface OnAdapterListener {
        void onDataSetChanged(boolean needScroll);
    }

    private OnAdapterListener adapterListener;


    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public QueryGoodsAdapter(Context context, List<ScGoodsSku> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_return_product, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        ScGoodsSku entity = entityList.get(position);

        holder.tvName.setText(entity.getSkuName());

        //退货时单个扫描商品需要
        holder.tvPrice.setText(String.format("%.2f", entity.getCostPrice()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.tv_quantity)
        TextView tvCount;
        @BindView(R.id.tv_amount)
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
    public void setEntityList(List<ScGoodsSku> entityList) {
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

        Collections.sort(entityList, new Comparator<ScGoodsSku>() {
            @Override
            public int compare(ScGoodsSku order1, ScGoodsSku order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }

    public void append(ScGoodsSku goods) {
        if (goods == null) {
            ZLogger.d("商品无效");
            return;
        }

        ScGoodsSku entity = query(goods.getBarcode());
        if (entity == null) {
            entityList.add(goods);
        }

        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(true);
        }
    }

    private ScGoodsSku query(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        if (entityList != null && entityList.size() > 0) {
            for (ScGoodsSku entity : entityList) {
                if (entity.getBarcode().equals(barcode)) {
                    return entity;
                }
            }
        }

        return null;
    }

}
