package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.im.bean.MsgBean;
import com.manfenjiayuan.im.constants.IMTechType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.param.TextParam;
import com.mfh.framework.core.utils.FaceUtil;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 采购－－采购订单
 * Created by Nat.ZZN on 15/8/5.
 */
public class ChatMessageAdapter
        extends RegularAdapter<EmbMsg, ChatMessageAdapter.ProductViewHolder> {

    private EmbMsg curOrder = null;

    public ChatMessageAdapter(Context context, List<EmbMsg> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        EmbMsg entity = entityList.get(position);

        if (curOrder != null && curOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }


        holder.tvFormatCreateTime.setText(entity.getFormatCreateTime());
        MsgBean msgBean = JSONObject.parseObject(entity.getParam(), MsgBean.class);
        String body = JSON.toJSONString(msgBean.getBody());
        if (IMTechType.TEXT.equals(msgBean.getType())){
            TextParam textParam = TextParam.fromJson(body);
            holder.tvContent.setText(FaceUtil.getSpannable(mContext, textParam.getContent(), 25, 25));
        }
        //TODO
        else{
            holder.tvContent.setText(FaceUtil.getSpannable(mContext, body, 25, 25));
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_formatCreateTime)
        TextView tvFormatCreateTime;
        @Bind(R.id.tv_content)
        TextView tvContent;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getLayoutPosition()
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    curOrder = entityList.get(position);
                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    @Override
    public void setEntityList(List<EmbMsg> entityList) {
        this.entityList = entityList;

        if (this.entityList != null && this.entityList.size() > 0){
            curOrder = this.entityList.get(0);
        }
        else{
            curOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void appendEntity(EmbMsg entity){
        if (entity == null){
            return;
        }

        if (this.entityList == null){
            this.entityList = new ArrayList<>();
        }

        this.entityList.add(entity);
        notifyDataSetChanged();
    }
    public EmbMsg getCurOrder() {
        return curOrder;
    }

    public void remove(EmbMsg order) {
        if (order == null) {
            return;
        }

        if (entityList != null) {
            entityList.remove(order);
        }
        if (curOrder == order) {
            curOrder = null;
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
