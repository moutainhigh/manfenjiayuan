package com.bingshanguxue.pda.bizz.company;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 选择批发商
 * Created by bingshanguxue on 15/8/5.
 */
public class InvCompanyAdapter
        extends RegularAdapter<CompanyInfo, InvCompanyAdapter.ProductViewHolder> {


    public InvCompanyAdapter(Context context, List<CompanyInfo> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_inv_company, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        CompanyInfo entity = entityList.get(position);

//        if (selectedEntity != null && selectedEntity.getId().compareTo(entity.getId()) == 0) {
//            holder.rootView.setSelected(true);
//        } else {
//            holder.rootView.setSelected(false);
//        }
        holder.tvProviderName.setText(entity.getName());
        //TODO,动态添加标签
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        //        @Bind(R.id.rootview)
//        View rootView;
//        @Bind(R.id.tv_provider_name)
        TextView tvProviderName;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvProviderName = (TextView) itemView.findViewById(R.id.tv_provider_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }
//                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<CompanyInfo> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
