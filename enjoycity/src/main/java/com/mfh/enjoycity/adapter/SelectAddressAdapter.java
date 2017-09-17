package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.utils.Constants;

import java.util.List;

import butterknife.Bind;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class SelectAddressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<ReceiveAddressEntity> mList;

    private ReceiveAddressEntity currentEntity;

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


    public SelectAddressAdapter(Context context, List<ReceiveAddressEntity> messageList) {
        mList = messageList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

//        String currentId = SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ)
//                .getString(Constants.PREF_KEY_LOGIN_ADDR_ID, null);
//        currentEntity = ReceiveAddressService.get().getEntityById(currentId);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddressViewHolder(mLayoutInflater.inflate(R.layout.listitem_address, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ReceiveAddressEntity entity = mList.get(position);

        ((AddressViewHolder)holder).tvName.setText(entity.getReceiver());
        ((AddressViewHolder)holder).tvTel.setText(entity.getTelephone());
        ((AddressViewHolder)holder).tvAddr.setText(String.format("送至: %s", entity.getSubName()));
//        ((AddressViewHolder)holder).ivMarker.setVisibility(View.VISIBLE);
        if (currentEntity != null && currentEntity.getId().equals(entity.getId())){
            ((AddressViewHolder)holder).ivMarker.setVisibility(View.VISIBLE);
        }else{
            ((AddressViewHolder)holder).ivMarker.setVisibility(View.INVISIBLE);
        }
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

        @BindView(R.id.iv_marker)
        ImageView ivMarker;
        @BindView(R.id.tv_name) TextView tvName;
        @BindView(R.id.tv_telephone) TextView tvTel;
        @BindView(R.id.tv_address) TextView tvAddr;
//        @BindView(R.id.iv_arrow) ImageView ivArrow;

        public AddressViewHolder(final View itemView) {
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

    public List<ReceiveAddressEntity> getData(){
        return mList;
    }

    public void setData(List<ReceiveAddressEntity> subdisBeans){
        this.mList = subdisBeans;
        this.notifyDataSetChanged();
    }

}
