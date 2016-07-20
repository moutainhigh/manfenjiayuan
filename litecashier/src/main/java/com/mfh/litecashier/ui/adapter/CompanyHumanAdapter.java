package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.database.entity.CompanyHumanEntity;


import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 帐号管理
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CompanyHumanAdapter
        extends RegularAdapter<CompanyHumanEntity, CompanyHumanAdapter.MenuOptioinViewHolder> {

    public CompanyHumanAdapter(Context context, List<CompanyHumanEntity> entityList) {
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
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_companyhuman, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final CompanyHumanEntity entity = entityList.get(position);

        Glide.with(mContext).load(entity.getHeaderUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getName());
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_username)
        TextView tvName;

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
