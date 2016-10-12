package com.manfenjiayuan.mixicook_vip.ui.address;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 收货地址
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class AddressMgrAdapter
        extends RegularAdapter<Reciaddr, AddressMgrAdapter.CategoryViewHolder> {

    public AddressMgrAdapter(Context context, List<Reciaddr> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onClickSetDefault(Long id);
        void onClickUpdate(Reciaddr reciaddr);
        void onClickDel(Long id);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListsner(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_address_mgr,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final Reciaddr entity = entityList.get(position);
        holder.tvReceiveName.setText(entity.getReceiveName());
        holder.tvReceivePhone.setText(entity.getReceivePhone());
        if (entity.getIsDefault() != null && entity.getIsDefault().equals(1)){
            holder.tvSubName.setText(StringUtils.toSpanned(String.format("<font color=#FE5000>[默认]</font><font color=#a6000000>%s</font>",
                    entity.getSubName())));
        }
        else{
            holder.tvSubName.setText(entity.getSubName());
        }

    }

    @Override
    public void setEntityList(List<Reciaddr> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_receiveName)
        TextView tvReceiveName;
        @Bind(R.id.tv_receivePhone)
        TextView tvReceivePhone;
        @Bind(R.id.tv_subName)
        TextView tvSubName;

        public CategoryViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if (position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
//
//                    notifyDataSetChanged();
//
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, position);
//                    }
//                }
//            });
        }

        @OnClick(R.id.button_setdefault)
        public void setDefault(){
            int position = getAdapterPosition();
            Reciaddr reciaddr = getEntity(position);
            if (reciaddr != null && adapterListener != null){
                adapterListener.onClickSetDefault(reciaddr.getId());
            }
        }

        @OnClick(R.id.button_update)
        public void update(){
            int position = getAdapterPosition();
            Reciaddr reciaddr = getEntity(position);
            if (reciaddr != null && adapterListener != null){
                adapterListener.onClickUpdate(reciaddr);
            }
        }

        @OnClick(R.id.button_delete)
        public void delete(){
            int position = getAdapterPosition();
            Reciaddr reciaddr = getEntity(position);
            if (reciaddr != null && adapterListener != null){
                adapterListener.onClickDel(reciaddr.getId());
            }
        }
    }
}
