package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.LossOrderItem;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 报损
 * Created by Nat.ZZN on 15/8/5.
 */
public class DefectiveAdapter
        extends SwipAdapter<LossOrderItem, DefectiveAdapter.ProductViewHolder> {

    private DoubleInputDialog changeQuantityDialog;

    public DefectiveAdapter(Context context, List<LossOrderItem> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_invlossorder, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        LossOrderItem entity = entityList.get(position);

        holder.tvName.setText(entity.getName());
        holder.tvCount.setText(String.format("%.2f", entity.getQuantity()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_quantity)
        TextView tvCount;

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

        /**
         * 修改数目
         */
        @OnClick(R.id.ll_quantity)
        public void changeQuantity() {
            final int position = getAdapterPosition();

            final LossOrderItem original = entityList.get(position);
            if (original == null) {
//                ZLogger.d("data is null");
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new DoubleInputDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("数量", 2, Math.abs(original.getQuantity()), new DoubleInputDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    original.setQuantity(quantity);

                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onDataSetChanged();
                    }
                }
            });
            changeQuantityDialog.show();
        }
    }

    @Override
    public void setEntityList(List<LossOrderItem> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    //TODO,重复的项需要累加
    public void addEntity(ScGoodsSku goods) {
        if (goods == null) {
            return;
        }
        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        }

        LossOrderItem entity = query(goods.getBarcode());
        if (entity != null) {
            entity.setQuantity(entity.getQuantity()+1);
            entity.setUpdatedDate(new Date());
        } else {
            entity = new LossOrderItem();
            entity.setId(goods.getId());
            entity.setProSkuId(goods.getProSkuId());
            entity.setBarcode(goods.getBarcode());
            entity.setName(goods.getSkuName());
            entity.setQuantity(1D);
            entity.setCreatedDate(new Date());
            entity.setUpdatedDate(new Date());

            this.entityList.add(entity);
        }
        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    private LossOrderItem query(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        if (entityList != null && entityList.size() > 0) {
            for (LossOrderItem entity : entityList) {
                if (entity.getBarcode().equals(barcode)) {
                    return entity;
                }
            }
        }

        return null;
    }


    /**
     * 按时间排序
     * */
    private void sortByUpdateDate() {
        if (entityList == null || entityList.size() < 1){
            return;
        }

        Collections.sort(entityList, new Comparator<LossOrderItem>() {
            @Override
            public int compare(LossOrderItem order1, LossOrderItem order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }
}
