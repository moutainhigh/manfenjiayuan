package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.manfenjiayuan.business.bean.CategoryOption;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 商品后台类目
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class CommodityCategoryAdapter
        extends RegularAdapter<CategoryOption, CommodityCategoryAdapter.CategoryViewHolder> {

    private CategoryOption curOption = null;

    public CommodityCategoryAdapter(Context context, List<CategoryOption> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private AdapterListener adapterListener;

    public void setOnAdapterListsner(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_commodity_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final CategoryOption bean = entityList.get(position);
        //类目名后面暂时不显示当前类目下商品数目
        holder.ivArrow.setVisibility(View.VISIBLE);
        holder.tvName.setText(bean.getValue());
    }

    @Override
    public void setEntityList(List<CategoryOption> entityList) {
        super.setEntityList(entityList);
        curOption = null;
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.iv_arrow)
        ImageView ivArrow;

        public CategoryViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    curOption = entityList.get(position);
                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });

        }

    }

    public CategoryOption getCurOption() {
        return curOption;
    }
}
