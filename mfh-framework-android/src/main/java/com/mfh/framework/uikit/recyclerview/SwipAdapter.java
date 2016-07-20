package com.mfh.framework.uikit.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.Collections;
import java.util.List;

/**
 * <h1>适配器</h1>
 * <ul>
 * <li>支持滑动删除</li>
 * </ul>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public abstract class SwipAdapter<D, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements MyItemTouchHelper.ItemTouchHelperAdapter {

    protected final LayoutInflater mLayoutInflater;
    protected Context mContext;
    protected List<D> entityList;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SwipAdapter(Context context, List<D> entityList) {
        this.entityList = entityList;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(entityList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(entityList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemRemoved(int position) {
        removeEntity(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
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
    }

    /**
     * 删除列表项
     */
    public void removeEntity(int position) {
        if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
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
