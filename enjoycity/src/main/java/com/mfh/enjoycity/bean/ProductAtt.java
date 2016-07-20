package com.mfh.enjoycity.bean;

/**
 * 商品详情
 * Created by Nat.ZZN on 2015/5/14.
 *
 */
public class ProductAtt implements java.io.Serializable{
//    {
//        "pathUrl": "http://chunchunimage.b0.upaiyun.com/product/1856.jpg",
//            "thumbnailUrl": "http://chunchunimage.b0.upaiyun.com/product/1856.jpg!small",
//            "id": 1856,
//            "productId": 627,
//            "path": "upai://1856.jpg",
//            "thumbnail": "upai://1856.jpg!small",
//            "description": "250ml6瓶鲜奶",
//            "fileType": 0,
//            "fileOrder": 0,
//            "createdDate": "2015-07-15 10:41:09",
//            "updatedDate": "2015-07-24 09:15:29"
//    }

    private String pathUrl;
    private Long productId;

    public void setPathUrl(String pathUrl) {
        this.pathUrl = pathUrl;
    }

    public String getPathUrl() {
        return pathUrl;
    }
}
