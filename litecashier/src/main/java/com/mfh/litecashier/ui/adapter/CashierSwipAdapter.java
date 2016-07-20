package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;
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
 * <h1>收银商品适配器</h1>
 * <ul>
 * <li>支持滑动删除</li>
 * </ul>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CashierSwipAdapter
        extends SwipAdapter<PosOrderItemEntity, CashierSwipAdapter.CashierViewHolder> {

    public interface OnAdapterListener {
        void onDataSetChanged(boolean needScroll);
    }
    protected OnAdapterListener adapterListener;
    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    private ChangeQuantityDialog changeQuantityDialog = null;

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

//        ShopcartEntity orderItemEntity;

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
            final int position = getAdapterPosition();

            //删除数据库
            final PosOrderItemEntity original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new ChangeQuantityDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("成交价", 2, original.getFinalPrice(), new ChangeQuantityDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {

                    original.setFinalPrice(quantity);
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
            changeQuantityDialog.init("数量", 2, original.getBcount(), new ChangeQuantityDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    original.setBcount(quantity);
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

    public CashierSwipAdapter(Context context, List<PosOrderItemEntity> entityList) {
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
        PosOrderItemEntity entity = entityList.get(position);

        // - replace the contents of the view with that element
        holder.tvName.setText(entity.getName());
        holder.tvFinalPrice.setText(String.format("%.2f", entity.getFinalPrice()));
        //计件：整数；记重：3位小数
        if (entity.getPriceType() == PriceType.WEIGHT) {
            holder.tvCount.setText(String.format("%.3f", entity.getBcount()));
        } else {
            holder.tvCount.setText(String.format("%.2f", entity.getBcount()));
        }
        holder.tvAmount.setText(String.format("%.2f", entity.getFinalAmount()));
    }

    @Override
    public void setEntityList(List<PosOrderItemEntity> entityList) {
        super.setEntityList(entityList);

        sortByUpdateDate();
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

    /**
     * 显示修改数量窗口
     */
    public void changeQuantity(Context context) {
        if (getItemCount() <= 0) {
            return;
        }

        final int position = 0;
        final PosOrderItemEntity original = entityList.get(position);
        if (original == null) {
            return;
        }

        if (changeQuantityDialog == null) {
            changeQuantityDialog = new ChangeQuantityDialog(mContext);
            changeQuantityDialog.setCancelable(true);
            changeQuantityDialog.setCanceledOnTouchOutside(true);
        }
        //TODO
        changeQuantityDialog.init("数量", 3, original.getBcount(), new ChangeQuantityDialog.OnResponseCallback() {
            @Override
            public void onQuantityChanged(Double quantity) {
                original.setBcount(quantity);
                original.setAmount(original.getBcount() * original.getCostPrice());
                original.setFinalAmount(original.getBcount() * original.getFinalPrice());
                notifyDataSetChanged();

                if (adapterListener != null) {
                    adapterListener.onDataSetChanged(false);
                }
            }
        });
        changeQuantityDialog.show();
    }

    /**
     * 直接修改数量
     */
    public void changeQuantity(Double value) {
        if (getItemCount() <= 0) {
            return;
        }

        final int position = 0;
        final PosOrderItemEntity original = entityList.get(position);
        if (original == null) {
            return;
        }

        original.setBcount(value);
        original.setAmount(original.getBcount() * original.getCostPrice());
        original.setFinalAmount(original.getBcount() * original.getFinalPrice());
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

        Collections.sort(entityList, new Comparator<PosOrderItemEntity>() {
            @Override
            public int compare(PosOrderItemEntity order1, PosOrderItemEntity order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });

//        notifyDataSetChanged();
//        if (adapterListener != null) {
//            adapterListener.onDataSetChanged();
//        }
    }

    public void append(PosOrderItemEntity goods) {
        if (goods == null) {
            return;
        }

        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        }

        PosOrderItemEntity entity = query(goods.getBarcode());
        if (entity != null) {
            entity.setBcount(entity.getBcount() + goods.getBcount());
            entity.setAmount(entity.getBcount() * entity.getCostPrice());
            entity.setFinalAmount(entity.getBcount() * entity.getFinalPrice());
            entity.setUpdatedDate(new Date());
        } else {
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
}
