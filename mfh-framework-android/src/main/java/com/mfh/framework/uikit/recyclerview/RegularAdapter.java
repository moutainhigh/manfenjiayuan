package com.mfh.framework.uikit.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>适配器</h1>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public abstract class RegularAdapter<D, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    protected final LayoutInflater mLayoutInflater;
    protected Context mContext;
    protected List<D> entityList;

    public RegularAdapter(Context context, List<D> entityList) {
        this.entityList = entityList;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    @Override
    public void onViewRecycled(VH holder) {
        super.onViewRecycled(holder);
    }


    public List<D> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<D> entityList) {
        this.entityList = entityList;
        notifyDataSetChanged();
    }

    public void appendEntityList(List<D> entityList){
        if (entityList == null){
            return;
        }

        if (this.entityList == null){
            this.entityList = new ArrayList<>();
        }

        this.entityList.addAll(entityList);
        notifyDataSetChanged();

    }
	
    /**
     * 删除列表项
     */
    public void removeEntity(int position) {
        if (entityList == null || position < 0 || position >= entityList.size()) {
//          ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
            return;
        }

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);
    }

    public D getEntity(int position) {
        if (this.entityList == null || position < 0 || position >= entityList.size()){
            return null;
        }

        return entityList.get(position);
    }
}
