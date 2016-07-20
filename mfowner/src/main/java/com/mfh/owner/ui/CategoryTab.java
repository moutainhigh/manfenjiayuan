package com.mfh.owner.ui;


import com.mfh.owner.R;
import com.mfh.owner.ui.web.BrowserFragment;
import com.mfh.owner.utils.MobileURLConf;

/**
 * 生活·分类Tab
 * */
public enum CategoryTab {

	//鲜奶
	MILK(0, "鲜奶", R.drawable.bottombar_home,
            BrowserFragment.class, MobileURLConf.URL_ME_SERVER),

	//蔬菜
	VEGETABLES(1, "蔬菜", R.drawable.bottombar_life,
            BrowserFragment.class, MobileURLConf.URL_ME_SERVER),

    //水果
    FRUIT(2, "水果", R.drawable.bottombar_calendar,
            BrowserFragment.class, MobileURLConf.URL_ME_SERVER),

	//鲜花
	FLOWERS(3, "鲜花", R.drawable.bottombar_message,
            BrowserFragment.class, MobileURLConf.URL_ME_SERVER),

	//烘培
	BAKERY(4, "烘培", R.drawable.bottombar_me,
            BrowserFragment.class, MobileURLConf.URL_ME_SERVER);

//	//烘培
//	TEST1(5, "测试1", R.drawable.bottombar_me,
//		   BrowserFragment.class, MobileURLConf.URL_ME_SERVER),
//	//烘培
//	TEST2(6, "测试2", R.drawable.bottombar_me,
//		   BrowserFragment.class, MobileURLConf.URL_ME_SERVER),
//	//烘培
//	TEST3(7, "测试3", R.drawable.bottombar_me,
//		   BrowserFragment.class, MobileURLConf.URL_ME_SERVER);

	private int idx;
//	private int resName;
    private String name;
	private int resIcon;
	private Class<?> clz;
    private String url;

	CategoryTab(int idx, String name, int resIcon, Class<?> clz, String url) {
		this.idx = idx;
		this.name = name;
		this.resIcon = resIcon;
		this.clz = clz;
        this.url = url;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

//	public int getResName() {
//		return resName;
//	}
//
//	public void setResName(int resName) {
//		this.resName = resName;
//	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
