package com.mfh.enjoycity.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.enjoycity.bean.SubdisBean;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.Date;
import java.util.List;

/**
 * 匿名用户·地址
 * Created by Nat.ZZN on 15-8-6..
 */
public class AnonymousAddressService extends BaseService<AnonymousAddressEntity, String, AnonymousAddressDao> {

    @Override
    protected Class<AnonymousAddressDao> getDaoClass() {
        return AnonymousAddressDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static AnonymousAddressService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static AnonymousAddressService get() {
        String lsName = AnonymousAddressService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new AnonymousAddressService();//初始化登录服务
        }
        return instance;
    }

    public void save(AnonymousAddressEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(AnonymousAddressEntity msg) {
        getDao().saveOrUpdate(msg);
    }

    public void saveOrUpdate(SubdisBean subdisBean) {
//                    dbService.clear();
        AnonymousAddressEntity entity = new AnonymousAddressEntity();
        entity.setId(String.valueOf(subdisBean.getId()));
        entity.setCreatedDate(new Date());
        entity.setSubdisId(subdisBean.getId());
        entity.setSubName(subdisBean.getSubdisName());
        entity.setAddrName(subdisBean.getStreet());
        saveOrUpdate(entity);
    }

    /**
     * 清空历史记录
     * */
    public void clear(){
        getDao().deleteAll();
    }


    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return
     */
    public List<AnonymousAddressEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public AnonymousAddressEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public AnonymousAddressEntity getEntityById(Long id){
        if (id == null){
            return null;
        }

        return getEntityById(String.valueOf(id));
    }
}
