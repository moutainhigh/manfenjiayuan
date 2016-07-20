package com.mfh.framework.database.dao;

import android.content.Context;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.IObject;
import com.mfh.comn.bean.Pair;
import com.mfh.comn.config.UConfig;
import com.mfh.comn.logic.SeqInit;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.configure.UConfigCache;
import com.mfh.framework.core.logger.ZLogger;

import net.tsz.afinal.FinalDb;
import net.tsz.afinal.db.table.TableInfo;

import java.io.File;
import java.util.List;

/**
 * 最基础的从本地数据库读取数据的DAO对象
 *
 * @param <T>
 * @author zhangyz created on 2013-6-8
 * @since Framework 1.0
 */
public abstract class BaseDbDao<T extends IObject, PK> extends ComnDao<T, PK> implements ISyncDao<T, PK> {
    protected String tableChName;
    protected String tableName;
    protected static FinalDb finalDb = null;

    /**
     * 系统启动时初始化Dao上下文。若指定数据库名不存在则创建，若已创建过则直接打开。
     *
     * @param context
     * @param dbName  数据库名
     * @param dbPath  数据库存储路径，若为空则放到程序私有目录下。
     * @return 是否新建库
     * @author zhangyz created on 2013-5-11
     */
    public static boolean initDao(Context context, String dbName, String dbPath) {
        ZLogger.d(String.format("Initialize--init database %s/%s", dbPath, dbName));
        FinalDb.DaoConfig config = new FinalDb.DaoConfig();
        config.setContext(context);
        config.setDbName(dbName);

        File fileDb;
        File dir;
        //若不加目录，放到私有目录下
        if (dbPath != null && dbPath.length() > 0) {
            //要求具有权限<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" >
            dir = new File(dbPath);
            if (dir.exists() && dir.isDirectory()) {
                config.setDbPath(dbPath);
            }

            ZLogger.d(String.format("check database %s/%s", dir, dbName));
            fileDb = new File(dir, dbName);
        } else {
            //dir = context.getFilesDir();//data/data/com.mfh.litecashier/files
            ///data/data/com.mfh.litecashier/databases/mfh_litecashier_release.db
            fileDb = context.getDatabasePath(dbName).getAbsoluteFile();
//            if (dir.exists() && dir.isDirectory()){
//                config.setDbPath(dir.getPath());
//            }
        }

        boolean bCreated = !fileDb.exists();
        ZLogger.d(String.format("database %s %s", fileDb, bCreated ? "not exist" : "exist already"));
        //创建数据库
        finalDb = FinalDb.create(config);
        if (finalDb != null) {
            ZLogger.d("create database success " + finalDb.getDb().getPath());
        } else {
            ZLogger.d("create database failed");
        }

        //初始化序列号
        UConfig uconfig = UConfigCache.getInstance().getDomain(UConfig.CONFIG_COMMON);
        if (uconfig != null) {
            SeqInit.init(uconfig);
            ZLogger.d("load configuration success");
        } else {
            ZLogger.d("load configuration failed");
        }

        return bCreated;
    }

    /**
     * 带有android上下文的构造函数
     * @param context
     */
    /*public BaseDbDao(Context context) {
        super();
        this.context = context;
        init();
    }*/

    /**
     * 无参构造函数
     */
    public BaseDbDao() {
        super();
        init();
    }

    private void init() {
        Pair<String, String> ret = initTableChName();
        if (ret == null)
            throw new RuntimeException("请设置表名和描述!");
        this.tableChName = ret.getV1();
        this.tableName = ret.getV2();
        if (tableName != null && tableName.length() > 0) {
            TableInfo.setTableName(pojoClass, tableName);
        }
    }

    /**
     * 获取表名
     *
     * @return
     * @author zhangyz created on 2013-6-13
     */
    public String getTableName() {
        return TableInfo.getTableName(pojoClass);
    }

