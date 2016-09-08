package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.CategoryOptionBean;
import com.mfh.enjoycity.view.CategoryItem;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 所有商品--类目
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class AllProductCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_CATEGORY
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<CategoryOptionBean> categoryBeanList;
    private int expandPosition;

    public interface AdapterListener
    {
        void onSelectCategory(CategoryOptionBean bean);
    }
    private AdapterListener adapterListener;

    public void setOnAdapterListsner(AdapterListener adapterListener)
    {
        this.adapterListener = adapterListener;
    }

    public AllProductCategoryAdapter(Context context, List<CategoryOptionBean> categoryBeanList) {
        this.categoryBeanList = categoryBeanList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_CATEGORY.ordinal()){
            return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.view_allproduct_category, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_CATEGORY.ordinal()) {
            final CategoryOptionBean bean = categoryBeanList.get(position);

            ((CategoryViewHolder)holder).tvName.setText(bean.getValue());
            ((CategoryViewHolder)holder).tvName.setTextColor(Color.parseColor("#b3009B4E"));
            ((CategoryViewHolder)holder).llSub.removeAllViews();

            List<CategoryOptionBean> items = bean.getItems();
            if (items != null && items.size() > 0){
                ((CategoryViewHolder)holder).ivArrow.setVisibility(View.VISIBLE);

                ((CategoryViewHolder)holder).llRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (expandPosition != position){
                            expandPosition = position;
                        }else{
                            expandPosition = -1;
                        }
                        notifyDataSetChanged();
                    }
                });

                if (items.size() > 1){
                    CategoryItem item = new CategoryItem(mContext);
//                    item.setViewBackground(mContext.getResources().getColor(R.color.black_40));
                    item.setTvName("全部商品", ContextCompat.getColor(mContext, R.color.black_80));
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (adapterListener != null) {
                                adapterListener.onSelectCategory(bean);
                            }
                        }
                    });
                    ((CategoryViewHolder)holder).llSub.addView(item);
                }

                for (final CategoryOptionBean option : items){
                    CategoryItem item = new CategoryItem(mContext);
//                    item.setViewBackground(mContext.getResources().getColor(R.color.black_40));
                    item.setTvName(option.getValue(), ContextCompat.getColor(mContext, R.color.black_80));
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (adapterListener != null) {
                                adapterListener.onSelectCategory(option);
                            }
                        }
                    });

                    ((CategoryViewHolder)holder).llSub.addView(item);
                }

                if (expandPosition == position){
                    ((CategoryViewHolder) holder).llSub.setVisibility(View.VISIBLE);
                    ((CategoryViewHolder)holder).ivArrow.setImageResource(R.drawable.ic_category_hide);
                }else {
                    ((CategoryViewHolder) holder).llSub.setVisibility(View.GONE);
                    ((CategoryViewHolder)holder).ivArrow.setImageResource(R.drawable.ic_category_show);
                }
            }
            else{
                ((CategoryViewHolder)holder).ivArrow.setVisibility(View.GONE);
                ((CategoryViewHolder)holder).llRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adapterListener != null) {
                            adapterListener.onSelectCategory(bean);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return (categoryBeanList == null ? 0 : categoryBeanList.size());
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE.ITEM_TYPE_CATEGORY.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public void setCategoryBeanList(List<CategoryOptionBean> categoryBeanList) {
        this.categoryBeanList = categoryBeanList;
        notifyDataSetChanged();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ll_root)
        LinearLayout llRoot;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.iv_arrow)
        ImageView ivArrow;
        @Bind(R.id.ll_sub)
        LinearLayout llSub;

        public CategoryViewHolder(final View itemView) {
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
