package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.mfh.framework.api.anon.sc.storeRack.StoreRackCardItem;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 *
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class Card2ViewAdapter extends RegularAdapter<StoreRackCardItem, Card2ViewAdapter.MenuOptioinViewHolder> {

    public Card2ViewAdapter(Context context, List<StoreRackCardItem> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(
                R.layout.itemview_card2_adapter, null, false);

       return new MenuOptioinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuOptioinViewHolder holder, int position) {
        final StoreRackCardItem bean = entityList.get(position);
//        ZLogger.d(String.format("position=%d, imageUrl=%s", position, bean.getImageUrl()));

        Glide.with(AppContext.getAppContext()).load(bean.getImageUrl())
                .error(R.mipmap.ic_image_error).into(holder.ivHeader);
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    StoreRackCardItem cardItem = getEntity(position);
                    if (cardItem != null){
                        ActivityRoute.redirect2Url2(AppContext.getAppContext(), cardItem.getLink());
                    }
//                    if (entityList == null || position < 0 || position >= entityList.size()) {
////                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
//
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(v, position);
//                    }
                }
            });
        }
    }

    @Override
    public void setEntityList(List<StoreRackCardItem> entityList) {
        super.setEntityList(entityList);
//        ZLogger.d(String.format("共有%s个元素个元素\"", entityList != null ? entityList.size() : 0));
    }
}
