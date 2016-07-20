package com.mfh.petitestock.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.entity.ShelveEntity;
import com.mfh.petitestock.database.logic.ShelveService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 商品－－绑定货架
 * Created by bingshanguxue on 15/8/5.
 */
public class GoodsShelvesAdapter extends RegularAdapter<ShelveEntity, GoodsShelvesAdapter.ProductViewHolder> {

    public GoodsShelvesAdapter(Context context, List<ShelveEntity> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_bindshelves2goods, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        ShelveEntity entity = entityList.get(position);


//        holder.tvName.setText(String.format("商品名称：%s", entity.get()));
        holder.tvBarcode.setText(String.format("商品条码：%s", entity.getBarcode()));
        holder.tvShelfNumber.setText(String.format("货架编号：%s", entity.getRackNo()));

        holder.tvCreateDate.setText(String.format("绑定时间：%s", TimeUtil.format(entity.getCreatedDate(), TimeCursor.InnerFormat)));
        holder.tvUpdateDate.setText(String.format("更新时间：%s", TimeUtil.format(entity.getUpdatedDate(), TimeCursor.InnerFormat)));

        holder.tvSyncStatus.setText(String.format("[%s]",
                L2CSyncStatus.translate(entity.getSyncStatus())));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_sync_status)
        TextView tvSyncStatus;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_shelfnumber)
        TextView tvShelfNumber;
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
                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }
//                    notifyDataSetChanged();//getAdapterPosition() return -1.
//
                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return false;
                    }
//                    notifyDataSetChanged();//getAdapterPosition() return -1.
//
                    if (adapterListener != null) {
                        adapterListener.onItemLongClick(itemView, position);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setEntityList(List<ShelveEntity> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void removeEntity(int position) {
        ShelveEntity entity = getEntity(position);
        if (entity == null){
            return;
        }

        super.removeEntity(position);
        ShelveService.get().deleteById(String.valueOf(entity.getId()));
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
