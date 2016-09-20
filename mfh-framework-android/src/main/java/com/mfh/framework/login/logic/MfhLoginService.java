package com.mfh.framework.login.logic;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mfh.comn.net.JsonParser;
import com.mfh.comn.net.ResponseBody;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.Sex;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.core.service.IService;
import com.mfh.framework.login.MfhLoginPreferences;
import com.mfh.framework.api.account.Office;
import com.mfh.framework.api.account.Subdis;
import com.mfh.framework.api.account.UserAttribute;
import com.mfh.framework.api.account.UserComInfo;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import net.tsz.afinal.http.AjaxParams;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 满分登录服务,保存用户信息，并持久化
 * Created by NAT.ZZN(bingshanguxue) on 14-5-5.
 */
public class MfhLoginService implements IService {

    public static final String CLIENTSESSION = "JSESSIONID";
    public static final String OPERATORID = "operatorId";

    //登录相关
    private SharedPreferences spLogin = null;
    private String loginName;//登录账号
    private String password;//登录密码
    private String sessionId = null;//会话,null表示未登录
    private Long guid = null;//humanId，获取当前消息通讯号
    private Long cpid = null;//通讯标识号,channel point id
    private Long userId = null;// 用户编号
    private String mySubdisIds = null;//关联的小区
    private Long spid = 0L;//所属公司编号,租户,tenantId
    private Long curOfficeId = null;//当前所属部门编号,officeId,netId
    private String curOfficeName = "";
    private Long curSubdis = null;
    private List<Office> offices;//关联网点
    private String curStockId = null;//当前仓储编号
    private String stockIds = null;//
    private String moduleNames = null;  //当前用户支持的功能列表
    private String subdisNames = "";    //小区名
    private String telephone = "";
    private String humanName = "";//用户名
    private Long sessionDate = 0L;
    private String cookie = "";
    private String headimage = "";
    private Integer sex;


    //保存上一次登录信息
    private String lastLoginName;//上一次登录账号（退出重新登录时，默认显示）。
    private String lastLoginPassword;//上一次登录密码
    private Long lastOfficeId = null;//上一次登录部门编号（不同公司/部门商品库不同，所以切换公司/部门后，需要重置商品库和配置信息）。
    private Long lastSpid = null;//上一次登录公司编号（不同公司/部门商品库不同，所以切换公司/部门后，需要重置商品库和配置信息）。

    private boolean isCompanyOrOfficeChanged;//是否切换账号
    private static MfhLoginService instance = null;

    /**
     * 构造函数
     */
    public MfhLoginService() {
        spLogin = MfhLoginPreferences.getLoginPreferences(MfhApplication.getAppContext());
        restore();
        ServiceFactory.putService(MfhLoginService.class.getName(), this);
    }

    /**
     * 获取实例
     *
     * @return
     */
    public static MfhLoginService get() {
        String lsName = MfhLoginService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new MfhLoginService();//初始化登录服务
        }
        return instance;
    }

    /**
     * 重新从缓存中读取
     */
    public void restore() {
        SharedPreferences sp = MfhLoginPreferences.getLastLoginPreferences(MfhApplication.getAppContext());
        lastLoginName = sp.getString(MfhLoginPreferences.PK_LAST_USERNAME, "");
        lastLoginPassword = sp.getString(MfhLoginPreferences.PK_LAST_PASSWORD, "");
        lastOfficeId = sp.getLong(MfhLoginPreferences.PK_LAST_OFFICE_ID, 0L);
        lastSpid = sp.getLong(MfhLoginPreferences.PK_LAST_SPID, 0L);

        if (spLogin == null){
            spLogin = MfhLoginPreferences.getLoginPreferences(MfhApplication.getAppContext());
        }
        loginName = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USERNAME, "");
        password = spLogin.getString(MfhLoginPreferences.PK_LOGIN_PASSWORD, "");
        curOfficeId = spLogin.getLong(MfhLoginPreferences.PK_LOGIN_OFFICEID, 0L);
        userId = spLogin.getLong(MfhLoginPreferences.PK_LOGIN_USER_ID, 0L);
        spid = spLogin.getLong(MfhLoginPreferences.PK_LOGIN_SPID, 0L);
        guid = spLogin.getLong(MfhLoginPreferences.PK_LOGIN_USER_GUID, 0L);
        cpid = spLogin.getLong(MfhLoginPreferences.PK_LOGIN_USER_CPID, 0L);
        mySubdisIds = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_SUBDIS_ID, null);
        stockIds = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_STOCKI_DS, null);
        curOfficeName = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_CURRENT_OFFICE_NAME, "");
        String officesStr = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_OFFICES, null);
