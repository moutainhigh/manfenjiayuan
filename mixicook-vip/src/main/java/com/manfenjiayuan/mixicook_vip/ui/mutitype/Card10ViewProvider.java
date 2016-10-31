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
import com.mfh.framework.api.anon.storeRack.StoreRackCardItem;

import java.util.List;

import me.drakeet.multitype.ItemViewProvider;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;


/**
 * 单张图片
 * Created by bingshanguxue on 09/10/2016.
 */

public class Card10ViewProvider extends ItemViewProvider<Card10,
        Card10ViewProvider.ViewHolder> {

    @NonNull @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.itemview_card10, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Card10 card) {
        List<StoreRackCardItem> items = card.getItems();
//        ZLogger.d(String.format("共有%s个元素个元素\"", items != null ? items.size() : 0));
        if (items != null && items.size() > 0){
            holder.setCardItem(items.get(0));
        }
        else{
            holder.setCardItem(null);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private StoreRackCardItem mCardItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageview);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCardItem != null){
                        ActivityRoute.redirect2Url2(AppContext.getAppContext(), mCardItem.getLink());
                    }
                }
            });
        }

        private void setCardItem(StoreRackCardItem cardItem){
            mCardItem = cardItem;
            Glide.with(context)
                    .load(mCardItem != null ? mCardItem.getImageUrl() : "")
                    .error(R.mipmap.ic_image_error).into(mImageView);
        }
    }
}
