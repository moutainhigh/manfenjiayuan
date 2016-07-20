package com.mfh.enjoycity.view;


import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.mfh.enjoycity.R;
import com.mfh.framework.uikit.compound.CustomSearchView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/***
 * 地址
 */
public class NaviSearchView extends RelativeLayout {
	@Bind(R.id.searchView)
	CustomSearchView searchView;

	public interface NaviSearchListener{
		void onDeepSearch(String queryText);
		void onSearch(String queryText);
	}
	public void setNaviSearchListener(NaviSearchListener listener){
		this.naviSearchListener = listener;
	}
	private NaviSearchListener naviSearchListener;

	public NaviSearchView(Context context) {
		super(context);
        init();
	}

	public NaviSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.view_navi_search, this);

		ButterKnife.bind(rootView);

		searchView.setHint("");
		searchView.setListener(new CustomSearchView.CustomSearchViewListener() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if(naviSearchListener != null){
					naviSearchListener.onSearch(charSequence.toString());
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}

			@Override
			public void doSearch(String queryText) {
				if(naviSearchListener != null){
					naviSearchListener.onDeepSearch(queryText);
				}
			}

		});
	}

	@OnClick(R.id.button_search)
    public void deepSearch(){
		if(naviSearchListener != null){
			naviSearchListener.onDeepSearch(searchView.getQueryText());
		}
    }

}
