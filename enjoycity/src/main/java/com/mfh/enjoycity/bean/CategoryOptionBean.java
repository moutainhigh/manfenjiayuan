package com.mfh.enjoycity.bean;

import java.util.List;

/**
 * 类目查询
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class CategoryOptionBean implements java.io.Serializable{
    private String access;
    private String code;
    private boolean hasChild;
    private String levelName;
    private String value;//名称
    private List<CategoryOptionBean> items;

    public CategoryOptionBean(){
    }

    public String getAccess() {
        return access;
    }

    public String getCode() {
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

    public void setCode(String code) {
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

    public List<CategoryOptionBean> getItems() {
        return items;
    }

    public void setItems(List<CategoryOptionBean> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("{\naccess=%s", access));
        sb.append(String.format("\ncode=%s", code));
        sb.append(String.format("\nhasChild=%s", hasChild));
        if (items != null && items.size() > 0){
            sb.append("options=[\n");
            for (CategoryOptionBean option : items){
                sb.append(String.format("%s", option.toString()));
            }
            sb.append("]\n");
        }
        sb.append(String.format("\nlevelName=%s", levelName));
        sb.append(String.format("\nvalue=%s\n}\n", value));
        return sb.toString();
    }
}
