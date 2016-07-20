package com.mfh.enjoycity.service;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspListBean;
import com.mfh.enjoycity.bean.CommonAddrTemp;
import com.mfh.enjoycity.database.ReceiveAddressService;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logic.ServiceFactory;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

/**
 * Created by kun on 15/8/18.
 */
public class MfhUserService {

    private boolean receiveAddrLoaded;//是否加载过用户收货地址

    private static MfhUserService instance = null;

    /**
     * 获取MfhUserService实例
     * @return MfhUserService
     */
    public static MfhUserService getInstance() {
        String lsName = MfhUserService.class.getName();
        if (ServiceFactory.checkService(lsName))
            instance = ServiceFactory.getService(lsName);
        else {
            instance = new MfhUserService();//初始化登录服务
        }
        return instance;
    }

    public MfhUserService(){
        restore();
    }

    public void restore(){
        receiveAddrLoaded = false;
    }


    public void reset(){

    }

    /**
     * 获取用户收货地址列表
     * */
    public void loadReceiveAddr(){
        if (receiveAddrLoaded){
            return;
        }

        NetCallBack.NetTaskCallBack queryResponseCallback = new NetCallBack.NetTaskCallBack<CommonAddrTemp,
                NetProcessor.Processor<CommonAddrTemp>>(
                new NetProcessor.Processor<CommonAddrTemp>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                    java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                        if (rspData != null){
                            RspListBean<CommonAddrTemp> retValue = (RspListBean<CommonAddrTemp>) rspData;
                            ReceiveAddressService.get().init(retValue.getValue());
                            receiveAddrLoaded = true;
                        }
                    }

//                            @Override
//                            protected void processFailure(Throwable t, String errMsg) {
//                                super.processFailure(t, errMsg);
//                                Log.d("Nat: updateUserPassword.processFailure", errMsg);
//                                DialogUtil.showHint("修改登录密码失败");
//                            }
                }
                , CommonAddrTemp.class
                , MfhApplication.getAppContext())
        {
        };
        EnjoycityApiProxy.queryAllReceiveAddress(queryResponseCallback);

    }
}
