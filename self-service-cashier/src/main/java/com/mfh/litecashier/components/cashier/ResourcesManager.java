package com.mfh.litecashier.components.cashier;

import com.bingshanguxue.cashier.model.wrapper.ResMenu;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源管理器
 * Created by bingshanguxue on 30/06/2017.
 */

public class ResourcesManager {

    //收银
    public static final Long CASHIER_MENU_ONLINE_ORDER = 10L;//订单列表
    public static final Long CASHIER_MENU_REGISTER_VIP = 12L;//注册
    public static final Long CASHIER_MENU_MEMBER_CARD = 13L;//会员卡（开卡）
    public static final Long CASHIER_MENU_HANGUP_ORDER = 14L;//挂单
    public static final Long CASHIER_MENU_RETURN_GOODS = 15L;//退货
    public static final Long CASHIER_MENU_MONEYBOX = 17L;//钱箱
    public static final Long CASHIER_MENU_BALANCE_QUERY = 18L;//余额查询
    public static final Long CASHIER_MENU_SETTINGS = 19L;//设置
    public static final Long CASHIER_MENU_PACKAGE = 20L;//包裹(取快递)
    public static final Long CASHIER_MENU_PRINT_ORDER = 22L;//打印订单
    public static final Long CASHIER_MENU_DISCOUNT = 23L;//订单打折
    public static final Long CASHIER_MENU_SCORE = 24L;//积分兑换
    public static final Long CASHIER_MENU_TOPUP = 25L;//充值
    public static final Long CASHIER_MENU_CHANGE_PAY_PWD = 26L;//修改会员支付密码
    public static final Long CASHIER_MENU_CUSTOMER_TRANSACTION = 27L;//会员交易查询
    public static final Long CASHIER_MENU_PICKUP_ORDER = 28L;//打印取货单

    /**
     * 首页左侧功能菜单
     */
    public static List<ResMenu> getHomeMenus() {
        List<ResMenu> functionalList = new ArrayList<>();
        functionalList.add(new ResMenu(CASHIER_MENU_ONLINE_ORDER,
                "订单列表", R.mipmap.ic_service_online_order));
        functionalList.add(new ResMenu(CASHIER_MENU_DISCOUNT,
                "折扣", R.mipmap.ic_menu_cashier_discount));
        functionalList.add(new ResMenu(CASHIER_MENU_TOPUP,
                "充值", R.mipmap.ic_service_topup));
        functionalList.add(new ResMenu(CASHIER_MENU_CHANGE_PAY_PWD,
                "修改支付密码", R.mipmap.ic_service_change_pay_pwd));
        functionalList.add(new ResMenu(CASHIER_MENU_CUSTOMER_TRANSACTION,
                "交易查询", R.mipmap.ic_service_customer_transaction));
        functionalList.add(new ResMenu(CASHIER_MENU_PICKUP_ORDER,
                "商品自提", R.mipmap.ic_service_groupbuy_order));
        functionalList.add(new ResMenu(CASHIER_MENU_SCORE,
                "积分兑换", R.mipmap.ic_cashier_score));
        functionalList.add(new ResMenu(CASHIER_MENU_MEMBER_CARD,
                "办卡", R.mipmap.ic_service_membercard));
        functionalList.add(new ResMenu(CASHIER_MENU_HANGUP_ORDER,
                "挂单", R.mipmap.ic_service_hangup_order));
        functionalList.add(new ResMenu(CASHIER_MENU_RETURN_GOODS,
                "退货", R.mipmap.ic_service_returngoods));
        functionalList.add(new ResMenu(CASHIER_MENU_PRINT_ORDER,
                "打印订单", R.mipmap.ic_service_feedpaper));
        functionalList.add(new ResMenu(CASHIER_MENU_MONEYBOX,
                "钱箱", R.mipmap.ic_service_moneybox));
        functionalList.add(new ResMenu(CASHIER_MENU_SETTINGS,
                "设置", R.mipmap.ic_service_settings));
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
            functionalList.add(new ResMenu(CASHIER_MENU_BALANCE_QUERY,
                    "余额查询", R.mipmap.ic_service_balance));
            functionalList.add(new ResMenu(CASHIER_MENU_REGISTER_VIP,
                    "注册", R.mipmap.ic_service_register_vip));
//            functionalList.add(CashierFunctional.generate(CASHIER_MENU_PACKAGE,
//                    "包裹", R.mipmap.ic_service_package));
        }

        return functionalList;
    }
}
