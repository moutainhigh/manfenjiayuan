package com.manfenjiayuan.mixicook_vip.ui.goods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 *
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CategoryGoodsAdapter extends RegularAdapter<ScGoodsSku, CategoryGoodsAdapter.MenuOptioinViewHolder> {

    public CategoryGoodsAdapter(Context context, List<ScGoodsSku> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onAdd2Cart(View view, ScGoodsSku product);
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(
                R.layout.itemview_categorygoods, null, false);

       return new MenuOptioinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuOptioinViewHolder holder, int position) {
        final ScGoodsSku bean = entityList.get(position);
//        ZLogger.d(String.format("position=%d, imageUrl=%s", position, bean.getImageUrl()));
        holder.tvName.setText(bean.getSkuName());
        holder.tvPrice.setText(MUtils.formatDouble("¥", "", bean.getCostPrice(), "", null, null));
        Glide.with(AppContext.getAppContext()).load(bean.getImgUrl())
                .error(R.mipmap.ic_image_error).into(holder.ivHeader);
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_header)
        ImageView ivHeader;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

//                    ScGoodsSku goodsSku = getEntity(position);

//                    DialogUtil.showHint(String.format("点击:%s%s", goodsSku.getSkuName(), goodsSku.getImgUrl()));
                    if (adapterListener != null) {
                        adapterListener.onItemClick(v, position);
                    }
                }
            });
        }

        @OnClick(R.id.ib_cart)
        public void add2Cart(){
            int position = getAdapterPosition();
            ScGoodsSku product = getEntity(position);
            if (product != null && adapterListener != null) {
                adapterListener.onAdd2Cart(ivHeader, product);
            }

        }
    }

    @Override
    public void setEntityList(List<ScGoodsSku> entityList) {
        super.setEntityList(entityList);
//        ZLogger.d(String.format("共有%s个元素个元素\"", entityList != null ? entityList.size() : 0));
    }
}
