package com.manfenjiayuan.mixicook_vip.ui.order;

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
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.model.CouponRuleWrapper;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.pmcstock.CoupBean;
import com.mfh.framework.api.pmcstock.MarketRules;
import com.mfh.framework.api.pmcstock.RuleBean;
import com.mfh.framework.core.utils.ObjectsCompact;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 定案优惠券
 * Created by Nat.ZZN on 15/8/5.
 */
public class OrderCouponsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_RULE,
        ITEM_TYPE_COUPON
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<CouponRuleWrapper> entityList;

    public interface OnAdapterListener {
        void onDataSetChanged();

//        void onToggleItem(CouponRuleWrapper CouponRuleWrapper);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public OrderCouponsAdapter(Context context, List<CouponRuleWrapper> entityList) {
        this.entityList = entityList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_RULE.ordinal()) {
            return new RuleViewHolder(mLayoutInflater.inflate(R.layout.itemview_order_coupons_rule, parent, false));
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_COUPON.ordinal()) {
            return new CouponViewHolder(mLayoutInflater.inflate(R.layout.itemview_order_coupons_coupon, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        CouponRuleWrapper entity = entityList.get(position);

        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_RULE.ordinal()) {
            //执行类型 营销类：4-折扣 5-减金额 6-返金额 7-赠送其他商品 8-赠送购买商品 9-送卡券 10-多倍积分
            if (entity.getRuleExecType() == 4) {
                ((RuleViewHolder) holder).rootView.setBackgroundResource(R.mipmap.bg_vip);
                ((RuleViewHolder) holder).frameVip.setVisibility(View.VISIBLE);
                ((RuleViewHolder) holder).frameCommon.setVisibility(View.GONE);
                ((RuleViewHolder) holder).tvDiscount.setText(String.format("%.1f", entity.getDiscount()));

//                ((RuleViewHolder)holder).tvScore.setText(Html.fromHtml(String.format("<font color=#fcc419>%.0f\n</font><font color=#FF009B4E>-获取积分</font>", vipScore)));
//                ((RuleViewHolder)holder).tvDiscount.setText(Html.fromHtml(String.format("<font color=#fcc419>%.0f\n</font><font color=#FF009B4E>-会员折扣</font>", entity.getDiscount())));
            } else if (entity.getRuleExecType() == 5) {
                ((RuleViewHolder) holder).rootView.setBackgroundResource(R.mipmap.bg_rule_reduce);
                ((RuleViewHolder) holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder) holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder) holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder) holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder) holder).tvDescription.setText(entity.getSubTitle());
            } else if (entity.getRuleExecType() == 6) {
                ((RuleViewHolder) holder).rootView.setBackgroundResource(R.mipmap.bg_rule_return);
                ((RuleViewHolder) holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder) holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder) holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder) holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder) holder).tvDescription.setText(entity.getSubTitle());
            } else if (entity.getRuleExecType() == 7) {
                ((RuleViewHolder) holder).rootView.setBackgroundResource(R.mipmap.bg_rule_present);
                ((RuleViewHolder) holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder) holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder) holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder) holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder) holder).tvDescription.setText(entity.getSubTitle());
            } else {
                ((RuleViewHolder) holder).rootView.setBackgroundResource(R.mipmap.bg_rule_present);
                ((RuleViewHolder) holder).frameVip.setVisibility(View.GONE);
                ((RuleViewHolder) holder).frameCommon.setVisibility(View.VISIBLE);
                ((RuleViewHolder) holder).tvExecNum.setText(String.format("%.1f", entity.getDiscount()));
                ((RuleViewHolder) holder).tvTitle.setText(entity.getTitle());
                ((RuleViewHolder) holder).tvDescription.setText(entity.getSubTitle());
            }
        } else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_COUPON.ordinal()) {
            //"¥%.1f",优惠券金额不会有小数
            ((CouponViewHolder) holder).tvAmount.setText(String.format("%.1f", entity.getDiscount()));
            ((CouponViewHolder) holder).tvTitle.setText(entity.getTitle());
            ((CouponViewHolder) holder).tvDescription.setText(entity.getSubTitle());
            ((CouponViewHolder) holder).tvValidDate.setText("");

            if (entity.isSelected()) {
//                holder.ibRatio.setSelected(true);
                ((CouponViewHolder) holder).ibRatio.setVisibility(View.VISIBLE);
            } else {
//                holder.ibRatio.setSelected(false);
                ((CouponViewHolder) holder).ibRatio.setVisibility(View.GONE);
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
        CouponRuleWrapper entity = entityList.get(position);
        if (ObjectsCompact.equals(entity.getType(), CouponRuleWrapper.TYPE_RULE)) {
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

                    CouponRuleWrapper entity = entityList.get(position);
                    if (ObjectsCompact.equals(entity.getType(), CouponRuleWrapper.TYPE_COUPON)) {
                        if (entity.isSelected()) {
                            entity.setSelected(false);
                        } else {
                            entity.setSelected(true);
                        }
//                //刷新列表
                        notifyItemChanged(position);

//                        if (adapterListener != null) {
//                            adapterListener.onToggleItem(entity);
//                        }

                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged();
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

                CouponRuleWrapper item = entityList.get(position);
                if (item.isSelected()) {
                    item.setSelected(false);
                } else {
                    item.setSelected(true);
                }
//                //刷新列表
//                entityList.remove(position);
                notifyItemChanged(position);
//
//                if (adapterListener != null) {
//                    adapterListener.onToggleItem(item);
//                }

                if (adapterListener != null) {
                    adapterListener.onDataSetChanged();
                }
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }
    }

    public List<CouponRuleWrapper> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<CouponRuleWrapper> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void digest(MarketRuleBrief marketRuleBrief) {
        List<CouponRuleWrapper> couponRules = new ArrayList<>();

        if (marketRuleBrief == null) {
            setEntityList(null);
            return;
        }

        MarketRules marketRules = marketRuleBrief.getMarketRules();
        if (marketRules == null) {
            setEntityList(null);
            return;
        }

        List<Long> ruleIds = marketRuleBrief.getRuleIds();
        List<Long> coponIds = marketRuleBrief.getCouponIds();
        if (ruleIds == null) {
            ruleIds = new ArrayList<>();
        }
        if (coponIds == null) {
            coponIds = new ArrayList<>();
        }

        List<RuleBean> ruleBeans = marketRules.getRuleBeans();
        if (ruleBeans != null && ruleBeans.size() > 0) {
            for (RuleBean rule : ruleBeans) {
                mergeRule(couponRules, rule, ruleIds.contains(rule.getId()));
            }
        }

        List<CoupBean> coupBeans = marketRules.getCoupBeans();
        if (coupBeans != null && coupBeans.size() > 0) {
            for (CoupBean coup : coupBeans) {
                mergeCoupon(couponRules, coup, coponIds.contains(coup.getMyCouponsId()));
            }
        }


        setEntityList(couponRules);
    }

    private void mergeRule(List<CouponRuleWrapper> source, RuleBean rule,
                           boolean isSelected) {
        CouponRuleWrapper entity = new CouponRuleWrapper();
        entity.setId(rule.getId());
        entity.setType(CouponRuleWrapper.TYPE_RULE);
        entity.setTitle(rule.getTitle());
        entity.setSubTitle(String.format("%s/%s",
                rule.getExecTypeCaption(), rule.getPlanTypeCaption()));
        entity.setDiscount(rule.getExecNum());
        entity.setRuleExecType(rule.getExecType());
        entity.setSelected(false);
        entity.setSelected(isSelected);

        if (source != null && source.size() > 0) {
            for (CouponRuleWrapper couponRule : source) {
                //删除重复的记录
                if (CouponRuleWrapper.TYPE_RULE.equals(couponRule.getType()) &&
                        rule.getId().equals(couponRule.getId())) {

                    source.remove(couponRule);
                    break;
                }
            }

        }

        if (source != null) {
            source.add(entity);
        }

        ZLogger.df(JSON.toJSONString(entity));
    }

    private void mergeCoupon(List<CouponRuleWrapper> source, CoupBean coupon,
                             boolean isSelected) {
        CouponRuleWrapper entity = new CouponRuleWrapper();
        entity.setId(coupon.getId());
        entity.setType(CouponRuleWrapper.TYPE_COUPON);
        entity.setTitle(coupon.getTitle());
        entity.setSubTitle(coupon.getSubTitle());
        entity.setDiscount(coupon.getDiscount());
        entity.setCouponsId(coupon.getMyCouponsId());
        entity.setSelected(false);
        entity.setSelected(isSelected);

        if (source != null && source.size() > 0) {
            for (CouponRuleWrapper couponRule : source) {
                //删除重复的记录
                if (CouponRuleWrapper.TYPE_COUPON.equals(couponRule.getType()) &&
                        coupon.getId().equals(couponRule.getId()) &&
                        coupon.getMyCouponsId().equals(couponRule.getCouponsId())) {

                    source.remove(couponRule);
                    break;
                }
            }
        }


        if (source != null) {
            source.add(entity);
        }
        ZLogger.df(JSON.toJSONString(entity));
    }


    /**
     * 按订单拆分，获取选中的优惠券
     */
    public List<CouponRuleWrapper> retrieveSelectedCouponRule() {
        List<CouponRuleWrapper> selectCoupons = new ArrayList<>();

        if (entityList != null && entityList.size() > 0) {
            for (CouponRuleWrapper couponRule : entityList) {
                if (couponRule.isSelected()) {
                    selectCoupons.add(couponRule);
                }
            }
        }

        return selectCoupons;
    }

}
