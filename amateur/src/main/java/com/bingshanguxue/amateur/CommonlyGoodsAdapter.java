package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.database.entity.CommonlyGoodsEntity;
import com.mfh.litecashier.database.logic.CommonlyGoodsService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 常用商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CommonlyGoodsAdapter
        extends RegularAdapter<CommonlyGoodsEntity, CommonlyGoodsAdapter.MenuOptioinViewHolder> {


    public CommonlyGoodsAdapter(Context context, List<CommonlyGoodsEntity> entityList) {
        super(context, entityList);
    }

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
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_measure, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final CommonlyGoodsEntity entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error).into(holder.ivHeader);

        holder.tvName.setText(entity.getName());
        holder.tvPrice.setText(String.format("¥ %.2f", entity.getCostPrice()));
        if (bRemoved) {
            holder.ibRemove.setVisibility(View.VISIBLE);
        } else {
            holder.ibRemove.setVisibility(View.GONE);
        }
    }

    @Override
    public void setEntityList(List<CommonlyGoodsEntity> entityList) {
        this.entityList = entityList;
        this.bRemoved = false;
        this.notifyDataSetChanged();

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_price)
        TextView tvPrice;
        @Bind(R.id.ib_remove)
        ImageButton ibRemove;

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
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
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

        @OnClick(R.id.ib_remove)
        public void remove() {
            removeEntity(getAdapterPosition());
        }
    }

    @Override
    public void removeEntity(int position) {
        try {
            if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }

            //删除数据库
            CommonlyGoodsEntity entity = entityList.get(position);
            if (entity != null) {
                CommonlyGoodsService.get().deleteById(String.valueOf(entity.getId()));
            }

            //刷新列表
            entityList.remove(position);
            notifyItemRemoved(position);

            if (adapterListener != null) {
                adapterListener.onDataSetChanged();
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    private boolean bRemoved;

    public boolean isbRemoved() {
        return bRemoved;
    }

    public void setbRemoved(boolean bRemoved) {
        this.bRemoved = bRemoved;
        this.notifyDataSetChanged();
    }
}
