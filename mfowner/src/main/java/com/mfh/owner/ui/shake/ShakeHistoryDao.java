package com.mfh.owner.ui.shake;

import android.widget.Toast;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.Pair;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.database.dao.BaseDbDao;


import net.tsz.afinal.db.sqlite.SqlBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 摇一摇 · 历史记录
 * 不绑定个人信息,切换账号时需要清空
 * Created by Nat.ZZN on 14-5-6.
 */
public class ShakeHistoryDao extends BaseDbDao<ShakeHistoryEntity, String> {

    private static final String TABLE_NAME = "shake_history";

    @Override
    protected Pair<String, String> initTableChName() {
        return new Pair<>("摇一摇历史表", TABLE_NAME);
    }

    @Override
    protected Class<ShakeHistoryEntity> initPojoClass() {
        return ShakeHistoryEntity.class;
    }

    @Override
    protected Class<String> initPkClass() {
        return String.class;
    }

    /**
     * 清理当前用户的所有记录
     * @param ownerId
     */
    public void clear(String ownerId) {
        try {
            getFinalDb().exeSql("delete from shake_history where guid = ?", new Object[]{ownerId});
        }
        catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void clear() {
        try {
            getFinalDb().exeSql("delete from shake_history", new Object[]{});
        }
        catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * 查询指定session下的消息类比，按照逆序
     * @param pageInfo
     * @return
     */
    public List<ShakeHistoryEntity> queryAll(PageInfo pageInfo) {
        return getFinalDb().findAllByWhere(ShakeHistoryEntity.class, null, "createdDate asc", pageInfo);//"id desc"
    }

//    /**
//     * 根据传过来的字符串，创建新记录
//     * @param headUrl
//     * getContext().getSharedPreferences("login", Activity.MODE_PRIVATE).getString("app.user.guid", null)
//     */
//    public void createNewRecord(String guid, String headUrl, String title, String description, String redirectUrl) {
//        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.INNER_DATAFORMAT);
//
//        ShakeHistoryEntity history = new ShakeHistoryEntity();
//        history.setGuid(guid);
//        history.setHeaderUrl(headUrl);
//        history.setTitle(title);
//        history.setDescription(description);
//        history.setRedirectUrl(redirectUrl);
//        history.setCreatedDate(new Date());
//        saveOrUpdate(history);
//    }


    /**
     * 生成where语句
     * @param sessionId 会话Id
     * @param searchToken 查询token
     * @return
     */
    private String genWhereSql(Long sessionId, String searchToken) {
        String sql = "sessionId = " + sessionId;
        if (searchToken != null && searchToken.length() > 0) {
            searchToken = "%" + searchToken + "%";
            sql += " and (msgInfo like '" + searchToken
                    + "' or humanName like '" + searchToken + "')";
        }
        return sql;
    }


    /**
     * 查询消息总数
     * @param sessionId
     * @param searchToken
     * @return
     */
    public Integer queryMsgCountBySessionId(Long sessionId, String searchToken) {
        String strWhere = genWhereSql(sessionId, searchToken);
        String countSql = SqlBuilder.getSelectCountSQLByWhere(ShakeHistoryEntity.class, strWhere);
        return getFinalDb().findTotalCount(countSql, null);
    }

    public Date queryTheOldTime(Long sessionId) {
       // String createDate = ().("select min(formatCreateTime) from emb_msg where sessionId=5771");
        String sql = "select * from shake_history where sessionId=" + sessionId;
        ShakeHistoryEntity msg = getFinalDb().findBySql(ShakeHistoryEntity.class, sql, null);
        try {
            if (msg.getCreatedDate() != null)
                return msg.getCreatedDate();
            else
                return new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT).parse("0000-00-00 00:00:00");
        } catch (ParseException e) {
            Toast.makeText(getContext(), "EmbMsgDao_queryTheOldTime", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return null;
    }


}
