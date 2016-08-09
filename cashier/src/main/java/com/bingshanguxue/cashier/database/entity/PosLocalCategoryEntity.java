package com.bingshanguxue.cashier.database.entity;

import com.mfh.comn.annotations.Table;
import com.mfh.comn.bean.ILongId;
import com.mfh.framework.core.MfhEntity;

/**
 * POS本地类目
 *
 * Created by Nat.ZZN(bingshanguxue) on 15-09-06..<br>
 * <table>
 *     <tr>
 *         <th>id</th>
 *     </tr>
 *     <tr>
 *         <td>Long</td>
 *     </tr>
 *     <tr>
 *         <td>自增主键</td>
 *     </tr>
 * </table>
 * ------------------------------------------------------------------------<br>
 * String       | int         | int      | Double   | String       | String<br>
 * orderBarCode | paystatus   | payType  | amount   | outTradeNo   | remark<br>
 * ------------------------------------------------------------------------<br>
 */
@Table(name="tb_pos_local_category_v1")
public class PosLocalCategoryEntity extends MfhEntity<Long> implements ILongId {

    /**
     * 本地商户订单号，64个字符以内、只能包含字母、数字、下划线;需保证在商户端不重复。<br>
     * 每一次支付记录都对应一个商户订单号
     * 终端号＋订单号＋时间戳（13位）
     * */
    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
