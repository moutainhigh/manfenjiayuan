package com.mfh.framework.api.res;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

/**
 * 商城订单
 * Created by bingshanguxue on 9/22/16.
 */
public class ResApi {
    public static String URL_REMOTE = MfhApi.URL_BASE_SERVER + "/res/remote/";
    public static String URL_REMOTESAVE = MfhApi.URL_BASE_SERVER + "/res/remotesave/";



    /**
     * 图片删除
     * /res/remotesave/delete?id=1
     */
    private static String URL_REMOTESAVE_DELETE = URL_REMOTESAVE + "delete";


    /**
     * 文件下载
     * /res/remote/download?id=2513&type=1
     */
    private static String URL_REMOTE_UPLOAD = URL_REMOTE + "download";

    public static void register() {
        URL_REMOTE = MfhApi.URL_BASE_SERVER + "/res/remote/";
        URL_REMOTESAVE = MfhApi.URL_BASE_SERVER + "/res/remotesave/";
        URL_REMOTESAVE_DELETE = URL_REMOTESAVE + "delete";

        URL_REMOTE_UPLOAD = URL_REMOTE + "download";
    }



    /**
     * 删除文件
     *
     * @param id           素材编号
     * @param responseType 若responseType=1，则返回：
     *                     {"code":"0","msg":"新增成功!","version":"1", "data":{"val":"240 "}}
     *                     <p>
     *                     若responseType=3，则返回：
     *                     {"code":"0","msg":"新增成功!","version":"1",
     *                     "data":{"val":"240:http:// resource.manfenjiayuan.com /material/23510013092322164184.jpg"}}
     *                     其中的url部分可以直接在页面上进行显示。
     */
    public static void delete(Long id, Integer responseType, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("id", String.valueOf(id));
        params.put("responseType", String.valueOf(responseType));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_REMOTESAVE_DELETE, params, responseCallback);
    }

}
