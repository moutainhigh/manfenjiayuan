package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.mfh.litecashier.R;

/**
 * Emoji
 * Created by kun on 15/12/23.
 */
public class MaterialEmojiGridAdapter extends BaseAdapter {

    private final int PAGE_SIZE = 20;

    public interface OnEmojiClickListener{
        void onItemSelected(int id);
    }

    public void setOnEmojiClickListener(OnEmojiClickListener emojiClickListener) {
        this.emojiClickListener = emojiClickListener;
    }
    private OnEmojiClickListener emojiClickListener;


    class ViewHolder {
        ImageView ivEmoji;
    }


    private Context context;
    private int page;

    public MaterialEmojiGridAdapter(Context context, int page) {
        this.context = context;
        this.page = page;
    }

    @Override
    public int getCount() {
        return 21;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(context, R.layout.view_emoji_item, null);

            viewHolder = new ViewHolder();
            viewHolder.ivEmoji = (ImageView) view.findViewById(R.id.iv_emoji);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        int id;
        if (i != PAGE_SIZE) {
            id = context.getResources().getIdentifier("smiley_" + (page * PAGE_SIZE + i), "drawable", context.getApplicationInfo().packageName);
            viewHolder.ivEmoji.setImageResource(id);
        }
        else {
            id = PAGE_SIZE;
//                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.ivEmoji.getLayoutParams();
//                params.width = DensityUtil.dip2px(context, 25);
//                params.height = DensityUtil.dip2px(context,20);
//                viewHolder.ivEmoji.setLayoutParams(params);
            viewHolder.ivEmoji.setImageResource(R.mipmap.ic_emoji_del);
        }
        final int finalId = id;
        viewHolder.ivEmoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emojiClickListener != null){
                    emojiClickListener.onItemSelected(finalId);
                }
            }
        });
        return view;
    }
}
