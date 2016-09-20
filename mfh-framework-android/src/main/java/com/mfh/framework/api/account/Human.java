package com.mfh.framework.api.account;


/**
 * 用户信息
 * Created by bingshanguxue on 2015/5/14.
 *
 */
public class Human implements java.io.Serializable{
    private Long id;//humanId
    /**
     * 适用场景：消息。
     * 在应用场景下和id(humanId)一样，但是也有可能和设备有关，这里区别于id,做数据隔离
     * */
    private String guid;
    private String headimageUrl;//头像
    private String name;//姓名
    private String mobile;//手机号

    private Long curScore;//当前积分

    public Human(){
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        if (guid == null){
            guid = "";
        }
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getHeadimageUrl() {
        return headimageUrl;
    }

    public void setHeadimageUrl(String headimageUrl) {
        this.headimageUrl = headimageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Long getCurScore() {
        if (curScore == null){
            curScore = 0L;
        }
        return curScore;
    }

    public void setCurScore(Long curScore) {
        this.curScore = curScore;
    }
}
