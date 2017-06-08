package com.manfenjiayuan.business.presenter;

import com.manfenjiayuan.business.mode.CustomerTopupMode;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.ICustomerTopupView;
import com.mfh.framework.api.account.UserAccount;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.mvp.OnModeListener;
import com.mfh.framework.network.NetFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 充值
 * Created by bingshanguxue on 16/3/17.
 */
public class CustomerTopupPresenter {
    private ICustomerTopupView mICustomerTopupView;
    private CustomerTopupMode mCustomerTopupMode;

    public CustomerTopupPresenter(ICustomerTopupView iCustomerView) {
        this.mICustomerTopupView = iCustomerView;
        this.mCustomerTopupMode = new CustomerTopupMode();
    }

    public void getCustomerByOther(String content) {
        if (StringUtils.isEmpty(content)) {
            if (mICustomerTopupView != null) {
                mICustomerTopupView.onICustomerTopupViewError(-1, null, "参数无效");
            }
            return;
        }

        //长度为8(466CAF31) ，会员卡芯片号
        if (content.length() == 8) {
            final String cardNo = MUtils.parseCardId(content);
            if (StringUtils.isEmpty(cardNo)) {
                if (mICustomerTopupView != null) {
                    mICustomerTopupView.onICustomerTopupViewError(0, cardNo, "卡芯片号无效");
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
                if (mICustomerTopupView != null) {
                    mICustomerTopupView.onICustomerTopupViewError(1, humanId, "付款码无效");
                }
            }

            getCustomerByOther(1, humanId);
        } else {
            if (mICustomerTopupView != null) {
                mICustomerTopupView.onICustomerTopupViewError(-1, null, "参数无效");
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

        mCustomerTopupMode.transferFromMyAccount(options, new OnModeListener<UserAccount>() {
            @Override
            public void onProcess() {
                if (mICustomerTopupView != null) {
                    mICustomerTopupView.onICustomerTopupViewLoading();
                }
            }

            @Override
            public void onSuccess(UserAccount data) {
                if (mICustomerTopupView != null) {
                    mICustomerTopupView.onICustomerTopupViewSuccess(type, content, data);
                }
            }

            @Override
            public void onError(String errorMsg) {

                if (mICustomerTopupView != null) {
                    mICustomerTopupView.onICustomerTopupViewError(type, content, errorMsg);
                }
            }
        });
    }


}
