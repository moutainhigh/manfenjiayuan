package com.bingshanguxue.pda.bizz.invcheck;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 收银－－服务菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ShelfnumberAdapter extends RegularAdapter<Shelfnumber, ShelfnumberAdapter.MenuOptioinViewHolder> {

    public ShelfnumberAdapter(Context context, List<Shelfnumber> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);
    }

    private AdapterListener adapterListener;
    private Shelfnumber curShelfnumber;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_shelfnumber, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final Shelfnumber entity = entityList.get(position);

        holder.tvNumber.setText(String.valueOf(entity.getNumber()));
    }

    @Override
    public void onViewRecycled(MenuOptioinViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void setEntityList(List<Shelfnumber> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        this.curShelfnumber = null;
        this.notifyDataSetChanged();
    }


    public Shelfnumber getCurShelfnumber() {
        return curShelfnumber;
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.tv_number)
        TextView tvNumber;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvNumber = (TextView) itemView.findViewById(R.id.tv_number);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    curShelfnumber = entityList.get(position);
                    if (adapterListener != null) {
                        adapterListener.onItemClick(v, position);
                    }
                }
            });

        }
    }

}
