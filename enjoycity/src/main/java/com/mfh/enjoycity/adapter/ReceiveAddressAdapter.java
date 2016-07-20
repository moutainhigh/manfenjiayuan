package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.framework.core.logger.ZLogger;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 收货地址
 * Created by Nat.ZZN(bingshanguxue) on 2015/4/20.
 */
public class ReceiveAddressAdapter extends BaseAdapter {

    private Context context;
    private List<ReceiveAddressEntity> data = new ArrayList<>();

    private ReceiveAddressEntity currentEntity;

    static class ViewHolder {
        @Bind(R.id.iv_marker) ImageView ivMarker;
        @Bind(R.id.tv_name) TextView tvName;
        @Bind(R.id.tv_telephone) TextView tvTel;
        @Bind(R.id.tv_address) TextView tvAddr;
//        @Bind(R.id.iv_arrow) ImageView ivArrow;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ReceiveAddressAdapter(Context context) {
        super();
        this.context = context;
    }

    public ReceiveAddressAdapter(Context context, List<ReceiveAddressEntity> data) {
        super();
        this.context = context;
        this.data = data;

        String currentId = SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ).getString(Constants.PREF_KEY_LOGIN_ADDR_ID, null);
        currentEntity = ReceiveAddressService.get().getEntityById(currentId);
    }

    @Override
    public int getCount() {
        return (data == null ? 0 : data.size());
    }

    @Override
    public Object getItem(int i) {
        if(data != null){
            return data.get(i);
        }
        return null;
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

        if (currentEntity != null && currentEntity.getId().equals(entity.getId())){
            viewHolder.ivMarker.setVisibility(View.VISIBLE);
        }else{
            viewHolder.ivMarker.setVisibility(View.INVISIBLE);
        }

        viewHolder.tvName.setText(entity.getReceiver());
        viewHolder.tvTel.setText(entity.getTelephone());
        viewHolder.tvAddr.setText(String.format("送至: %s", entity.getSubName()));
        ZLogger.d(String.format("ReceiveAddressEntity: receiver:%s, telephone:%s, subname:%s",
                entity.getReceiver(), entity.getTelephone(), entity.getSubName()));
        return view;
    }

    public void setData(List<ReceiveAddressEntity> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

}
