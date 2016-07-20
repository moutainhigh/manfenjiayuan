package com.manfenjiayuan.im.database.dao;

import android.text.TextUtils;

import com.manfenjiayuan.im.database.entity.IMConversation;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.framework.database.dao.BaseDbDao;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.List;

/**
 * 会话
 * Created by Administrator on 14-5-6.
 */
public class IMConversationDao extends BaseDbDao<IMConversation, Long> {

    private static final String TABLE_NAME = "emb_session";
    private static final String TABLE_NAME_CH = "消息会话表";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>(TABLE_NAME_CH, TABLE_NAME);
    }

    @Override
    protected Class<IMConversation> initPojoClass() {
        return IMConversation.class;
    }

    @Override
    protected Class<Long> initPkClass() {
        return Long.class;
    }

    /**
     * 查询自该时间戳以来的新的会话Id
     *
     * @param lastUpdate
     * @return
     */
    public List<Long> getNewSessionIds(String ownerId, long lastUpdate) {
//        Log.d("Nat: IMConversationDao", String.format("getNewSessionIds.ownerId=%s, lastUpdate=%s", ownerId, String.valueOf(lastUpdate)));
        //TODO
        List<Long> ret = getFinalDb().findAllBySql(Long.class,
                "select id from emb_session where lastUpdate > ? and ownerId = ?", new String[]{Long.toString(lastUpdate), ownerId});
        return ret;
    }

    public List<IMConversation> getNewSessions(String ownerId, long lastUpdate) {
//        Log.d("Nat: EmbSessionDa", String.format("getNewSessions.ownerId=%s, lastUpdate=%s", ownerId, String.valueOf(lastUpdate)));
        //TODO
        List<IMConversation> ret = getFinalDb().findAllBySql(IMConversation.class,
                "select id from emb_session where lastUpdate > ? and ownerId = ?", new String[]{Long.toString(lastUpdate), ownerId});
        return ret;
    }



    /**
     * 获取指定人员所有未读消息数
     *
     * @param ownerId
     * @return
     */
    public Integer getTotalUnReadCount(String ownerId) {
        if (this.getFinalDb().tableIsExist("emb_session") && this.getFinalDb().tableIsExist("emb_msg"))
            return this.getFinalDb().findBySql(Integer.class,
                    "select sum(unReadCount) from emb_session where ownerId=?", new String[]{ownerId});
        else
            return 0;
    }

    /**
     * 查询属于本人会话的个数
     *
     * @param ownerId
     * @return
     */
    public Integer getMyCount(String ownerId) {
        getFinalDb().checkTableExist(IMConversation.class);

        if(TextUtils.isEmpty(ownerId)){
            return 0;
        }
        return this.getFinalDb().findBySql(Integer.class,
                "select count(*) from emb_session where ownerId=?", new String[]{ownerId});
    }

    /**
     * 获取指定session的未读消息数
     *
     * @param sessionId
     * @return
     */
    public Integer getSessionUnReadCount(Long sessionId) {
        getFinalDb().checkTableExist(IMConversation.class);
        if (sessionId == null){
            return null;
        }

//        "select unReadCount from emb_session where id=?"
        return this.getFinalDb().findBySql(Integer.class,
                "select unReadCount from emb_session where id=?", new String[]{sessionId.toString()});
    }

    /**
     * 增加或减少未读消息个数
     *
     * @param sessionId 会话Id
     * @param unCount   未读数
     */
    public void addUnReadCount(Long sessionId, int unCount) {
        IMConversation session = getEntityById(sessionId);
        if (session == null)
            return;
        int oldCount = Integer.valueOf(String.valueOf(session.getUnreadcount()));
        if (oldCount == -1)
            oldCount = 0;

        if (unCount < 0) {//对应于点击会话进去的场景，无论多少只要点进去就认为全部读过了，直接置0.
            oldCount = 0;
        }
        else {
            oldCount += unCount;
            if (oldCount < 0)
                oldCount = 0;//防止小于0出现。
        }
        getFinalDb().exeSql("update emb_session set unReadCount = ? where id = ?", new Object[]{oldCount, sessionId});
    }

    /**
     * 将未读消息数置为0
     *
     * @param sessionId
     */
    public void resetUnReadMsgCount(Long sessionId) {
        getFinalDb().exeSql("update emb_session set unReadCount = 0 where id = ?", new Object[]{sessionId});
    }

    /**
     * 按照时间逆序查询我的所有会话，支持分页信息
     *
     * @param ownerId
     * @param pageInfo
     */
    public List<IMConversation> queryMySessions(String ownerId, String searchToken, PageInfo pageInfo) {
        String sqlWhere = "ownerId='" + ownerId + "'";
        if (searchToken != null && searchToken.length() > 0) {
//            sqlWhere += "and humanName or nickName like '%" + searchToken + "%'";
            sqlWhere += "and humanName like '%" + searchToken + "%'";
        }
        return getFinalDb().findAllByWhere(IMConversation.class, sqlWhere, "topSessionOrder desc, lastUpdate desc", pageInfo);
    }

    /**
     * 按照时间逆序查询我的所有会话，支持分页信息
     *
     * @param ownerName
     * @param pageInfo
     */
    public List<IMConversation> queryForSearch(String ownerName, String searchToken, PageInfo pageInfo) {
        String sqlWhere = "ownerId='" + ownerName + "'";
        if (searchToken != null && searchToken.length() > 0) {
            sqlWhere += "and (humanName like '%" + searchToken + "%'" + " or nicknamebin like '%" + searchToken + "%')";
            return getFinalDb().findAllByWhere(IMConversation.class, sqlWhere, "topSessionOrder desc,sessionOrder,lastUpdate desc", pageInfo);
        }
        return null;
    }

    /**
     * 清理当前用户的所有会话
     *
     * @param ownerName
     */
    public void clearSessions(String ownerName) {
        try {
            String sqlWhere = "ownerId='" + ownerName + "'";
            getFinalDb().deleteByWhere(IMConversation.class, sqlWhere);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * 获取置顶序号
     */
    public Long getTopOrder(Long sessionId) {
        return this.getEntityById(sessionId).getTopSessionOrder();
    }

    /**
     * 判断指定次序是否为最小次序
     *
     * @param sessionId 会话标识
     * @return
     */
    public boolean isTopOrder(Long sessionId) {
        /*Integer curOrder = this.getEntityById(sessionId).getSessionOrder();
        if (IMConversation.DEFAULT_ORDER == curOrder.intValue())
            return false;
        Integer minOrder = getFinalDb().findBySql(Integer.class, "select min(sessionOrder) from emb_session", null);
        if (curOrder.equals(minOrder))
            return true;
        else
            return false;*/
        Long curTopSessionOrder = this.getEntityById(sessionId).getTopSessionOrder();
        if (curTopSessionOrder != IMConversation.DEFAULT_NOT_TOP_ORDER)
            return true;
        else
            return false;
    }

    /**
     * 将对话置顶
     *
     * @param sessionId
     */
    public void updateTopOrder(Long sessionId) {
        /*Integer minOrder = getFinalDb().findBySql(Integer.class, "select min(sessionOrder) from emb_session", null);
        minOrder = minOrder - 1;
        getFinalDb().exeSql("update emb_session set sessionOrder = ? where id = ?", new Object[]{minOrder, sessionId});*/
        getFinalDb().exeSql("update emb_session set topSessionOrder = ? where id = ?", new Object[]{System.currentTimeMillis(), sessionId});
    }

    /**
     * 取消置顶
     *
     * @param sessionId 会话标识
     */
    public void resetTopOrder(Long sessionId) {
        /*getFinalDb().exeSql("update emb_session set sessionOrder = ? where id = ?",
                new Object[]{IMConversation.DEFAULT_ORDER, sessionId});*/
        getFinalDb().exeSql("update emb_session set topSessionOrder = ? where id = ?", new Object[]{IMConversation.DEFAULT_NOT_TOP_ORDER, sessionId});
    }

    public List<String> getCpointNoRepeat() {
        List<String> c_pointId = this.getFinalDb().findAllBySql(String.class, "select channelpointid from emb_session group by channelpointid", null);
        return c_pointId;
    }

    public boolean getListByCpointId(String s) {
        //通过channelpointid这个字段来取值，如果能够取超过两个值，那就是已经绑定的用户了
        List<IMConversation> list = this.getFinalDb().findAllBySql(IMConversation.class, "select * from emb_session where channelpointid = '" + s + "'", null);
        if (list.size() >= 2)
            return true;
        else
            return false;
    }

    public IMConversation getSessionByHumanId(Long id) {
        //通过用户id获取到session对话
        return getFinalDb().findBySql(IMConversation.class, "select * from emb_session where humanId =" + id, null);
    }

    /**
     * 给定一个系统Id，创建系统会话
     *
     * @param sessionId
     */
    public void createSystemSession(Long sessionId) {
        IMConversation session = new IMConversation();
        session.setId(sessionId);
        session.setHumanname("系统通知");
        session.setNicknamebin("系统通知");
        session.setOwnerId(MfhLoginService.get().getLoginName());
        session.setLastupdate(20000000000L);
        saveOrUpdate(session);
    }

    public List<IMConversation> getGroupList(String ownerId, String searchToken, PageInfo pageInfo) {
        String where = "ownerId='" + ownerId + "'" + " and isGroup = 1";
        if (!TextUtils.isEmpty(searchToken)) {
            where += " and nicknamebin like '%" + searchToken + "%'";
        }
        return getFinalDb().findAllByWhere(IMConversation.class, where, "humanId", pageInfo);
    }
}
