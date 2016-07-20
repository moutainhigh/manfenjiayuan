package com.manfenjiayuan.loveshopping;


import com.manfenjiayuan.loveshopping.fragment.H5HomeFragment;
import com.manfenjiayuan.loveshopping.fragment.MainMineFragment;
import com.manfenjiayuan.loveshopping.fragment.OrderFragment;
import com.manfenjiayuan.loveshopping.fragment.ShopcartFragment;

/**
 * 首页Tab
 * */
public enum MainTab {

	//首页
	HOME(0, R.string.mainmenu_home, R.drawable.main_menu_home,
			H5HomeFragment.class),

	//生活
	ORDER(1, R.string.mainmenu_order, R.drawable.main_menu_order,
			OrderFragment.class),

    //预定/提醒
    SHOPCART(2, R.string.mainmenu_cart, R.drawable.main_menu_cart,
			ShopcartFragment.class),

	//我
	MY(3, R.string.mainmenu_my, R.drawable.main_menu_my,
			MainMineFragment.class);

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
