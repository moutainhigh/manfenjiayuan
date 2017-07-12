package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bingshanguxue.cashier.model.wrapper.ResMenu;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.utils.NoDoubleClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 收银－－服务菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CashierMenuAdapter
        extends RegularAdapter<ResMenu, CashierMenuAdapter.MenuOptioinViewHolder> {

    public CashierMenuAdapter(Context context, List<ResMenu> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.tabitem_home,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final ResMenu entity = entityList.get(position);

        holder.buttonImage.setImageResource(entity.getResId());

        if (entity.getBadgeNumber() > 0){
            holder.ivBadge.setVisibility(View.VISIBLE);
        }
        else{
            holder.ivBadge.setVisibility(View.INVISIBLE);
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_buttonImage)
        ImageView buttonImage;
        @BindView(R.id.iv_badge)
        ImageView ivBadge;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new NoDoubleClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if (entityList == null || position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
//
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(v, position);
//                    }
//                }

                @Override
                public void onClickOnce(View v) {
                    super.onClickOnce(v);
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(v, position);
                    }
                }
            });
        }
    }

    /**
     * 设置数字标识
     * */
    public void setBadgeNumber(Long id, int badgeNumber){
        if (entityList != null && entityList.size() > 0){
            for (ResMenu entity : entityList){
                if (entity.getId().equals(id)){
                    entity.setBadgeNumber(badgeNumber);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

}
