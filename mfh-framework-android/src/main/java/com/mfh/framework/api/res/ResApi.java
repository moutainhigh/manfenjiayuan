package com.mfh.framework.api.res;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 商城订单
 * Created by bingshanguxue on 9/22/16.
 */
public class ResApi {
    private final static String URL_REMOTE = MfhApi.URL_BASE_SERVER + "/res/remote/";
    private final static String URL_REMOTESAVE = MfhApi.URL_BASE_SERVER + "/res/remotesave/";

    /**
     * 图片上传修改
     * 采用multipart/form-data或post方式提交图片。
     * /res/remotesave/upload? responseType=1
     */
    public static final String URL_REMOTESAVE_UPLOAD = URL_REMOTESAVE + "upload";


    /**
     * 图片删除
     * /res/remotesave/delete?id=1
     */
    public static final String URL_REMOTESAVE_DELETE = URL_REMOTESAVE + "delete";


    /**
     * 文件下载
     * /res/remote/download?id=2513&type=1
     */
    public static final String URL_REMOTE_UPLOAD = URL_REMOTE + "download";

    /**
     * 图片上传修改
     * 采用multipart/form-data或post方式提交图片。
     *
     * @param responseType 响应方式	1：只返回素材id 3：返回素材id和访问url
     */
    public static void upload(File file, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        try {
            if (file != null) {
                params.put("fileToUpload", file);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("不存在的文件:" + file.getAbsolutePath());
        }
        //
        params.put("responseType", "1");
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_REMOTESAVE_UPLOAD, params, responseCallback);
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
