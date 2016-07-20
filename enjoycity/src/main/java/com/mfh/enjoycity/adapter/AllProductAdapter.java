package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.CategoryProductBean;
import com.mfh.framework.uikit.widget.ChildGridView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 所有商品
 * Created by Nat.ZZN on 15/6/5.
 */
public class AllProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_HEADERE,
        ITEM_TYPE_CATEGORY_PRODUCT,
        ITEM_TYPE_FOOTER
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<CategoryProductBean> categoryBeanList;
    private int headerVCount, categoryProductVCount;

    public interface OnAdapterListener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener)
    {
        this.adapterListener = adapterListener;
    }

    public AllProductAdapter(Context context, List<CategoryProductBean> categoryBeanList) {
        this.categoryBeanList = categoryBeanList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        headerVCount = 1;
        categoryProductVCount = (categoryBeanList == null ? 0 : categoryBeanList.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_HEADERE.ordinal()){
            return new HeaderViewHolder(mLayoutInflater.inflate(R.layout.view_header_all_product, parent, false));
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_CATEGORY_PRODUCT.ordinal()){
            return new CategoryProductViewHolder(mLayoutInflater.inflate(R.layout.view_item_home_category, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_CATEGORY_PRODUCT.ordinal()) {
            CategoryProductBean categoryProductBean = categoryBeanList.get(position);

            ((CategoryProductViewHolder)holder).tvTitle.setText(categoryProductBean.getCategoryName());
            ((CategoryProductViewHolder)holder).gridView.setAdapter(new ProductGridAdapter(mContext, categoryProductBean.getProductBeans()));
        }
    }

    @Override
    public int getItemCount() {
        return headerVCount + categoryProductVCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == headerVCount - 1){
            return ITEM_TYPE.ITEM_TYPE_HEADERE.ordinal();
        }
        return ITEM_TYPE.ITEM_TYPE_CATEGORY_PRODUCT.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View view) {
            super(view);
        }
    }

    public class CategoryProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_title)
        TextView tvTitle;
        @Bind(R.id.button_more)
        Button btnMore;
        @Bind(R.id.grid_products)
        ChildGridView gridView;

        public CategoryProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });

        }
    }

}
