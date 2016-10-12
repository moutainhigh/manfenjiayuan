package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.api.anon.storeRack.StoreRackCard;
import com.mfh.framework.api.anon.storeRack.StoreRackCardItem;

import java.util.List;

import me.drakeet.multitype.ItemViewProvider;


/**
 * 水平按钮
 * Created by bingshanguxue on 09/10/2016.
 */

public class Card2ViewProvider extends ItemViewProvider<StoreRackCard,
        Card2ViewProvider.ViewHolder> {

    @NonNull @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.itemview_card2, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull StoreRackCard card) {
        List<StoreRackCardItem> items = card.getItems();
//        ZLogger.d(String.format("共有%s个元素个元素\"", items != null ? items.size() : 0));
        holder.setEntityList(items);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView recyclerView;
        private Card2ViewAdapter mAdapter;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.horizontal_list);

            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recyclerView.setLayoutManager(layoutManager);

            mAdapter = new Card2ViewAdapter(AppContext.getAppContext(), null);
            recyclerView.setAdapter(mAdapter);
        }

        private void setEntityList(List<StoreRackCardItem> entityList){
            mAdapter.setEntityList(entityList);
        }
    }
}
