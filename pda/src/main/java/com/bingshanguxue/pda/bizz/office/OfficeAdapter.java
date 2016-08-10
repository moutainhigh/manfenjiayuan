package com.bingshanguxue.pda.bizz.office;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.mfh.framework.login.entity.Office;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class OfficeAdapter extends RegularAdapter<Office, OfficeAdapter.AddressViewHolder> {

    public OfficeAdapter(Context context, List<Office> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }



    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddressViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_office,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final AddressViewHolder holder, final int position) {
        final Office entity = entityList.get(position);

        holder.tvName.setText(String.format("%d--%s",
                entity.getCode(), entity.getValue()));
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {

//        @Bind(R.id.tv_name)
        TextView tvName;

        public AddressViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null){
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

}
