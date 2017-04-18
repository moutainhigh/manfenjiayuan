package com.bingshanguxue.pda.bizz.goods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invSkuStore.InvSkuBizBean;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 商品
 * Created by bingshanguxue on 15/8/5.
 */
public class InvSkuBizAdapter extends RegularAdapter<InvSkuBizBean, InvSkuBizAdapter.ProductViewHolder> {

    public InvSkuBizAdapter(Context context, List<InvSkuBizBean> entityList) {
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
                .inflate(R.layout.cardview_invsku_biz, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            InvSkuBizBean entity = entityList.get(position);

            holder.tvSkuName.setText(entity.getSkuName());
            holder.tvBarcode.setText(entity.getBarcode());
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.tv_companyName)
        TextView tvSkuName;
//        @Bind(R.id.tv_hintPrice)
        TextView tvBarcode;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvSkuName = (TextView) itemView.findViewById(R.id.tv_skuName);
            tvBarcode = (TextView) itemView.findViewById(R.id.tv_barcode);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
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
    public void setEntityList(List<InvSkuBizBean> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }


}
