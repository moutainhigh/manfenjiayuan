package com.mfh.enjoycity.utils;


import com.mfh.enjoycity.bean.ShopProductBean;
import com.mfh.enjoycity.database.AnonymousAddressEntity;
import com.mfh.enjoycity.database.AnonymousAddressService;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 订单帮助类
 * Created by Nat.ZZN on 2015/8/12.
 */
public class ShopcartHelper {

    public static final double MIN_DELIVER_PRICE = 29;//满29起送
    public static final double NO_FREIGHT_PRICE = 49;//满49免邮
    public static final double FREIGHT_DEF = 6;//运费，默认为6元

    /**地址*/
    private Long addressId;//地址编号
    private Long subdisId; //小区编号
    private String subName; //小区名
    private Long addrvalid;//公寓编号
    private String addrName; //楼幢地址名
    private String receiver;//收货人
    private String telephone; //手机号


    private static ShopcartHelper instance;
    public static ShopcartHelper getInstance(){
        if (instance == null){
            instance = new ShopcartHelper();
            instance.restore();
        }
        return instance;
    }

    public Long getAddressId() {
        return addressId;
    }

    public Long getSubdisId() {
        return subdisId;
    }

    public String getSubName() {
        return subName;
    }

    public Long getAddrvalid() {
        return addrvalid;
    }

