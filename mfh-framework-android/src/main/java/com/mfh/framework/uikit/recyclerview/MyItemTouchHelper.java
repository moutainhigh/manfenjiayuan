package com.mfh.framework.uikit.recyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * 处理拖拽和滑动删除
 */
public class MyItemTouchHelper extends ItemTouchHelper.Callback {
    private ItemTouchHelperAdapter callback;

    public interface ItemTouchHelperAdapter {
        void onItemMoved(int fromPosition, int toPosition);

        void onItemRemoved(int position);
    }

    public MyItemTouchHelper(ItemTouchHelperAdapter ad) {
        callback = ad;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //设置拖拽标志
        int dragFlgs;
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            dragFlgs = ItemTouchHelper.UP |
                    ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT |
                    ItemTouchHelper.RIGHT;
        }
        else{
            dragFlgs = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        }
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;//左右滑动

        return makeMovementFlags(dragFlgs, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        callback.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        callback.onItemRemoved(viewHolder.getAdapterPosition());
    }

//    /**
//     * 长按选中item（拖拽开始时）调用
//     * */
//    @Override
//    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE){
//            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
//        }
//        super.onSelectedChanged(viewHolder, actionState);
//    }
//
//    /**
//     * 手指松开（拖拽完成时）调用
//     * */
//    @Override
//    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        super.clearView(recyclerView, viewHolder);
//
//        viewHolder.itemView.setBackgroundColor(0);
//    }
    //    @SuppressWarnings("deprecation")
//    @Override
//    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
//            View itemView = viewHolder.itemView;
//            Paint p = new Paint();
//
//
//            MainActivity.BasicListAdapter.ViewHolder vh= (MainActivity.BasicListAdapter.ViewHolder)viewHolder;
//            p.setColor(recyclerView.getResources().getColor(R.color.primary_light));
//
//            if(dX > 0){
//                c.drawRect((float)itemView.getLeft(), (float)itemView.getTop(), dX, (float)itemView.getBottom(), p);
//                String toWrite = "Left"+itemView.getLeft()+" Top "+itemView.getTop()+" Right "+dX+" Bottom "+itemView.getBottom();
////                Log.d("OskarSchindler", toWrite);
//            }
//            else{
//                String toWrite = "Left"+(itemView.getLeft()+dX)+" Top "+itemView.getTop()+" Right "+dX+" Bottom "+itemView.getBottom();
////                Log.d("OskarSchindler", toWrite);
//                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);
//            }
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        }
//    }
}
