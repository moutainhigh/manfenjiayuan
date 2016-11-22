package com.mfh.enjoycity.ui;


import com.mfh.enjoycity.ui.web.BrowserFragment;
import com.mfh.framework.api.mobile.MobileApi;

/**
 * 生活·分类Tab
 * */
public enum ShopTab {

	//美团
	MILK(0, "美团", BrowserFragment.class, MobileApi.URL_MEITUAN),
	//小米
	VEGETABLES(1, "小米", BrowserFragment.class, MobileApi.URL_XIAOMI),
    //淘宝
    FRUIT(2, "淘宝", BrowserFragment.class, MobileApi.URL_TAOBAO),
	//赶集
	FLOWERS(3, "赶集", BrowserFragment.class, MobileApi.URL_GANJI),
	//途牛
	BAKERY(4, "途牛", BrowserFragment.class, MobileApi.URL_TUNIU),
	//唯品会
	TEST1(5, "唯品会", BrowserFragment.class, MobileApi.URL_M_VIP),
//	//一号店
//	TEST2(6, "一号店", BrowserFragment.class, MobileURLConf.URL_YHD),
	//天猫
	TEST3(6, "天猫", BrowserFragment.class, MobileApi.URL_TMALL);

	private int idx;
    private String name;
	private Class<?> clz;
    private String url;

	ShopTab(int idx, String name, Class<?> clz, String url) {
		this.idx = idx;
		this.name = name;
		this.clz = clz;
        this.url = url;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
