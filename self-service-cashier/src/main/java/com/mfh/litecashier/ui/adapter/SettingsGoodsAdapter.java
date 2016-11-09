package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.uikit.widget.BadgeDrawable;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * POS商品库
 * Created by bingshanguxue on 15/8/5.
 */
public class SettingsGoodsAdapter
        extends RegularAdapter<PosProductEntity, SettingsGoodsAdapter.ProductViewHolder> {

    private PosProductEntity curPosOrder = null;
    private CommonDialog confirmDialog = null;

    public SettingsGoodsAdapter(Context context, List<PosProductEntity> entityList) {
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
                .inflate(R.layout.itemview_settings_goods_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosProductEntity entity = entityList.get(position);

//        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
//            holder.rootView.setSelected(true);
//        } else {
//            holder.rootView.setSelected(false);
//        }
        BadgeDrawable drawablePriceType =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue))
                        .text1(PriceType.name(entity.getPriceType()))
                        .build();
        BadgeDrawable drawableProLine =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue))
                        .text2(String.valueOf(entity.getProdLineId()))
                        .text1("产品线")
                        .build();
        BadgeDrawable drawableCateType =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue))
                        .text2(String.valueOf(entity.getCateType()))
                        .text1(CateApi.backendCatetypeName(entity.getCateType()))
                        .build();

        BadgeDrawable drawableStatus =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue))
                        .text2(String.valueOf(entity.getStatus()))
                        .build();
        if (entity.getStatus() != null && entity.getStatus().equals(1)) {
            drawableStatus.setText1("出售中");
        } else {
            drawableStatus.setText1("售罄");
        }

        SpannableString badgeBrief = new SpannableString(TextUtils.concat(drawablePriceType.toSpannable(),
                "  " , drawableCateType.toSpannable(),
                "  " , drawableProLine.toSpannable(),
                "  " , drawableStatus.toSpannable()));
        holder.tvBadge.setText(badgeBrief);

        holder.tvId.setText(String.format("编号：%d", entity.getId()));
        holder.tvName.setText(String.format("商品：(%s)/%s", entity.getBarcode(), entity.getName()));
        holder.tvSpuId.setText(String.format("SPU编号：%d", entity.getProductId()));
        holder.tvSkuId.setText(String.format("SKU编号：%d", entity.getProSkuId()));
        holder.tvCostPrice.setText(MUtils.formatDouble("零售价：", "",
                entity.getCostPrice(), "", "/", entity.getUnit()));
        holder.tvPackageNum.setText(String.format("箱规：%.2f", entity.getPackageNum()));
        holder.tvTenantId.setText(String.format("租户编号：%d", entity.getTenantId()));
        holder.tvStockQuantity.setText(String.format("库存：%.2f", entity.getQuantity()));
        holder.tvProviderId.setText(String.format("批发商编号：%d", entity.getProviderId()));
        holder.tvProcateId.setText(String.format("类目编号：%d", entity.getProcateId()));

        holder.tvCreateDate.setText(String.format("创建时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeCursor.InnerFormat)));
        holder.tvUpdateDate.setText(String.format("更新时间：%s",
                TimeUtil.format(entity.getUpdatedDate(), TimeCursor.InnerFormat)));

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        //        @Bind(R.id.rootview)
//        View rootView;
        @Bind(R.id.tv_badge)
        TextView tvBadge;
        @Bind(R.id.tv_id)
        TextView tvId;
        @Bind(R.id.tv_spu_id)
        TextView tvSpuId;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_sku_id)
        TextView tvSkuId;
        @Bind(R.id.tv_costprice)
        TextView tvCostPrice;
        @Bind(R.id.tv_packageNum)
        TextView tvPackageNum;
        @Bind(R.id.tv_tenant_id)
        TextView tvTenantId;
        @Bind(R.id.tv_stock_quantity)
        TextView tvStockQuantity;
        @Bind(R.id.tv_provider_id)
        TextView tvProviderId;
        @Bind(R.id.tv_procateId)
        TextView tvProcateId;
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
        @Bind(R.id.tv_updatedate)
        TextView tvUpdateDate;

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
                    curPosOrder = entityList.get(position);
                    notifyDataSetChanged();
//                    notifyItemChanged(position);

//                    //加载支付记录
//                    JSONArray payInfoArray = new JSONArray();
//                    List<PosOrderPayEntity> payEntityList = PosOrderPayService.get().queryAllBy(String.format("orderBarCode = '%s'", curPosOrder.getBarCode()));
//                    for (PosOrderPayEntity payEntity : payEntityList){
//                        payInfoArray.add(payEntity);
//                    }
//                    ZLogger.d(String.format("{payInfo:%s}", payInfoArray.toJSONString()));

                    ZLogger.d(String.format("barcode : [%s]", curPosOrder.getBarcode()));
                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();

                    removeEntity(position);

//                    if (adapterListener != null) {
//                        adapterListener.onItemLongClick(itemView, getPosition());
//                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void removeEntity(final int position) {
        if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
            return;
        }

        final PosProductEntity entity = entityList.get(position);
        if (entity == null) {
            return;
        }

        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(mContext);
        }

        confirmDialog.setMessage(String.format("<p>商品条码：%s\n</p>",
                entity.getBarcode()) + "<p>[删除商品]: 删除后会可能会影响收银。\n</p>");
        confirmDialog.setPositiveButton("删除商品", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                entityList.remove(position);
                notifyItemRemoved(position);

                PosProductService.get().deleteById(String.valueOf(entity.getId()));
            }
        });
        confirmDialog.setNegativeButton("点错了", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        confirmDialog.show();
    }

    @Override
    public void setEntityList(List<PosProductEntity> entityList) {
        this.entityList = entityList;
        if (this.entityList != null && this.entityList.size() > 0) {
            this.curPosOrder = this.entityList.get(0);
        } else {
            this.curPosOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void appendEntityList(List<PosProductEntity> entityList) {
        if (entityList == null) {
            return;
        }

        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        }

        for (PosProductEntity order : entityList) {
            if (!this.entityList.contains(order)) {
                this.entityList.add(order);
            }
        }

        if (this.curPosOrder == null && this.entityList.size() > 0) {
            this.curPosOrder = this.entityList.get(0);
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public PosProductEntity getCurPosOrder() {
        return curPosOrder;
    }

}
