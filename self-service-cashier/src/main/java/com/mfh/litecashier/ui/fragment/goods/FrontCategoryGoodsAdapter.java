package com.mfh.litecashier.ui.fragment.goods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.FrontCategoryGoods;
import com.mfh.litecashier.database.entity.PosCategoryGoodsTempEntity;
import com.mfh.litecashier.database.logic.PosCategoryGodosTempService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 前台类目商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class FrontCategoryGoodsAdapter
        extends RegularAdapter<FrontCategoryGoods, FrontCategoryGoodsAdapter.MenuOptioinViewHolder> {

    public FrontCategoryGoodsAdapter(Context context, List<FrontCategoryGoods> entityList) {
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
        return new MenuOptioinViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_frontcategory_goods,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final FrontCategoryGoods entity = entityList.get(position);

        holder.tvName.setText(entity.getName());
        holder.tvPrice.setText(entity.getBarcode());

        String sqlWhere = String.format("productId = '%d'", entity.getProductId());
        List<PosCategoryGoodsTempEntity>  goodsTempEntities = PosCategoryGodosTempService.getInstance()
                .queryAllBy(sqlWhere);
        if (goodsTempEntities != null && goodsTempEntities.size() > 0){
            holder.ibRatio.setVisibility(View.VISIBLE);
            entity.setSelected(true);
        }
        else{
            holder.ibRatio.setVisibility(View.GONE);
            entity.setSelected(false);
        }
    }

    @Override
    public void setEntityList(List<FrontCategoryGoods> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.ib_ratio)
        ImageButton ibRatio;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    FrontCategoryGoods entity = getEntity(position);
                    if (entity == null){
                        return;
                    }

                    if (entity.isSelected()) {
                        entity.setSelected(false);
                    } else {
                        entity.setSelected(true);
                    }

                    PosCategoryGodosTempService.getInstance().saveOrUpdateGoods(entity);
//                //刷新列表
                    notifyItemChanged(position);

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
    }

}
