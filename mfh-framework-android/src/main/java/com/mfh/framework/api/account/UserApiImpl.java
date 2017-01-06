package com.mfh.framework.api.account;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by bingshanguxue on 5/24/16.
 */
public class UserApiImpl extends UserApi {

    public static void validSession(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.getHttp(true).post(URL_VALID_SESSION, params, responseCallback);
    }

    /**
     * 修改用户密码
     *
     * {"code":"0","msg":"操作成功!","version":"1","data":""}
     *
     * @param humanId 登录用户编号
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     * 注：确认新密码在调用接口前做处理，默认确认新密码和新密码相同。
     * */
    public static void updateUserPassword(Long humanId, String oldPwd, String newPwd,
                                          AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        params.put("oldPwd", oldPwd);
        params.put("newPwd", newPwd);
        params.put("confirmPwd", newPwd);
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_USER_UPDATE_LOGINPWD, params, responseCallback);
    }


    /**
     * 更新用户个人资料
     * @param humanId
     * @param jsonString Json格式字符串
     * */
    public static void updateProfile(String jsonString, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("jsonStr", jsonString);
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_USER_UPDATE, params, responseCallback);
    }

    /**
     * 修改个人头像
     * @param humanId
     * */
    public static void uploadUserHeader(Long humanId, File file, AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("humanId", String.valueOf(humanId));
        try {
            params.put("fileToUpload", file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("不存在的文件:" + file.getAbsolutePath());
        }
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_USER_UPLOAD_HEAD, params, responseCallback);
    }

    /**
     * 获取用户个人信息,需要用户登录
     * <ol>适用场景
     *     <li>个人信息页面基本信息加载</li>
     * </ol>
     * */
    public static void getMyProfile(AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("JSESSIONID", MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_MY_PROFILE, params, responseCallback);
    }

    /**
     * 设置用户的默认服务网点
     * <ol>
     *     适用场景
     *     <li>门店用户注册后自动调用</li>
     * </ol>*/
    public static void createParamDirect(Long humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        JSONObject paramObject = new JSONObject();
        paramObject.put("humanId", humanId);
        paramObject.put("paramName", "defaultNet");//固定不变
        paramObject.put("paramValue", MfhLoginService.get().getCurOfficeId());//当前门店登录账号的网点编号
        params.put("param", paramObject.toJSONString());
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_CUSTOMER_CREATEPARAMDIRECT, params, responseCallback);
    }


    /**
     * 查询用户
     * <ol>
     *     适用场景
     *     <li>会员收银支付查询</li>
     * </ol>
     * */
    public static void findHumanByPhone(String phoneNumber, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("mobile", phoneNumber);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FIND_HUMAN_BY_IDENTITY, params, responseCallback);
    }
    /**
     * 查询用户
     * <ol>
     *     适用场景
     *     <li>会员收银支付查询</li>
     * </ol>
     * */
    public static void findHumanByCard(String cardNo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("cardNo", cardNo);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FIND_HUMAN_BY_IDENTITY, params, responseCallback);
    }
    /**
     * 查询用户
     * <ol>
     *     适用场景
     *     <li>会员收银支付查询</li>
     * </ol>
     * */
    public static void findHumanByHumanId(String humanId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("humanId", humanId);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_FIND_HUMAN_BY_IDENTITY, params, responseCallback);
    }

    /**
     * 获取默认参数
     * @param paramName
     * <ol>
     *     <li>defaultNet，默认网点</li>
     * </ol>*/
    public static void getMyPramValue(String paramName, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("paramName", paramName);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_GET_MYPARAMVALUE, params, responseCallback);
    }

    /**
     * 获取能力信息，可以调用接口获取小伙伴详细信息，包括可能存在的认证信息
     * */
    public static void queryPrivList(AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.getHttp(true).post(URL_QUERY_PRIVLIST, params, responseCallback);
    }

}
