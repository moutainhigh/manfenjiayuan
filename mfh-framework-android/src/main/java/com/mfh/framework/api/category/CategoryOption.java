package com.mfh.framework.api.category;

import java.util.List;

/**
 * 类目查询
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 */
public class CategoryOption implements java.io.Serializable {
    private String access;
    private Long code;
    private boolean hasChild;
    private String levelName;
    private String value;//名称
    private List<CategoryOption> items;

    public CategoryOption() {
    }

    public String getAccess() {
        return access;
    }

    public Long getCode() {
        return code;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public String getLevelName() {
        return levelName;
    }

    public String getValue() {
        return value;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<CategoryOption> getItems() {
        return items;
    }

    public void setItems(List<CategoryOption> items) {
        this.items = items;
    }
}
