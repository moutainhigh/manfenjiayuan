package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.CategoryProductBean;
import com.mfh.enjoycity.ui.AllProductActivity;
import com.mfh.enjoycity.ui.CategoryTabActivity;
import com.mfh.enjoycity.ui.HotSalesActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.compound.BadgeViewButton;
import com.mfh.framework.uikit.widget.ChildGridView;

import java.util.List;

import butterknife.Bind;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Homepage
 * Created by Nat.ZZN on 15/8/5.
 */
public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_HEADERE,
        ITEM_TYPE_NAVI,
        ITEM_TYPE_DISCOUNT,
        ITEM_TYPE_CATEGORY_MENU,
        ITEM_TYPE_CATEGORY_PRODUCT
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private HomeAdapterData mShopData;
    private int headerVCount, naviVCount, discountVCount, categoryMenuVCount, categoryProductVCount;

    public interface OnAdapterListener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener)
    {
        this.adapterListener = adapterListener;
    }

    public HomeAdapter(Context context, HomeAdapterData shopData) {
        this.mShopData = shopData;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        headerVCount = 1;
        naviVCount = 1;
        discountVCount = 1;
        categoryMenuVCount = 1;
        categoryProductVCount = (mShopData.getCategoryProductBeans() == null ? 0 : mShopData.getCategoryProductBeans().size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_HEADERE.ordinal()){
            return new HeaderViewHolder(mLayoutInflater.inflate(R.layout.view_item_home_recycler_header, parent, false));
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_NAVI.ordinal()){
            return new NaviViewHolder(mLayoutInflater.inflate(R.layout.view_item_home_navi, parent, false));
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_DISCOUNT.ordinal()){
            return new DiscountViewHolder(mLayoutInflater.inflate(R.layout.view_item_home_discount, parent, false));
        }
        else if (viewType== ITEM_TYPE.ITEM_TYPE_CATEGORY_MENU.ordinal()){
            return new CategoryMenuViewHolder(mLayoutInflater.inflate(R.layout.view_item_home_category_menu, parent, false));
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_CATEGORY_PRODUCT.ordinal()){
            return new CategoryProductViewHolder(mLayoutInflater.inflate(R.layout.view_item_home_category, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ZLogger.d("onBindViewHolder.position= " + position);
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_DISCOUNT.ordinal()) {
            ((DiscountViewHolder)holder).tvTitle.setText("discount");
            ((DiscountViewHolder)holder).gridView.setAdapter(new ProductGridAdapter(mContext, mShopData.getDiscountProductBeans()));
        }
        else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_NAVI.ordinal()) {
//            ((NaviViewHolder)holder).btnHot.init(R.drawable.ic_launcher, "热卖");
//            ((NaviViewHolder)holder).btnAll.init(R.drawable.ic_launcher, "全部商品");
        }
        else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_CATEGORY_MENU.ordinal()) {
            ((CategoryMenuViewHolder) holder).gridView.setAdapter(new CategoryGridAdapter(mContext, mShopData.getCategoryMenus()));
        }
        else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_CATEGORY_PRODUCT.ordinal()) {
            List<CategoryProductBean> categoryProductBeans = mShopData.getCategoryProductBeans();
            CategoryProductBean categoryProductBean = categoryProductBeans.get(position - (headerVCount + naviVCount + discountVCount + categoryMenuVCount));

            ((CategoryProductViewHolder)holder).tvTitle.setText(categoryProductBean.getCategoryName());
            ((CategoryProductViewHolder)holder).gridView.setAdapter(new ProductGridAdapter(mContext, categoryProductBean.getProductBeans()));
        }
    }

    @Override
    public int getItemCount() {
        return headerVCount + naviVCount + discountVCount + categoryMenuVCount + categoryProductVCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return ITEM_TYPE.ITEM_TYPE_HEADERE.ordinal();
        }
        else if (position == 1){
            return ITEM_TYPE.ITEM_TYPE_NAVI.ordinal();
        }
        else if (position == 2){
            return ITEM_TYPE.ITEM_TYPE_DISCOUNT.ordinal();
        }
        else if (position == 3){
            return ITEM_TYPE.ITEM_TYPE_CATEGORY_MENU.ordinal();
        }
        else// if (position >= 2)
        {
            return ITEM_TYPE.ITEM_TYPE_CATEGORY_PRODUCT.ordinal();
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View view) {
            super(view);
        }
    }

    public class NaviViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.btn_recent)
        BadgeViewButton btnRecent;
        @BindView(R.id.btn_hot)
        BadgeViewButton btnHot;
        @BindView(R.id.btn_all)
        BadgeViewButton btnAll;

        public NaviViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
//            btnRecent.init(R.drawable.ic_launcher, "我常买");
//            btnHot.init(R.drawable.ic_launcher, "热卖");
//            btnAll.init(R.drawable.ic_launcher, "全部商品");
        }

        @OnClick(R.id.btn_recent)
        public void showRecent(){
            Bundle extras = new Bundle();
            extras.putLong(HotSalesActivity.EXTRA_KEY_SHOP_ID, mShopData.getShopId());
            HotSalesActivity.actionStart(mContext, extras);

        }
        @OnClick(R.id.btn_hot)
        public void showHot(){
            Bundle extras = new Bundle();
            extras.putLong(HotSalesActivity.EXTRA_KEY_SHOP_ID, mShopData.getShopId());
            HotSalesActivity.actionStart(mContext, extras);
        }
        @OnClick(R.id.btn_all)
         public void showAll(){
            Bundle extras = new Bundle();
            extras.putLong(AllProductActivity.EXTRA_KEY_SHOP_ID, mShopData.getShopId());
            CategoryTabActivity.actionStart(mContext, extras);
//            UIHelper.redirectToActivity(mContext, AllProductActivity.class);
        }
    }

    public class DiscountViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.button_more)
        Button btnMore;
        @BindView(R.id.grid_products)
        ChildGridView gridView;

        public DiscountViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }
    }

    public class CategoryMenuViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.grid_category)
        ChildGridView gridView;

        public CategoryMenuViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });

        }
    }

    public class CategoryProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.button_more)
        Button btnMore;
        @BindView(R.id.grid_products)
        ChildGridView gridView;

        public CategoryProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });

        }
    }

}
