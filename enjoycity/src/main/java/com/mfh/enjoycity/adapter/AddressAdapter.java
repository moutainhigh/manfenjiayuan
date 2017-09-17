package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.ReceiveAddressEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 地址
 * Created by NAT.ZZN on 2015/4/20.
 */
public class AddressAdapter extends BaseAdapter {

    private Context context;
    private List<ReceiveAddressEntity> data = new ArrayList<>();

    static class ViewHolder {
        //        @Bind(R.id.iv_marker) ImageView ivMarker;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_telephone)
        TextView tvTel;
        @BindView(R.id.tv_address)
        TextView tvAddr;
        @BindView(R.id.iv_arrow)
        ImageView ivArrow;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public AddressAdapter(Context context) {
        super();
        this.context = context;
    }

    public AddressAdapter(Context context, List<ReceiveAddressEntity> data) {
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
            view = View.inflate(context, R.layout.listitem_address, null);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        ReceiveAddressEntity entity = data.get(i);

        viewHolder.tvName.setText(entity.getReceiver());
        viewHolder.tvTel.setText(entity.getTelephone());
        viewHolder.tvAddr.setText(entity.getSubName());
        viewHolder.ivArrow.setVisibility(View.VISIBLE);
        return view;
    }

    public void setData(List<ReceiveAddressEntity> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

}
