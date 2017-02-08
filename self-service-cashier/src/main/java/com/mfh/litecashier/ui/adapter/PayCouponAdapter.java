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

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.cashier.MarketRulesWrapper;
import com.mfh.framework.api.pmcstock.CoupBean;
import com.mfh.framework.api.pmcstock.MarketRules;
import com.mfh.framework.api.pmcstock.RuleBean;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
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
                ((RuleViewHolder)holder).tvScore.setText(String.format("%.0f", Math.abs(vipScore)));
                ((RuleViewHolder)holder).tvDiscount.setText(String.format("%.1f", entity.getDiscount()));

//                ((RuleViewHolder)holder).tvScore.setText(Html.fromHtml(String.format("<font color=#fcc419>%.0f\n</font><font color=#FF009B4E>-获取积分</font>", vipScore)));
//                ((RuleViewHolder)holder).tvDiscount.setText(Html.fromHtml(String.format("<font color=#fcc419>%.0f\n</font><font color=#FF009B4E>-会员折扣</font>", entity.getDiscount())));
            }
            else if (entity.getRuleExecType() == 5){
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_reduce);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
            else if (entity.getRuleExecType() == 6){
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_return);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
            else if (entity.getRuleExecType() == 7){
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_present);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
            else{
                ((RuleViewHolder)holder).rootView.setBackgroundResource(R.mipmap.bg_rule_present);
                ((RuleViewHolder)holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder)holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder)holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder)holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder)holder).tvDescription.setText(entity.getSubTitle());
            }
        }
        else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_COUPON.ordinal()){
            //"¥%.1f",优惠券金额不会有小数
            ((CouponViewHolder)holder).tvAmount.setText(String.format("%.1f", entity.getDiscount()));
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
        if (ObjectsCompact.equals(entity.getType(), CouponRule.TYPE_RULE)){
            return ITEM_TYPE.ITEM_TYPE_RULE.ordinal();
        }
        return ITEM_TYPE.ITEM_TYPE_COUPON.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public class RuleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.frame_common)
        RelativeLayout frameCommon;
        @BindView(R.id.tv_exec_num)
        TextView tvExecNum;
        @BindView(R.id.tv_coupon_title)
        TextView tvTitle;
        @BindView(R.id.tv_coupon_description)
        TextView tvDescription;
        @BindView(R.id.frame_vip)
        LinearLayout frameVip;
        @BindView(R.id.tv_score)
        TextView tvScore;
        @BindView(R.id.tv_discount)
        TextView tvDiscount;

        public RuleViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public class CouponViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_coupon_amount)
        TextView tvAmount;
        @BindView(R.id.tv_coupon_title)
        TextView tvTitle;
        @BindView(R.id.tv_coupon_description)
        TextView tvDescription;
        @BindView(R.id.tv_coupon_valid_date)
        TextView tvValidDate;
        @BindView(R.id.ib_ratio)
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
                    if (ObjectsCompact.equals(entity.getType(), CouponRule.TYPE_COUPON)){
                        if (entity.isSelected()) {
                            entity.setSelected(false);
                        } else {
                            entity.setSelected(true);
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
                if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                    return;
                }

                CouponRule item = entityList.get(position);
                if (item.isSelected()) {
                    item.setSelected(false);
                } else {
                    item.setSelected(true);
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

    public List<CouponRule> getEntityList() {
        return entityList;
    }

    public void digest(List<MarketRulesWrapper> orderMarketRules) {
        List<CouponRule> couponRules = merge(orderMarketRules);
        setEntityList(couponRules);
    }

    /**
     * 保存卡券和促销规则
     * */
    public List<CouponRule> merge(List<MarketRulesWrapper> orderMarketRulesList){
        List<CouponRule> couponRules = new ArrayList<>();

        //遍历拆分订单
        if (orderMarketRulesList == null || orderMarketRulesList.size() <= 0){
            ZLogger.d("orderMarketRulesList无效");
            return null;
        }
        for (MarketRulesWrapper orderMarketRules : orderMarketRulesList){
            //遍历卡券和促销规则

            List<MarketRules> marketRulesList = orderMarketRules.getResults();
            if (marketRulesList == null || marketRulesList.size() <= 0){
                ZLogger.d("marketRulesList无效");
                continue;
            }
            for (MarketRules marketRules : marketRulesList){
                //保存卡券和促销规则
                if (marketRules == null){
                    ZLogger.d("marketRules无效");
                    continue;
                }

//                 促销规则不显示
//                List<RuleBean> ruleBeans = marketRules.getRuleBeans();
//                if (ruleBeans != null && ruleBeans.size() > 0){
//                    for (RuleBean rule : ruleBeans){
//                        mergeRule(couponRules, rule);
//                    }
//                }

                List<CoupBean> coupBeans = marketRules.getCoupBeans();
                if (coupBeans != null && coupBeans.size() > 0){
                    for (CoupBean coup : coupBeans){
                        mergeCoupon(couponRules, coup);
                    }
                }
            }
        }

        return couponRules;
    }

    private void mergeRule(List<CouponRule> source, RuleBean rule){
        CouponRule entity = new CouponRule();
        entity.setId(rule.getId());
        entity.setType(CouponRule.TYPE_RULE);
        entity.setTitle(rule.getTitle());
        entity.setSubTitle(String.format("%s/%s",
                rule.getExecTypeCaption(), rule.getPlanTypeCaption()));
        entity.setDiscount(rule.getExecNum());
        entity.setRuleExecType(rule.getExecType());
        entity.setSelected(false);

        if (source != null && source.size() > 0){
            for (CouponRule couponRule : source){
                //删除重复的记录
                if (CouponRule.TYPE_RULE.equals(couponRule.getType()) &&
                        rule.getId().equals(couponRule.getId())){

                    source.remove(couponRule);
                    break;
                }
            }

        }

        if (source != null){
            source.add(entity);
        }

        ZLogger.df(JSON.toJSONString(entity));
    }

    private void mergeCoupon(List<CouponRule> source, CoupBean coupon){
        CouponRule entity = new CouponRule();
        entity.setId(coupon.getId());
        entity.setType(CouponRule.TYPE_COUPON);
        entity.setTitle(coupon.getTitle());
        entity.setSubTitle(coupon.getSubTitle());
        entity.setDiscount(coupon.getDiscount());
        entity.setCouponsId(coupon.getMyCouponsId());
        entity.setSelected(false);


        if (source != null && source.size() > 0){
            for (CouponRule couponRule : source){
                //删除重复的记录
                if (CouponRule.TYPE_COUPON.equals(couponRule.getType()) &&
                        coupon.getId().equals(couponRule.getId()) &&
                        coupon.getMyCouponsId().equals(couponRule.getCouponsId())){

                    //// TODO: 14/10/2016 这里可能会有问题，目的应该是相同的卡券仅显示一个，但是其他信息应该替换掉。 
                    // TODO: 14/10/2016 由于不考虑本地拆分订单，所以这里应该也只有一份关联订单编号
                    source.remove(couponRule);
                    break;
                }
            }
        }

        if (source != null){
            source.add(entity);
        }
        ZLogger.df(JSON.toJSONString(entity));
    }

    public void setVipScore(Double score){
        this.vipScore = score;
        notifyDataSetChanged();
    }
}
