package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.litecashier.ui.dialog.ChangeQuantityDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 退单
 * Created by Nat.ZZN on 15/8/5.
 */
public class ReturnProductAdapter
        extends SwipAdapter<PosOrderItemEntity, ReturnProductAdapter.ProductViewHolder> {

    private ChangeQuantityDialog changeQuantityDialog;

    public interface OnAdapterListener {
        void onDataSetChanged(boolean needScroll);
    }

    private OnAdapterListener adapterListener;


    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public ReturnProductAdapter(Context context, List<PosOrderItemEntity> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_return_product, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrderItemEntity entity = entityList.get(position);

        holder.tvName.setText(entity.getName());

        //退货时单个扫描商品需要
        holder.tvPrice.setText(String.format("%.2f", entity.getCostPrice()));
        holder.tvCount.setText(String.format("%.2f", Math.abs(entity.getBcount())));
        holder.tvAmount.setText(String.format("%.2f", Math.abs(entity.getAmount())));
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

        /**
         * 修改数目
         */
        @OnClick(R.id.ll_quantity)
        public void changeQuantity() {
            final int position = getAdapterPosition();

            final PosOrderItemEntity original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new ChangeQuantityDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("数量", 2, Math.abs(original.getBcount()), new ChangeQuantityDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    original.setBcount(0-quantity);
                    original.setAmount(original.getBcount() * original.getCostPrice());
                    original.setFinalAmount(original.getBcount() * original.getFinalPrice());
//                    ShopcartService.get().saveOrUpdate(original);

                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onDataSetChanged(false);
                    }
                }
            });
            changeQuantityDialog.show();
        }
    }

    @Override
    public void setEntityList(List<PosOrderItemEntity> entityList) {
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

        Collections.sort(entityList, new Comparator<PosOrderItemEntity>() {
            @Override
            public int compare(PosOrderItemEntity order1, PosOrderItemEntity order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }

    public void append(PosOrderItemEntity goods) {
        if (goods == null) {
            ZLogger.d("商品无效");
            return;
        }

        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        }

        PosOrderItemEntity entity = query(goods.getBarcode());
        if (entity != null) {
            ZLogger.d("更新商品成功");
            entity.setBcount(entity.getBcount() + goods.getBcount());
            entity.setAmount(entity.getBcount() * entity.getCostPrice());
            entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
            entity.setUpdatedDate(new Date());
        } else {
            ZLogger.d("添加商品成功");
            this.entityList.add(goods);
        }

        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(true);
        }
    }

    private PosOrderItemEntity query(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return null;
        }

        if (entityList != null && entityList.size() > 0) {
            for (PosOrderItemEntity entity : entityList) {
                if (entity.getBarcode().equals(barcode)) {
                    return entity;
                }
            }
        }

        return null;
    }

    public double getBcount() {
        double count = 0;
        if (entityList != null && entityList.size() > 0) {
            for (PosOrderItemEntity entity : entityList) {
                count += entity.getBcount();
            }
        }

        return count;
    }

    /**
     * 获取订单总金额
     */
    public double getFinalAmount() {
        double amount = 0;
        if (entityList != null && entityList.size() > 0) {
            for (PosOrderItemEntity entity : entityList) {
                //TODO 会员价
                amount += entity.getFinalAmount();
            }
        }

        return amount;
    }


}
