package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.enjoycity.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 地址
 * Created by Administrator on 2015/4/20.
 */
public class SimpleCommunityAdapter extends BaseAdapter {

    private Context context;
    private List<String> data = new ArrayList<>();

    static class ViewHolder {
        @BindView(R.id.tv_title) TextView tvTitle;
        @BindView(R.id.iv_arrow) ImageView ivArrow;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public SimpleCommunityAdapter(Context context) {
        super();
        this.context = context;
    }

    public SimpleCommunityAdapter(Context context, List<String> data) {
        super();
        this.context = context;
        this.data = data;
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
            view = View.inflate(context, R.layout.listview_item_custom, null);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvTitle.setText(data.get(i));
        viewHolder.ivArrow.setVisibility(View.VISIBLE);
        return view;
    }

    public void setData(List<String> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

}
