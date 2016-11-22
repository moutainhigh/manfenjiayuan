package com.bingshanguxue.pda.bizz.invcheck;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.entity.InvCheckGoodsEntity;
import com.bingshanguxue.pda.database.service.InvCheckGoodsService;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.List;

/**
 * 库存盘点纪录商品
 * Created by bingshanguxue on 15/8/5.
 */
public class InvCheckHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_PRODUCT = 1;

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<InvCheckGoodsEntity> entityList;

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();

        void onConflictSolved();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public InvCheckHistoryAdapter(Context context, List<InvCheckGoodsEntity> entityList) {
        this.entityList = entityList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_PRODUCT) {
            return new ProductViewHolder(mLayoutInflater
                    .inflate(R.layout.itemview_invcheck_history, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE_PRODUCT) {
            InvCheckGoodsEntity entity = entityList.get(position);

            ((ProductViewHolder) holder).tvName.setEndText(entity.getName());
            ((ProductViewHolder) holder).tvBarcode.setEndText(entity.getBarcode());
            ((ProductViewHolder) holder).tvQuantity.setText(String.format("盘点数：%.2f",
                    entity.getQuantityCheck()));
            ((ProductViewHolder) holder).tvShelfNumber.setEndText(String.valueOf(entity.getShelfNumber()));
            ((ProductViewHolder) holder).tvCreateDate.setEndText(TimeCursor.InnerFormat.format(entity.getCreatedDate()));

            ((ProductViewHolder) holder).tvSyncStatus.setText(String.format("[%s]",
                    L2CSyncStatus.translate(entity.getSyncStatus())));

            //盘点方式
            if (entity.getUpdateHint() == InvCheckGoodsEntity.HINT_MERGER) {
                ((ProductViewHolder) holder).tvUpdateHint.setText("[合并]");
            } else if (entity.getUpdateHint() == InvCheckGoodsEntity.HINT_OVERRIDE) {
                ((ProductViewHolder) holder).tvUpdateHint.setText("[覆盖]");
            } else {
                ((ProductViewHolder) holder).tvUpdateHint.setText("[冲突]");
            }
        }
    }

    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    //根据这个类型判断去创建不同item的ViewHolder
    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE_PRODUCT;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.tv_name)
TextLabelView tvName;
//        @Bind(R.id.tv_sync_status)
        TextView tvSyncStatus;
//        @Bind(R.id.tv_update_hint)
        TextView tvUpdateHint;
//        @Bind(R.id.tv_barcode)
TextLabelView tvBarcode;
//        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
//        @Bind(R.id.tv_shelfnumber)
TextLabelView tvShelfNumber;
//        @Bind(R.id.tv_createDate)
TextLabelView tvCreateDate;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvName = (TextLabelView) itemView.findViewById(R.id.tv_name);
            tvSyncStatus = (TextView) itemView.findViewById(R.id.tv_sync_status);
            tvUpdateHint = (TextView) itemView.findViewById(R.id.tv_update_hint);
            tvBarcode = (TextLabelView) itemView.findViewById(R.id.tv_barcode);
            tvQuantity = (TextView) itemView.findViewById(R.id.tv_quantity);
            tvShelfNumber = (TextLabelView) itemView.findViewById(R.id.tv_shelfnumber);
            tvCreateDate = (TextLabelView) itemView.findViewById(R.id.tv_createDate);

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

    public void setEntityList(List<InvCheckGoodsEntity> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<InvCheckGoodsEntity> getEntityList() {
        return entityList;
    }

    public void removeAll() {
        if (entityList != null && entityList.size() > 0) {
            for (InvCheckGoodsEntity entity : entityList) {
                InvCheckGoodsService.get().deleteBy(String.format("barcode = '%s'", entity.getBarcode()));
            }
        }

        setEntityList(null);
    }

}
