package com.mfh.litecashier.ui.fragment.goods;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bingshanguxue.vector_uikit.TextDrawable;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 字母
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class LetterAdapter
        extends RegularAdapter<Letter, LetterAdapter.MenuOptioinViewHolder> {

    public LetterAdapter(Context context, List<Letter> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuOptioinViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_letter, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final Letter entity = entityList.get(position);

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.BLACK)
                .useFont(Typeface.DEFAULT)
                .toUpperCase()
                .width(DensityUtil.dip2px(CashierApp.getAppContext(), 80))  // width in px
                .height(DensityUtil.dip2px(CashierApp.getAppContext(), 80)) // height in px
                .fontSize(DensityUtil.sp2px(CashierApp.getAppContext(), 50))/* size in px */
                .endConfig()
                .buildRect(entity.getName(), Color.TRANSPARENT);

        holder.ivHeader.setImageDrawable(drawable);

    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_header)
        ImageView ivHeader;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
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
