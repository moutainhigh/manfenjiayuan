/*
 * 文件名称: UConfig.java
 * 版权信息: Copyright 2001-2011 ZheJiang Collaboration Data System Co., LTD. All right reserved.
 * ----------------------------------------------------------------------------------------------
 * 修改历史:
 * ----------------------------------------------------------------------------------------------
 * 修改原因: 新增
 * 修改人员: LuoJingtian
 * 修改日期: 2011-12-19
 * 修改内容: 
 */
package com.mfh.framework.configure;

import com.mfh.comn.config.ConfigClass;
import com.mfh.comn.config.ConfigItem;
import com.mfh.comn.config.ConfigLocationItem;
import com.mfh.comn.config.ConfigsParseHelper;
import com.mfh.comn.config.UConfig;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一配置缓存, 适合于容器持有. 对不需要缓存的配置读取, 建议使用 {@link com.mfh.framework.configure.UConfigHelper}.<BR><BR>
 * 使用示例:
 * 
 * <pre>
 * UConfigCache cache = new UConfigCache();
 * UConfig uc = cache.getUConfig(&quot;SHK.COMMON&quot;);
 * System.out.println(uc.getString(&quot;myurl&quot;));
 * System.out.println(uc.getString(&quot;password&quot;));
 * cache.destroy();
 * </pre>
 * 
 * @author <a href="mailto:luojt@zjcds.com">LuoJingtian</a> created on 2011-12-19
 * @since chch Framework 1.0
 */
public final class UConfigCache extends UConfigWrapper {

    /** 统一配置缓存: key=domain value=UConfig */
    private Map<String, UConfig> uconfigs = new HashMap<>();
    private Map<String, ConfigItem> configItems = null; 
    private static UConfigCache instance = null;

    public UConfigCache() {
        super();
    }
    
    public UConfigCache(String configAlias) {
        super(configAlias);
    }

    public static UConfigCache getInstance(){
        if (instance == null) {
            synchronized (UConfigCache.class) {
                if (instance == null)
                    instance = new UConfigCache();
            }
        }
        return instance;
    }
        
    public static UConfigCache getInstance(String configAlias){
        if (instance == null) {
            synchronized (UConfigCache.class) {
                if (instance == null) {
                    instance = new UConfigCache(configAlias);
                }
            }
        }
        return instance;
    }

    /**
     * 初始化统一配置缓存
     * @author LuoJingtian created on 2011-12-20 
     * @since chch Framework 1.0
     */
    private void initConfigs(String configAlias) {
        configItems = ConfigsParseHelper.getConfigs(configAlias);
        ConfigClass configClass = ConfigsParseHelper.getConfigClass(configAlias);

        UConfig uconfig;
        ConfigItem configItem;
        String domain;

        for (Map.Entry<String, ConfigItem> entry : configItems.entrySet()) {
            domain = entry.getKey();
            configItem = entry.getValue();

            if (uconfigs.containsKey(domain)) {
                logger.warn("初始化配置失败: 重复的配置域[" + domain + "].");
                continue;
            }

            try {
                uconfig = configClass.getUconfigClass().newInstance();
                uconfig.init(configItem, configClass.getConfigDomainClass());
                uconfigs.put(domain, uconfig);
            }
            catch (Exception e) {
                // 某一个配置初始化失败, 记录日志, 继续初始化下一个配置
                logger.warn("初始化配置失败:" + e.getMessage() + configItem, e);
            }
        }
    }


    public Map<String, ConfigItem> getConfigItems() {
        if (configItems == null)
            initConfigs(configAlias);
        return configItems;
    }

    /**
     * 根据配置项所属域获取统一配置接口.
     * @param domain 统一配置项所属域
     * @return 统一配置接口
     * @author LuoJingtian created on 2011-12-20 
     * @since chch Framework 1.0
     */
    public UConfig getDomain(String domain) {
        if (uconfigs.isEmpty()) {
            initConfigs(configAlias);
        }
        return uconfigs.get(domain);
    }

    public String getDomainString(String domain, String key){
        UConfig config = getDomain(domain);
        if (config != null){
            return config.getString(key);
        }
        else{
            return null;
        }
    }

    public String getDomainString(String domain, String key, String defVal){
        UConfig config = getDomain(domain);
        if (config != null){
            return config.getString(key, defVal);
        }
        else{
            return null;
        }
    }


    public void clearUConfigInner(String domain){
        if (uconfigs.isEmpty())
            return;
        uconfigs.remove(domain);
    }
    
    /**
     * 清空统一配置缓存, 释放资源.
     * @author LuoJingtian created on 2011-12-20 
     * @since chch Framework 1.0
     */
    public void destroy() {
        uconfigs.clear();
        configItems = null;
    }
    
    public static UConfig getUConfig(String domain){
        return getInstance().getDomain(domain);
    }
    
    /**
     * 清除一个配置项  
     * @param domain
     * @author zhangyz created on 2012-3-27
     */
    public static void clearUConfig(String domain){
        getInstance().clearUConfigInner(domain);
    }
    
    /**
     * 获取配置项的配置路径，若存在多个，取最后一个
     * @param domain
     * @return
     * @author zhangyz created on 2012-3-27
     */
    public static URL getUConfigFilePath(String domain){
        ConfigItem configItem = getInstance().getConfigItems().get(domain);
        ConfigLocationItem item = configItem.getLastConfigLocationItem();
        if (item == null)
            return null;
        return getConfigUrl(item.getLocation());
    }
    
    /**
     * 获取所有配置路径
     * @param domain
     * @return
     * @author zhangyz created on 2012-3-22
     */
    public static URL[] getConfigLocations(String domain){
        ConfigItem configItem = getInstance().getConfigItems().get(domain);
        ConfigLocationItem dc = configItem.getDefaultLocation();
        if (dc == null)
            return new URL[0];
        List<ConfigLocationItem> configs = configItem.getExtendsLocations();
        URL[] ret = new URL[1 + configs.size()];
        URL url = getConfigUrl(configItem.getDefaultLocation().getLocation());
        ret[0] = url;
        int index = 1;
        for (ConfigLocationItem item:configs){
            url = getConfigUrl(item.getLocation());
            ret[index ++] = url;
        }
        return ret;
    }
}
