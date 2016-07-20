package com.mfh.buyers.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.buyers.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/4/20.
 */
public class RechargeListViewAdapter extends BaseAdapter {

    private Context context;
    private List<String> data = new ArrayList<String>();

    private int curSelectedId = 0;

    static class ViewHolder {
        @Bind(R.id.tv_title) TextView tvTitle;
        @Bind(R.id.iv_marker) ImageView ivMarker;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public RechargeListViewAdapter(Context context) {
        super();
        this.context = context;
        this.curSelectedId = 0;
    }

    public RechargeListViewAdapter(Context context, List<String> data) {
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
            view = View.inflate(context, R.layout.listitem_recharge, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.tvTitle.setText(data.get(i));

        if (curSelectedId == i){
            viewHolder.ivMarker.setSelected(true);
            viewHolder.ivMarker.setVisibility(View.VISIBLE);
        }else{
            viewHolder.ivMarker.setVisibility(View.INVISIBLE);
            viewHolder.ivMarker.setSelected(false);
        }

        return view;
    }

    public  void setSelectId(int selectId) {
        this.curSelectedId = selectId;
        this.notifyDataSetChanged();
    }


    public void setData(List<String> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
}
