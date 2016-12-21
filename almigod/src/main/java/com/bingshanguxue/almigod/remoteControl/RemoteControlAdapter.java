package com.bingshanguxue.almigod.remoteControl;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.almigod.R;
import com.mfh.framework.anlaysis.remoteControl.RemoteControl;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 远程控制
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class RemoteControlAdapter extends RegularAdapter<RemoteControl, RemoteControlAdapter.MenuOptioinViewHolder> {

    public RemoteControlAdapter(Context context, List<RemoteControl> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

//        void onItemLongClick(View view, int position);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterLitener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.cardview_remotecontrol, parent, false);
//        v.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 105),
//                DensityUtil.dip2px(mContext, 122)));


//            return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_RemoteControl_option, parent, false));
        return new MenuOptioinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuOptioinViewHolder holder, int position) {
        final RemoteControl entity = entityList.get(position);

        holder.tvId.setText(String.valueOf(entity.getId()));
        holder.tvName.setText(entity.getName());
        holder.tvDescription.setText(entity.getDescription());
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_id)
        TextView tvId;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_description)
        TextView tvDescription;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(v, position);
                    }
                }
            });
        }
    }


}
