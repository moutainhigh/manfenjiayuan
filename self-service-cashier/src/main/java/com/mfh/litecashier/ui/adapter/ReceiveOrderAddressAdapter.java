package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.ReceiveOrderHumanInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 快递录入－－收货地址
 * Created by Nat.ZZN(bingshanguxue) on 2015/4/20.
 */
public class ReceiveOrderAddressAdapter extends ArrayAdapter<ReceiveOrderHumanInfo> {

    static class ViewHolder {
//        @Bind(R.id.iv_marker)
//        ImageView ivMarker;
        @BindView(R.id.tv_address)
        TextView tvAddress;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ReceiveOrderAddressAdapter(Context context, List<ReceiveOrderHumanInfo> objects) {
        super(context, R.layout.itemview_receiveorder_address, objects);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(getContext(), R.layout.itemview_receiveorder_address, null);

            viewHolder = new ViewHolder(view);
            viewHolder.tvAddress = (TextView) view.findViewById(R.id.tv_address);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        ReceiveOrderHumanInfo entity = getItem(i);
        if (entity != null) {
            viewHolder.tvAddress.setText(entity.getAddress());
        } else {
            viewHolder.tvAddress.setText("");
        }
        return view;
    }

}
