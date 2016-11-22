package com.mfh.enjoycity.utils;


import com.mfh.enjoycity.bean.ShopProductBean;
import com.mfh.enjoycity.database.AnonymousAddressEntity;
import com.mfh.enjoycity.database.AnonymousAddressService;
import com.mfh.enjoycity.database.ReceiveAddressEntity;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
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
            String id = SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ)
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
            String id = SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ)
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

        SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ)
                .edit().putString(Constants.PREF_KEY_LOGIN_ADDR_ID, null)
                .commit();
        SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ)
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

            SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ)
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

            SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ)
                    .edit()
                    .putString(Constants.PREF_KEY_ANONYMOUS_ADDR_ID, id)
                    .commit();
        }
    }

    public boolean bSelectAddress(){
        if (MfhLoginService.get().haveLogined()){
            String id = SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ).getString(Constants.PREF_KEY_LOGIN_ADDR_ID, null);
            if (id != null){
                ReceiveAddressService dbService = ReceiveAddressService.get();
                ReceiveAddressEntity entity = dbService.getDao().query(id);
                if (entity != null){
                    return true;
                }
            }
        }else{
            String id = SharedPrefesManagerFactory.getPreferences(Constants.PREF_NAME_APP_BIZ).getString(Constants.PREF_KEY_ANONYMOUS_ADDR_ID, null);
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

}
