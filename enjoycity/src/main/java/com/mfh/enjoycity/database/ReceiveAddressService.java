package com.mfh.enjoycity.database;

import android.content.Intent;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.enjoycity.bean.CommonAddrTemp;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.UIHelper;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 注册用户·地址
 * Created by Nat.ZZN on 15-8-6..
 */
public class ReceiveAddressService extends BaseService<ReceiveAddressEntity, String, ReceiveAddressDao> {
    private SimpleDateFormat format = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);

    @Override
    protected Class<ReceiveAddressDao> getDaoClass() {
        return ReceiveAddressDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    private static ReceiveAddressService instance = null;
    /**
     * 返回 IMConversationService 实例
     * @return
     */
    public static ReceiveAddressService get() {
        String lsName = ReceiveAddressService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new ReceiveAddressService();//初始化登录服务
        }
        return instance;
    }

    public void save(ReceiveAddressEntity msg) {
        getDao().save(msg);
    }

    public void saveOrUpdate(ReceiveAddressEntity msg) {
        getDao().saveOrUpdate(msg);
    }
//    public void saveOrUpdate(SubdisBean subdisBean) {
////                    dbService.clear();
//        ReceiveAddressEntity entity = new ReceiveAddressEntity();
//        entity.setId(String.valueOf(subdisBean.getId()));
//        entity.setCreatedDate(new Date());
//        entity.setSubdisId(subdisBean.getId());
//        entity.setSubName(subdisBean.getSubdisName());
//        entity.setAddrName(subdisBean.getStreet());
//        saveOrUpdate(entity);
//    }

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
    public List<ReceiveAddressEntity> queryAll(PageInfo pageInfo) {
        return getDao().queryAll(pageInfo);
    }

    public ReceiveAddressEntity query(String id){
        return getDao().query(id);
    }

    public ReceiveAddressEntity getEntityById(String id){
        try{
            return getDao().getEntityById(id);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public void init(List<CommonAddrTemp> dataList){
        if (dataList == null){
            return;
        }

        clear();


        ZLogger.d(String.format("init.size= %d", dataList.size()));
        for(CommonAddrTemp bean : dataList){
            ZLogger.d(String.format("init.bean= %s", bean.toString()));
            ReceiveAddressEntity entity = new ReceiveAddressEntity();
            entity.setId(String.valueOf(bean.getId()));
            entity.setCreatedDate(new Date());
            entity.setReceiver(bean.getReceiveName());
            entity.setTelephone(bean.getReceivePhone());
            entity.setAddressId(bean.getId());
            entity.setSubdisId(bean.getSubdisId());
            entity.setSubName(bean.getSubName());
            entity.setAddrName(bean.getAddrName());
            entity.setAddrvalid(bean.getAddrvalid());

            saveOrUpdate(entity);
        }


        Intent intent = new Intent(Constants.BROADCAST_ACTION_USER_RECV_ADDR_REFRESH);
        MfhApplication.getAppContext().sendBroadcast(intent);
    }
}
