package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.HistorySearchEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by bingshanguxue on 2015/4/20.
 */
public class HistorySearchAdapter extends BaseAdapter {

    private Context context;
    private List<HistorySearchEntity> data = new ArrayList<>();

    private int curSelectedId = 0;

    static class ViewHolder {
        @Bind(R.id.tv_query) TextView tvQuery;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public HistorySearchAdapter(Context context) {
        this(context, null);
    }

    public HistorySearchAdapter(Context context, List<HistorySearchEntity> data) {
        super();
        this.context = context;
        this.data = data;
        this.curSelectedId = 0;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
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
            view = View.inflate(context, R.layout.view_item_history_search, null);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        HistorySearchEntity item = data.get(i);
        viewHolder.tvQuery.setText(item.getQueryContent());

        if (curSelectedId == i){
            view.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.category_listitem_background_selected));
//            viewHolder.tvTitle.setTextColor(Color.parseColor("#ffff0000"));
//            viewHolder.tvTitle.setTextColor(context.getResources().getColor(R.color.category_selected));
//            view.setSelected(true);
        }else{
            view.setBackgroundColor(ContextCompat.getColor(context,
                    R.color.category_listitem_background_normal));
//            viewHolder.tvTitle.setTextColor(Color.parseColor("#ffffffff"));
//            viewHolder.tvTitle.setTextColor(context.getResources().getColor(R.color.category_normal));
//            view.setSelected(false);
        }

        return view;
    }

    public void setSelectId(int selectId) {
        this.curSelectedId = selectId;

//      notifyDataSetInvalidated();
        notifyDataSetChanged();
    }

    public void setAdapterData(List<HistorySearchEntity> data){
        this.data = data;

        notifyDataSetChanged();
    }
}
