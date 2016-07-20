package com.mfh.owner.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.owner.R;
import com.mfh.owner.ui.shake.ShakeHistoryEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/4/20.
 */
public class ShakeHistoryAdapter extends BaseAdapter {

    private Context context;
    private List<ShakeHistoryEntity> data = new ArrayList<>();

    static class ViewHolder {
        @Bind(R.id.iv_header) ImageView ivHeader;
        @Bind(R.id.tv_title) TextView tvTitle;
        @Bind(R.id.tv_description) TextView tvDescription;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ShakeHistoryAdapter(Context context) {
        super();
        this.context = context;
    }

    public ShakeHistoryAdapter(Context context, List<ShakeHistoryEntity> data) {
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
            view = View.inflate(context, R.layout.listitem_shake_history, null);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        ShakeHistoryEntity entity = data.get(i);

        //显示头像
        Glide.with(context)
                .load(entity.getIconUrl())
                .error(R.drawable.chat_tmp_user_head).into(viewHolder.ivHeader);

        viewHolder.tvTitle.setText(entity.getTitle());
        viewHolder.tvDescription.setText(entity.getDescription());
        return view;
    }

    public void setData(List<ShakeHistoryEntity> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

}
