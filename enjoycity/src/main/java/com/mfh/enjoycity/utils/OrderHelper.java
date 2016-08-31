package com.mfh.enjoycity.utils;


import com.mfh.enjoycity.bean.ShopProductBean;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 订单帮助类
 * Created by Nat.ZZN on 2015/8/12.
 */
public class OrderHelper {
    private List<ShopProductBean> shopBeanList;//订单商品(店铺＋商品)
    private int productCount;//订单商品总数
    private int productTotalCount;//订单商品总件数
    private double productTotalAmount;//商品总价
    private double orderTotalAmount;//订单总金额
    private double deliverAmount;//配送费
    private List<ShoppingCartEntity> productEntityList;

    //收货时间
    private boolean deliverTimeEnabled;//收货时间是否有效（是否已经设置）
    private String displayDeliverTime;//界面显示时间
    private String dueDate;//送货开始时间
    private String dueDateEnd;//送货结束时间


    private static OrderHelper instance;
    public static OrderHelper getInstance(){
        if (instance == null){
            instance = new OrderHelper();
            instance.restore();
        }
        return instance;
    }

    public int getProductCount() {
        return productCount;
    }

    public boolean isDeliverTimeEnabled() {
        return deliverTimeEnabled;
    }

    public String getDisplayDeliverTime() {
        return displayDeliverTime;
    }

    public void setDisplayDeliverTime(String displayDeliverTime) {
        this.displayDeliverTime = displayDeliverTime;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDueDateEnd() {
        return dueDateEnd;
    }

    public List<ShopProductBean> getShopBeanList() {
        return shopBeanList;
    }

    public List<ShoppingCartEntity> getProductEntityList() {
        return productEntityList;
    }

    public int getProductTotalCount() {
        return productTotalCount;
    }

    public double getProductTotalAmount() {
        return productTotalAmount;
    }

    public double getOrderTotalAmount() {
        return orderTotalAmount;
    }

    public double getDeliverAmount() {
        return deliverAmount;
    }

    /**
     * */
    public void restore(){
        resetDeliverTime();
    }

    /**
     * 保存订单数据
     * */
    public void saveOrderProducts(List<ShopProductBean> beanList){
        List<ShoppingCartEntity> entityListTemp = new ArrayList<>();

        if (beanList != null && beanList.size() > 0){
            int count = 0;
            int totalCount = 0;
            double price = 0;
            double deliver = 0;

            for (ShopProductBean bean : beanList){
                List<ShoppingCartEntity> entityList = bean.getEntityList();

                count += entityList.size();
                for (ShoppingCartEntity entity : entityList){
                    totalCount += entity.getProductCount();
                    price += entity.getTotalAmount();
                    entityListTemp.add(entity);
                }

                if (bean.getTotalAmount() < ShopcartHelper.NO_FREIGHT_PRICE){
                    deliver += ShopcartHelper.FREIGHT_DEF;
                }
            }

            productCount = count;
            productTotalCount = totalCount;
            productTotalAmount = price;
            deliverAmount = deliver;
        }
        else{
            productCount = 0;
            productTotalCount = 0;
            productTotalAmount = 0;
            deliverAmount = 0;
        }
        orderTotalAmount = productTotalAmount + deliverAmount;

        productEntityList = entityListTemp;
        this.shopBeanList = beanList;

    }
    public void clearOrderProducts(){
        if (productEntityList == null){
            return;
        }

        ShoppingCartService dbService = ShoppingCartService.get();
        for (ShoppingCartEntity entity : productEntityList){
            dbService.deleteById(entity.getId());
        }
    }

    /**
     * 设置收货时间
     * @param timeDisplay 显示时间：今天09:00-10:00
     * */
    public void setDeliverTime(String timeDisplay){
        if (!StringUtils.isEmpty(timeDisplay)){
            try{
                ZLogger.d("timeDisplay:" + timeDisplay);
                displayDeliverTime = timeDisplay;

                String dateStr = timeDisplay.substring(0, 2);
                String timeStr = timeDisplay.substring(2, timeDisplay.length());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);
                SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);//12小时制
                SimpleDateFormat sdf5 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);//24小时制

                Calendar calendar = Calendar.getInstance();
                if (dateStr.contains("今天")){
                    dateStr = sdf.format(calendar.getTime());
                }
                else if (dateStr.contains("明天")){
                    calendar.add(Calendar.DATE, 1);
                    dateStr = sdf.format(calendar.getTime());
                }
                else{
                    resetDeliverTime();
                    return;
                }

                String[] timeA= timeStr.split("-");
                if (timeA != null && timeA.length >= 2){
                    String startTime = String.format("%s %s", dateStr, timeA[0]);
                    String endTime = String.format("%s %s", dateStr, timeA[1]);


                    Date startDate = sdf3.parse(startTime);
                    Date endDate = sdf3.parse(endTime);

                    dueDate = sdf5.format(startDate);
                    dueDateEnd = sdf5.format(endDate);

                    deliverTimeEnabled = true;

                }else{
                    resetDeliverTime();
                    return;
                }
            }
            catch (Exception ex){
                resetDeliverTime();
                return;
            }
        }
        else{
            resetDeliverTime();
        }
    }

    public void resetDeliverTime(){
        displayDeliverTime = "未选择";
        deliverTimeEnabled = false;
    }


}
