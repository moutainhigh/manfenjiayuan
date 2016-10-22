package com.bingshanguxue.almigod.pos;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.almigod.R;
import com.mfh.framework.api.posRegister.PosRegister;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 选择批发商
 * Created by bingshanguxue on 15/8/5.
 */
public class PosRegisterAdapter
        extends RegularAdapter<PosRegister, PosRegisterAdapter.ProductViewHolder> {


    public PosRegisterAdapter(Context context, List<PosRegister> entityList) {
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
                .inflate(R.layout.itemview_posregister, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosRegister entity = entityList.get(position);

//        if (selectedEntity != null && selectedEntity.getId().compareTo(entity.getId()) == 0) {
//            holder.rootView.setSelected(true);
//        } else {
//            holder.rootView.setSelected(false);
//        }
        holder.tvId.setText(String.format("设备编号: %d", entity.getId()));
        holder.tvChannelId.setText(String.format("渠道编号: %d", entity.getChannelId()));
        holder.tvChannelPointId.setText(String.format("端点号:%s", entity.getChannelPointId()));
        holder.tvSerialNo.setText(String.format("SerialNo:%s", entity.getSerialNo()));
        holder.tvNetId.setText(String.format("网点编号:%d", entity.getNetId()));
        holder.tvCreateDate.setText(String.format("创建时间: %s",
                TimeUtil.format(entity.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        holder.tvUpdateDate.setText(String.format("更新时间: %s",
                TimeUtil.format(entity.getUpdatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_id)
        TextView tvId;
        @Bind(R.id.tv_channelId)
        TextView tvChannelId;
        @Bind(R.id.tv_channelPointId)
        TextView tvChannelPointId;
        @Bind(R.id.tv_serialNo)
        TextView tvSerialNo;
        @Bind(R.id.tv_netId)
        TextView tvNetId;
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
        @Bind(R.id.tv_updateDate)
        TextView tvUpdateDate;

        public ProductViewHolder(final View itemView) {
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
//                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<PosRegister> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
