package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mfh.enjoycity.AppHelper;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.CategoryMenuBean;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/17.
 */
public class CategoryGridAdapter extends BaseAdapter {
    private Context context;//用于接收传递过来的Context对象
    private List<CategoryMenuBean> data = new ArrayList<>();

    private DisplayImageOptions options;
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

//    public interface FunctionGridAdapterListener{
//        public void onSelectFunction(int index);
//    }
//    private FunctionGridAdapterListener listener;
//    public void registerLister(FunctionGridAdapterListener listener){
//        this.listener = listener;
//    }

    public CategoryGridAdapter(Context context) {
//        super();
//        this.context = context;


        this(context, null);
    }

    public CategoryGridAdapter(Context context, List<CategoryMenuBean> data) {
        super();
        this.context = context;
        this.data = data;

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.mipmap.img_default)
                .showImageForEmptyUri(R.mipmap.img_default)
                .showImageOnFail(R.mipmap.img_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
//                .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                .build();
    }


    static class ViewHolder {
        @Bind(R.id.iv_menu)
        ImageView ivMenu;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(context, R.layout.view_item_home_category_menuitem, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
            //设置边框效果
//            view.setBackgroundResource(R.drawable.gridview_item_background);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        CategoryMenuBean bean = data.get(i);
        if (AppHelper.IMAGE_LOAD_MOD_UINIVERSAL){
            ImageLoader.getInstance().displayImage(bean.getImageUrl(), viewHolder.ivMenu, options, animateFirstListener);
        }else{
            Glide.with(context).load(bean.getImageUrl())
                    .error(R.mipmap.img_default).into(viewHolder.ivMenu);
        }

//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(listener != null){
//                    listener.onSelectFunction(i);
//                }
//            }
//        });

        return view;
    }

    public void setData(List<CategoryMenuBean> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }


    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 300);
                    displayedImages.add(imageUri);
                }
            }
        }
    }


}
