package com.mfh.litecashier.ui.fragment.goods.query;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 字母
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class QueryGoodsAdapter
        extends RegularAdapter<PosProductEntity, QueryGoodsAdapter.MenuOptioinViewHolder> {

    public QueryGoodsAdapter(Context context, List<PosProductEntity> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuOptioinViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_querygods, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final PosProductEntity entity = entityList.get(position);

        holder.tvName.setText(entity.getName());
        holder.tvCostPrice
                .setText(MUtils.formatDouble(null, null,
                        entity.getCostPrice(), "", "/", entity.getUnit()));
        if (entity.getStatus().equals(0)){
            holder.overlayView.setVisibility(View.VISIBLE);
        }
        else {
            holder.overlayView.setVisibility(View.GONE);
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_costprice)
        TextView tvCostPrice;
        @BindView(R.id.overlay)
        View overlayView;

        public MenuOptioinViewHolder(final View itemView) {
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
                        adapterListener.onItemClick(v, position);
                    }
                }
            });

        }
    }
}
