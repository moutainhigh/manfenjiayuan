package com.mfh.owner.ui;


import com.mfh.owner.R;
import com.mfh.owner.fragments.IndividualFragment;
import com.mfh.owner.fragments.LifeFragment;
import com.mfh.owner.fragments.OrderFragment;

/**
 * 首页Tab
 * */
public enum MainTab {

	//首页
//	HOME(0, R.string.bottombar_title_homepage, R.drawable.bottombar_home,
//			HomeFragment.class),

	//生活
	LIFE(0, R.string.bottombar_title_life, R.drawable.bottombar_life,
			LifeFragment.class),

    //预定/提醒
    ORDER(1, R.string.bottombar_title_order, R.drawable.bottombar_calendar,
            OrderFragment.class),

//	//对话
//	CONVERSATION(2, R.string.bottombar_title_message, R.drawable.bottombar_message,
//			ConversationAllFragment.class),

	//我
	INDIVIDUAL(3, R.string.bottombar_title_individual, R.drawable.bottombar_me,
			IndividualFragment.class);


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