    public String getAddrName() {
        return addrName;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getTelephone() {
        return telephone;
    }


    public void restore(){
        setOrderAddr();
    }


    /**
     * 清空购物车
     * */
    public void clear(){

    }

    /**
     * 设置默认收货地址
     * */
    public void setOrderAddr(){
        if (MfhLoginService.get().haveLogined()){
            String id = SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ)
                    .getString(Constants.PREF_KEY_LOGIN_ADDR_ID, null);
            if (id != null){
                ReceiveAddressService dbService = ReceiveAddressService.get();
                ReceiveAddressEntity entity = dbService.getDao().query(id);
                if (entity != null){
                    addressId = entity.getAddressId();
                    subdisId = entity.getSubdisId();
                    subName = entity.getSubName();
                    addrvalid = entity.getAddrvalid();
                    addrName = entity.getAddrName();
                    receiver = entity.getReceiver();
                    telephone = entity.getTelephone();
                    return;
                }
            }
        }else{
            String id = SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ)
                    .getString(Constants.PREF_KEY_ANONYMOUS_ADDR_ID, null);
            if (id != null){
                AnonymousAddressService dbService = AnonymousAddressService.get();
                AnonymousAddressEntity entity = dbService.getDao().query(id);
                if (entity != null){
                    addressId = null;
                    subdisId = entity.getSubdisId();
                    subName = entity.getSubName();
                    addrvalid = null;
                    addrName = entity.getAddrName();
                    receiver = "";
                    telephone = "";
                    return;
                }
            }
        }
    }

    /***
     * 恢复初始设置
     */
    public void reset(){
        addressId = null;
        subdisId = null;
        subName = "";
        addrvalid = null;
        addrName = "";
        receiver = "";
        telephone = "";

        SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ)
                .edit().putString(Constants.PREF_KEY_LOGIN_ADDR_ID, null)
                .commit();
        SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ)
                .edit().putString(Constants.PREF_KEY_ANONYMOUS_ADDR_ID, null)
                .commit();
    }

    /**
     * 更新订单地址信息
     * @param id
     * */
    public void refreshMemberOrderAddr(String id){
        if (id == null || !MfhLoginService.get().haveLogined()){
            return;
        }

        ReceiveAddressService dbService = ReceiveAddressService.get();
        ReceiveAddressEntity entity = dbService.getDao().query(id);
        if (entity != null){
            subdisId = entity.getSubdisId();
            subName = entity.getSubName();
            addrName = entity.getAddrName();
            receiver = entity.getReceiver();
            telephone = entity.getTelephone();

            SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ)
                    .edit()
                    .putString(Constants.PREF_KEY_LOGIN_ADDR_ID, id)
                    .commit();
        }
    }
    /**
     * 更新匿名用户订单地址信息
     * */
    public void refreshAnonymousOrderAddr(String id){
        if (id == null || MfhLoginService.get().haveLogined()){
            return;
        }

        AnonymousAddressService dbService = AnonymousAddressService.get();
        AnonymousAddressEntity entity = dbService.getDao().query(id);
        if (entity != null){
            subdisId = entity.getSubdisId();
            subName = entity.getSubName();
            addrName = entity.getAddrName();
            receiver = "";
            telephone = "";

            SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ)
                    .edit()
                    .putString(Constants.PREF_KEY_ANONYMOUS_ADDR_ID, id)
                    .commit();
        }
    }

    public boolean bSelectAddress(){
        if (MfhLoginService.get().haveLogined()){
            String id = SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ).getString(Constants.PREF_KEY_LOGIN_ADDR_ID, null);
            if (id != null){
                ReceiveAddressService dbService = ReceiveAddressService.get();
                ReceiveAddressEntity entity = dbService.getDao().query(id);
                if (entity != null){
                    return true;
                }
            }
        }else{
            String id = SharedPreferencesManager.getPreferences(Constants.PREF_NAME_APP_BIZ).getString(Constants.PREF_KEY_ANONYMOUS_ADDR_ID, null);
            if (id != null){
                AnonymousAddressService dbService = AnonymousAddressService.get();
                AnonymousAddressEntity entity = dbService.getDao().query(id);
                if (entity != null){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 下单
     * */
    public void preCreateOrder(){
        ShoppingCartService dbService = ShoppingCartService.get();
        List<ShopProductBean> beanList = dbService.queryAllForAdapter();


        //TODO
        OrderHelper.getInstance().restore();
        OrderHelper.getInstance().saveOrderProducts(beanList);
    }


    /**
     * 购物车测试数据
     * */
    public void generateShoppingcartData(){
        ShoppingCartService shoppingCartService = ShoppingCartService.get();
        shoppingCartService.clear();

        Long[] shopId = new Long[]{Long.valueOf("133138"), Long.valueOf("131228"),  Long.valueOf("133148")};
        String[] shopName = new String[]{"店铺1", "店铺2",  "店铺3"};
        Long[] productIds = new Long[]{Long.valueOf("514"), Long.valueOf("516"), Long.valueOf("543"),
                Long.valueOf("520"), Long.valueOf("521"), Long.valueOf("526"),
                Long.valueOf("528"), Long.valueOf("531"), Long.valueOf("532"),
                Long.valueOf("640"), Long.valueOf("641"), Long.valueOf("643")};
        for(int i=0; i<productIds.length; i++){
            ShoppingCartEntity entity = new ShoppingCartEntity();

            entity.setId(String.valueOf(shopId[i]) + String.valueOf(productIds[i]));
            entity.setCreatedDate(new Date());
            entity.setProductId(productIds[i]);
            entity.setProductName("商品 " + i);
            entity.setProductPrice(0.01);
            entity.setProductImageUrl("http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg");
            entity.setProductCount(new Random().nextInt(5) + 1);
            int index = new Random().nextInt(3);
            entity.setShopId(shopId[index]);
            shoppingCartService.saveOrUpdate(entity);
        }
    }


//    {
//        "id": 123,
//            "productId": 456,
//            "productName": "商品名",
//            "productPrice": 88.88,
//            "productImageUrl": "商品图片链接",
//            "shopId": 789
//    }
    public void generateHybridShopcartData(String jsonStr){
        ShoppingCartService dbService = ShoppingCartService.get();
        dbService.addToShopcartFromHybird(jsonStr);
    }

    public void generateHybridShopcartData(){
        ShoppingCartService dbService = ShoppingCartService.get();
        dbService.clear();
        generateHybridShopcartData("{\n" +
                "    \"productId\": 514,\n" +
                "    \"productName\": \"商品名514\",\n" +
                "    \"productPrice\": 0.01,\n" +
                "    \"productCount\": 1,\n" +
                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
                "    \"shopId\": 131228\n" +
                "}");
//
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 514,\n" +
//                "    \"productId\": 514,\n" +
//                "    \"productName\": \"商品名1\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shopId\": 133138\n
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 516,\n" +
//                "    \"productId\": 516,\n" +
//                "    \"productName\": \"商品名2\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133138,\n" +
//                "        \"shopName\": \"店铺1\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 543,\n" +
//                "    \"productId\": 543,\n" +
//                "    \"productName\": \"商品名3\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133138,\n" +
//                "        \"shopName\": \"店铺1\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 520,\n" +
//                "    \"productId\": 520,\n" +
//                "    \"productName\": \"商品名4\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133138,\n" +
//                "        \"shopName\": \"店铺1\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        //
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 521,\n" +
//                "    \"productId\": 521,\n" +
//                "    \"productName\": \"商品名5\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 131228,\n" +
//                "        \"shopName\": \"店铺2\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 526,\n" +
//                "    \"productId\": 526,\n" +
//                "    \"productName\": \"商品名6\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 131228,\n" +
//                "        \"shopName\": \"店铺2\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 528,\n" +
//                "    \"productId\": 528,\n" +
//                "    \"productName\": \"商品名7\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 131228,\n" +
//                "        \"shopName\": \"店铺2\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        //
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 531,\n" +
//                "    \"productId\": 531,\n" +
//                "    \"productName\": \"商品名8\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133148,\n" +
//                "        \"shopName\": \"店铺3\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        dbService.addToShopcartFromHybird("{\n" +
//                "    \"id\": 532,\n" +
//                "    \"productId\": 532,\n" +
//                "    \"productName\": \"商品名9\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133148,\n" +
//                "        \"shopName\": \"店铺3\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 640,\n" +
//                "    \"productId\": 640,\n" +
//                "    \"productName\": \"商品名10\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133148,\n" +
//                "        \"shopName\": \"店铺3\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 641,\n" +
//                "    \"productId\": 641,\n" +
//                "    \"productName\": \"商品名11\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133148,\n" +
//                "        \"shopName\": \"店铺3\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
//        generateHybridShopcartData("{\n" +
//                "    \"id\": 643,\n" +
//                "    \"productId\": 643,\n" +
//                "    \"productName\": \"商品名12\",\n" +
//                "    \"productPrice\": 88.88,\n" +
//                "    \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/__30528866__6827859.jpg\",\n" +
//                "    \"shop\": {\n" +
//                "        \"shopId\": 133148,\n" +
//                "        \"shopName\": \"店铺3\",\n" +
//                "        \"productImageUrl\": \"http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg\"\n" +
//                "    }\n" +
//                "}");
    }

}
