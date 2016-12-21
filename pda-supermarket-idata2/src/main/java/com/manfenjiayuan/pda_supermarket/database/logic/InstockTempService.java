package com.manfenjiayuan.pda_supermarket.database.logic;

import com.manfenjiayuan.pda_supermarket.database.dao.InstockTempDao;
import com.manfenjiayuan.pda_supermarket.database.entity.InstockTempEntity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.util.List;

/**
 * 库存盘点
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..
 */
public class InstockTempService extends BaseService<InstockTempEntity, String, InstockTempDao> {
    @Override
    protected Class<InstockTempDao> getDaoClass() {
        return InstockTempDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static InstockTempService instance = null;

    /**
     * 返回 IMConversationService 实例
     *
     * @return
     */
    public static InstockTempService get() {
        String lsName = InstockTempService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new InstockTempService();//初始化登录服务
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

    public InstockTempEntity getEntityById(String id) {
        try {
            return getDao().getEntityById(id);
        } catch (Exception e) {
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void save(InstockTempEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(InstockTempEntity msg) {
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
    public List<InstockTempEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public List<InstockTempEntity> queryAll() {
        return getDao().queryAll();
    }

    public List<InstockTempEntity> queryAllBy(String strWhere) {
        return getDao().queryAllBy(strWhere);
    }

    public List<InstockTempEntity> queryAllBy(String strWhere, PageInfo pageInfo) {
        return getDao().queryAllBy(strWhere, pageInfo);
    }

    public List<InstockTempEntity> queryAllByDesc(String strWhere) {
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

    public void saveOrUpdate(ScOrderItem scOrderItem) {
        if (scOrderItem == null) {
            return;
        }
        InstockTempEntity entity = getEntityById(String.valueOf(scOrderItem.getId()));
        if (entity == null) {
            entity = new InstockTempEntity();
            entity.setId(scOrderItem.getId());
        }
        entity.setSkuId(scOrderItem.getSkuId());
        entity.setProductName(scOrderItem.getProductName());
        entity.setUnitName(scOrderItem.getUnitName());
        entity.setBcount(scOrderItem.getBcount());//订单数量
        entity.setPrice(scOrderItem.getPrice());
        entity.setAmount(scOrderItem.getAmount());
        entity.setPriceType(scOrderItem.getPriceType());
        entity.setCommitCount(scOrderItem.getCommitCount());//实际组货数量
        entity.setCommitAmount(scOrderItem.getCommitAmount());

        entity.setIsEnable(1);//默认是可收货状态，退货时设为0
        saveOrUpdate(entity);

    }

}
