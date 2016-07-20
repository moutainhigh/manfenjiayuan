package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.ProductDetail;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/4/17.
 */
public class ProductGridAdapter extends BaseAdapter {
    private Context context;//用于接收传递过来的Context对象
    private List<ProductDetail> data = new ArrayList<>();

//    public interface FunctionGridAdapterListener{
//        public void onSelectFunction(int index);
//    }
//    private FunctionGridAdapterListener listener;
//    public void registerLister(FunctionGridAdapterListener listener){
//        this.listener = listener;
//    }

    public ProductGridAdapter(Context context) {
        super();
        this.context = context;
    }

    public ProductGridAdapter(Context context, List<ProductDetail> data) {
        super();
        this.context = context;
        this.data = data;
    }


    static class ViewHolder {
        @Bind(R.id.iv_product)
        ImageView ivProduct;
        @Bind(R.id.iv_promote_label)
        ImageView ivPromoteLabel;
        @Bind(R.id.tv_discount) TextView tvDiscount;
        @Bind(R.id.tv_product_name) TextView tvProductName;
        @Bind(R.id.tv_product_price) TextView tvProductPrice;
        @Bind(R.id.ib_shopcart)
        ImageButton ibShopcart;

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
            view = View.inflate(context, R.layout.view_productcard, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
            //设置边框效果
//            view.setBackgroundResource(R.drawable.gridview_item_background);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        ProductDetail cell = data.get(i);

        Glide.with(context).load(cell.getThumbnail())
                .error(R.mipmap.img_default).into(viewHolder.ivProduct);

        viewHolder.tvProductName.setText(cell.getProduct().getName());
//        viewHolder.tvProductPrice.setText(String.format("￥ %.2f", cell.getCostPrice()));
        viewHolder.tvProductPrice.setText(String.format("￥ %s", cell.getCostPrice()));

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

    public void setData(List<ProductDetail> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }



}
