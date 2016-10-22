package com.mfh.framework.api.scOrder;

import com.mfh.framework.api.MfhApi;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.AfinalFactory;
import com.mfh.framework.network.NetFactory;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import static com.mfh.framework.api.MfhApi.PARAM_KEY_JSESSIONID;

/**
 * 商城订单
 * Created by bingshanguxue on 9/22/16.
 */
public class ScOrderApi {
    private final static String URL_SCORDER = MfhApi.URL_BASE_SERVER + "/scOrder/";

    /**
     * 当前登录人员即发货人员，自己进行发货
     * /scOrder/sendOrder?orderId=
     */
    public static final String URL_SENDORDER = URL_SCORDER + "sendOrder";
    /**
     * 新增商城订单
     * /scOrder/saveOrder
     * */
    public final static String URL_SAVEORDER = URL_SCORDER + "saveOrder";
    /**
     * 查询满分小伙伴服务
     * */
    public final static String URL_COUNT_SERVICEMFHPARTER= URL_SCORDER + "countServiceMfhPartner";
    /**
     * 查询订单
     * /scOrder/getByCode?barcode=9903000000273899
     * */
    public final static String URL_GETBYCODE = URL_SCORDER + "getByCode";
    /**
     * 根据条码或订单编号查询订单,可以同时指定status进行验证。needDetail=true代表返回订单明细详情等。
     * /scOrder/getByBarcode?barcode=&status=0
     * */
    public final static String URL_GETBYBARCODE = URL_SCORDER + "getByBarcode";
    /**
     * 获取网点的配送规则
     * /scOrder/getTransFeeRule?netId
     * */
    public final static String URL_GETTRANSFREERULE = URL_SCORDER + "getTransFeeRule";


    /**
     * 当前登录买手可以抢单去组货（也就是拣货）的订单列表，若需要明确指定网点，则可以传递netId参数
     * /scOrder/findPrepareAbleOrders?rows=20&page=1
     * */
    public final static String URL_FINDPREPAREABLEORDERS = URL_SCORDER + "findPrepareAbleOrders";
    /**
     * 当前登录的小伙伴即买手收到消息后点击接单，id为订单编号
     * (针对还未组货的订单)
     * /scOrder/acceptOrderWhenOrdered?id=
     */
    public final static String URL_ACCEPTORDER_WHENORDERED = URL_SCORDER + "acceptOrderWhenOrdered";

    /**
     * 买手在线下POS设备上将组货到的商品明细称重，获取实际数量，然后调整订单。其中id是线上订单id。bcount是实际称重的数量，skuId是产品编号
     * /scOrder/updateCommitInfoWhenPrepaired?id=&jsonStr=[{skuId:11, bcount:10}, {skuId:12, bcount:10}]
     * */
    public final static String URL_UPDATECOMMITINFO_WHENPREPAIRED = URL_SCORDER + "updateCommitInfoWhenPrepaired";
    /**
     * 替换{@link #URL_UPDATECOMMITINFO_WHENPREPAIRED}接口，
     * 支持：“增加“妥投”,扫描订单条码,可以显示订单商品,点击打勾按钮,即可完成妥投。⽀支持单品的退 单,删除单品后,订单价格会发⽣生变化”，也使用这个接口updateCommitInfo，退单时这个明细的数量为0即可。
     * /scOrder/updateCommitInfo?id=&jsonStr=[{skuId:11, bcount:10}, {skuId:12, bcount:10}]
     * */
    public final static String URL_UPDATECOMMITINFO = URL_SCORDER + "updateCommitInfo";

    /**
     * 当前登录人员即买手或发货人员，选择一个骑手并进行发货，并且通知骑手，其中transHumanId为骑手编号，orderId为订单编号
     * (针对已组货的订单)
     * /scOrder/prepareOrder?orderId=&transHumanId=
     */
    public final static String URL_PREPAREORDER = URL_SCORDER + "prepareOrder";

    /**
     * 获取当前登录骑手待配送的订单列表,netId可不传
     * /scOrder/findSendAbleOrders?netId=1211&humanId=
     */
    public final static String URL_FINDSENDABLEORDERS = URL_SCORDER + "findSendAbleOrders";
    /**
     * {@link #URL_FINDSENDABLEORDERS}
     * 当前登录人员即骑手或配送人员，（无需自行接单,前面买手已指定),自己开始进行配送物流
     * /scOrder/sendToEndCustom?orderId=
     */
    public final static String URL_SEND_TOENDCUSTOM = URL_SCORDER + "sendToEndCustom";

    /**
     * 获取指定网点可配送抢单的订单列表，netId若不传则使用当前登录骑手所在网点
     * /scOrder/findAcceptAbleSendOrders?netId=1211;
     */
    public final static String URL_FINDACCEPTABLE_SENDORDERS = URL_SCORDER + "findAcceptAbleSendOrders";
    /**
     * {@link #URL_FINDACCEPTABLE_SENDORDERS}
     * 当前登录人员即骑手或配送人员，接单，并开始进行配送物流
     * /scOrder/acceptTransToEndCustom?orderId=
     */
    public final static String URL_ACCEPTTRANS_TOENDCUSTOM = URL_SCORDER + "acceptTransToEndCustom";


