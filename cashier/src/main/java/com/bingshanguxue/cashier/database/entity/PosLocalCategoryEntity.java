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
@Table(name="tb_pos_local_category_v0001")
public class PosLocalCategoryEntity extends MfhEntity<Long> implements ILongId {

    public static final int CLOUD_ACTIVE = 1;
    public static final int CLOUD_DEACTIVE = 0;

    /**类目名称*/
    private String name = "";
    /**是否和云端同步:默认1同步，0不同步*/
    private int isCloudActive = CLOUD_DEACTIVE;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsCloudActive() {
        return isCloudActive;
    }

    public void setIsCloudActive(int isCloudActive) {
        this.isCloudActive = isCloudActive;
    }
}
