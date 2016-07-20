package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.SubdisBean;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by shengkun on 15/6/5.
 */
public class SearchCommunityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<SubdisBean> mList;

    public interface OnAdapterListener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener)
    {
        this.adapterListener = adapterListener;
    }


    public SearchCommunityAdapter(Context context, List<SubdisBean> messageList) {
        mList = messageList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommunityViewHolder(mLayoutInflater.inflate(R.layout.view_item_community, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final SubdisBean itemData = mList.get(position);

        ((CommunityViewHolder)holder).tvName.setText(itemData.getSubdisName());
        ((CommunityViewHolder)holder).tvAddress.setText(itemData.getStreet());
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
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
                    if (adapterListener != null){
                        adapterListener.onItemClick(itemView, getPosition());
                    }
                }
            });
        }
    }

    public List<SubdisBean> getData(){
        return mList;
    }

    public void setData(List<SubdisBean> subdisBeans){
        this.mList = subdisBeans;
        this.notifyDataSetChanged();
    }

}
