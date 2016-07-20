package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.CoupBean;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.litecashier.bean.CouponRule;
import com.mfh.litecashier.bean.RuleBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 支付－－优惠券
 * Created by Nat.ZZN on 15/8/5.
 */
public class PayCouponAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_RULE,
        ITEM_TYPE_COUPON
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<CouponRule> entityList;
    private Double vipScore = 0D;//会员领取积分

    public interface OnAdapterListener {
        void onDataSetChanged();

        void onToggleItem(CouponRule couponRule);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public PayCouponAdapter(Context context, List<CouponRule> entityList) {
        this.entityList = entityList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_RULE.ordinal()){
            return new RuleViewHolder(mLayoutInflater.inflate(R.layout.itemview_pay_rule, parent, false));
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_COUPON.ordinal()){
            return new CouponViewHolder(mLayoutInflater.inflate(R.layout.itemview_pay_coupon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        CouponRule entity = entityList.get(position);

        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_RULE.ordinal()){
            //执行类型 营销类：4-折扣 5-减金额 6-返金额 7-赠送其他商品 8-赠送购买商品 9-送卡券 10-多倍积分
            if (entity.getRuleExecType() == 4){
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_vip);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.GONE);
                ((RuleViewHolder)holder).tvScore.setText(String.format("%.0f", vipScore));
                ((RuleViewHolder)holder).tvDiscount.setText(String.format("%.0f", entity.getDiscount()));

//                ((RuleViewHolder)holder).tvScore.setText(Html.fromHtml(String.format("<font color=#fcc419>%.0f\n</font><font color=#FF009B4E>-获取积分</font>", vipScore)));
//                ((RuleViewHolder)holder).tvDiscount.setText(Html.fromHtml(String.format("<font color=#fcc419>%.0f\n</font><font color=#FF009B4E>-会员折扣</font>", entity.getDiscount())));
            }
            else if (entity.getRuleExecType() == 5){
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_reduce);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.0f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
            else if (entity.getRuleExecType() == 6){
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_return);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.0f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
            else if (entity.getRuleExecType() == 7){
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_present);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.0f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
            else{
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_present);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.0f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
        }
        else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_COUPON.ordinal()){
            //"¥%.2f",优惠券金额不会有小数
            ((CouponViewHolder)holder).tvAmount.setText(String.format("%.0f", entity.getDiscount()));
            ((CouponViewHolder)holder).tvTitle.setText(entity.getTitle());
            ((CouponViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            ((CouponViewHolder)holder).tvValidDate.setText("");

            if (entity.isSelected()) {
//                holder.ibRatio.setSelected(true);
                ((CouponViewHolder)holder).ibRatio.setVisibility(View.VISIBLE);
            } else {
//                holder.ibRatio.setSelected(false);
                ((CouponViewHolder)holder).ibRatio.setVisibility(View.GONE);
            }
        }
    }

//    /** 对TextView设置不同状态时其文字颜色。 */
//    private ColorStateList createColorStateList(int normal, int pressed, int focused, int unable) {
//        int[] colors = new int[] { pressed, focused, normal, focused, unable, normal };
//        int[][] states = new int[6][];
//        states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
//        states[1] = new int[] { android.R.attr.state_enabled, android.R.attr.state_focused };
//        states[2] = new int[] { android.R.attr.state_enabled };
//        states[3] = new int[] { android.R.attr.state_focused };
//        states[4] = new int[] { android.R.attr.state_window_focused };
//        states[5] = new int[] {};
//        ColorStateList colorList = new ColorStateList(states, colors);
//        return colorList;
//    }

    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    @Override
    public int getItemViewType(int position) {
        CouponRule entity = entityList.get(position);
        if (entity.getType() == CouponRule.TYPE_RULE){
            return ITEM_TYPE.ITEM_TYPE_RULE.ordinal();
        }
        return ITEM_TYPE.ITEM_TYPE_COUPON.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public class RuleViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.frame_common)
        RelativeLayout frameCommon;
        @Bind(R.id.tv_exec_num)
        TextView tvExecNum;
        @Bind(R.id.tv_coupon_title)
        TextView tvTitle;
        @Bind(R.id.tv_coupon_description)
        TextView tvDescription;
        @Bind(R.id.frame_vip)
        LinearLayout frameVip;
        @Bind(R.id.tv_score)
        TextView tvScore;
        @Bind(R.id.tv_discount)
        TextView tvDiscount;

        public RuleViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class CouponViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_coupon_amount)
        TextView tvAmount;
        @Bind(R.id.tv_coupon_title)
        TextView tvTitle;
        @Bind(R.id.tv_coupon_description)
        TextView tvDescription;
        @Bind(R.id.tv_coupon_valid_date)
        TextView tvValidDate;
        @Bind(R.id.ib_ratio)
        ImageButton ibRatio;

        public CouponViewHolder(final View itemView) {
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

                    CouponRule entity = entityList.get(position);
                    if (entity.getType() == CouponRule.TYPE_COUPON){
                        if (entity.isSelected()) {
                            entity.setIsSelected(false);
                        } else {
                            entity.setIsSelected(true);
                        }
//                //刷新列表
                        notifyItemChanged(position);

                        if (adapterListener != null) {
                            adapterListener.onToggleItem(entity);
                        }
                    }

//                    curPosOrder = entityList.get(position);
//                    notifyDataSetChanged();//getAdapterPosition() return -1.
//
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, position);
//                    }
                }
            });
        }

        @OnClick(R.id.ib_ratio)
        public void toggle() {
            try {
                int position = getAdapterPosition();

                CouponRule item = entityList.get(position);
                if (item.isSelected()) {
                    item.setIsSelected(false);
                } else {
                    item.setIsSelected(true);
                }
//                //刷新列表
//                entityList.remove(position);
                notifyItemChanged(position);
//
                if (adapterListener != null) {
                    adapterListener.onToggleItem(item);
                }
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }
    }

    public void setEntityList(List<CouponRule> entityList) {
        this.entityList = entityList;
        this.vipScore = 0D;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void setEntityList(List<RuleBean> ruleBeans, List<CoupBean> coupBeans) {
        if (this.entityList == null){
            this.entityList = new ArrayList<>();
        }
        else{
            this.entityList.clear();
        }

        if (ruleBeans != null && ruleBeans.size() > 0){
            for (RuleBean rule : ruleBeans){
                CouponRule entity = new CouponRule();
                entity.setType(CouponRule.TYPE_RULE);
                entity.setTitle(rule.getTitle());
                entity.setSubTitle(String.format("%s/%s", rule.getExecTypeCaption(), rule.getPlanTypeCaption()));
                entity.setDiscount(rule.getExecNum());
                entity.setRuleExecType(rule.getExecType());
                entity.setIsSelected(false);

                this.entityList.add(entity);
            }
        }

        if (coupBeans != null && coupBeans.size() > 0){
            for (CoupBean coup : coupBeans){
                CouponRule entity = new CouponRule();
                entity.setType(CouponRule.TYPE_COUPON);
                entity.setTitle(coup.getTitle());
                entity.setSubTitle(coup.getSubTitle());
                entity.setDiscount(coup.getDiscount());
                entity.setCouponsId(coup.getMyCouponsId());
                entity.setIsSelected(false);

                this.entityList.add(entity);
            }
        }


        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

//    public List<CouponRule> getEntityList() {
//        return entityList;
//    }

    /**
     * format like: 233,234
     */
    public String getSelectCouponIds() {
        StringBuilder sb = new StringBuilder();
        if (entityList != null && entityList.size() > 0) {
            int len = entityList.size();
            for (int i = 0; i < len; i++) {
                CouponRule coupon = entityList.get(i);
                if (coupon.getType() == CouponRule.TYPE_RULE || !coupon.isSelected()) {
                    continue;
                }

                if (i > 0) {
                    sb.append(",");
                }
                sb.append(coupon.getCouponsId());
            }
        }

        return sb.toString();
    }


    public void setVipScore(Double score){
        this.vipScore = score;
        notifyDataSetChanged();
    }
}
