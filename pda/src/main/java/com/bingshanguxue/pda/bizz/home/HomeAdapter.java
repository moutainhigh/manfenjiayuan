package com.bingshanguxue.pda.bizz.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bingshanguxue.pda.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class HomeAdapter extends RegularAdapter<HomeMenu, HomeAdapter.MenuOptioinViewHolder> {

    public HomeAdapter(Context context, List<HomeMenu> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onCommandSelected(HomeMenu option);
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(
                R.layout.itemview_homemenu, null, false);
//        v.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 105),
//                DensityUtil.dip2px(mContext, 122)));


//            return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_homemenu_option, parent, false));
        return new MenuOptioinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuOptioinViewHolder holder, int position) {
        final HomeMenu bean = entityList.get(position);

        holder.ivHeader.setImageResource(bean.getResId());
        if (bean.getBadgeNumber() > 0){
            holder.ivBadge.setVisibility(View.VISIBLE);
        }
        else{
            holder.ivBadge.setVisibility(View.GONE);
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.iv_header)
        ImageView ivHeader;
        //        @Bind(R.id.iv_header)
        ImageView ivBadge;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);
            ivBadge = (ImageView) itemView.findViewById(R.id.iv_badge);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    HomeMenu entity = getEntity(position);
                    if (entity != null){
                        if (adapterListener != null) {
                            adapterListener.onCommandSelected(entity);
                        }
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
            for (HomeMenu entity : entityList){
                if (entity.getId().equals(id)){
                    entity.setBadgeNumber(badgeNumber);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

}
