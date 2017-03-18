package com.manfenjiayuan.business.hostserver;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.business.R;
import com.mfh.framework.api.tenant.TenantInfo;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 选择域名
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class HostServerAdapter
        extends RegularAdapter<TenantInfo, HostServerAdapter.MenuOptioinViewHolder> {

    public HostServerAdapter(Context context, List<TenantInfo> entityList) {
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
        return new MenuOptioinViewHolder(mLayoutInflater.
                inflate(R.layout.itemview_hostserver, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final TenantInfo entity = entityList.get(position);

        holder.ivHeader.setImageResource(TenantInfoWrapper.getImageResource(entity.getSaasId()));
        holder.tvName.setText(entity.getSaasName());
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.iv_header)
        ImageView ivHeader;
        TextView tvName;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);

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
