package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 生鲜前台类目
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class FrontendFreshCategoryAdapter
        extends RegularAdapter<CategoryInfo, FrontendFreshCategoryAdapter.CategoryViewHolder> {

    private CategoryInfo curOption = null;

    public FrontendFreshCategoryAdapter(Context context, List<CategoryInfo> entityList) {
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
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_category_text,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final CategoryInfo bean = entityList.get(position);

        if (curOption != null && curOption.getId().compareTo(bean.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvName.setText(bean.getNameCn());
    }

    @Override
    public void setEntityList(List<CategoryInfo> entityList) {
        super.setEntityList(entityList);

        if (entityList != null && entityList.size() > 0){
            curOption = entityList.get(0);
        }
        else{
            curOption = null;
        }

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_name)
        TextView tvName;

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

    public CategoryInfo getCurOption() {
        return curOption;
    }
}
