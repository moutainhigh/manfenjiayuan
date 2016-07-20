package com.manfenjiayuan.mixicook_vip.ui;


import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.home.HomeFragment;
import com.manfenjiayuan.mixicook_vip.ui.my.MyFragment;
import com.manfenjiayuan.mixicook_vip.ui.reserve.ReserveFragment;
import com.manfenjiayuan.mixicook_vip.ui.shopcart.ShopcartFragment;

/**
 * 首页Tab
 * */
public enum MainTab {
	//首页
	LIFE(0, R.string.bottombar_title_home, R.drawable.bottombar_home,
			HomeFragment.class),

    //预定
    ORDER(1, R.string.bottombar_title_reserve, R.drawable.bottombar_calendar,
			ReserveFragment.class),

	//购物车
	CONVERSATION(2, R.string.bottombar_title_shopcart, R.drawable.main_menu_cart,
			ShopcartFragment.class),

	//我的
	INDIVIDUAL(3, R.string.bottombar_title_me, R.drawable.bottombar_me,
			MyFragment.class);


	private int idx;
	private int resName;
	private int resIcon;
	private Class<?> clz;

	MainTab(int idx, int resName, int resIcon, Class<?> clz) {
		this.idx = idx;
		this.resName = resName;
		this.resIcon = resIcon;
		this.clz = clz;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public int getResName() {
		return resName;
	}

	public void setResName(int resName) {
		this.resName = resName;
	}

	public int getResIcon() {
		return resIcon;
	}

	public void setResIcon(int resIcon) {
		this.resIcon = resIcon;
	}

	public Class<?> getClz() {
		return clz;
	}

	public void setClz(Class<?> clz) {
		this.clz = clz;
	}
}
