package com.manfenjiayuan.mixicook_vip.ui.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.tencent.bugly.crashreport.inner.InnerAPI.context;

/**
 * 菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CloudMenuAdapter extends RegularAdapter<CloudMenu, CloudMenuAdapter.MenuOptioinViewHolder> {

    public CloudMenuAdapter(Context context, List<CloudMenu> entityList) {
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
                R.layout.itemview_cloudmenu, null, false);
//        v.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 105),
//                DensityUtil.dip2px(mContext, 122)));

//            return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_homemenu_option, parent, false));
        return new MenuOptioinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuOptioinViewHolder holder, int position) {
        final CloudMenu bean = entityList.get(position);

        Glide.with(context).load(bean.getUrl())
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
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(v, position);
                    }
                }
            });
        }
    }


}
