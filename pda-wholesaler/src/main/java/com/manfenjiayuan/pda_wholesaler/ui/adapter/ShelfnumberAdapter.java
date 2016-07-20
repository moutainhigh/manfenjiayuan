package com.manfenjiayuan.pda_wholesaler.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.bean.Shelfnumber;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 收银－－服务菜单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ShelfnumberAdapter extends RecyclerView.Adapter<ShelfnumberAdapter.MenuOptioinViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<Shelfnumber> entityList;

    private Shelfnumber curShelfnumber;

    public interface AdapterListener {
        void onItemClick(View view, int position);
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public ShelfnumberAdapter(Context context, List<Shelfnumber> entityList) {
        this.entityList = entityList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
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
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    @Override
    public void onViewRecycled(MenuOptioinViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public void setEntityList(List<Shelfnumber> entityList) {
        this.entityList = entityList;
        this.curShelfnumber = null;
        this.notifyDataSetChanged();
    }

    public List<Shelfnumber> getEntityList() {
        return entityList;
    }

    public Shelfnumber getCurShelfnumber() {
        return curShelfnumber;
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_number)
        TextView tvNumber;

        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

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
