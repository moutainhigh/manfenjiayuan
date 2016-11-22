package com.mfh.enjoycity.ui;


import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.web.BrowserFragment;
import com.mfh.framework.api.mobile.MobileApi;

/**
 * 生活·分类Tab
 * */
public enum MessageTab {

	//鲜奶
	SYSTEM(0, "系统消息", R.drawable.material_favorite_pressed,
            BrowserFragment.class, MobileApi.URL_ME_SERVER),

	//蔬菜
	SELF(1, "我的账单", R.drawable.material_favorite_pressed,
            BrowserFragment.class, MobileApi.URL_ME_SERVER),

    //水果
    RECEIPT(2, "退款消息", R.drawable.material_favorite_pressed,
            BrowserFragment.class, MobileApi.URL_ME_SERVER);


	private int idx;
//	private int resName;
    private String name;
	private int resIcon;
	private Class<?> clz;
    private String url;

	MessageTab(int idx, String name, int resIcon, Class<?> clz, String url) {
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