//        ZLogger.d("offices:" + officesStr);
        this.offices = JSONObject.parseArray(officesStr, Office.class);
        sessionId = spLogin.getString(MfhLoginPreferences.PK_LAST_SESSION_ID, null);

        curStockId = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_CURRENT_STOCKID, null);
        moduleNames = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_MODULES, null);
        subdisNames = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_SUBDIS_NAME, null);
        telephone = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_TELEPHONE, "");
        humanName = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_HUMANNAME, "");
        sessionDate = spLogin.getLong(MfhLoginPreferences.PK_LOGIN_SESSION_DATE, 0L);
        cookie = spLogin.getString(MfhLoginPreferences.PK_LOGIN_HTTP_COOKIE, "");
        headimage = spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_HEADIMAGE, "");
        sex = spLogin.getInt(MfhLoginPreferences.PK_I_USER_SEX, Sex.UNKNOWN);
    }

    /**
     * 保存用户相关信息
     */
    public void save() {
        SharedPreferences lastLoginSp = MfhLoginPreferences.getLastLoginPreferences(MfhApplication.getAppContext());
        SharedPreferences.Editor lastLoginEditor = lastLoginSp.edit();
        lastLoginEditor.putString(MfhLoginPreferences.PK_LAST_USERNAME, lastLoginName);
        lastLoginEditor.putString(MfhLoginPreferences.PK_LAST_PASSWORD, lastLoginPassword);
        lastLoginEditor.putLong(MfhLoginPreferences.PK_LAST_OFFICE_ID, lastOfficeId);
        lastLoginEditor.putLong(MfhLoginPreferences.PK_LAST_SPID, lastSpid);
        lastLoginEditor.commit();

        SharedPreferences.Editor loginEditor = spLogin.edit();
        loginEditor.putString(MfhLoginPreferences.PK_LAST_SESSION_ID, sessionId);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USERNAME, loginName);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_PASSWORD, password);
        loginEditor.putLong(MfhLoginPreferences.PK_LOGIN_SPID, spid);
        loginEditor.putLong(MfhLoginPreferences.PK_LOGIN_OFFICEID, curOfficeId);
        loginEditor.putLong(MfhLoginPreferences.PK_LOGIN_USER_ID, userId);
        loginEditor.putLong(MfhLoginPreferences.PK_LOGIN_USER_GUID, guid);
        loginEditor.putLong(MfhLoginPreferences.PK_LOGIN_USER_CPID, cpid);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_TELEPHONE, telephone);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_HTTP_COOKIE, cookie);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_HUMANNAME, humanName);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_HEADIMAGE, headimage);
        loginEditor.putInt(MfhLoginPreferences.PK_I_USER_SEX, sex);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_SUBDIS_ID, mySubdisIds);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_STOCKI_DS, stockIds);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_CURRENT_OFFICE_NAME, curOfficeName);
        JSONArray officesArr = new JSONArray();
        if (offices != null && offices.size() > 0) {
            for (Office office : offices) {
                officesArr.add(office);
            }
        }
