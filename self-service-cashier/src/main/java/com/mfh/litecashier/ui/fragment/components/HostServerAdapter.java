package com.mfh.litecashier.ui.fragment.components;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.wrapper.HostServer;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 选择域名
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class HostServerAdapter
        extends RegularAdapter<HostServer, HostServerAdapter.MenuOptioinViewHolder> {

    public HostServerAdapter(Context context, List<HostServer> entityList) {
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
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_hostserver, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final HostServer entity = entityList.get(position);

        holder.ivHeader.setImageResource(entity.getResId());
        holder.tvName.setText(entity.getName());
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
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
