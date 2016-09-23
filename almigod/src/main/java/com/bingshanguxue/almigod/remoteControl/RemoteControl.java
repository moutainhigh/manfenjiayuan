package com.bingshanguxue.almigod.remoteControl;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 9/23/16.
 */

public class RemoteControl implements Serializable{
    private Long id;//编号
    private String name;
    private String description;//描述

    public RemoteControl(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
