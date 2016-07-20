package com.mfh.petitestock.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.petitestock.bean.wrapper.HomeMenu;
import com.mfh.petitestock.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 收银－－服务菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_OPTION
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<HomeMenu> options;

    public interface AdapterListener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onCommandSelected(HomeMenu option);
    }
    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener)
    {
        this.adapterListener = adapterListener;
    }

    public HomeAdapter(Context context, List<HomeMenu> options) {
        this.options = options;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_OPTION.ordinal()){
            View v = mLayoutInflater.inflate(
                    R.layout.itemview_homemenu_option, null, false);
            v.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 105), DensityUtil.dip2px(mContext, 122)));

//            return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_homemenu_option, parent, false));
            return new MenuOptioinViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_OPTION.ordinal()) {
            final HomeMenu bean = options.get(position);

            ((MenuOptioinViewHolder) holder).ivHeader.setImageResource(bean.getResId());
        }
    }

    @Override
    public int getItemCount() {
        return (options == null ? 0 : options.size());
    }

    //根据这个类型判断去创建不同item的ViewHolder
    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE.ITEM_TYPE_OPTION.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    public void setOptions(List<HomeMenu> options) {
        this.options = options;
        this.notifyDataSetChanged();
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= options.size()){
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onCommandSelected(options.get(position));
                    }
                }
            });
        }
    }


}
