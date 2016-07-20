package com.mfh.framework.uikit.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


/**
 * <h1>自定义RecyclerView</h1>
 * <h2>1.支持空视图显示</h2>
 * <h2>2.支持弹性</h2>
 * */
public class RecyclerViewEmptySupport extends RecyclerView {
    private Context mContext;
    private View emptyView;
    private View loadingView;
    private int mMaxOverDistance;

    private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            showEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            showEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            showEmptyView();
        }
    };


    public RecyclerViewEmptySupport(Context context) {
        super(context);
        this.mContext = context;
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }


//    public void init(int maxOverDistance){
//        this.mMaxOverDistance = maxOverDistance;
//
//        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
//        float density = metrics.density;
//        mMaxOverDistance = (int)(density * mMaxOverDistance);
//    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if(adapter!=null){
            adapter.registerAdapterDataObserver(observer);
            observer.onChanged();
        }
    }

//    @Override
//    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
//        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxOverDistance, isTouchEvent);
//    }

    public void setEmptyView(View v){
        emptyView = v;
    }

    public void showEmptyView(){
        Adapter<?> adapter = getAdapter();
        if(adapter!=null && emptyView!=null){
            if(adapter.getItemCount()==0){
                emptyView.setVisibility(View.VISIBLE);
                RecyclerViewEmptySupport.this.setVisibility(View.GONE);
            }
            else{
                emptyView.setVisibility(View.GONE);
                RecyclerViewEmptySupport.this.setVisibility(View.VISIBLE);
            }
        }
    }

}
