package com.bingshanguxue.almigod.clientLog;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.almigod.R;
import com.mfh.framework.api.clientLog.ClientLog;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 日志列表
 * Created by bingshanguxue on 15/8/5.
 */
public class ClientLogAdapter
        extends RegularAdapter<ClientLog, ClientLogAdapter.ProductViewHolder> {


    public ClientLogAdapter(Context context, List<ClientLog> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.cardview_clientlog, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        ClientLog entity = entityList.get(position);

//        if (selectedEntity != null && selectedEntity.getId().compareTo(entity.getId()) == 0) {
//            holder.rootView.setSelected(true);
//        } else {
//            holder.rootView.setSelected(false);
//        }
        holder.tvHardwareInfo.setText(String.format("硬件信息:%s", entity.getHardwareInformation()));
        holder.tvAndroidLev.setText(String.format("系统版本:%s", entity.getAndroidLevel()));
        holder.tvSoftVer.setText(String.format("软件版本:%s", entity.getSoftVersion()));
        holder.tvLoginName.setText(String.format("登录名:%s", entity.getLoginName()));
        holder.tvErrorTime.setText(String.format("错误时间:%s",
                TimeUtil.format(entity.getErrorTime(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        holder.tvStackInfo.setText(String.format("堆栈信息:%s", entity.getStackInformation()));

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_hardwareInformation)
        TextView tvHardwareInfo;
        @BindView(R.id.tv_androidLevel)
        TextView tvAndroidLev;
        @BindView(R.id.tv_softVersion)
        TextView tvSoftVer;
        @BindView(R.id.tv_loginName)
        TextView tvLoginName;
        @BindView(R.id.tv_errorTime)
        TextView tvErrorTime;
        @BindView(R.id.tv_stackInformation)
        TextView tvStackInfo;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }
//                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<ClientLog> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
