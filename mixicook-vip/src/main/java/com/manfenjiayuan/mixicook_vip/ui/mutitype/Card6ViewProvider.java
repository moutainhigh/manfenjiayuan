package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.mfh.framework.api.anon.sc.storeRack.StoreRackCardItem;

import java.util.List;

import me.drakeet.multitype.ItemViewProvider;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;


/**
 * 促销卡片
 * Created by bingshanguxue on 09/10/2016.
 */

public class Card6ViewProvider extends ItemViewProvider<Card6,
        Card6ViewProvider.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.itemview_card6, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Card6 card) {
        List<StoreRackCardItem> items = card.getItems();
//        ZLogger.d(String.format("共有%s个元素个元素\"", items != null ? items.size() : 0));
        holder.setCardItems(items);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivLeft;
        private ImageView ivTop;
        private ImageView ivBottomLeft;
        private ImageView ivBottomRight;

        private List<StoreRackCardItem> mCardItems;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLeft = (ImageView) itemView.findViewById(R.id.iv_left);
            ivTop = (ImageView) itemView.findViewById(R.id.iv_top);
            ivBottomLeft = (ImageView) itemView.findViewById(R.id.iv_bottom_left);
            ivBottomRight = (ImageView) itemView.findViewById(R.id.iv_bottom_right);

            ivLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirect2(0);
                }
            });
            ivTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirect2(1);
                }
            });
            ivBottomLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirect2(2);
                }
            });
            ivBottomRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    redirect2(3);
                }
            });
        }


        private void redirect2(int position) {
            if (mCardItems == null || position < 0 || position >= mCardItems.size()) {
                return;
            }
            StoreRackCardItem cardItem = mCardItems.get(position);
            if (cardItem != null) {
                ActivityRoute.redirect2Url2(AppContext.getAppContext(), cardItem.getLink());
            }
        }

        private void setCardItems(List<StoreRackCardItem> cardItems) {
            mCardItems = cardItems;

            if (mCardItems == null){
                return;
            }
            for (int i = 0 ; i < mCardItems.size() ; i++){
                StoreRackCardItem cardItem = mCardItems.get(i);
                if (cardItem == null){
                    continue;
                }
                if (i == 0){
                    Glide.with(context)
                            .load(cardItem.getImageUrl())
                            .error(R.mipmap.ic_image_error).into(ivLeft);
                }
                else if (i == 1){
                    Glide.with(context)
                            .load(cardItem.getImageUrl())
                            .error(R.mipmap.ic_image_error).into(ivTop);
                }
                else if (i == 2){
                    Glide.with(context)
                            .load(cardItem.getImageUrl())
                            .error(R.mipmap.ic_image_error).into(ivBottomLeft);
                }
                else if (i == 3){
                    Glide.with(context)
                            .load(cardItem.getImageUrl())
                            .error(R.mipmap.ic_image_error).into(ivBottomRight);
                }
            }

        }
    }
}
