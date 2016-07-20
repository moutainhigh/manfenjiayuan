package com.mfh.owner.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.owner.R;
import com.mfh.owner.bean.AddressListViewData;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/4/20.
 */
public class AddressListViewAdapter extends BaseAdapter {

    private Context context;
    //Explicit type argument <> can be replaced with <>
    private List<AddressListViewData> data = new ArrayList<>();

    private int curSelectedId = 0;

    static class ViewHolder {
        @Bind(R.id.iv_header) ImageView ivHeader;
        @Bind(R.id.tv_name) TextView tvName;
        @Bind(R.id.tv_distance) TextView tvDistance;
        @Bind(R.id.tv_address) TextView tvAddress;
        @Bind(R.id.iv_marker) ImageView ivMarker;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public AddressListViewAdapter(Context context) {
        super();
        this.context = context;
        this.curSelectedId = 0;
    }

    public AddressListViewAdapter(Context context, List<AddressListViewData> data) {
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
            view = View.inflate(context, R.layout.listitem_address, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        AddressListViewData item = data.get(i);
        viewHolder.tvName.setText(item.getName());
        viewHolder.tvAddress.setText(item.getAddress());
        //TODO,根据经纬度计算距离
        viewHolder.tvDistance.setText("15km");

        if (curSelectedId == i){
            viewHolder.ivMarker.setSelected(true);
        }else{
            viewHolder.ivMarker.setSelected(false);
        }

        return view;
    }

    public  void setSelectId(int selectId) {
        this.curSelectedId = selectId;
    }


    public void setData(List<AddressListViewData> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
}
