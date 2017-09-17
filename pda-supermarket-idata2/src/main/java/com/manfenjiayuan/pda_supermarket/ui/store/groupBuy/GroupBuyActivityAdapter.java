package com.manfenjiayuan.pda_supermarket.ui.store.groupBuy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.rxapi.bean.GroupBuyActivity;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 团购活动
 * Created by bingshanguxue on 15/8/5.
 */
public class GroupBuyActivityAdapter extends RegularAdapter<GroupBuyActivity, GroupBuyActivityAdapter.ProductViewHolder> {

    public GroupBuyActivityAdapter(Context context, List<GroupBuyActivity> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.cardview_groupbuy_activity, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        GroupBuyActivity entity = entityList.get(position);
        if (entity == null) {
            ZLogger.w("GroupBuyActivity is null" + position);
            return;
        }

        Date rightNow = new Date();
        if (entity.getGropuEndDate() == null || entity.getGropuEndDate().compareTo(rightNow) < 0) {
            holder.tvTag.setVisibility(View.INVISIBLE);
        } else {
            holder.tvTag.setVisibility(View.VISIBLE);
        }
        holder.labelDate.setEndText(TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM));
        holder.labelTitle.setEndText(entity.getActivityTitle());
        holder.labelSubTitle.setEndText(entity.getActivitySubTitle());
        holder.labelTotalMoney.setEndText(MUtils.formatDouble(entity.getTotalMoney(), ""));
        holder.labelJoinHumanNum.setEndText(String.valueOf(entity.getJoinHumanNum()));
        holder.labelUnTakeHuman.setEndText(String.valueOf(entity.getUnTakeHuman()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_tag)
        TextView tvTag;
        @BindView(R.id.label_activity_date)
        TextLabelView labelDate;
        @BindView(R.id.label_activity_title)
        TextLabelView labelTitle;
        @BindView(R.id.label_activity_subtitle)
        TextLabelView labelSubTitle;
        @BindView(R.id.label_activity_total_money)
        TextLabelView labelTotalMoney;
        @BindView(R.id.label_activity_joinHumanNum)
        TextLabelView labelJoinHumanNum;
        @BindView(R.id.label_activity_unTakeHuman)
        TextLabelView labelUnTakeHuman;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

//                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<GroupBuyActivity> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<GroupBuyActivity> getEntityList() {
        return entityList;
    }

}
