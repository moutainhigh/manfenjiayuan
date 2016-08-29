package com.mfh.enjoycity.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseDbDao;

import java.util.List;

/**
 * 注册用户·地址
 * Created by Nat.ZZN on 15-8-6..
 */
public class ReceiveAddressDao extends BaseDbDao<ReceiveAddressEntity, String> {

    private static final String TABLE_NAME = "receive_address";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("table_receive_address", TABLE_NAME);
    }

    @Override
    protected Class<ReceiveAddressEntity> initPojoClass() {
        return ReceiveAddressEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return
     */
    public List<ReceiveAddressEntity> queryAll(PageInfo pageInfo) {
        try{
            return getFinalDb().findAllByWhere(ReceiveAddressEntity.class, null, "createdDate asc", pageInfo);//"id desc"
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }

    public ReceiveAddressEntity query(String id){
        try{
            return getFinalDb().findById(id, ReceiveAddressEntity.class);
        }
        catch (Exception e){
            ZLogger.e(e.toString());
            return null;
        }
    }
}
