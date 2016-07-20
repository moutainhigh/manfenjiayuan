package com.mfh.enjoycity.bean;

import java.util.List;

/**
 * 类目查询
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class CategoryInfoBean implements java.io.Serializable{
    private String parent;
    private List<CategoryOptionBean> options;

    public CategoryInfoBean(){
    }


    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<CategoryOptionBean> getOptions() {
        return options;
    }

    public void setOptions(List<CategoryOptionBean> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("parent=%s\n", (parent == null ? "" : parent)));
        sb.append("options=[\n");
        if (options != null && options.size() > 0){
            for (CategoryOptionBean option : options){
                sb.append(String.format("%s", option.toString()));
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
