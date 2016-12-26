package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 退单
 * Created by Nat.ZZN on 15/8/5.
 */
public class ReturnProductAdapter
        extends SwipAdapter<CashierShopcartEntity, ReturnProductAdapter.ProductViewHolder> {

    private DoubleInputDialog changeQuantityDialog;

    public interface OnAdapterListener {
        void onDataSetChanged(boolean needScroll);
    }

    private OnAdapterListener adapterListener;


    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public ReturnProductAdapter(Context context, List<CashierShopcartEntity> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_return_product, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        CashierShopcartEntity entity = entityList.get(position);

        holder.tvName.setText(entity.getSkuName());

        //退货时单个扫描商品需要
        holder.tvPrice.setText(String.format("%.2f", entity.getCostPrice()));
        holder.tvCount.setText(String.format("%.2f", Math.abs(entity.getBcount())));
        holder.tvAmount.setText(String.format("%.2f", Math.abs(entity.getAmount())));
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

        /**
         * 修改数目
         */
        @OnClick(R.id.ll_quantity)
        public void changeQuantity() {
            final int position = getAdapterPosition();

            final CashierShopcartEntity original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new DoubleInputDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("数量", 2, Math.abs(original.getBcount()), new DoubleInputDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    original.setBcount(0-quantity);
                    original.setAmount(original.getBcount() * original.getCostPrice());
                    original.setFinalAmount(original.getBcount() * original.getFinalPrice());
//                    ShopcartService.get().saveOrUpdate(original);

                    CashierShopcartService.getInstance().saveOrUpdate(original);
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
    public void setEntityList(List<CashierShopcartEntity> entityList) {
        super.setEntityList(entityList);
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(true);
        }
    }

    @Override
    public void removeEntity(int position) {
        CashierShopcartEntity entity = getEntity(position);
        if (entity == null){
            return;
        }

        CashierShopcartService.getInstance().deleteById(String.valueOf(entity.getId()));
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

        Collections.sort(entityList, new Comparator<CashierShopcartEntity>() {
            @Override
            public int compare(CashierShopcartEntity order1, CashierShopcartEntity order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }

    public double getBcount() {
        double count = 0;
        if (entityList != null && entityList.size() > 0) {
            for (CashierShopcartEntity entity : entityList) {
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
            for (CashierShopcartEntity entity : entityList) {
                //TODO 会员价
                amount += entity.getFinalAmount();
            }
        }

        return amount;
    }


}
