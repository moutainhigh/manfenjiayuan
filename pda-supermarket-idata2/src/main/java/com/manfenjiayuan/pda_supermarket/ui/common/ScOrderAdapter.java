package com.manfenjiayuan.pda_supermarket.ui.common;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.uikit.widget.BadgeDrawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 商城订单列表
 * Created by bingshanguxue on 15/8/5.
 */
public class ScOrderAdapter extends RegularAdapter<ScOrder, ScOrderAdapter.ProductViewHolder> {

    private boolean isAlarmEnabled;
    private boolean isSortEnabled;
    public ScOrderAdapter(Context context, List<ScOrder> entityList) {
        super(context, entityList);
    }

    public ScOrderAdapter(Context context, List<ScOrder> entityList, boolean isAlarmEnabled) {
        super(context, entityList);
        this.isAlarmEnabled = isAlarmEnabled;
    }

    public ScOrderAdapter(Context context, List<ScOrder> entityList, boolean isAlarmEnabled, boolean isSortEnabled) {
        super(context, entityList);
        this.isAlarmEnabled = isAlarmEnabled;
        this.isSortEnabled = isSortEnabled;
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_scorder, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        ScOrder entity = entityList.get(position);
        holder.labelCreatedDate.setEndText(TimeUtil.format(entity.getCreatedDate(),
                TimeCursor.FORMAT_YYYYMMDDHHMM));
        holder.labelContact.setEndText(String.format("%s/%s",
                entity.getReceiveName(), entity.getReceivePhone()));
        holder.labelAddr.setEndText(entity.getAddress());
        holder.labelQuantity.setEndText(MUtils.formatDouble(entity.getBcount(), ""));
        holder.labelAmount.setEndText(MUtils.formatDouble(entity.getAmount(), ""));

        Date dueDate = entity.getDueDate();
        Date dueDateEnd = entity.getDueDateEnd();
//        Calendar today = Calendar.getInstance();
        Date rightNow = new Date();
//        if (dueDate != null){
//            Calendar due = Calendar.getInstance();
//            due.setTime(dueDate);
//            due.get(Calendar.DAY_OF_MONTH);
//        }

        if (TimeUtil.isSameDay(dueDate, dueDateEnd)){
            holder.labelTransDate.setEndText(String.format("%s-%s",
                    TimeUtil.getCaptionTimeV2(dueDate, false),
                    TimeUtil.format(dueDateEnd, TimeUtil.FORMAT_HHMM)));
        }
        else {
            holder.labelTransDate.setEndText(String.format("%s-%s",
                    TimeUtil.getCaptionTimeV2(dueDate, false),
                    TimeUtil.getCaptionTimeV2(dueDateEnd, false)));
        }

        if (isAlarmEnabled){
            // TODO: 21/10/2016 时间翻译显示
            BadgeDrawable alarmDrawable =
                    new BadgeDrawable.Builder()
                            .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                            .textSize(DensityUtil.sp2px(mContext, 16))
                            .badgeColor(0xff666666)
                            .text1("")
                            .build();
            alarmDrawable.setText1(String.format("%s-%s",
                    TimeUtil.format(dueDate, TimeUtil.FORMAT_HHMM),
//                    TimeUtil.getCaptionTimeV2(dueDate, false),
                    TimeUtil.format(dueDateEnd, TimeUtil.FORMAT_HHMM)));
            //当前时间是这个区间，就显示黄色，如果超过了，就显示红色，如果还没到，就是绿色
            if (ObjectsCompact.compare(rightNow, dueDate) < 0){
                alarmDrawable.setBadgeColor(ContextCompat.getColor(mContext,
                        R.color.material_green_500));
            }
            else{
                if (ObjectsCompact.compare(rightNow, dueDateEnd) > 0){
                    alarmDrawable.setBadgeColor(ContextCompat.getColor(mContext,
                            R.color.material_red_500));
                }
                else{
                    alarmDrawable.setBadgeColor(ContextCompat.getColor(mContext,
                            R.color.material_orange_500));
                }
            }

            holder.tvAlarm.setText(alarmDrawable.toSpannable());
            holder.tvAlarm.setVisibility(View.VISIBLE);
        }
        else{
            holder.tvAlarm.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.label_createdDate)
        TextLabelView labelCreatedDate;
        @Bind(R.id.tv_alarm)
        TextView tvAlarm;
        @Bind(R.id.label_contact)
        TextLabelView labelContact;
        @Bind(R.id.label_addr)
        TextLabelView labelAddr;
        @Bind(R.id.label_quantity)
        TextLabelView labelQuantity;
        @Bind(R.id.label_amount)
        TextLabelView labelAmount;
        @Bind(R.id.label_transDate)
        TextLabelView labelTransDate;

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

                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<ScOrder> entityList) {
        this.entityList = entityList;

        if (isSortEnabled){
            sortByDueDate();
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }


    @Override
    public void appendEntityList(List<ScOrder> entityList) {
        if (entityList == null){
            return;
        }

        if (this.entityList == null){
            this.entityList = new ArrayList<>();
        }
        this.entityList.addAll(entityList);

        if (isSortEnabled){
            sortByDueDate();
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * 按时间排序
     * */
    private void sortByDueDate() {
        if (entityList == null || entityList.size() < 1){
            return;
        }

        Collections.sort(entityList, mComparator);
    }

    private Comparator<ScOrder> mComparator = new Comparator<ScOrder>() {
        @Override
        public int compare(ScOrder order1, ScOrder order2) {
            if (order1 == null || order1.getDueDate() == null){
                return -1;
            }

            if (order2 == null || order2.getDueDate() == null){
                return 0;
            }

            return order1.getDueDate().compareTo(order2.getDueDate());
        }
    };

}
