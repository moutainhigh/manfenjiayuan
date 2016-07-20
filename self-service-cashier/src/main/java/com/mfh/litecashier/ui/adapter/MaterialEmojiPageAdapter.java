package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.mfh.litecashier.R;

/**
 * Emoji
 * Created by kun on 15/12/23.
 */
public class MaterialEmojiPageAdapter extends PagerAdapter {

    private Context context;//用于接收传递过来的Context对象

    public interface OnEmojiClickListener{
        void onItemSelected(int id);
    }
    private OnEmojiClickListener emojiClickListener;


    public MaterialEmojiPageAdapter(Context context, OnEmojiClickListener emojiClickListener) {
        super();
        this.context = context;
        this.emojiClickListener = emojiClickListener;
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = View.inflate(context, R.layout.view_emoji_card, null);
        GridView gridView = (GridView) view.findViewById(R.id.griview_emojicard);

//            ViewHolder viewHolder;
//            if (view == null) {
//                view = View.inflate(context, R.layout.view_emoji_card, null);
//
//                viewHolder = new ViewHolder();
//                viewHolder.emojiGridView = (GridView) view.findViewById(R.id.griview_emojicard);
//                view.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) view.getTag();
//            }
        MaterialEmojiGridAdapter adapter = new MaterialEmojiGridAdapter(context, position);
        adapter.setOnEmojiClickListener(new MaterialEmojiGridAdapter.OnEmojiClickListener() {
            @Override
            public void onItemSelected(int id) {
                if (emojiClickListener != null){
                    emojiClickListener.onItemSelected(id);
                }
            }
        });
        gridView.setAdapter(adapter);

        container.addView(view);

        return view;

    }
}
