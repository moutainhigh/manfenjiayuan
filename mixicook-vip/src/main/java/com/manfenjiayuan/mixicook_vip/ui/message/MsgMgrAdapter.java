package com.manfenjiayuan.mixicook_vip.ui.message;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.im.constants.IMBizType;
import com.manfenjiayuan.im.constants.IMTechType;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.uikit.widget.BadgeDrawable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 消息管理器
 * Created by bingshanguxue on 15/8/5.
 */
public class MsgMgrAdapter
        extends RegularAdapter<EmbMsg, MsgMgrAdapter.ProductViewHolder> {

    private EmbMsg curPosOrder = null;
    private CommonDialog confirmDialog = null;

    public MsgMgrAdapter(Context context, List<EmbMsg> entityList) {
        super(context, entityList);
    }


    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the view for this view holder
        View view = mLayoutInflater.inflate(R.layout.cardview_message, parent, false);
        // Call the view holder's constructor, and pass the view to it;
// return that new view holder
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        EmbMsg entity = entityList.get(position);

        BadgeDrawable drawableIsRead =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue))
                        .build();
        if (entity.getIsRead() == 1) {
            drawableIsRead.setText1("已读");
        } else {
            drawableIsRead.setText1("未读");
        }

        BadgeDrawable drawableTechType =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue))
                        .text1(entity.getTechType())
                        .text2(IMTechType.name(entity.getTechType()))
                        .build();

        BadgeDrawable drawableBizType =
                new BadgeDrawable.Builder()
                        .type(BadgeDrawable.TYPE_WITH_TWO_TEXT)
//                        .badgeColor(0xFF5722)
                        .badgeColor(ContextCompat.getColor(mContext, R.color.lightskyblue))
                        .text1(String.valueOf(entity.getBizType()))
                        .text2(IMBizType.name(entity.getBizType()))
                        .build();
        SpannableString badgeBrief = new SpannableString(TextUtils.concat(drawableIsRead.toSpannable(),
                "  ", TextUtils.concat(drawableTechType.toSpannable(),
                        "  ", drawableBizType.toSpannable())));
        holder.tvBadge.setText(badgeBrief);

        holder.tvId.setText(String.format("编号：%s", entity.getId()));
        holder.tvFrom.setText(String.format("发送方：GUID(%s)\nCID(%s)\n" +
                        "CPID(%s)\n" +
                        "CTYPE(%s)",
                entity.getFromGuid(), entity.getFromChannelId(),
                entity.getFromChannelPointId(), entity.getFromChannelType()));
        holder.tvTo.setText(String.format("接收方：CID(%s)\n" +
                        "CPID(%s)\n",
                entity.getToChannelId(), entity.getToChannelPointId(),
                entity.getFromChannelPointId(), entity.getFromChannelType()));
        holder.tvBody.setText(String.format("消息内容：%s", entity.getMsgBean()));
        holder.tvCreateDate.setText(String.format("创建时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeCursor.InnerFormat)));
        holder.tvUpdateDate.setText(String.format("更新时间：%s",
                TimeUtil.format(entity.getUpdatedDate(), TimeCursor.InnerFormat)));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        //        @Bind(R.id.rootview)
//        View rootView;
        @BindView(R.id.tv_badge)
        TextView tvBadge;
        @BindView(R.id.tv_id)
        TextView tvId;
        @BindView(R.id.tv_from)
        TextView tvFrom;
        @BindView(R.id.tv_to)
        TextView tvTo;
        @BindView(R.id.tv_body)
        TextView tvBody;

        @BindView(R.id.tv_createDate)
        TextView tvCreateDate;
        @BindView(R.id.tv_updatedate)
        TextView tvUpdateDate;

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
                    curPosOrder = entityList.get(position);
                    notifyDataSetChanged();
//                    notifyItemChanged(position);

//                    //加载支付记录
//                    JSONArray payInfoArray = new JSONArray();
//                    List<PosOrderPayEntity> payEntityList = PosOrderPayService.get().queryAllBy(String.format("orderBarCode = '%s'", curPosOrder.getBarCode()));
//                    for (PosOrderPayEntity payEntity : payEntityList){
//                        payInfoArray.add(payEntity);
//                    }
//                    ZLogger.d(String.format("{payInfo:%s}", payInfoArray.toJSONString()));

//                    ZLogger.d(String.format("barcode : [%s]", curPosOrder.getBarcode()));
                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();

                    removeEntity(position);

//                    if (adapterListener != null) {
//                        adapterListener.onItemLongClick(itemView, getPosition());
//                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setEntityList(List<EmbMsg> entityList) {
        this.entityList = entityList;
        if (this.entityList != null && this.entityList.size() > 0) {
            this.curPosOrder = this.entityList.get(0);
        } else {
            this.curPosOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void appendEntityList(List<EmbMsg> entityList) {
        if (entityList == null) {
            return;
        }

        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        }

        for (EmbMsg order : entityList) {
            if (!this.entityList.contains(order)) {
                this.entityList.add(order);
            }
        }

        if (this.curPosOrder == null && this.entityList.size() > 0) {
            this.curPosOrder = this.entityList.get(0);
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public EmbMsg getCurPosOrder() {
        return curPosOrder;
    }

}
