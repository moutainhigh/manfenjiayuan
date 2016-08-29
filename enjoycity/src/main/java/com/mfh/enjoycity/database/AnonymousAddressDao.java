package com.mfh.enjoycity.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseDbDao;

import java.util.List;

/**
 * 匿名用户·地址
 * 不绑定个人信息,切换账号时需要清空
 * Created by Nat.ZZN on 15-8-6.
 */
public class AnonymousAddressDao extends BaseDbDao<AnonymousAddressEntity, String> {

    private static final String TABLE_NAME = "annoymous_address";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("table_annoymous_address", TABLE_NAME);
    }

    @Override
    protected Class<AnonymousAddressEntity> initPojoClass() {
        return AnonymousAddressEntity.class;
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
    public List<AnonymousAddressEntity> queryAll(PageInfo pageInfo) {
        try{
            return getFinalDb().findAllByWhere(AnonymousAddressEntity.class, null, "createdDate asc", pageInfo);//"id desc"
        }
        catch(Exception ex){
            ZLogger.e(ex.toString());
            return null;
        }
    }

    public AnonymousAddressEntity query(String id){
        return getFinalDb().findById(id, AnonymousAddressEntity.class);
    }

}
