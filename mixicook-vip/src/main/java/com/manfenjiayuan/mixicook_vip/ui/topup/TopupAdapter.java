package com.manfenjiayuan.mixicook_vip.ui.topup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class TopupAdapter extends RegularAdapter<TopAmount, TopupAdapter.MenuOptioinViewHolder> {

    public TopupAdapter(Context context, List<TopAmount> entityList) {
        super(context, entityList);
    }

    private TopAmount curEntity;

    public interface AdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
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

        holder.tvAmount.setText(MUtils.formatDouble(null, null, entity.getAmount(), "", "", "元"));
        if (entity.isSelected()){
            holder.ibRatio.setVisibility(View.VISIBLE);
        }
        else{
            holder.ibRatio.setVisibility(View.GONE);
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_amount)
        TextView tvAmount;
        @BindView(R.id.ib_ratio)
        ImageView ibRatio;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    TopAmount entity = getEntity(position);
                    if (entity == null){
                        return;
                    }

                    if (curEntity != null){
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
        curEntity = null;
        super.setEntityList(entityList);
    }

    public TopAmount getCurEntity() {
        return curEntity;
    }
}
