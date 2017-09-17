package com.manfenjiayuan.business.mvp.presenter;

import com.manfenjiayuan.business.mvp.mode.CustomerMode;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.mvp.view.ICustomerView;
import com.mfh.framework.rxapi.bean.Human;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.network.NetFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 会员
 * Created by bingshanguxue on 16/3/17.
 */
public class CustomerPresenter {
    private ICustomerView mICustomerView;
    private CustomerMode mCustomerMode;

    public CustomerPresenter(ICustomerView iCustomerView) {
        this.mICustomerView = iCustomerView;
        this.mCustomerMode = new CustomerMode();
    }

    /**
     * 搜索会员
     * @param content 会员卡芯片号／手机号／微信付款码
     * */
    public void getCustomerByOther(String content) {
        if (StringUtils.isEmpty(content)) {
            if (mICustomerView != null) {
                mICustomerView.onICustomerViewError(-1, null, "参数无效");
            }
            return;
        }

        //长度为8(466CAF31) ，会员卡芯片号
        if (content.length() == 8) {
            final String cardNo = MUtils.parseCardId(content);
            if (StringUtils.isEmpty(cardNo)) {
                if (mICustomerView != null) {
                    mICustomerView.onICustomerViewError(0, cardNo, "卡芯片号无效");
                }
                return;
            }

            getCustomerByOther(0, cardNo);
        }
        //长度为11(15250065084)，手机号
        else if (content.length() == 11) {
            getCustomerByOther(2, content);
        }
        //长度为15(000000000712878)，微信付款码
        else if (content.length() == 15) {
            String humanId = MUtils.parseMfPaycode(content);
            if (StringUtils.isEmpty(humanId)) {
                if (mICustomerView != null) {
                    mICustomerView.onICustomerViewError(1, humanId, "付款码无效");
                }
                return;
            }

            getCustomerByOther(1, humanId);
        } else {
            if (mICustomerView != null) {
                mICustomerView.onICustomerViewError(-1, null, "参数无效");
            }
        }
    }

    /**
     * 加载会员信息
     * @param type
     * <ul>
     *     <li>0--长度为8(466CAF31) ，会员卡芯片号</li>
     *     <li>1--长度为15(000000000712878)，微信付款码</li>
     *     <li>2--长度为11(15250065084)，手机号</li>
     * </ul>
     * @param content
     */
    public void getCustomerByOther(final int type, final String content) {
        Map<String, String> options = new HashMap<>();
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        if (!StringUtils.isEmpty(content)) {
            if (type == 0) {
                options.put("cardNo", content);
            } else if (type == 1) {
                options.put("humanId", content);
            } else if (type == 2) {
                options.put("mobile", content);
            }
        }

        mCustomerMode.getCustomerByOther(options, new OnModeListener<Human>() {
            @Override
            public void onProcess() {
                if (mICustomerView != null) {
                    mICustomerView.onICustomerViewLoading();
                }
            }

            @Override
            public void onSuccess(Human data) {
//                {"code":"0","msg":"查询成功!","version":"1","data":null}
                if (mICustomerView != null) {
                    mICustomerView.onICustomerViewSuccess(type, content, data);
                }
            }

            @Override
            public void onError(String errorMsg) {

                if (mICustomerView != null) {
                    mICustomerView.onICustomerViewError(type, content, errorMsg);
                }
            }
        });
    }


}
