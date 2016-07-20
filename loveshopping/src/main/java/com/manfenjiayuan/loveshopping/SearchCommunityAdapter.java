package com.manfenjiayuan.loveshopping;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.login.entity.Subdis;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by bingshanguxue on 15/6/5.
 */
public class SearchCommunityAdapter extends RegularAdapter<Subdis, SearchCommunityAdapter.CommunityViewHolder> {

    public interface OnAdapterListener {
        void onItemClick(View view, int position);
    }

    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public SearchCommunityAdapter(Context context, List<Subdis> entityList) {
        super(context, entityList);
    }


    @Override
    public CommunityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommunityViewHolder(mLayoutInflater.inflate(R.layout.item_community_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(CommunityViewHolder holder, int position) {
        final Subdis itemData = entityList.get(position);
        holder.tvName.setText(itemData.getSubdisName());
        holder.tvAddress.setText(itemData.getStreet());
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }


    public class CommunityViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_address)
        TextView tvAddress;

        public CommunityViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, getPosition());
                    }
                }
            });
        }
    }

}
