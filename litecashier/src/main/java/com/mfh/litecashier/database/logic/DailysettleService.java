package com.mfh.litecashier.database.logic;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.litecashier.database.dao.DailysettleDao;
import com.mfh.litecashier.database.entity.DailysettleEntity;

import java.util.List;

/**
 * <h1>POS--日结</h1><br>
 * <p>
 *
 * </p>
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class DailysettleService extends BaseService<DailysettleEntity, String, DailysettleDao> {

    @Override
    protected Class<DailysettleDao> getDaoClass() {
        return DailysettleDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static DailysettleService instance = null;
    /**
     * 返回 DailysettleService 实例
     * @return
     */
    public static DailysettleService get() {
//        String lsName = PosOrderService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new PosOrderService();
//        }
        if (instance == null) {
            synchronized (DailysettleService.class) {
                if (instance == null) {
                    instance = new DailysettleService();
                }
            }
        }
        return instance;
    }

    public DailysettleEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(DailysettleEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(DailysettleEntity msg) {
        getDao().saveOrUpdate(msg);
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
    public List<DailysettleEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<DailysettleEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<DailysettleEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<DailysettleEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }public List<DailysettleEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<DailysettleEntity> queryAllBy(String strWhere) {
        try{
            return getDao().queryAllBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    /**
     *  按条件删除
     *  */
    public void deleteBy(String strWhere){
        try{
            getDao().deleteBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }
    /**
     *  逐条删除
     *  */
    public void deleteById(String id){
        try{
            getDao().deleteById(id);
        }catch (Exception e){
            ZLogger.e(e.toString());
        }
    }


}
