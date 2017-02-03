package com.mfh.framework.api.posRegister;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.network.AfinalFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.Serializable;

/**
 * Created by bingshanguxue on 9/20/16.
 */

public class PosRegisterApi {
    public static String URL_POS_REGISTER = MfhApi.URL_BASE_SERVER + "/posRegister/";


    /**
     * <b>设备注册接口</b><br>
     * 传递唯一序列号如网卡序列号，后台返回一个整形编号<br>
     * /posRegister/create?jsonStr={"serialNo":"2222"}
     */
    private static String URL_CREATE = URL_POS_REGISTER + "create";

    private static String URL_UPDATE = URL_POS_REGISTER + "update";

    public static String URL_LIST = URL_POS_REGISTER + "list";

    public static void register() {

        URL_POS_REGISTER = MfhApi.URL_BASE_SERVER + "/posRegister/";

        URL_CREATE = URL_POS_REGISTER + "create";
        URL_UPDATE = URL_POS_REGISTER + "update";
        URL_LIST = URL_POS_REGISTER + "list";
    }


    /**
     * 入参
     * */
    public static class ParamsIn implements Serializable{
        private String serialNo;
        private String channelId;
        private String channelPointId;
        private Long netId;

        public ParamsIn() {
        }

        public String getSerialNo() {
            return serialNo;
        }

        public void setSerialNo(String serialNo) {
            this.serialNo = serialNo;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        public String getChannelPointId() {
            return channelPointId;
        }

        public void setChannelPointId(String channelPointId) {
            this.channelPointId = channelPointId;
        }

        public Long getNetId() {
            return netId;
        }

        public void setNetId(Long netId) {
            this.netId = netId;
        }
    }

    /**
     * 设备注册
     * http://devnew.manfenjiayuan.cn/pmc/posRegister/create?jsonStr={serialNo=222222}
     */
    public static void create(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_CREATE, params, responseCallback);
    }

    public static void update(String jsonStr, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
//        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_UPDATE, params, responseCallback);
    }

    public static void list(PageInfo pageInfo, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (pageInfo != null) {
            params.put("page", Integer.toString(pageInfo.getPageNo()));
            params.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
////        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
//        params.put("jsonStr", jsonStr);

        AfinalFactory.postDefault(URL_LIST, params, responseCallback);
    }
}
