package com.mfh.framework.uikit.base;

import android.content.Context;

import com.mfh.framework.BizConfig;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.database.dao.BaseDbDao;
import com.mfh.framework.database.upgrade.SqlliteUpgradeSupport;
import com.mfh.framework.core.service.BaseService;
import com.mfh.framework.core.service.DataSyncStrategy;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.network.NetStateService;
import com.mfh.comn.config.UConfig;
import com.mfh.comn.upgrade.UpgradeConfigParseHelper;

/**
 * 系统初始化服务，所有启动时需要的检测和初始化工作放在此处
 *
 * @author zhangyz created on 2013-5-9
 * @since Framework 1.0
 */
public class InitService extends BaseService {

    public static InitService getService(Context context) {
        return ServiceFactory.getService(InitService.class, context);
    }

    @Override
    protected Class getDaoClass() {
        return null;
    }

    @Override
    public DataSyncStrategy getDataSyncStrategy() {
        return null;
    }

    /**
     * 系统初始化过程
     * @param context
     * @author zhangyz created on 2013-5-9
     */
    public void init (Context context) {
        NetStateService.getInstance().register(context);

        checkDb(context);

        initOther();
    }

    /**
     * 检测和初始化数据库
     *
     * @author zhangyz created on 2013-5-7
     */
    public void checkDb(Context context) {
        ZLogger.d("check database start");
//        UConfigCache uConfigCache = UConfigCache.getInstance();
//        if (uConfigCache == null){
//            ZLogger.d("uConfigCache=null");
//            return;
//        }
        //com.dinsc.comn.utils.SyncUtil.copyDatabase(context, com.dins.itm.comn.Constants.DBNAME);
        String dbName;
        if (BizConfig.RELEASE){
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                    UConfig.CONFIG_PARAM_DB_NAME, "mfh_release.db");
        }
        else{
            dbName = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                    "dev." + UConfig.CONFIG_PARAM_DB_NAME, "mfh_dev.db");
        }
        if (StringUtils.isEmpty(dbName)) {
            ZLogger.d("数据库名称不能为空");
            return;
        }
        ZLogger.d(String.format("db.name=%s", dbName));

        String dbPath = UConfigCache.getInstance().getDomainString(UConfig.CONFIG_COMMON,
                UConfig.CONFIG_PARAM_DB_PATH);
        ZLogger.d(String.format("db.path=%s", dbPath));

        UpgradeConfigParseHelper helper = new UpgradeConfigParseHelper();
        UConfig uc = UConfigCache.getInstance().getDomain(UConfig.CONFIG_DBUPGRADE);
        SqlliteUpgradeSupport support = new SqlliteUpgradeSupport();

        boolean bCreate = BaseDbDao.initDao(context, dbName, dbPath);
        if (bCreate) {
            helper.doDbUpdate(uc, support, bCreate);
        }
        else {
            helper.doDbUpdate(uc, support);
        }
    }


    /**
     * 其他必要的初始化工作
     *
     * @author zhangyz created on 2013-5-9
     */
    protected void initOther() {

    }
}
