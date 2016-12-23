package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.api.pmcstock.StockOutItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 包裹
 * Created by Nat.ZZN on 15/8/5.
 */
public class StockOutAdapter
        extends RegularAdapter<StockOutItem, StockOutAdapter.ProductViewHolder> {

    public StockOutAdapter(Context context, List<StockOutItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_stockout_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        StockOutItem entity = entityList.get(position);

        holder.btnToggle.setSelected(entity.isbSelected());
        holder.tvPhone.setText(entity.getHumanPhone());
        holder.tvName.setText(entity.getHumanName());
        holder.tvTransport.setText(String.format("%s/%s", entity.getTransportName(), entity.getItemTypeName()));
        holder.tvBarCode.setText(entity.getBarcode());
        holder.tvCreateDate.setText(entity.getCreatedDate());
        holder.tvPayStatus.setText(entity.getPaystatus() == 1 ? "已支付" : "未支付");
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_phone)
        TextView tvPhone;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_transport)
        TextView tvTransport;
        @BindView(R.id.tv_barcode)
        TextView tvBarCode;
        @BindView(R.id.tv_createDate)
        TextView tvCreateDate;
        @BindView(R.id.tv_payStatus)
        TextView tvPayStatus;
        @BindView(R.id.button_toggle_item)
        ImageButton btnToggle;

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
        @OnClick(R.id.button_toggle_item)
        public void toggleItem() {
            try {
                int position = getAdapterPosition();

                StockOutItem item = entityList.get(position);
                if (item.isbSelected()){
                    item.setbSelected(false);
                }else{
                    item.setbSelected(true);
                }
                notifyItemChanged(position);

                if (adapterListener != null) {
                    adapterListener.onDataSetChanged();
                }
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }
    }

    @Override
    public void setEntityList(List<StockOutItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void appendEntity(StockOutItem item){
        if (entityList == null){
            entityList = new ArrayList<>();
        }
        entityList.add(item);

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void removeEntity(String orderId){
        if (StringUtils.isEmpty(orderId)){
            return;
        }

        try{
            Long oid = Long.valueOf(orderId);
            if (entityList != null && entityList.size() > 1){
                for (int i=0; i< entityList.size(); i++){
                    StockOutItem item = entityList.get(i);
                    if (item.getGoodsId() != null && item.getGoodsId().compareTo(oid) == 0){
                        notifyItemRemoved(i);
                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged();
                        }
                        return;
                    }
                }
            }
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }
    }


    public List<StockOutItem> getSelectedEntityList() {
        List<StockOutItem> items = new ArrayList<>();

        if (entityList != null && entityList.size() > 0){
            for (StockOutItem item : entityList){
                if(item.isbSelected()){
                    items.add(item);
                }
            }
        }

        return items;
    }

    public List<StockOutItem> getSelectedPaidEntityList() {
        List<StockOutItem> items = new ArrayList<>();

        if (entityList != null && entityList.size() > 0){
            for (StockOutItem item : entityList){
                if(item.isbSelected() && item.getPaystatus() == 1){
                    items.add(item);
                }
            }
        }

        return items;
    }

    public void toggleAll(boolean selected){
        if (entityList != null && entityList.size() > 0){
            for (StockOutItem item : entityList){
                item.setbSelected(selected);
            }
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public boolean isSelectAll(){
        if (entityList != null && entityList.size() > 0){
            for (StockOutItem item : entityList){
                if (!item.isbSelected()){
                    return false;
                }
            }
        }else{
            return false;
        }

        return true;
    }
}
