package com.bingshanguxue.pda.bizz.invrecv;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;

import java.util.List;

/**
 * 签收订单
 * Created by bingshanguxue on 15/8/5.
 */
public class InvRecvConvertAdapter extends SwipAdapter<ProductConvertItemWrapper,
        InvRecvConvertAdapter.ProductViewHolder> {

    public InvRecvConvertAdapter(Context context, List<ProductConvertItemWrapper> entityList) {
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
                .inflate(R.layout.itemview_product_convert, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            ProductConvertItemWrapper entity = entityList.get(position);

            holder.tvName.setEndText(entity.getProSkuName());
            holder.tvBarcode.setEndText(String.format("%.2f%%",
                    MathCompact.mult(entity.getPartRate(), 100D)));
            holder.tvQuantity.setEndText(MUtils.formatDouble(null, null,
                    entity.getQuantityCheck(), "0", "/", ""));
            holder.tvAmount.setEndText(MUtils.formatDouble(null, null,
                    entity.getAmount(), "0", null, null));
            holder.tvPrice.setEndText(MUtils.formatDouble(null, null,
                    entity.getPrice(), "无", " ", ""));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        //        @BindView(R2.id.tv_name)
        private TextLabelView tvName;
        //        @BindView(R2.id.tv_barcode)
        private TextLabelView tvBarcode;
        //        @BindView(R2.id.tv_quantity)
        private TextLabelView tvQuantity;
        //        @BindView(R2.id.tv_price)
        private TextLabelView tvPrice;
        //        @BindView(R2.id.tv_amount)
        private TextLabelView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvName = (TextLabelView) itemView.findViewById(R.id.tv_name);
            tvBarcode = (TextLabelView) itemView.findViewById(R.id.tv_barcode);
            tvQuantity = (TextLabelView) itemView.findViewById(R.id.tv_quantity);
            tvPrice = (TextLabelView) itemView.findViewById(R.id.tv_price);
            tvAmount = (TextLabelView) itemView.findViewById(R.id.tv_amount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return false;
                    }
//                    notifyDataSetChanged();//getAdapterPosition() return -1.
//
                    if (adapterListener != null) {
                        adapterListener.onItemLongClick(itemView, position);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setEntityList(List<ProductConvertItemWrapper> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void removeEntity(int position) {
        ProductConvertItemWrapper entity = getEntity(position);
        if (entity == null) {
            return;
        }

        InvRecvGoodsService.get().deleteById(String.valueOf(entity.getId()));

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
//
//    public void sortByInspectStatusDesc(){
//        if (entityList == null || entityList.size() < 1){
//            return;
//        }
//
//        Collections.sort(entityList, new Comparator<ProductConvertItemWrapper>() {
//            @Override
//            public int compare(ProductConvertItemWrapper order1, ProductConvertItemWrapper order2) {
//                return order2.getInspectStatus() - order1.getInspectStatus();
//            }
//        });
//    }

}
