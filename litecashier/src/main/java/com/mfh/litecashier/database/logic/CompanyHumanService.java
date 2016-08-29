package com.mfh.litecashier.database.logic;


import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.litecashier.bean.CompanyHuman;
import com.mfh.litecashier.database.dao.CompanyHumanDao;
import com.mfh.litecashier.database.entity.CompanyHumanEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.List;

/**
 * 公司账号管理系统
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class CompanyHumanService extends BaseService<CompanyHumanEntity, String, CompanyHumanDao> {

    private static CompanyHumanService instance = null;
    /**
     * 返回 CompanyHumanService 实例
     * @return
     */
    public static CompanyHumanService get() {
//        String lsName = CompanyHumanService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new CompanyHumanService();//初始化登录服务
//        }
        if (instance == null) {
            synchronized (CompanyHumanService.class) {
                if (instance == null) {
                    instance = new CompanyHumanService();
                }
            }
        }
        return instance;
    }


    @Override
    protected Class<CompanyHumanDao> getDaoClass() {
        return CompanyHumanDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }



    public CompanyHumanEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(CompanyHumanEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(CompanyHumanEntity msg) {
        getDao().saveOrUpdate(msg);
    }

    public void saveOrUpdate(CompanyHuman human){
        try{
            if (human == null){
                return;
            }

            CompanyHumanEntity entity = new CompanyHumanEntity();
            entity.setHumanId(human.getId());
            entity.setUpdatedDate(human.getUpdatedDate());
            entity.setName(human.getName());
            entity.setUserId(human.getUserId());
            entity.setCompanyId(human.getCompanyId());
            entity.setUserName(human.getUserName());
            entity.setPassword(human.getPassword());
            entity.setSalt(human.getSalt());
            entity.setHeaderUrl("");

            saveOrUpdate(entity);
        }
        catch(Exception e){
            ZLogger.e(e.toString());
        }
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
    public List<CompanyHumanEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<CompanyHumanEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<CompanyHumanEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }
    public List<CompanyHumanEntity> queryAllByDesc(String strWhere) {
        return getDao().queryAllByDesc(strWhere);
    }
}
