package com.mfh.owner.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mfh.owner.R;
import com.mfh.owner.bean.CategoryListViewData;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by NAT.ZZN on 2015/4/20.
 */
public class CategoryListViewAdapter extends BaseAdapter {

    private Context context;
    private List<CategoryListViewData> data = new ArrayList<>();

    private int curSelectedId = 0;

    static class ViewHolder {
        @Bind(R.id.marker_left) View markerLeft;
        @Bind(R.id.tv_title) TextView tvTitle;
        @Bind(R.id.marker_right) View markerRight;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public CategoryListViewAdapter(Context context) {
        super();
        this.context = context;
        this.curSelectedId = 0;
    }

    public CategoryListViewAdapter(Context context, List<CategoryListViewData> data) {
        super();
        this.context = context;
        this.data = data;
        this.curSelectedId = 0;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(context, R.layout.listitem_category, null);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        CategoryListViewData item = data.get(i);
        viewHolder.tvTitle.setText(item.getTitle());

        if (curSelectedId == i){
            viewHolder.markerLeft.setVisibility(View.VISIBLE);
            viewHolder.markerRight.setVisibility(View.INVISIBLE);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.category_listitem_background_selected));
//            viewHolder.tvTitle.setTextColor(Color.parseColor("#ffff0000"));
//            viewHolder.tvTitle.setTextColor(context.getResources().getColor(R.color.category_selected));
//            view.setSelected(true);
            viewHolder.tvTitle.setSelected(true);
        }else{
            viewHolder.markerLeft.setVisibility(View.INVISIBLE);
            viewHolder.markerRight.setVisibility(View.VISIBLE);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.category_listitem_background_normal));
//            viewHolder.tvTitle.setTextColor(Color.parseColor("#ffffffff"));
//            viewHolder.tvTitle.setTextColor(context.getResources().getColor(R.color.category_normal));
//            view.setSelected(false);
            viewHolder.tvTitle.setSelected(false);
        }

        return view;
    }

    public void setSelectId(int selectId) {
        this.curSelectedId = selectId;

//      notifyDataSetInvalidated();
        notifyDataSetChanged();
    }

    public void setAdapterData(List<CategoryListViewData> data){
        this.data = data;

        notifyDataSetChanged();
    }
}
