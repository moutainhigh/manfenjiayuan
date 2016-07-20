package com.manfenjiayuan.pda_supermarket.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.login.entity.Office;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class OfficeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<Office> mList;

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public OfficeAdapter(Context context, List<Office> messageList) {
        mList = messageList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddressViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_office, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Office entity = mList.get(position);

        ((AddressViewHolder) holder).tvName.setText(String.format("%d--%s", entity.getCode(), entity.getValue()));
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    //根据这个类型判断去创建不同item的ViewHolder
    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_name)
        TextView tvName;

        public AddressViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= mList.size()) {
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public List<Office> getData() {
        return mList;
    }

    public void setData(List<Office> subdisBeans) {
        this.mList = subdisBeans;
        this.notifyDataSetChanged();
    }

}
