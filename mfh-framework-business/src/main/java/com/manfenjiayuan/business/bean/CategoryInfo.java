package com.manfenjiayuan.business.bean;


import java.util.List;

/**
 * 类目查询
 * Created by Nat.ZZN(bingshanguxue) on 2015/8/18.
 *
 */
public class CategoryInfo implements java.io.Serializable{
    private String parent;
    private List<CategoryOption> options;

    public CategoryInfo(){
    }


    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<CategoryOption> getOptions() {
        return options;
    }

    public void setOptions(List<CategoryOption> options) {
        this.options = options;
    }
}
