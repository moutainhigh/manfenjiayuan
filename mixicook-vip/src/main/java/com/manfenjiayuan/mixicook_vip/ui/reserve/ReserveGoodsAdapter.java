package com.manfenjiayuan.mixicook_vip.ui.reserve;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.manfenjiayuan.business.utils.MUtils;
import com.bingshanguxue.vector_uikit.NumberPickerView;
import com.manfenjiayuan.mixicook_vip.MainEvent;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartEntity;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 生鲜预定商品
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class ReserveGoodsAdapter
        extends RegularAdapter<ChainGoodsSku, ReserveGoodsAdapter.CategoryViewHolder> {

    public ReserveGoodsAdapter(Context context, List<ChainGoodsSku> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListsner(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_reserve_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final ChainGoodsSku entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.tvHeader);
        holder.tvName.setText(entity.getSkuName());
        holder.tvPrice.setText(MUtils.formatDouble(null, null,
                entity.getHintPrice(), "", "/", entity.getBuyUnit()));

        PurchaseShopcartEntity purchaseShopcartEntity = PurchaseShopcartService
                .getInstance().getFreshGoods(entity.getTenantId(), entity.getBarcode());
        if (purchaseShopcartEntity != null){
            holder.mNumberPickerView.setValue(String.format("%.0f", purchaseShopcartEntity.getQuantity()));
        }
        else{
            holder.mNumberPickerView.setValue(null);
        }
    }

    @Override
    public void setEntityList(List<ChainGoodsSku> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_header)
        ImageView tvHeader;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.numberPickerView)
        NumberPickerView mNumberPickerView;

        public CategoryViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });

            mNumberPickerView.setonOptionListener(new NumberPickerView.onOptionListener() {

                @Override
                public void onPreIncrease(int value) {

                }

                @Override
                public void onPreDecrease(int value) {

                }

                @Override
                public void onValueChanged(int value) {
                    try{
                        int position = getAdapterPosition();
                        ChainGoodsSku entity = getEntity(position);
                        if (entity == null){
                            return;
                        }
                        if (value == 0){
                            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' and barcode = '%s'",
                                    PurchaseShopcartEntity.PURCHASE_TYPE_FRESH,
                                    entity.getTenantId(), entity.getBarcode());
                            PurchaseShopcartService.getInstance().deleteBy(sqlWhere);
//                            notifyItemRemoved(position);
                        }
                        else {
                            PurchaseShopcartService.getInstance()
                                    .saveOrUpdateFreshGoods(entity,
                                            Double.valueOf(String.valueOf(value)));
//                            notifyItemChanged(position);
                        }
                        EventBus.getDefault().post(new MainEvent(MainEvent.EID_SHOPCART_DATASET_CHANGED));

                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged();
                        }
                    }
                    catch (Exception ex){
                        ZLogger.e("onValueChanged failed, " + ex.toString());
                    }
                }
            });

        }
    }
}
