package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.bean.StockInItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 快递代收
 * Created by bingshanguxue on 15/8/5.
 */
public class StockInAdapter extends SwipAdapter<StockInItem, StockInAdapter.ProductViewHolder> {
    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public StockInAdapter(Context context, List<StockInItem> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_stockin_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        StockInItem entity = entityList.get(position);

        holder.tvOrderNumber.setText(String.valueOf(entity.getFdorderNumber()));
        holder.tvPhone.setText(entity.getMobile());
        holder.tvName.setText(entity.getName());
        holder.btnStockRatio.setSelected(entity.getNeedmsg().equals(1));
        if (entity.getBindwx().equals(1)) {
            holder.tvWxBindSttus.setText("已绑定");
            holder.btnSmsRatio.setEnabled(false);
        } else {
            holder.tvWxBindSttus.setText("未绑定");
            holder.btnSmsRatio.setEnabled(true);
            holder.btnSmsRatio.setSelected(entity.getMustsms().equals(0));
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_orderNumber)
        TextView tvOrderNumber;
        @Bind(R.id.tv_phone)
        TextView tvPhone;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_wxBindStatus)
        TextView tvWxBindSttus;
        @Bind(R.id.button_stock_ratio)
        ImageButton btnStockRatio;
        @Bind(R.id.button_sms_ratio)
        ImageButton btnSmsRatio;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }

        @OnClick(R.id.button_stock_ratio)
        public void toggleStock() {
            try {
                int position = getAdapterPosition();
                if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                    return;
                }

                StockInItem item = entityList.get(position);
                if (item.getNeedmsg().equals(1)) {
                    item.setNeedmsg(0);
                } else {
                    item.setNeedmsg(1);
                }
//                //刷新列表
//                entityList.remove(position);
                notifyItemChanged(position);
//
                if (adapterListener != null) {
                    adapterListener.onDataSetChanged();
                }
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }

        @OnClick(R.id.button_sms_ratio)
        public void toggleSms() {
            try {
                int position = getAdapterPosition();
                if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                    return;
                }

                StockInItem item = entityList.get(position);
                if (item.getMustsms().equals(1)) {
                    item.setMustsms(0);
                } else {
                    item.setMustsms(1);
                }
//                //刷新列表
//                entityList.remove(position);
                notifyItemChanged(position);
//
                if (adapterListener != null) {
                    adapterListener.onDataSetChanged();
                }
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }
    }

    @Override
    public void removeEntity(int position) {
        if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
            return;
        }

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void setEntityList(List<StockInItem> entityList) {
        super.setEntityList(entityList);
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }


    public void appendEntity(StockInItem item) {
        if (item == null) {
            return;
        }

        if (entityList == null) {
            entityList = new ArrayList<>();
        }

        if (isAlreadyExist(item)) {
            DialogUtil.showHint("快递单号重复");
            return;
        }
        entityList.add(0, item);

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    private boolean isAlreadyExist(StockInItem item) {
        if (entityList != null && entityList.size() > 0) {
            for (StockInItem entity : entityList) {
                if (entity.getFdorderNumber().equalsIgnoreCase(item.getFdorderNumber())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<StockInItem> getSmsEntityList() {
        List<StockInItem> items = new ArrayList<>();

        if (entityList != null && entityList.size() > 0) {
            for (StockInItem entity : entityList) {
                if (!entity.getBindwx().equals(1) && entity.getMustsms().equals(1)) {
                    items.add(entity);
                }
            }
        }

        return items;
    }
}
