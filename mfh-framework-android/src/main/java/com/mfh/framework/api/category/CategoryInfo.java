package com.mfh.framework.api.category;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 8/15/16.
 */
public class CategoryInfo implements Serializable {
    private Long id;
    private String nameCn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }
}
