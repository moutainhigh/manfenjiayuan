package com.manfenjiayuan.mixicook_vip.ui.reserve;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.model.PosCategory;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 生鲜预定类目
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class ReserveSideCategoryAdapter
        extends RegularAdapter<PosCategory, ReserveSideCategoryAdapter.CategoryViewHolder> {

    private PosCategory curOption = null;

    public ReserveSideCategoryAdapter(Context context, List<PosCategory> entityList) {
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
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_category_text, parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final PosCategory bean = entityList.get(position);

        if (curOption != null && curOption.getId().compareTo(bean.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        //类目名后面暂时不显示当前类目下商品数目
        holder.tvName.setText(bean.getNameCn());
    }

    @Override
    public void setEntityList(List<PosCategory> entityList) {
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
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_name)
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

    public PosCategory getCurOption() {
        return curOption;
    }
}
