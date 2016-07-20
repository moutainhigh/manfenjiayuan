package com.mfh.enjoycity.database;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 店铺信息
 * Created by bingshanguxue on 14-5-6.
 */
public class ShopService extends BaseService<ShopEntity, String, ShopDao> {
    private SimpleDateFormat format = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);

    @Override
    protected Class<ShopDao> getDaoClass() {
        return ShopDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static ShopService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static ShopService get() {
        String lsName = ShopService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new ShopService();//初始化登录服务
        }
        return instance;
    }

    public void save(ShopEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(ShopEntity msg) {
        getDao().saveOrUpdate(msg);
    }

    /**
     * 清空历史记录
     * */
    public void clear(){
        getDao().deleteAll();
    }

    public List<ShopEntity> queryAll() {
        return getDao().queryAll();
    }

    public ShopEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public ShopEntity getEntityById(Long id){
        if (id == null){
            return null;
        }

        return getEntityById(String.valueOf(id));
    }

}
