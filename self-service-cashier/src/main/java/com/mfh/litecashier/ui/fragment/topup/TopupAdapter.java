package com.mfh.litecashier.ui.fragment.topup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 充值
 * Created by bingshanguxue on 15/8/5.
 */
public class TopupAdapter extends RegularAdapter<TopAmount, TopupAdapter.MenuOptioinViewHolder> {

    public TopupAdapter(Context context, List<TopAmount> entityList) {
        super(context, entityList);
    }

    private TopAmount curEntity;

    public interface AdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
        void onDataSetChanged();
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.itemview_topup, null, false);
//        v.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 105),
//                DensityUtil.dip2px(mContext, 122)));

//            return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_homemenu_option, parent, false));
        return new MenuOptioinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuOptioinViewHolder holder, int position) {
        final TopAmount entity = entityList.get(position);

        Double amount = entity.getCurrent();
        if (!entity.isEditabled()) {
            if (amount > 1D) {
                holder.tvAmount.setText(String.format("%.0f", amount));
            } else {
                holder.tvAmount.setText(String.format("%.2f", amount));
            }
        } else {
            if (amount == null) {
                holder.tvAmount.setText("自定义");
            } else {
                holder.tvAmount.setText(String.format("%.2f", amount));
            }
        }

        holder.tvAmount.setSelected(entity.isSelected());
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_text)
        TextView tvAmount;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    TopAmount entity = getEntity(position);
                    if (entity == null) {
                        return;
                    }

                    if (curEntity != null) {
                        curEntity.setSelected(false);
                    }
                    entity.setSelected(true);
                    curEntity = entity;
                    notifyDataSetChanged();
                    if (adapterListener != null) {
                        adapterListener.onItemClick(v, position);
                    }
                }
            });
        }
    }

    @Override
    public void setEntityList(List<TopAmount> entityList) {
        if (entityList != null && entityList.size() > 0) {
            curEntity = entityList.get(0);
//            curEntity.setSelected(true);
//            entityList.add(curEntity);
        } else {
            curEntity = null;
        }

        super.setEntityList(entityList);

        notifyDataSetChanged(true);
    }

    public TopAmount getCurEntity() {
        return curEntity;
    }

    public void notifyDataSetChanged(boolean invokeUI) {
        notifyDataSetChanged();

        if (invokeUI && adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
