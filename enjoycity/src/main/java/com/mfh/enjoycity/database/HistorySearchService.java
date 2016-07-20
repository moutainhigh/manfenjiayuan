package com.mfh.enjoycity.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 注册用户·地址
 * Created by Nat.ZZN on 15-8-6..
 */
public class HistorySearchService extends BaseService<HistorySearchEntity, String, HistorySearchDao> {
    private SimpleDateFormat format = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);

    @Override
    protected Class<HistorySearchDao> getDaoClass() {
        return HistorySearchDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static HistorySearchService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static HistorySearchService get() {
        String lsName = HistorySearchService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new HistorySearchService();//初始化登录服务
        }
        return instance;
    }

    public void save(HistorySearchEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(HistorySearchEntity msg) {
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
    public List<HistorySearchEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public void addNewEntity(String shopId, String shopName, String queryText){
        try{
            if(StringUtils.isEmpty(shopId) || StringUtils.isEmpty(queryText)){
                ZLogger.e("shopId or queryText can not be null.");
                return;
            }
            getDao().delete(String.format("shopId = %s and queryContent = %s", shopId, queryText));

            HistorySearchService dbService = HistorySearchService.get();
            HistorySearchEntity entity = new HistorySearchEntity();
            entity.setId(shopId + String.valueOf(TimeUtil.genTimeStamp()));
            entity.setCreatedDate(new Date());
            entity.setShopId(shopId);
            entity.setShopName(shopName);
            entity.setQueryContent(queryText);
            dbService.saveOrUpdate(entity);
        }
        catch(Exception e){
            ZLogger.e(e.toString());
        }

    }
}
