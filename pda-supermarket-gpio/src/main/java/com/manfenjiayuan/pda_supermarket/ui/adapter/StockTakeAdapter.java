package com.manfenjiayuan.pda_supermarket.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.StockTakeEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.StockTakeService;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 库存盘点纪录商品
 * Created by bingshanguxue on 15/8/5.
 */
public class StockTakeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_PRODUCT = 1;

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<StockTakeEntity> entityList;

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

    public StockTakeAdapter(Context context, List<StockTakeEntity> entityList) {
        this.entityList = entityList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_PRODUCT) {
            return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_stock_take, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE_PRODUCT) {
            StockTakeEntity entity = entityList.get(position);

            ((ProductViewHolder) holder).tvName.setText(entity.getName());
            ((ProductViewHolder) holder).tvBarcode.setText(String.format("商品条码：%s", entity.getBarcode()));
            ((ProductViewHolder) holder).tvQuantity.setText(String.format("盘点数：%.2f", entity.getQuantityCheck()));
            ((ProductViewHolder) holder).tvShelfNumber.setText(String.format("区域编号：%d", entity.getShelfNumber()));
            ((ProductViewHolder) holder).tvCreateDate.setText(String.format("盘点时间：%s", TimeCursor.InnerFormat.format(entity.getCreatedDate())));

            ((ProductViewHolder) holder).tvSyncStatus.setText(String.format("[%s]",
                    L2CSyncStatus.translate(entity.getSyncStatus())));

            //盘点方式
            if (entity.getUpdateHint() == StockTakeEntity.HINT_MERGER) {
                ((ProductViewHolder) holder).tvUpdateHint.setText("[合并]");
            } else if (entity.getUpdateHint() == StockTakeEntity.HINT_OVERRIDE) {
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
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_sync_status)
        TextView tvSyncStatus;
        @Bind(R.id.tv_update_hint)
        TextView tvUpdateHint;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_shelfnumber)
        TextView tvShelfNumber;
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;

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

    public void setEntityList(List<StockTakeEntity> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<StockTakeEntity> getEntityList() {
        return entityList;
    }

    public void removeAll() {
        if (entityList != null && entityList.size() > 0) {
            for (StockTakeEntity entity : entityList) {
                StockTakeService.get().deleteBy(String.format("barcode = '%s'", entity.getBarcode()));
            }
        }

        setEntityList(null);
    }

    /**
     * 盘库成功后修改商品信息
     *
     * @param barcodes 冲突商品条码，多个条码以逗号','隔开
     *                 format like "111,222,333"
     */
    public void commitFinished(String barcodes) {
        String[] codes = org.apache.commons.lang3.StringUtils.splitByWholeSeparator(barcodes, ",");

        int length = codes.length;


        if (entityList != null && entityList.size() > 0) {
            for (StockTakeEntity entity : entityList) {
                if (length > 1) {
                    if (barcodes.startsWith(entity.getBarcode() + ",")
                            || barcodes.contains("," + entity.getBarcode() + ",")
                            || barcodes.endsWith("," + entity.getBarcode())) {
                        entity.setStatus(StockTakeEntity.STATUS_CONFLICT);
                        StockTakeService.get().saveOrUpdate(entity);
                    } else {
                        entity.setStatus(StockTakeEntity.STATUS_FINISHED);
                        StockTakeService.get().saveOrUpdate(entity);
                    }
                } else {
                    if (barcodes.equals(entity.getBarcode())) {
                        entity.setStatus(StockTakeEntity.STATUS_CONFLICT);
                        StockTakeService.get().saveOrUpdate(entity);
                    } else {
                        entity.setStatus(StockTakeEntity.STATUS_FINISHED);
                        StockTakeService.get().saveOrUpdate(entity);
                    }
                }
            }
        }

        if (adapterListener != null) {
            adapterListener.onConflictSolved();
        }
    }


}
