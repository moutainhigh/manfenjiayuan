package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.enjoycity.AppHelper;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.ShopProductBean;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.ui.dialog.SelectTimeDialog;
import com.mfh.enjoycity.utils.OrderHelper;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.enjoycity.utils.TextViewUtil;
import com.mfh.enjoycity.view.SimpleLabel;
import com.mfh.framework.login.logic.MfhLoginService;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 下单
 * Created by Nat.ZZN on 15/8/5.
 */
public class CreateOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public enum ITEM_TYPE {
        ITEM_TYPE_ADDRESS,
        ITEM_TYPE_ORDER_SHOP,
        ITEM_TYPE_ORDER_DELIVER,
        ITEM_TYPE_SETTLE
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<ShopProductBean> mList;

    private int addrVcount = 1, deliverVcount = 1, settleVcount = 1;

    private DisplayImageOptions options;

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public CreateOrderAdapter(Context context, List<ShopProductBean> messageList) {
        mList = messageList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.img_default)
                .showImageForEmptyUri(R.mipmap.img_default)
                .showImageOnFail(R.mipmap.img_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_ADDRESS.ordinal()) {
            return new AddressViewHolder(mLayoutInflater.inflate(R.layout.view_address_item, parent, false));
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_ORDER_SHOP.ordinal()) {
            return new ShopViewHolder(mLayoutInflater.inflate(R.layout.view_createorder_shop, parent, false));
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_ORDER_DELIVER.ordinal()) {
            return new DeliverViewHolder(mLayoutInflater.inflate(R.layout.view_createorder_deliver, parent, false));
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_SETTLE.ordinal()) {
            return new SettleViewHolder(mLayoutInflater.inflate(R.layout.view_createorder_settle, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_ADDRESS.ordinal()) {
            if (MfhLoginService.get().haveLogined()) {
                ((AddressViewHolder) holder).ivMarker.setImageResource(R.mipmap.icon_address_gray);
                ((AddressViewHolder) holder).tvName.setText(ShopcartHelper.getInstance().getReceiver());
                ((AddressViewHolder) holder).tvTelephone.setText(ShopcartHelper.getInstance().getTelephone());

                ((AddressViewHolder) holder).tvAddress.setText(ShopcartHelper.getInstance().getSubName());
            } else {
                ((AddressViewHolder) holder).ivMarker.setImageResource(R.drawable.ic_shop);
                ((AddressViewHolder) holder).tvName.setText(ShopcartHelper.getInstance().getSubName());
                ((AddressViewHolder) holder).tvTelephone.setText("");

                ((AddressViewHolder) holder).tvAddress.setText(ShopcartHelper.getInstance().getAddrName());
            }

            ((AddressViewHolder) holder).ivArrow.setVisibility(View.INVISIBLE);
        } else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_ORDER_SHOP.ordinal()) {
            ShopProductBean data = mList.get(position - addrVcount);

            ShopEntity shopEntity = ShopService.get().getEntityById(data.getShopId());
            if (shopEntity != null) {
                ((ShopViewHolder) holder).tvShopName.setText(shopEntity.getShopName());

                if (AppHelper.IMAGE_LOAD_MOD_UINIVERSAL) {
                    ImageLoader.getInstance()
                            .displayImage(shopEntity.getShopLogoUrl(), ((ShopViewHolder) holder).ivShopIcon, options, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String imageUri, View view) {
                                }

                                @Override
                                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                }

                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                }
                            }, new ImageLoadingProgressListener() {
                                @Override
                                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                }
                            });
                } else {
                    Glide.with(mContext).load(shopEntity.getShopLogoUrl())
                            .error(R.mipmap.img_default).into(((ShopViewHolder) holder).ivShopIcon);
                }
            }

            ((ShopViewHolder) holder).llLess.removeAllViews();
            ((ShopViewHolder) holder).llMore.removeAllViews();
            List<ShoppingCartEntity> productList = data.getEntityList();
            if (productList != null && productList.size() > 0) {

                int productCount = data.getTotalProductCount();
                ((ShopViewHolder) holder).tvTotalDescription.setText(String.format("共%d件商品", productCount));
                ((ShopViewHolder) holder).llLess.setVisibility(View.VISIBLE);

                if (productCount > 3) {
                    ((ShopViewHolder) holder).llMore.setVisibility(View.GONE);
                    ((ShopViewHolder) holder).llToggle.setVisibility(View.VISIBLE);
                    ((ShopViewHolder) holder).tvToggle.setText("展开");
                    ((ShopViewHolder) holder).tvTotalDescription.setVisibility(View.VISIBLE);
                } else {
                    ((ShopViewHolder) holder).llMore.setVisibility(View.GONE);
                    ((ShopViewHolder) holder).llToggle.setVisibility(View.GONE);
//                    ((OrderProductViewHolder)holder).btnToggle.setVisibility(View.GONE);
                }
                for (int i = 0; i < productCount; i++) {
                    ShoppingCartEntity entity = productList.get(i);
                    View productView = View.inflate(mContext, R.layout.view_createorder_product_item, null);
                    ((TextView) productView.findViewById(R.id.tv_product_name)).setText(entity.getProductName());
                    ((TextView) productView.findViewById(R.id.tv_product_price)).setText(String.format("￥ %.2f", entity.getProductPrice()));
                    ((TextView) productView.findViewById(R.id.tv_product_count)).setText(String.format("X %d", entity.getProductCount()));
                    ((TextView) productView.findViewById(R.id.tv_product_description)).setText(entity.getDescription());
                    if (i < 3) {
                        ((ShopViewHolder) holder).llLess.addView(productView);
                    } else {
                        ((ShopViewHolder) holder).llMore.addView(productView);
                    }
                }
            }
        } else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_ORDER_DELIVER.ordinal()) {
            ((DeliverViewHolder) holder).tvTime.setText(OrderHelper.getInstance().getDisplayDeliverTime());
        } else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_SETTLE.ordinal()) {
            ((SettleViewHolder) holder).slbTotalAmount.setTvDetailForPrice(OrderHelper.getInstance().getProductTotalAmount(), R.color.green_normal, R.color.red);
            ((SettleViewHolder) holder).slbDistribute.setTvDetailForPrice(OrderHelper.getInstance().getDeliverAmount(), R.color.green_normal, R.color.red);
            ((SettleViewHolder) holder).slbDiscountCoupon.setTvDetailForPrice(0, R.color.green_normal, R.color.red);
            ((SettleViewHolder) holder).slbMemberDiscont.setTvDetailForPrice(0, R.color.green_normal, R.color.red);

            TextViewUtil.showPrice(((SettleViewHolder) holder).tvTotalAmount,
                    OrderHelper.getInstance().getOrderTotalAmount(),
                    ContextCompat.getColor(mContext, R.color.green_normal),
                    ContextCompat.getColor(mContext, R.color.red));
//            TextViewUtil.showPrice(((SettleViewHolder)holder).tvTotalAmount, OrderHelper.getInstance().getpTotalPrice(), R.color.green_normal , R.color.red);
        }
    }

    @Override
    public int getItemCount() {
        return addrVcount + (mList == null ? 0 : mList.size()) + deliverVcount + settleVcount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE.ITEM_TYPE_ADDRESS.ordinal();
        }
        if (position == getItemCount() - 2) {
            return ITEM_TYPE.ITEM_TYPE_ORDER_DELIVER.ordinal();
        }
        if (position == getItemCount() - 1) {
            return ITEM_TYPE.ITEM_TYPE_SETTLE.ordinal();
        }
        return ITEM_TYPE.ITEM_TYPE_ORDER_SHOP.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    public void addData(int position, ShopProductBean data) {
        mList.add(position, data);
        notifyItemInserted(position);
    }

    public List<ShopProductBean> getData() {
        return mList;
    }

    /**
     * 收货地址
     */
    public class AddressViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_marker)
        ImageView ivMarker;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.tv_telephone)
        TextView tvTelephone;

        @BindView(R.id.iv_arrow)
        ImageView ivArrow;

        public AddressViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                }
