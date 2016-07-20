package com.mfh.owner.ui.map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.owner.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/4/20.
 */
public class LocationAdapter extends BaseAdapter {

    private Context context;
    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    private int curSelectedId = -1;//这里默认值为-1，因为列表有headerview

    static class ViewHolder {
        ImageView ivMarker;
        TextView tvTitle;
        TextView tvSubTitle;
    }

    public LocationAdapter(Context context) {
        super();
        this.context = context;
        this.curSelectedId = 0;
    }

    public LocationAdapter(Context context, List<Map<String, String>> data) {
        super();
        this.context = context;
        this.data = data;
        this.curSelectedId = 0;
    }

    private void init(){
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
            viewHolder = new ViewHolder();
            view = View.inflate(context, R.layout.listitem_share_location, null);
            viewHolder.ivMarker = (ImageView) view.findViewById(R.id.iv_marker);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
            viewHolder.tvSubTitle = (TextView) view.findViewById(R.id.tv_subtitle);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Map<String, String> entity = data.get(i);

        viewHolder.tvTitle.setText(entity.get("DATA_KEY_NAME"));
        viewHolder.tvSubTitle.setText(entity.get("DATA_KEY_ADDRESS"));

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

    public void setData(List<Map<String, String>> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
}
