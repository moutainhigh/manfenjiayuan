package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.HumanCompanyOption;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 快递录入－－收货地址
 * Created by Nat.ZZN(bingshanguxue) on 2015/4/20.
 */
public class ExpressCompanyAdapter extends ArrayAdapter<HumanCompanyOption> {
    public ExpressCompanyAdapter(Context context, List<HumanCompanyOption> objects) {
        super(context, R.layout.itemview_receiveorder_address, objects);
    }

    static class ViewHolder {
        //        @Bind(R.id.iv_marker) ImageView ivMarker;
        @Bind(R.id.tv_address)
        TextView tvAddress;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(getContext(), R.layout.itemview_receiveorder_address, null);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        HumanCompanyOption entity = getItem(i);
        viewHolder.tvAddress.setText(entity.getValue());
        return view;
    }

}
