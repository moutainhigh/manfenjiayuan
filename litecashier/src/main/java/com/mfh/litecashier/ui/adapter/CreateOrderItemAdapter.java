package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.manfenjiayuan.business.bean.ChainGoodsSku;
import com.manfenjiayuan.business.bean.InvSendIoOrderItem;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.manfenjiayuan.business.bean.wrapper.CreateOrderItemWrapper;
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
 *  新增采购收货单/退货单/调拨单明细
 * Created by bingshanguxue on 15/8/5.
 */
public class CreateOrderItemAdapter
        extends SwipAdapter<CreateOrderItemWrapper, CreateOrderItemAdapter.ProductViewHolder> {

    private ChangeQuantityDialog changeQuantityDialog;

    private boolean isPriceEnabled;//是否可以修改
    private boolean isQuantityEnabled;

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public CreateOrderItemAdapter(Context context, List<CreateOrderItemWrapper> entityList) {
        super(context, entityList);
    }

    public CreateOrderItemAdapter(Context context, List<CreateOrderItemWrapper> entityList,
                                  boolean isPriceEnabled, boolean isQuantityEnabled) {
        super(context, entityList);
        this.isPriceEnabled = isPriceEnabled;
        this.isQuantityEnabled = isQuantityEnabled;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_createorder_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        CreateOrderItemWrapper entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        if (isPriceEnabled){
            Drawable drawable = mContext.getResources().getDrawable(R.mipmap.ic_marker_edit);
            holder.tvBuyprice.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
//            holder.tvQuantity.setCompoundDrawables(null, null, drawable, null);
//            holder.tvQuantity.setCompoundDrawablesRelative(null, null, drawable, null);
        }
        else{
//            holder.tvQuantity.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//            holder.tvQuantity.setCompoundDrawables(null, null, null, null);
            holder.tvBuyprice.setCompoundDrawablesRelative(null, null, null, null);
        }

        if (isQuantityEnabled){
            Drawable drawable = mContext.getResources().getDrawable(R.mipmap.ic_marker_edit);
            holder.tvQuantity.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
        else{
            holder.tvQuantity.setCompoundDrawablesRelative(null, null, null, null);
        }

        if (StringUtils.isEmpty(entity.getUnitSpec())){
            holder.tvBuyprice.setText(MUtils.formatDouble(entity.getPrice(), "无"));
        }
        else{
            holder.tvBuyprice.setText(MUtils.formatDouble(null, null, entity.getPrice(), "无", "/", entity.getUnitSpec()));
        }
        holder.tvQuantity.setText(MUtils.formatDouble(entity.getQuantityCheck(), "无"));
        holder.tvAmount.setText(MUtils.formatDouble(entity.getAmount(), "无"));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_buyprice)
        TextView tvBuyprice;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }

        /**
         * 修改采购价
         */
        @OnClick(R.id.ll_buyprice)
        public void updateBuyPrice() {
            if (!isPriceEnabled){
                return;
            }
            final int position = getAdapterPosition();

            final CreateOrderItemWrapper original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new ChangeQuantityDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("采购价", 2, original.getPrice(), new ChangeQuantityDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    original.setPrice(quantity);
                    original.setAmount(original.getPrice() * original.getQuantityCheck());

                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onDataSetChanged();
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

            final CreateOrderItemWrapper original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new ChangeQuantityDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("修改数量", 2, original.getQuantityCheck(), new ChangeQuantityDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    original.setQuantityCheck(quantity);
                    original.setAmount(original.getPrice() * original.getQuantityCheck());

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
    public void setEntityList(List<CreateOrderItemWrapper> entityList) {
        super.setEntityList(entityList);
//        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
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
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * 设置采购订单明细<br>
     * 适用场景：新建采购收货单
     * */
    public void setSendOrderItems(List<InvSendOrderItem> entityList) {
        if (this.entityList == null){
            this.entityList = new ArrayList<>();
        }
        else{
            this.entityList.clear();
        }
        if (entityList != null && entityList.size() > 0){
            for (InvSendOrderItem entity : entityList){
                this.entityList.add(CreateOrderItemWrapper.fromInvSendOrderItem(entity));
            }
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * 采购收货订单明细
     * */
    public void setSendIoOrderItems(List<InvSendIoOrderItem> entityList) {
        if (this.entityList == null){
            this.entityList = new ArrayList<>();
        }
        else{
            this.entityList.clear();
        }
        if (entityList != null && entityList.size() > 0){
            for (InvSendIoOrderItem entity : entityList){
                this.entityList.add(CreateOrderItemWrapper.fromInvSendIoOrderItem(entity));
            }
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * 添加批发商商品
     * */
    public boolean appendSupplyGoods(ChainGoodsSku goods){
        if (goods == null) {
            ZLogger.d("参数无效");
            return false;
        }

        if (entityList == null){
            entityList = new ArrayList<>();
        }

        CreateOrderItemWrapper entity = query(goods.getId());
        if (entity != null) {
            entity.setQuantityCheck(entity.getQuantityCheck() + 1D);
            if (entity.getPrice() == null){
                entity.setAmount(0D);
            }
            else{
                entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            }
            entity.setUpdatedDate(new Date());
        } else {
            entity = CreateOrderItemWrapper.fromSupplyGoods(goods, 1D);

            this.entityList.add(entity);
        }

        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }

        return true;
    }

    /**
     * 添加库存商品
     * */
    public void appendStockGoods(ScGoodsSku goods){
        if (goods == null) {
            return;
        }

        if (entityList == null){
            entityList = new ArrayList<>();
        }

        CreateOrderItemWrapper entity = query(goods.getTenantSkuId());
        if (entity != null) {
            entity.setQuantityCheck(entity.getQuantityCheck() + 1D);
            entity.setAmount(entity.getQuantityCheck() * entity.getPrice());
            entity.setUpdatedDate(new Date());
        } else {
            entity = CreateOrderItemWrapper.fromStockGoods(goods);
            this.entityList.add(entity);
        }

        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    private CreateOrderItemWrapper query(Long chainSkuId) {
        if (chainSkuId == null) {
            return null;
        }

        if (entityList != null && entityList.size() > 0) {
            for (CreateOrderItemWrapper entity : entityList) {
                if (entity.getChainSkuId().equals(chainSkuId)) {
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

        Collections.sort(entityList, new Comparator<CreateOrderItemWrapper>() {
            @Override
            public int compare(CreateOrderItemWrapper order1, CreateOrderItemWrapper order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }
}