    /**
     * 子类提供表名
     *
     * @return v1:中文名；v2：表名，为空的代表使用类名
     * @author zhangyz created on 2013-6-8
     */
    protected abstract Pair<String, String> initTableChName();

    /**
     * 获取实体中文名
     *
     * @return
     * @author zhangyz created on 2013-5-18
     */
    public String getTableChName() {
        return tableChName;
    }

    /**
     * 根据主键获取指定的对象
     *
     * @param pkId
     * @return
     * @author zhangyz created on 2013-6-7
     */
    @Override
    public T getEntityById(PK pkId) {
        if (pkId == null)
            return null;
        return getFinalDb().findById(pkId, pojoClass);
    }

    /**
     * 根据主键判断是否存在
     *
     * @param pkId
     * @return
     */
    public boolean entityExistById(PK pkId) {
        Integer count = getFinalDb().findCountById(pojoClass, pkId);
        if (count > 0)
            return true;
        else
            return false;
    }

    /**
     * 保存一个对象。
     *
     * @param bean
     * @author zhangyz created on 2013-6-7
     */
    @SuppressWarnings("unchecked")
    @Override
    public PK save(T bean) {
        getFinalDb().save(bean);
        return (PK) bean.getId();
    }

    /**
     * 批量保存
     *
     * @param rs
     */
    public void saveQueryResult(RspQueryResult<T> rs) {
        List<EntityWrapper<T>> beans = rs.getRowDatas();
        for (EntityWrapper<T> wb : beans) {
            save(wb.getBean());
        }
    }

    /**
     * 获取记录总数
     *
     * @return
     * @author zhangyz created on 2013-6-7
     */
    public int getCount() {
        return getFinalDb().findCount(this.pojoClass);
    }

    /**
     * 保存一个对象,主键由数据库自动生成
     *
     * @param bean
     * @return 返回主键值
     * @author zhangyz created on 2013-6-7
     */
    @SuppressWarnings("unchecked")
    public PK saveWithDbId(T bean) {
        getFinalDb().saveBindId(bean);
        return (PK) bean.getId();
    }

    /**
     * 修改一个对象
     *
     * @param bean
     * @author zhangyz created on 2013-6-7
     */
    @Override
    public void update(T bean) {
        getFinalDb().update(bean);
    }

    /**
     * 新增或修改一个对象
     *
     * @param bean
     */
    @Override
    public void saveOrUpdate(T bean) {
        if (bean == null) {
            return;
        }
        if (entityExistById((PK) bean.getId()))
            update(bean);
        else
            save(bean);
    }

    /**
     * 清空表
     *
     * @author zhangyz created on 2013-6-7
     */
    @Override
    public void deleteAll() {
        if (finalDb != null) {
            finalDb.deleteByWhere(pojoClass, null);
        }
    }

    /**
     * 删除指定的一条数据
     *
     * @param pkId
     * @author zhangyz created on 2013-6-7
     */
    @Override
    public void deleteById(PK pkId) {
        if (finalDb != null) {
            finalDb.deleteById(pojoClass, pkId);
        }
    }

    public void deleteBy(String strWhere){
        if (finalDb != null) {
            finalDb.deleteByWhere(pojoClass, strWhere);
        }
    }


    public static FinalDb getFinalDb() {
        if (finalDb == null){
            throw new RuntimeException("本地数据库还未初始化");
        }

        return finalDb;
    }

    public void beginTransaction() {
        if (finalDb != null) {
            finalDb.getDb().beginTransaction();
        }
    }

    public void setTransactionSuccessful() {
        if (finalDb != null) {
            finalDb.getDb().setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (finalDb != null) {
            finalDb.getDb().endTransaction();
        }
    }

    /**
     * 释放数据库
     *
     * @param persist
     * @author zhangyz created on 2013-6-13
     */
    public void release(FinalDb finalDb) {
//        finalDb.close();
    }
}
