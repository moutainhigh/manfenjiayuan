package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bingshanguxue.cashier.model.CoupBean;
import com.bingshanguxue.cashier.model.RuleBean;
import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DrawableUtils;
import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 素材－－优惠券
 * Created by Nat.ZZN on 15/8/5.
 */
public class MaterialCouponAdapter
        extends RegularAdapter<CouponRule, MaterialCouponAdapter.CouponViewHolder> {

    public MaterialCouponAdapter(Context context, List<CouponRule> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onDataSetChanged();

        void onToggleItem(CouponRule couponRule);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public CouponViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CouponViewHolder(mLayoutInflater.inflate(R.layout.itemview_material_coupon_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final CouponViewHolder holder, final int position) {
        CouponRule entity = entityList.get(position);

        Drawable rootViewBackgroundDrawable = holder.rootView.getBackground().mutate();
        if (position == 0){
            holder.rootView.setBackground(DrawableUtils.tintDrawable(mContext,
                    rootViewBackgroundDrawable, R.color.material_green_500, true));
        }
        else if (position == 1){
            holder.rootView.setBackground(DrawableUtils.tintDrawable(mContext,
                    rootViewBackgroundDrawable, R.color.material_orange_500, true));
        }
        else{
            holder.rootView.setBackground(DrawableUtils.tintDrawable(mContext,
                    rootViewBackgroundDrawable, R.color.material_red_500, true));
        }

        //"¥%.2f",优惠券金额不会有小数
        holder.tvAmount.setText(String.format("%.0f", entity.getDiscount()));
        holder.tvTitle.setText(entity.getTitle());
        holder.tvDescription.setText(entity.getSubTitle());
        holder.tvValidDate.setText("");

        if (ObjectsCompact.equals(entity.getType(), CouponRule.TYPE_COUPON)){
            if (entity.isSelected()) {
//                holder.ibRatio.setSelected(true);
                holder.ibRatio.setVisibility(View.VISIBLE);
            } else {
//                holder.ibRatio.setSelected(false);
                holder.ibRatio.setVisibility(View.GONE);
            }
        }
        else{
            holder.ibRatio.setVisibility(View.GONE);
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

    @Override
    public void setEntityList(List<CouponRule> entityList) {
        super.setEntityList(entityList);

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
                entity.setSelected(false);

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
                entity.setSelected(false);

                this.entityList.add(entity);
            }
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * format like: 233,234
     */
    public String getSelectCouponIds() {
        StringBuilder sb = new StringBuilder();
        if (entityList != null && entityList.size() > 0) {
            int len = entityList.size();
            for (int i = 0; i < len; i++) {
                CouponRule coupon = entityList.get(i);
                if (ObjectsCompact.equals(coupon.getType(), CouponRule.TYPE_RULE)
                        || !coupon.isSelected()) {
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


}