//        ZLogger.d("offices:" + officesArr.toJSONString());
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_OFFICES, officesArr.toJSONString());

        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_CURRENT_STOCKID, curStockId);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_SUBDIS_NAME, subdisNames);
        loginEditor.putLong(MfhLoginPreferences.PK_LOGIN_SESSION_DATE, sessionDate);
        loginEditor.putString(MfhLoginPreferences.PK_LOGIN_USER_MODULES, moduleNames);
        loginEditor.commit();
    }

    /**
     * 清除当前登录用户的缓存信息
     */
    public void clear() {
        MfhLoginPreferences.clearLoginPreferences(MfhApplication.getAppContext());

        restore();
    }


    /**
     * 获取当前用户所关联的小区列表
     *
     * @return
     */
    public String getMySubdisIds() {
        return mySubdisIds;
    }

    public void setMySubdisIds(String mySubdisIds) {
        this.mySubdisIds = mySubdisIds;
    }


    public String getStockIds() {
        return stockIds;
    }

    public void setStockIds(String stockIds) {
        this.stockIds = stockIds;
    }

    public String getModuleNames() {
        return moduleNames;
    }


    public void setModuleNames(String moduleNames) {
        this.moduleNames = moduleNames;
    }

    /**
     * 获取模块名数组
     *
     * @return
     */
    public String[] getModuleNameArray() {
        if (StringUtils.isBlank(moduleNames))
            return null;
        return StringUtils.splitByWholeSeparator(moduleNames, ",");
    }

    /**
     * 获取当前登录后选择的小区
     *
     * @return
     */
    public Long getCurSubdis() {
        if (curSubdis == null) {
            String[] ids = StringUtils.splitByWholeSeparator(mySubdisIds, ",");
            if (ids != null && ids.length > 0) {
                return Long.parseLong(ids[0]);
            } else {
                return null;
            }
        } else
            return curSubdis;
    }

    public String getCurStockId() {
        return curStockId;
    }

    public void setCurStockId(String curStockId) {
        this.curStockId = curStockId;
    }

    public Long getCurOfficeId() {
        if (curOfficeId == null) {
            return 0L;
        }
        return curOfficeId;
    }

    public void setCurOfficeId(Long curOfficeId) {
        this.curOfficeId = curOfficeId;
    }

    public String getCurOfficeName() {
        return curOfficeName;
    }

    public void setCurOfficeName(String curOfficeName) {
        this.curOfficeName = curOfficeName;
    }

    public List<Office> getOffices() {
        return offices;
    }

    public void setOffices(List<Office> offices) {
        this.offices = offices;
    }

    //    public Long getCurrentStockId(){
