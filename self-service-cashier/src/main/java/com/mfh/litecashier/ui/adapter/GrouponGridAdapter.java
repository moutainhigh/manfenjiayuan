package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.CashierFunctional;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 团购网格导航
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class GrouponGridAdapter
        extends RegularAdapter<CashierFunctional, GrouponGridAdapter.MenuOptioinViewHolder> {

    public GrouponGridAdapter(Context context, List<CashierFunctional> entityList) {
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
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_groupon_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final CashierFunctional entity = entityList.get(position);

        if (entity.getType() == 0) {
//            Glide.with(mContext).load(entity.getResId())
//                    .error(R.mipmap.ic_image_error).into(holder.ivHeader);
            holder.ivHeader.setImageResource(entity.getResId());
        } else {
            Glide.with(mContext).load(entity.getImageUrl())
                    .error(R.mipmap.ic_image_error).into(holder.ivHeader);
        }
    }

//    public void setEntityList(List<PosCategory> localList, List<PosCategory> cloudList) {
//        if (this.entityList == null){
//            this.entityList = new ArrayList<>();
//        }
//        else{
//            this.entityList.clear();
//        }
//
//        if (localList != null){
//            this.entityList.addAll(localList);
//        }
//        if (cloudList != null){
//            this.entityList.addAll(cloudList);
//        }
//        this.notifyDataSetChanged();
//    }

//    public void setEntityList(List<PosCategory> localList, List<PosCategory> publicList,
//                              List<PosCategory> customList) {
//        if (this.entityList == null){
//            this.entityList = new ArrayList<>();
//        }
//        else{
//            this.entityList.clear();
//        }
//
//        if (localList != null){
//            this.entityList.addAll(localList);
//        }
//        if (publicList != null){
//            this.entityList.addAll(publicList);
//        }
//        if (customList != null){
//            this.entityList.addAll(customList);
//        }
//        this.notifyDataSetChanged();
//    }

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

}
