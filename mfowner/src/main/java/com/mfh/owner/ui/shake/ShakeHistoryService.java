package com.mfh.owner.ui.shake;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;

import java.text.SimpleDateFormat;

/**
 * 消息服务类
 * Created by Administrator on 14-5-6.
 */
public class ShakeHistoryService extends BaseService<ShakeHistoryEntity, String, ShakeHistoryDao> {
    private String theLastCreateTime = "";//本轮最大游标
    private long cursorValue = -1L;//针对一个session此轮下载涉及到的最大游标
    private SimpleDateFormat format = new SimpleDateFormat(TimeCursor.INNER_DATAFORMAT);

    @Override
    protected Class<ShakeHistoryDao> getDaoClass() {
        return ShakeHistoryDao.class;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    public void save(ShakeHistoryEntity msg) {
        dao.save(msg);
    }

    public void saveOrUpdate(ShakeHistoryEntity msg) {
        dao.saveOrUpdate(msg);
    }

    /**
     * 清空历史记录
     * */
    public void clear(){
        dao.clear();
    }

}