//        if (curStockId == null){
//            if (StringUtils.isEmpty(officeIds)){
//                return  null;
//            }else{
//               String[] ids = StringUtils.splitByWholeSeparator(officeIds, ",");
//                //TODO
//            }
//
//        }else{
//            return curStockId;
//        }
//    }

    public Long getCpId() {
        return cpid;
    }

    public void setCpid(Long cpid) {
        this.cpid = cpid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrentSessionId() {
        return sessionId;
    }

    public void setLastSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLoginName() {
        return loginName;
    }

    public Long getCurrentGuId() {
        return this.guid;
    }

    public void setCurrentGuid(Long guid) {
        this.guid = guid;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 修改密码
     */
    public void changePassword(String password) {
        this.password = password;

        SharedPreferences.Editor editor = spLogin.edit();
        editor.putString(MfhLoginPreferences.PK_LOGIN_PASSWORD, password);
        editor.commit();
    }

    /**
     * 切换子账号
     * */
    public void changeHuman(Long humanId, String username, String password){
        ZLogger.d(String.format("切换账号：%s(%s)", username, humanId));
        this.sessionId = "";
        this.guid = humanId;
        this.loginName = username;
        this.password = password;

        SharedPreferences.Editor editor = spLogin.edit();
        editor.putString(MfhLoginPreferences.PK_LAST_SESSION_ID, "");//清空session
        editor.putLong(MfhLoginPreferences.PK_LOGIN_USER_GUID, humanId);
        editor.putString(MfhLoginPreferences.PK_LOGIN_USERNAME, username);
        editor.putString(MfhLoginPreferences.PK_LOGIN_PASSWORD, password);
        editor.commit();
    }


    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Long getSpid() {
        if (spid == null){
            return 0L;
        }
        return spid;
    }

    public String getSubdisNames() {
        return subdisNames;
    }

    public void setSubdisNames(String subdisNames) {
        this.subdisNames = subdisNames;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getHumanName() {
//        return humanName;
        return spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_HUMANNAME, "");
    }

    public void setHumanName(String humanName) {
        this.humanName = humanName;
    }

    public void changeHumanName(String humanName) {
        this.humanName = humanName;

        SharedPreferences.Editor editor = spLogin.edit();
        editor.putString(MfhLoginPreferences.PK_LOGIN_USER_HUMANNAME, humanName);
        editor.commit();
    }

    public String getHeadimage() {
//        return headimage;
        return spLogin.getString(MfhLoginPreferences.PK_LOGIN_USER_HEADIMAGE, "");
    }

    public void setHeadimage(String headimage) {
        this.headimage = headimage;
    }

    public void updateHeadimage(String headimage) {
        SharedPreferences.Editor editor = spLogin.edit();
        editor.putString(MfhLoginPreferences.PK_LOGIN_USER_HEADIMAGE, headimage);
        editor.commit();

        this.headimage = headimage;
    }

    public Long getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Long sessionDate) {
        this.sessionDate = sessionDate;
    }

    /**
     * 修改密码
     */
    public void updateSex(Integer sex) {
        this.sex = sex;

        SharedPreferences.Editor editor = spLogin.edit();
        editor.putInt(MfhLoginPreferences.PK_I_USER_SEX, sex);
        editor.commit();
    }


    public String getLastLoginName() {
        if (lastLoginName == null){
            return "";
        }
        return lastLoginName;
    }

    public void setLastLoginName(String lastLoginName) {
        this.lastLoginName = lastLoginName;
    }

    public String getLastLoginPassword() {
        if (lastLoginPassword == null){
            return "";
        }
            return lastLoginPassword;
    }

    public void setLastLoginPassword(String lastLoginPassword) {
        this.lastLoginPassword = lastLoginPassword;
    }

    public Long getLastOfficeId() {
        if (lastOfficeId == null){
            return 0L;
        }
        return lastOfficeId;
    }

    public void setLastOfficeId(Long lastOfficeId) {
        this.lastOfficeId = lastOfficeId;
    }

    public Long getLastSpid() {
        if (lastSpid == null){
            return 0L;
        }
        return lastSpid;
    }

    public void setLastSpid(Long lastSpid) {
        this.lastSpid = lastSpid;
    }

    /**
     * 是否已经成功登录过
     *
     * @return
     */
    public boolean haveLogined() {
        return sessionId != null;
    }


    public boolean isCompanyOrOfficeChanged() {
        return isCompanyOrOfficeChanged;
    }

    /**
     * 检查是否切换公司和部门
     *
     * @return true,切换公司和部门,需要清空商品库，常用商品，重新同步;false,不做任何操作
     */
    public boolean checkCompanyOrOffice() {
        if (lastOfficeId == null || lastSpid == null){
            return true;
        }

        if (curOfficeId == null || spid == null){
            return true;
        }

        ZLogger.d(String.format("checkCompanyOrOffice:(%d,%d)-(%d,%d)", lastOfficeId, lastSpid, curOfficeId, spid));
        if (lastOfficeId.compareTo(curOfficeId) != 0 || lastSpid.compareTo(spid) != 0){
            return true;
        }

        return false;
    }

    /**
     * 登录同步
     */
    public String doLogin() {
        if (loginName == null || password == null) {
            return null;
        }

        ZLogger.d("同步执行登录操作");
        UserMixInfo um = doLogin(this.loginName, this.password);
        if (um != null) {
            saveUserMixInfo(this.loginName, this.password, um);
            ZLogger.d("同步登录成功");
            //同步登录不注册消息桥
//            refreshMsgBridge();
            return um.getSessionId();
        } else {
            return null;
        }
    }

    /**
     * 执行登录(同步调用)
     *
     * @param name
     * @param pwd
     * @return
     */
    public UserMixInfo doLogin(String name, String pwd) {
        try {
            Object ret = AfinalFactory.getHttp(false).postSync(MfhApi.URL_LOGIN,
                    new AjaxParams(MfhApi.PARAM_KEY_USERNAME, name,
                            MfhApi.PARAM_KEY_PASSWORD, pwd,
                            "needMenu", true,
                            MfhApi.PARAM_KEY_LOGIN_TYPE, MfhApi.PARAM_VALUE_LOGIN_TYPE_PMC));

            //解析
            JsonParser parser = new JsonParser();
            ResponseBody resp = parser.parser(ret.toString(), UserMixInfo.class, JsonParser.defaultFormat);

            if (!resp.isSuccess()) {
                //401/unau###
                String errMsg = resp.getRetCode() + ":" + resp.getReturnInfo();
//            throw new RuntimeException(errMsg);
                ZLogger.d("同步登录失败: " + errMsg);
                return null;
            } else {
                IResponseData rspData = resp.getData();
                RspBean<UserMixInfo> retValue = (RspBean<UserMixInfo>) rspData;
                return retValue.getValue();
            }

        } catch (Exception e) {
            ZLogger.e("登录异常: " + e.toString());
            return null;
        }
    }

    /**
     * 执行异步登录
     *
     * @param name
     * @param pwd
     * @param loginCallback
     * @param loginType
     * @param loginKind
     * @return
     */
    public void doLoginAsync(final String name, final String pwd, final LoginCallback loginCallback,
                             String url, String loginType, String loginKind) {
        //回调
        NetCallBack.NetTaskCallBack callback = new NetCallBack.NetTaskCallBack<UserMixInfo,
                NetProcessor.Processor<UserMixInfo>>(new NetProcessor.Processor<UserMixInfo>() {
            @Override
            public void processResult(IResponseData rspData) {
                RspBean<UserMixInfo> retValue = (RspBean<UserMixInfo>) rspData;
//                Log.d("Nat: loginResponse", String.format("retValue= %s", retValue.toString()));
                UserMixInfo um = retValue.getValue();

                saveUserMixInfo(name, pwd, um);

                if (loginCallback != null) {
                    loginCallback.loginSuccess(um);
                }
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                if (loginCallback != null) {
                    loginCallback.loginFailed(errMsg);
                }
            }

        }, UserMixInfo.class, MfhApplication.getAppContext()) {
        };

        AjaxParams params = new AjaxParams();
        params.put("needMenu", "true");
        params.put(MfhApi.PARAM_KEY_USERNAME, name);
        params.put(MfhApi.PARAM_KEY_PASSWORD, pwd);
        if (!TextUtils.isEmpty(loginKind)) {
            params.put(MfhApi.PARAM_KEY_LOGIN_KIND, loginKind);
        }
        if (!TextUtils.isEmpty(loginType)) {
            params.put(MfhApi.PARAM_KEY_LOGIN_TYPE, loginType);
        }

        //先记录登录用户名和密码，用于登录失败时重登录
        this.loginName = name;
        this.password = pwd;

        AfinalFactory.getHttp(false).post(url, params, callback);
    }

    /**
     * 异步登录*/
    public void doLoginAsync(String name, String pwd, LoginCallback loginCallback) {
//        doLoginAsync(name, pwd, loginCallback, MfhApi.URL_LOGIN,
//                MfhApi.PARAM_VALUE_LOGIN_TYPE_DEF, MfhApi.PARAM_KEY_LOGIN_KIND);
        doLoginAsync(name, pwd, loginCallback, MfhApi.URL_LOGIN, null, null);
    }

    /**
     * 使会话失效
     */
    public void expireSession(NetCallBack.NormalNetTask callback) {
        if (sessionId == null)
            return;
        AjaxParams params = new AjaxParams();
        params.put("sid", sessionId);
        params.put("lgdrt", "2");
        AfinalFactory.postDefault(MfhApi.URL_LOGOUT, params, callback);
    }


    //这个方法获取的值不正确，可能在别的地方创建了MfhLoginService的实例
    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
        SharedPreferences.Editor editor = spLogin.edit();
        editor.putString(MfhLoginPreferences.PK_LOGIN_HTTP_COOKIE, cookie);
        editor.commit();
    }

    /**
     * 保存用户登录相关信息
     *
     * @param username 登录用户名
     * @param password 登录密码
     * @param um       用户详细信息
     */
    public void saveUserMixInfo(String username, String password, UserMixInfo um) {
        if (um == null) {
            return;
        }

        final MfhLoginService loginService = this;
        loginService.loginName = username;
        loginService.password = password;
        loginService.lastLoginName = username;
        loginService.lastLoginPassword = password;

        loginService.guid = um.getHumanId();
        loginService.sessionId = um.getSessionId();
        loginService.telephone = um.getPhonenumber();

        if (um.getCookiees() != null) {
            loginService.cookie = um.getCookiees().get(0);
        }
        Object objId = um.getId();
        if (objId instanceof String)
            loginService.userId = Long.parseLong(objId.toString());
        else
            loginService.userId = um.getId();

        UserAttribute userAttribute = um.getUserAttribute();
        if (userAttribute != null) {
//            loginService.cpid = userAttribute.getCpid();
            loginService.humanName = userAttribute.getHumanName();
            loginService.headimage = userAttribute.getHeadimage();
            loginService.sex = userAttribute.getSex();
        }

        List<UserComInfo> userComInfos = um.getComInfos();
        StringBuilder sbStockIds = new StringBuilder();
        StringBuilder sbSubdisNames = new StringBuilder();
        if (userComInfos != null && userComInfos.size() > 0) {
            UserComInfo userComInfo = userComInfos.get(0);
            loginService.spid = userComInfo.getSpid();

            loginService.moduleNames = userComInfo.getModuleNames();
            loginService.mySubdisIds = userComInfo.getSubdisIds();
            loginService.curOfficeId = userComInfo.getCurOffice();

            loginService.curStockId = null;
            loginService.offices = userComInfo.getOffices();
            if (loginService.offices != null) {
                for (int i = 0; i < loginService.offices.size(); i++) {
                    Office office = loginService.offices.get(i);
                    String stockId = office.getStockId();
                    if (!StringUtils.isEmpty(stockId)) {
                        if (i > 0) {
                            sbStockIds.append(",");
                        }
                        sbStockIds.append(stockId);
                    }

                    if (office.getCode().compareTo(loginService.curOfficeId) == 0) {
                        loginService.curStockId = office.getStockId();
                        loginService.curOfficeName = office.getValue();
                    }
                }
            }

            List<Subdis> subdises = userComInfo.getSubdisList();
            if (null != subdises) {
                for (int i = 0; i < subdises.size(); i++) {
                    //loginService.subdisNames.put(subdises.get(i).getId(), subdises.get(i).getSubdisName());
                    if (i > 0) {
                        sbSubdisNames.append(",");
                    }
                    sbSubdisNames.append(subdises.get(i).getSubdisName());
                }
            }
        }
        loginService.stockIds = sbStockIds.toString();
        loginService.subdisNames = sbSubdisNames.toString();
        loginService.sessionDate = new Date().getTime() + 1000 * 60 * 60 * 3;//三个小时内不去请求

        //保存新的登录信息前先判断是否已经切换账号
        isCompanyOrOfficeChanged = checkCompanyOrOffice();
        loginService.lastSpid = loginService.spid;
        loginService.lastOfficeId = loginService.curOfficeId;

        loginService.save();

        restore();
    }

}

