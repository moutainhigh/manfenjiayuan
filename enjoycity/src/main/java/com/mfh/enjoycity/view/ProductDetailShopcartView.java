package com.mfh.enjoycity.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manfenjiayuan.business.widget.NumberPickerView;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.activity.ShoppingCartActivity;


import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 商品详情－－购物车
 */
public class ProductDetailShopcartView extends RelativeLayout {

	@Bind(R.id.view_numberpicker)
	NumberPickerView numberPickerView;
	@Bind(R.id.tv_badgeNumber) TextView badgeNumber;
	@Bind(R.id.btn_add_to_cart)
	Button btnAddToShopcart;

	private ShoppingCartEntity entity;
	private int mode;

	public interface ViewListener{
		void onFinsih();
	}
	private ViewListener listener;
	public void setViewListener(ViewListener listener){
		this.listener = listener;
	}

	public ProductDetailShopcartView(Context context) {
		super(context);
        init();
	}

	public ProductDetailShopcartView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.view_productdetail_shopcart, this);

		ButterKnife.bind(rootView);

		numberPickerView.setTheme(R.drawable.ic_decrease_circle, R.drawable.ic_increase_circle);
		numberPickerView.setonOptionListener(new NumberPickerView.onOptionListener() {
			@Override
			public void onPreIncrease() {

			}

			@Override
			public void onPreDecrease() {

			}

			@Override
			public void onValueChanged(int value) {
				refresh();
			}
		});
		numberPickerView.setValue(1);
	}

	public void init(ShoppingCartEntity entity){
		ShoppingCartService dbService = ShoppingCartService.get();

		this.entity = entity;

		//购物车中所有的商品数
		badgeNumber.setText(String.valueOf(dbService.getDao().getCount()));

		refresh();
	}

	@OnClick(R.id.btn_add_to_cart)
	public void addToShopcart(){
		ShoppingCartService dbService = ShoppingCartService.get();
		dbService.addToShopcart(entity, numberPickerView.getValue());

		if (listener != null){
			listener.onFinsih();
		}
	}

	@OnClick(R.id.shopcart_number)
	public void toShopcart(){
		ShoppingCartActivity.actionStart(getContext(), 0);
	}

	private void refresh(){
		if (numberPickerView.getValue() > 0){
			btnAddToShopcart.setEnabled(true);
		}else{
			btnAddToShopcart.setEnabled(false);
		}
	}

}
