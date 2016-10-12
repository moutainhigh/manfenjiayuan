package com.manfenjiayuan.mixicook_vip.ui.location;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.address.AddressBrief;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 2015/4/20.
 */
public class PoiAdapter extends BaseAdapter {

    private Context context;
    private List<AddressBrief> data = new ArrayList<>();

    private int curSelectedId = -1;//这里默认值为-1，因为列表有headerview

    static class ViewHolder {
        ImageView ivMarker;
        TextView tvTitle;
        TextView tvSubTitle;
    }

    public PoiAdapter(Context context) {
        super();
        this.context = context;
        this.curSelectedId = 0;
    }

    public PoiAdapter(Context context, List<AddressBrief> data) {
        super();
        this.context = context;
        this.data = data;
        this.curSelectedId = 0;
    }

    private void init(){
    }

    @Override
    public int getCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return data != null ? data.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = View.inflate(context, R.layout.itemview_poi, null);
            viewHolder.ivMarker = (ImageView) view.findViewById(R.id.iv_marker);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
            viewHolder.tvSubTitle = (TextView) view.findViewById(R.id.tv_subtitle);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        AddressBrief entity = data.get(i);

        viewHolder.tvTitle.setText(entity.getName());
        viewHolder.tvSubTitle.setText(entity.getAddress());

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

    public void setData(List<AddressBrief> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
}
