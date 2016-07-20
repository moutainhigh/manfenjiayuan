package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.CashierShopcartService;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * <h1>收银商品适</h1>
 * <ul>
 * <li>支持滑动删除</li>
 * </ul>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CashierSwipAdapter
        extends SwipAdapter<CashierShopcartEntity, CashierSwipAdapter.CashierViewHolder> {

    public interface OnAdapterListener {
        void onPriceClicked(int position);

        void onQuantityClicked(int position);

        void onDataSetChanged(boolean needScroll);
    }

    protected OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class CashierViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_finalPrice)
        TextView tvFinalPrice;
        @Bind(R.id.tv_quantity)
        TextView tvCount;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public CashierViewHolder(final View itemView) {
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
         * 修改成交价
         */
        @OnClick(R.id.ll_finalPrice)
        public void changeFinalPrice() {
//            final int position = getAdapterPosition();
////
//            final CashierShopcartEntity original = entityList.get(position);
//            if (original == null) {
//                return;
//            }
            if (adapterListener != null) {
                adapterListener.onPriceClicked(getAdapterPosition());
            }
        }

        /**
         * 修改数目
         */
        @OnClick(R.id.ll_quantity)
        public void changeQuantity() {
//            final int position = ;
////
//            final CashierShopcartEntity original = entityList.get(position);
//            if (original == null) {
//                return;
//            }

            if (adapterListener != null) {
                adapterListener.onQuantityClicked(getAdapterPosition());
            }

        }
    }

    public CashierSwipAdapter(Context context, List<CashierShopcartEntity> entityList) {
        super(context, entityList);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CashierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CashierViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_cashier,
                parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final CashierViewHolder holder, final int position) {
        // - get element from your dataset at this position
        try {
            CashierShopcartEntity entity = getEntity(position);
            if (entity != null){
                // - replace the contents of the view with that element
                holder.tvAmount.setText(String.format("%.2f", entity.getFinalAmount()));
                holder.tvName.setText(entity.getName());
                holder.tvFinalPrice.setText(String.format("%.2f", entity.getFinalPrice()));
                //计件：整数；记重：3位小数
                if (entity.getPriceType() == PriceType.WEIGHT) {
                    holder.tvCount.setText(String.format("%.3f", entity.getBcount()));
                } else {
                    holder.tvCount.setText(String.format("%.2f", entity.getBcount()));
                }
            }

        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }

    }

    @Override
    public void setEntityList(List<CashierShopcartEntity> entityList) {
        super.setEntityList(entityList);

        sortByUpdateDate();
        notifyDataSetChanged(true);
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
                amount += entity.getFinalAmount();
            }
        }

        return amount;
    }

    /**
     * 直接修改数量
     */
    public void changeQuantity(Double value) {
        final CashierShopcartEntity original = getEntity(0);
        if (original == null) {
            return;
        }

        original.setBcount(value);
        original.setAmount(original.getBcount() * original.getCostPrice());
        original.setFinalAmount(original.getBcount() * original.getFinalPrice());
        CashierShopcartService.getInstance().saveOrUpdate(original);
        notifyDataSetChanged();

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

//        notifyDataSetChanged();
//        if (adapterListener != null) {
//            adapterListener.onDataSetChanged();
//        }
    }

    /**
     * 添加商品
     * */
    public void append(String orderBarCode, PosProductEntity goods, Double bCount){
        //添加商品
        CashierShopcartService.getInstance().append(orderBarCode, goods, bCount);

        //刷新订单列表
        List<CashierShopcartEntity> shopcartEntities = CashierShopcartService.getInstance()
                .queryAllBy(String.format("posTradeNo = '%s'", orderBarCode));
        setEntityList(shopcartEntities);
    }

    public void notifyDataSetChanged(int position, boolean needScroll){
        notifyItemChanged(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(needScroll);
        }
    }

    public void notifyDataSetChanged(boolean needScroll){
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(needScroll);
        }
    }


    /**
     * 检查是否有未设置价格或零价格商品
     */
    public boolean haveEmptyPrice() {
        if (entityList != null && entityList.size() > 0) {
            for (CashierShopcartEntity entity : entityList) {
                if (entity.getFinalPrice() == null || entity.getFinalPrice() < 0.01) {
                    return true;
                }
            }
        }

        return false;
    }

}
