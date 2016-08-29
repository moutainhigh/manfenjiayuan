package com.mfh.enjoycity.database;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.database.dao.BaseDbDao;

import java.util.List;

/**
 * 购物车
 * 不绑定个人信息,切换账号时需要清空
 * Created by Nat.ZZN on 14-5-6.
 */
public class ShoppingCartDao extends BaseDbDao<ShoppingCartEntity, String> {

    private static final String TABLE_NAME = "shopping_cart";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("table_shopping_cart", TABLE_NAME);
    }

    @Override
    protected Class<ShoppingCartEntity> initPojoClass() {
        return ShoppingCartEntity.class;
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
    public List<ShoppingCartEntity> queryAll(PageInfo pageInfo) {
        try{
            return getFinalDb().findAllByWhere(ShoppingCartEntity.class, null, "createdDate asc", pageInfo);//"id desc"
        }
        catch (Exception ex){
            ZLogger.e(ex.toString());
            return null;
        }
    }
    public List<ShoppingCartEntity> queryAll() {
        try {
            return getFinalDb().findAllByWhere(ShoppingCartEntity.class, null, "createdDate asc");//"id desc"
        } catch (Exception ex) {
            ZLogger.e(ex.toString());

            return null;
        }
    }

}