//            });
        }
    }

    /**
     * 店铺订单详情
     */
    public class ShopViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_shop_icon)
        ImageView ivShopIcon;
        @BindView(R.id.tv_shop_name)
        TextView tvShopName;
        @BindView(R.id.ll_less)
        LinearLayout llLess;
        @BindView(R.id.ll_more)
        LinearLayout llMore;
        @BindView(R.id.ll_toggle)
        LinearLayout llToggle;
        @BindView(R.id.tv_total_description)
        TextView tvTotalDescription;
        @BindView(R.id.tv_toggle)
        TextView tvToggle;
        @BindView(R.id.iv_arrow)
        ImageView ivArrow;

        public ShopViewHolder(final View itemView) {
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

        @OnClick(R.id.ll_toggle)
        public void toggle() {
            //TODO,选择时间
            if (llMore.getVisibility() == View.VISIBLE) {
                llMore.setVisibility(View.GONE);
                tvToggle.setText("展开");
                ivArrow.setImageResource(R.drawable.icon_arrow_down);
//                btnToggle.setCompoundDrawables(null, null, mContext.getDrawable(R.drawable.icon_arrow_down), null);
                tvTotalDescription.setVisibility(View.VISIBLE);
            } else {
                llMore.setVisibility(View.VISIBLE);
                tvToggle.setText("收起");
                ivArrow.setImageResource(R.drawable.icon_arrow_up);
//                tvToggle.setCompoundDrawables(null, null, mContext.getDrawable(R.drawable.icon_arrow_right), null);
                tvTotalDescription.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 送货时间
     */
    public class DeliverViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_time)
        TextView tvTime;

        public DeliverViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectTimeDialog dialog = new SelectTimeDialog(mContext);
                    dialog.setResponseCallback(new SelectTimeDialog.OnResponseCallback() {
                        @Override
                        public void onSelectTime(String text) {
                            OrderHelper.getInstance().setDeliverTime(text);

                            tvTime.setText(OrderHelper.getInstance().getDisplayDeliverTime());
                        }
                    });
                    dialog.show();
                }
            });
        }
    }

    /**
     * 结算
     */
    public class SettleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.labelTotalAmount)
        SimpleLabel slbTotalAmount;
        @BindView(R.id.labelDistribute)
        SimpleLabel slbDistribute;
        @BindView(R.id.labelDiscountCoupon)
        SimpleLabel slbDiscountCoupon;
        @BindView(R.id.labelMemberDiscount)
        SimpleLabel slbMemberDiscont;
        @BindView(R.id.tv_total_amount)
        TextView tvTotalAmount;

        public SettleViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            slbTotalAmount.setTvTitle("商品总价");
            slbDistribute.setTvTitle("配送费");
            slbDiscountCoupon.setTvTitle("优惠券");
            slbMemberDiscont.setTvTitle("会员折扣");
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });

//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    if (adapterListener != null) {
//                        adapterListener.onItemLongClick(itemView, getPosition());
//                    }
//                    return false;
//                }
//            });

        }
    }

//    public double getTotalPrice(){
//        double totalPrice = 0;
//        if (mList != null && mList.size() > 0){
//            for(ShopProductBean entity : mList){
//                totalPrice += entity.getTotalAmount();
//            }
//        }
//        return totalPrice;
//    }


}
