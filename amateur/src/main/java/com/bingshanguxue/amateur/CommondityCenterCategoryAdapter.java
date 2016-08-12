package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 商品中心－－类目
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CommondityCenterCategoryAdapter
        extends RegularAdapter<CashierFunctional, CommondityCenterCategoryAdapter.MenuOptioinViewHolder> {

    public CommondityCenterCategoryAdapter(Context context, List<CashierFunctional> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_commoditycenter_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final CashierFunctional entity = entityList.get(position);
//            holder.ivHeader.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 156), DensityUtil.dip2px(mContext, 156)));
        holder.ivHeader.setImageResource(entity.getResId());
    }


    @Override
    public void setEntityList(List<CashierFunctional> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);

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
    }

}