    /**
     * 骑手将某个订单送达前，调用本接口先判断是否需要补差价；返回差额，正值代表需要退钱给用户，负值代表需要用户补钱
     * /scOrder/checkOddAmount?id=
     */
    public final static String URL_CHECK_ODDAMOUNT = URL_SCORDER + "checkOddAmount";
    /**
     * 订单需要补差价时，当前登录的骑手调用扫用户码该接口执行补收货款（其实退款给客户也支持）。
     * 其中humanId为被扫码的用户id。
     * /scOrder/checkAndReturnOddAmount?id=&humanId= 或
     * /scOrder/checkAndReturnOddAmount?id=&payType=2|256
     */
    public final static String URL_CHECKANDRETURN_ODDAMOUNT = URL_SCORDER + "checkAndReturnOddAmount";
    /**
     * 骑手将订单配送给最终客户，订单流程结束
     * /scOrder/arriveToEndCustom?orderIds=12,22
     */
    public final static String URL_ARRIVE_TOENDCUSTOM = URL_SCORDER + "arriveToEndCustom";

    /**
     * 查询服务中的订单列表
     * 使用场景
     * <ol>
     *     买手或骑手查询手中服务中的订单列表,可以指定某个发货点.其中roleType=0代表买手，roleType=1代表骑手
     *     <li>/scOrder/findServicingOrders?netId=136076&roleType=0|1&rows=20&page=1</li>
     * </ol>
     * /scOrder/arriveToEndCustom?orderIds=12,22
     */
    public final static String URL_FIND_SERVICINGORDERS = URL_SCORDER + "findServicingOrders";

    /**
     * 查询已服务完毕的订单列表
     * 使用场景
     * <ol>
     *     骑手查询已服务完毕的订单列表,可以指定某个发货点;其中roleType=0代表买手，roleType=1代表骑手
     *     <li>/scOrder/findServicedOrders?netId=136076&roleType=1&rows=20&page=1</li>
     *     买手查询已配送的订单列表
     *     <li>/scOrder/findServicedOrders?netId=136076&roleType=0&status=3,6&rows=20&page=1</li>
     *     另外买手查询已完成(也就是已服务完毕)的订单列表
     *     <li>/scOrder/findServicedOrders?netId=136076&roleType=0&status=12&rows=20&page=1</li>
     * </ol>
     * /scOrder/arriveToEndCustom?orderIds=12,22
     */
    public final static String URL_FIND_SERVICEDORDERS = URL_SCORDER + "findServicedOrders";



    /**
     * 发货
     * http://devnew.manfenjiayuan.cn/pmc/scOrder/sendOrder?JSESSIONID=9d01cbf0-059b-4ee9-9391-1612e9276165&orderId=557612
     * 1:orderId参数不能为空!
     */
    public static void sendOrder(Long orderId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("orderId", orderId == null ? "" : String.valueOf(orderId));
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_SENDORDER, params, responseCallback);
    }

    /**
     * 新增商城订单
     * @param order 订单信息
     * @param items 订单明细
     * @param cartIds 购物车编号
     * @param ruleIds 促销规则
     * */
    public static void saveOrder(String order, String items, String cartIds, String ruleIds,
                                 AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put("order", order);
        params.put("items", items);
        params.put("cartIds", cartIds);
        params.put("ruleIds", ruleIds);
        params.put("needAmount", "true");

        AfinalFactory.postDefault(URL_SAVEORDER, params, responseCallback);
    }

    /**
     * 查询满分小伙伴服务
     * */
    public static void countServiceMfhPartner(AjaxCallBack<? extends Object> responseCallback){
        AjaxParams params = new AjaxParams();
        params.put(PARAM_KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AfinalFactory.postDefault(URL_COUNT_SERVICEMFHPARTER, params, responseCallback);
    }

    /**
     * 发货
     * http://devnew.manfenjiayuan.cn/pmc/scOrder/sendOrder?JSESSIONID=9d01cbf0-059b-4ee9-9391-1612e9276165&orderId=557612
     * 1:orderId参数不能为空!
     */
    public static void getByCode(String barcode, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        params.put("barcode", barcode);
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GETBYCODE, params, responseCallback);
    }

    /**
     * 获取网点的配送规则
     * /scOrder/getTransFeeRule?netId
     * @param netId 网点编号，为空的时候后台取默认的配置
     * */
    public static void getTransFeeRule(Long netId, AjaxCallBack<? extends Object> responseCallback) {
        AjaxParams params = new AjaxParams();
        if (netId != null){
            params.put("netId", String.valueOf(netId));
        }
        params.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AfinalFactory.postDefault(URL_GETTRANSFREERULE, params, responseCallback);
    }

}
