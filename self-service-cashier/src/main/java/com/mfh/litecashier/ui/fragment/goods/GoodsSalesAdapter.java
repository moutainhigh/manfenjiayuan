package com.mfh.litecashier.ui.fragment.goods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.ProductAggDate;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * 批发商商品
 * Created by bingshanguxue on 15/8/5.
 */
public class GoodsSalesAdapter extends SwipAdapter<ProductAggDate, GoodsSalesAdapter.ProductViewHolder> {

    public GoodsSalesAdapter(Context context, List<ProductAggDate> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;
    private Double maxVal = 0D;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_goods_sales, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            ProductAggDate entity = entityList.get(position);

            holder.tvName.setText(TimeUtil.format(entity.getAggDate(), TimeCursor.FORMAT_YYYYMMDD));
            holder.tvQuantity.setText(MUtils.formatDouble(entity.getProductNum(), "无"));
            holder.progressBar.setMax(Integer.parseInt(String.format("%.0f", maxVal * 100)));
//            if (entity.getProductNum() > 0 && entity.getProductNum() < 1){
//                holder.progressBar.setProgress(1);
//            }
//            else{
                holder.progressBar.setProgress(Integer.parseInt(String.format("%.0f", entity.getProductNum() * 100)));
//            }

            holder.progressBar.animate();
//            ZLogger.d(String.format("max=%f, progress=%f", maxVal, entity.getProductNum()));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            tvName = (TextView) itemView.findViewById(R.id.tv_name);
//            tvQuantity = (TextView) itemView.findViewById(R.id.tv_quantity);
//            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);

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
    public void setEntityList(List<ProductAggDate> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        Double maxVal = 0D;
        if (this.entityList != null && this.entityList.size() > 0){
            for (ProductAggDate entity : entityList){
                maxVal = Math.max(maxVal, entity.getProductNum());
            }
        }
        this.maxVal = maxVal;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
