package com.mfh.petitestock.database.logic;

import com.manfenjiayuan.business.wrapper.L2CSyncStatus;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.petitestock.database.dao.ShelveDao;
import com.mfh.petitestock.database.entity.ShelveEntity;

import java.util.Date;
import java.util.List;

/**
 * 库存盘点
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class ShelveService extends BaseService<ShelveEntity, String, ShelveDao> {
    @Override
    protected Class<ShelveDao> getDaoClass() {
        return ShelveDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static ShelveService instance = null;

    /**
     * 返回 IMConversationService 实例
     *
     * @return
     */
    public static ShelveService get() {
        String lsName = ShelveService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new ShelveService();//初始化登录服务
        }
        return instance;
    }

    public boolean entityExistById(String id) {
        try {
            return getDao().entityExistById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return false;
        }
    }

    public ShelveEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(ShelveEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(ShelveEntity msg) {
        getDao().saveOrUpdate(msg);
    }

    /**
     * 清空历史记录
     */
    public void clear() {
        getDao().deleteAll();
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     *
     * @param pageInfo
     * @return
     */
    public List<ShelveEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<ShelveEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<ShelveEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<ShelveEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllBy(strWhere, pageInfo);
    }

    public List<ShelveEntity> queryAllByDesc(String strWhere) {
        return getDao().queryAllByDesc(strWhere);
    }

    public void deleteById(String id) {
        try {
            getDao().deleteById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public void deleteBy(String strWhere) {
        try {
            getDao().deleteBy(strWhere);
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 添加新商品
     */
    public void addNewEntity(String rackNo, String barcode) {
        if (StringUtils.isEmpty(barcode) || StringUtils.isEmpty(rackNo)) {
            return;
        }

        ShelveEntity entity = new ShelveEntity();
        entity.setRackNo(rackNo);
        entity.setBarcode(barcode);
        entity.setSyncStatus(L2CSyncStatus.SYNC_STATUS_INIT);
        entity.setCreatedDate(new Date());//使用当前日期，表示加入购物车信息
        entity.setUpdatedDate(new Date());

        saveOrUpdate(entity);
    }
}
