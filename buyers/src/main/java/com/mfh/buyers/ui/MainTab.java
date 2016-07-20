package com.mfh.buyers.ui;


import com.mfh.buyers.R;
import com.mfh.buyers.fragments.ConversationAllFragment;
import com.mfh.buyers.fragments.IndividualFragment;
import com.mfh.buyers.fragments.MfParterFragment;

/**
 * 首页Tab
 * */
public enum MainTab {
	//小伙伴
	LIFE(0, R.string.bottombar_title_buyers, R.drawable.bottombar_life,
			MfParterFragment.class),

	//对话
	CONVERSATION(1, R.string.bottombar_title_message, R.drawable.bottombar_message,
			ConversationAllFragment.class),

	//我
	INDIVIDUAL(1, R.string.bottombar_title_individual, R.drawable.bottombar_me,
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
