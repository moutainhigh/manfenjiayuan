package com.manfenjiayuan.pda_supermarket.database.logic;

import com.manfenjiayuan.pda_supermarket.database.dao.PosOrderDao;
import com.manfenjiayuan.pda_supermarket.database.entity.PosOrderEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.List;

/**
 * POS--销售订单流水
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class PosOrderService extends BaseService<PosOrderEntity, String, PosOrderDao> {

    @Override
    protected Class<PosOrderDao> getDaoClass() {
        return PosOrderDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static PosOrderService instance = null;
    /**
     * 返回 PosOrderService 实例
     * @return
     */
    public static PosOrderService get() {
//        String lsName = PosOrderService.class.getName();
//        if (ServiceFactory.checkService(lsName))
//            instance = ServiceFactory.getService(lsName);
//        else {
//            instance = new PosOrderService();
//        }
        if (instance == null) {
            synchronized (PosOrderService.class) {
                if (instance == null) {
                    instance = new PosOrderService();
                }
            }
        }
        return instance;
    }

    public PosOrderEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(PosOrderEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(PosOrderEntity msg) {
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
    public List<PosOrderEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }
    public List<PosOrderEntity> queryAll() {
        return getDao().queryAll();
    }
    public List<PosOrderEntity> queryAll(String strWhere, PageInfo pageInfo) {
        try{
            return getDao().queryAll(strWhere, pageInfo);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<PosOrderEntity> queryAllAsc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllAsc(strWhere, pageInfo);
    }
    public List<PosOrderEntity> queryAllDesc(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllDesc(strWhere, pageInfo);
    }
    public List<PosOrderEntity> queryAllBy(String strWhere) {
        try{
            return getDao().queryAllBy(strWhere);
        }catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
    public List<PosOrderEntity> queryAllBy(String strWhere, String orderBy) {
        try{
            return getDao().queryAllBy(strWhere, orderBy);
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
