package com.manfenjiayuan.pda_supermarket.ui.store;

import com.bingshanguxue.pda.bizz.home.HomeMenu;
import com.manfenjiayuan.pda_supermarket.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingshanguxue on 04/07/2017.
 */

public class ResourcesManager {
    public static List<HomeMenu> STORE_MENUS = new ArrayList<>();
    public static List<HomeMenu> BUY_MENUS = new ArrayList<>();
    public static List<HomeMenu> STOCK_MENUS = new ArrayList<>();


    static {
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_CASHIER,
                "收银", R.mipmap.ic_cashier));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_GOODS,
                "商品", R.mipmap.ic_goods));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_STORE_IN,
                "商品建档", R.mipmap.ic_goods_storein));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_SENDORDER_NEW,
                "订货", R.mipmap.ic_order_goods));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_RECVORDER_NEW,
                "收货", R.mipmap.ic_receive_goods));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_RECVORDER_CONVERT,
                "转换收货", R.mipmap.ic_invrecvorder_convert));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_RETURNORDER,
                "退货", R.mipmap.ic_return_goods));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_OUT,
                "出库", R.mipmap.ic_stock_out));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_STOCK_IN,
                "入库", R.mipmap.ic_stock_in));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_INV_CONVERT,
                "库存转换", R.mipmap.ic_inv_convert));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_QUERY_BILL,
                "单据查询", R.mipmap.ic_query_bill));

        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_PRINT_TAGS,
                "价签打印", R.mipmap.ic_print_tags));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_BIND_GOODS_2_TAGS,
                "电子价签", R.mipmap.ic_bind_tags));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_WHOLESALER_GOODS_SHELVES,
                "货架号", R.mipmap.ic_shelves));

        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_CREATE_INV_LOSSORDER,
                "报损", R.mipmap.ic_report_loss));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_INVLOSSORDER_STOCKTAKE,
                "报损盘点", R.mipmap.ic_invlossorder_stock));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_INVCHECKORDER_STOCKTAKE,
                "盘点", R.mipmap.ic_stocktake));
        STORE_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_PICKUP_ORDER,
                "自提定安", R.mipmap.ic_package));


        BUY_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_BUY_SCORDER,
                "买手订单", R.mipmap.ic_buy_scorder));
        BUY_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_BUY_PREPARE,
                "组货", R.mipmap.ic_buy_prepare));


        STOCK_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_INSTOCK_SCORDER,
                "骑手订单", R.mipmap.ic_instock_scorder));
        STOCK_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_EMBRACE,
                "揽件", R.mipmap.ic_lanjian));
        STOCK_MENUS.add(new HomeMenu(HomeMenu.OPTION_ID_TUOTOU,
                "妥投", R.mipmap.ic_tuotou));
    }
}
