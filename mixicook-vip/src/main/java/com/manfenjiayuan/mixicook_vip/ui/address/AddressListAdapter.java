package com.manfenjiayuan.mixicook_vip.ui.address;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 购物车
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class AddressListAdapter
        extends RegularAdapter<Reciaddr, AddressListAdapter.CategoryViewHolder> {

    public AddressListAdapter(Context context, List<Reciaddr> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListsner(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_address_list,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final Reciaddr entity = entityList.get(position);
        holder.tvReceiveName.setText(entity.getReceiveName());
        holder.tvReceivePhone.setText(entity.getReceivePhone());

        StringBuilder sb = new StringBuilder();

        if (entity.getLatitude() == null || entity.getLatitude().equals(0D) ||
                entity.getLongitude() == null || entity.getLongitude().equals(0D)){
            sb.append("<font color=#FE5000>[地址无效]</font>");
        }
//        else{
//            sb.append(String.format("(%f,%f)", entity.getLongitude(), entity.getLatitude()));
//        }
        if (entity.getIsDefault() != null && entity.getIsDefault().equals(1)){
            sb.append("<font color=#FE5000>[默认]</font>");
        }
        sb.append(String.format("<font color=#a6000000>%s</font>", entity.getSubName()));
//        ZLogger.d(sb.toString());
        holder.tvSubName.setText(StringUtils.toSpanned(sb.toString()));

//        if (entity.getIsDefault() != null && entity.getIsDefault().equals(1)){
//            Spanned spanned = StringUtils.toSpanned(String.format("<font color=#FE5000>[默认]</font>" +
//                    "<font color=#a6000000>%s</font>",
//                    entity.getSubName()));
//            holder.tvSubName.setText(spanned);
//        }
//        else{
//            holder.tvSubName.setText(entity.getSubName());
//        }
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });



        }
    }
}
