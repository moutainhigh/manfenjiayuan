package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;

import java.util.List;

import me.drakeet.multitype.ItemViewProvider;


/**
 * 滚屏广告
 * Created by bingshanguxue on 09/10/2016.
 */

public class Card1ViewProvider extends ItemViewProvider<Card1,
        Card1ViewProvider.ViewHolder> {

    @NonNull @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.itemview_card1, parent, false);
        return new ViewHolder(root);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Card1 card) {
        List<Card1Item> items = card.getItems();
//        ZLogger.d(String.format("共有%s个元素", items != null ? items.size() : 0));
        holder.setEntityList(items);

    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        private AdvertisementViewPager advViewpager;
        private Card1ViewAdapter mBannerAdapter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.advViewpager = (AdvertisementViewPager) itemView.findViewById(R.id.adv_viewpager);
            mBannerAdapter = new Card1ViewAdapter(AppContext.getAppContext(), null, advViewpager,
                    new Card1ViewAdapter.OnBannerAdapterCallback() {
                        @Override
                        public void onRedirectTo(String url) {
                            ActivityRoute.redirect2Url2(AppContext.getAppContext(), url);
                        }
                    });

            advViewpager.setAdapter(mBannerAdapter);

            //TODO,定时切换(每隔5秒切换一次)
            advViewpager.startSlide(3 * 1000);
        }

        private void setEntityList(List<Card1Item> entityList){
            mBannerAdapter.setEntityList(entityList);
        }
    }
}
