package com.bingshanguxue.cashier.hardware.scale;

import com.alibaba.fastjson.JSON;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DataConvertUtil;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;

/**
 * 寺冈电子秤（DIGI-DS781）
 * Created by bingshanguxue on 5/18/16.
 */
public class SMScaleHelper {

    /**
     * DataFormat
     * <ol>
     * Without additional parity (Total 37 Bytes )
     * </ol>
     * <p/>
     * <p/>
     * //[
     * 43 0D
     * // 30 30 30 2E 30 30 30 0D
     * // 34 30 30 2E 30 30 30 0D
     * // 55 30 30 30 2E 30 30 0D
     * // 54 30 30 30 30 2E 30 30 0D
     * // 0A
     * ]
     * <p/>
     * // [
     * // C CR                      （StatusFlag+WeightConditionFlag+CR）
     * // 0 0 0 . 0 0 0 CR     1+6+1（HeaderCode+ NetWeight+ CR）
     * // 4 0 0 . 0 0 0 CR     1+6+1（HeaderCode+ TareWeight+CR）
     * // U 0 0 0 . 0 0 CR     1+6+1（HeaderCode+ UnitPrice+ CR）
     * // T 0 0 0 0 . 0 0 CR   1+7+1（HeaderCode+ TotalPrice+CR）
     * // LF                        （LF）
     * // ]
     */
    public static DS781A parseData(byte[] data) {
        if (data == null) {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.df("解析寺冈电子秤串口数据失败，空数据");
            }
            return null;
        }

        if (data.length == 37) {
            return parseFormat37(data);
        } else if (data.length == 35) {
            return parseFormat35(data);
        }
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
            ZLogger.ef(String.format("解析寺冈电子秤串口数据失败:<%d><%s>",
                data.length, DataConvertUtil.ByteArrToHex(data)));
        }

        return null;
    }


    /**
     * Without additional parity (Total 37 Bytes )
     * [STATUS_FLAG] [WEIGHT_CONDITION_FLAG] CR
     * 30 [NET_WEIGHT] CR
     *
     * */
    private static DS781A parse37(byte[] data) {


        byte[] netWeightData = new byte[6];
        System.arraycopy(data, 4, netWeightData, 0, 6);

        byte[] tareWeightData = new byte[6];
        System.arraycopy(data, 12, tareWeightData, 0, 6);

        byte[] unitpriceData = new byte[6];
        System.arraycopy(data, 20, unitpriceData, 0, 6);

        byte[] totalPriceData = new byte[7];
        System.arraycopy(data, 28, totalPriceData, 0, 6);

        return null;
    }


    /**
     * without statusflag and weightConditionFlag
     *
     * @B000.250400.000U000.00T0000.00
     * 40 42 0D
     * 30 30 30 2E 32 35 30 0D
     * 34 30 30 2E 30 30 30 0D
     * 55 30 30 30 2E 30 30 0D
     * 54 30 30 30 30 2E 30 30 0D
     * 0A
     *
     * <42 0D 30 30 30 2E 31 39 30 0D 34 30 30 2E 30 30 30 0D 55 30 30 30 2E 30 30 0D 54 30 30 30 30 2E 30 30 0D 0A >
     * <0D 30 30 30 2E 31 39 30 0D 34 30 30 2E 30 30 30 0D 55 30 30 30 2E 30 30 0D 54 30 30 30 30 2E 30 30 0D 0A >
     * @param data <0D
     *             30 30 30 2E 30 37 30 0D
     *             34 30 30 2E 30 30 30 0D
     *             55 30 30 30 2E 30 30 0D
     *             54 30 30 30 30 2E 30 30 0D
     *             0A >
     */
    public static DS781A parse(byte[] data) {
        try {
            if (data == null) {
                if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                    ZLogger.df("解析寺冈电子秤串口数据失败，空数据");
                }
                return null;
            }

            int len = data.length;ZLogger.d("len = " + len);

            if (len > 34) {
                return parseCase1(data);
            } else if (len == 34){
                return parseCase2(data);
            } else {
                return parseCase3(data);
            }
//            if (data.length < 34) {
//                if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
//                    ZLogger.df("解析寺冈电子秤串口数据失败，数据长度至少要有34位");
//                }
//                return null;
//            }
//
//            //由于串口接收数据可能不完整，这里从后往前计算
//
//            int pos = len - 1;
//
//            pos -= 8;ZLogger.d("total_price_pos = " + pos);
//            byte[] bTotalPrice = new byte[7];
//            if (data[pos - 1] == DS781A.HEADER_TOTAL_PRICE) {
//                System.arraycopy(data, pos, bTotalPrice, 0, 7);
//                pos -= 1;
//            }
//
//            pos -= 7;ZLogger.d("unit_price_pos = " + pos);
//            byte[] bUnitPrice = new byte[6];
//            if (data[pos - 1] == DS781A.HEADER_UNIT_PRICE) {
//                System.arraycopy(data, pos, bUnitPrice, 0, 6);
//                pos -= 1;
//            }
//
//            pos -= 7;ZLogger.d("target_weight_pos = " + pos);
//            byte[] bTareWeight = new byte[6];
//            if (data[pos - 1] == DS781A.HEADER_TARE_WEIGHT) {
//                System.arraycopy(data, pos, bTareWeight, 0, 6);
//                pos -= 1;
//            }
//
//            pos -= 7;ZLogger.d("net_weight_pos = " + pos);
//            byte[] bNetWeight = new byte[6];
//            if (data[pos - 1] == DS781A.HEADER_NET_WEIGHT) {
//                System.arraycopy(data, pos, bNetWeight, 0, 6);
//                pos -= 1;
//            }
//            ZLogger.d("pos = " + pos);
//            //0-2
//            byte statusFlag = data[0];
//            byte weightConditionFlag = data[1];
//
//            return generateDS781A(bNetWeight, bTareWeight, bUnitPrice, bTotalPrice);
        } catch (Exception e) {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.ef(e.toString());
            }
        }
        return null;

    }

    /**
     * 数据包长度大于34位
     * */
    private static DS781A parseCase1(byte[] data) {
        try {
            int len = data.length;ZLogger.d("len = " + len);
            int pos = len - 1;

            //倒序遍历，从第一个OD开始解析
            for (int i = pos; i >= 0; i--) {
                if (data[i] == DS781A.HEADER_TOTAL_PRICE) {
                    break;
                }
                len -= 1;
            }

            if (len >= 34) {
                byte[] temp = new byte[len];
                System.arraycopy(data, pos, temp, 0, len);
                return parseCase2(temp);
            }
        } catch (Exception e) {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.ef(e.toString());
            }
        }
        return null;
    }

    /**
     * 数据包长度等于34，正好是有效数据的临界点，不包括Status Flag，Weight Condition Flag，LF
     * */
    private static DS781A parseCase2(byte[] data) {
        try {
            int len = data.length;ZLogger.d("len = " + len);

            int start = len - 9;
            int end = len - 1;
            byte[] bTotalPrice = new byte[7];
            //判断随后一个数据是否是结束符
            if (data[end] == DS781A.TERMINATION_CR && data[start] == DS781A.HEADER_TOTAL_PRICE) {
                System.arraycopy(data, start + 1, bTotalPrice, 0, 7);
            } else {
                ZLogger.e("解析失败, TOTAL_PRICE 格式必须是<0D * * * * * * * 0D>");
                return null;
            }

            start -= 8;
            end -= 9;//remove header code
            byte[] bUnitPrice = new byte[6];
            if (data[end] == DS781A.TERMINATION_CR && data[start] == DS781A.HEADER_UNIT_PRICE) {
                System.arraycopy(data, start + 1, bUnitPrice, 0, 6);
            } else {
                ZLogger.e("解析失败, UNIT_PRICE 格式必须是<55 * * * * * * 0D>");
                return null;
            }

            start -= 8;
            end -= 8;//remove header code
            byte[] bTareWeight = new byte[6];
            if (data[end] == DS781A.TERMINATION_CR && data[start] == DS781A.HEADER_TARE_WEIGHT) {
                System.arraycopy(data, start + 1, bTareWeight, 0, 6);
            } else {
                ZLogger.e("解析失败, TARE_PRICE 格式必须是<34 * * * * * * 0D>");
                return null;
            }

            start -= 8;
            end -= 8;//remove header code
            byte[] bNetWeight = new byte[6];
            if (data[end] == DS781A.TERMINATION_CR && data[start] == DS781A.HEADER_NET_WEIGHT) {
                System.arraycopy(data, start + 1, bNetWeight, 0, 6);
            } else {
                ZLogger.e("解析失败, NET_PRICE 格式必须是<30 * * * * * * 0D>");
                return null;
            }

            //0-2
//            byte statusFlag = data[0];
//            byte weightConditionFlag = data[1];

            return generateDS781A(bNetWeight, bTareWeight, bUnitPrice, bTotalPrice);
        } catch (Exception e) {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.ef(e.toString());
            }

            return null;
        }
    }

    /**
     * 数据包长度小于34位
     * */
    private static DS781A parseCase3(byte[] data) {
        try {
            int len = data.length;ZLogger.d("len = " + len);
            int pos = len - 1;
            byte[] bNetWeight = new byte[6];

            //倒序遍历，从第一个OD开始解析
            for (int i = pos; i >= 0; i--) {
                if (data[i] == DS781A.TERMINATION_CR) {
                    pos = i;
                    if (pos >= 7 && data[pos - 7] == DS781A.TERMINATION_CR) {
                        System.arraycopy(data, pos - 6, bNetWeight, 0, 6);
                        return generateDS781A(bNetWeight, null, null, null);
                    }
                }
                len -= 1;
            }
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
        return null;
    }

    /**
     * 数据包长度小于34位
     * */
    public static DS781A parseCase4(byte[] data) {
        try {
            if (data == null) {
                if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                    ZLogger.d("解析寺冈电子秤串口数据失败，空数据");
                }
                return null;
            }

            int len = data.length;//ZLogger.d("len = " + len);
            if (len < 9) {
                if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                    ZLogger.d("解析失败, NET_PRICE 格式是<30 * * * * * * 0D> 为了验证数据的完整性，长度至少要等于9");
                }
                return null;
            }
            int end = len - 1;
            byte[] bNetWeight = new byte[6];

            //正序校验 1
            for (int i = 0; i <= end; i++) {
                if (data[i] == DS781A.HEADER_NET_WEIGHT) {
                    if (((i + 7) <= end && data[i + 7] == DS781A.TERMINATION_CR)
                            && ((i + 8) <= end && data[i + 8] == DS781A.HEADER_TARE_WEIGHT)) {
                        System.arraycopy(data, i+1, bNetWeight, 0, 6);
                        return generateDS781A(bNetWeight, null, null, null);
                    }
                }
            }

            //正序校验 2
            for (int i = 0; i <= end; i++) {
                if (data[i] == DS781A.TERMINATION_CR) {
                    if (((i + 1) <= end && data[i + 1] == DS781A.HEADER_NET_WEIGHT)
                            && ((i + 7) <= end && data[i + 7] == DS781A.TERMINATION_CR)) {
                        System.arraycopy(data, i+1, bNetWeight, 0, 6);
                        return generateDS781A(bNetWeight, null, null, null);
                    }
                }
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
        return null;
    }

    /**
     * <40 42 0D 30 30 30 2E 32 35 30 0D >
     * */
//    private static DS781A parse() {
//
//    }
    private static DS781A parseFormat37(byte[] data) {
        try {
            //0-2
            byte statusFlag = data[0];
            byte weightConditionFlag = data[1];

            //3-10
            byte[] bNetWeight = new byte[6];
            System.arraycopy(data, 4, bNetWeight, 0, 6);
            //11-18
            byte[] bTareWeight = new byte[6];
            System.arraycopy(data, 12, bTareWeight, 0, 6);
            //19-26
            byte[] bUnitPrice = new byte[6];
            System.arraycopy(data, 20, bUnitPrice, 0, 6);
            //27-35
            byte[] bTotalPrice = new byte[7];
            System.arraycopy(data, 28, bTotalPrice, 0, 7);

            return generateDS781A(bNetWeight, bTareWeight, bUnitPrice, bTotalPrice);
        } catch (Exception e) {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.ef(e.toString());
            }

            return null;
        }
    }

    /**
     * without statusflag and weightConditionFlag
     *
     * <42 0D 30 30 30 2E 31 39 30 0D 34 30 30 2E 30 30 30 0D 55 30 30 30 2E 30 30 0D 54 30 30 30 30 2E 30 30 0D 0A >
     * <0D 30 30 30 2E 31 39 30 0D 34 30 30 2E 30 30 30 0D 55 30 30 30 2E 30 30 0D 54 30 30 30 30 2E 30 30 0D 0A >
     * @param data <0D
     *             30 30 30 2E 30 37 30 0D
     *             34 30 30 2E 30 30 30 0D
     *             55 30 30 30 2E 30 30 0D
     *             54 30 30 30 30 2E 30 30 0D
     *             0A >
     */
    private static DS781A parseFormat35(byte[] data) {
//        ZLogger.d(String.format("解析寺冈电子秤串口数据:<%s>, hex:<%s>, 长度为: %d",
//                new String(data),
//                DataConvertUtil.ByteArrToHex(data), data.length));

        try {
            byte[] bNetWeight = new byte[6];
            System.arraycopy(data, 2, bNetWeight, 0, 6);
            byte[] bTareWeight = new byte[6];
            System.arraycopy(data, 10, bTareWeight, 0, 6);
            byte[] bUnitPrice = new byte[6];
            System.arraycopy(data, 18, bUnitPrice, 0, 6);
            byte[] bTotalPrice = new byte[7];
            System.arraycopy(data, 26, bTotalPrice, 0, 7);

            return generateDS781A(bNetWeight, bTareWeight, bUnitPrice, bTotalPrice);
        } catch (Exception e) {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.ef(String.format("解析寺冈电子秤串口数据失败:<%d><%s>/n%s",
                    data.length, DataConvertUtil.ByteArrToHex(data), e.toString()));
            }
            return null;
        }
    }

    private static DS781A generateDS781A(byte[] bNetWeight, byte[] bTareWeight, byte[] bUnitPrice, byte[] bTotalPrice) {
        try {
            String sNetWeight = DataConvertUtil.ByteArrToHex(bNetWeight, "");
            String sTareWeight = DataConvertUtil.ByteArrToHex(bTareWeight, "");
            String sUnitPrice = DataConvertUtil.ByteArrToHex(bUnitPrice, "");
            String sTotalPrice = DataConvertUtil.ByteArrToHex(bTotalPrice, "");
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.df(String.format("netWeight:%s, tareWeight=%s, unitPrice=%s, totalPrice=%s",
                    sNetWeight, sTareWeight, sUnitPrice, sTotalPrice));
            }

            String sNetWeight2 = DataConvertUtil.hexStr2Str(sNetWeight);
            String sTareWeight2 = DataConvertUtil.hexStr2Str(sTareWeight);
            String sUnitPrice2 = DataConvertUtil.hexStr2Str(sUnitPrice);
            String sTotalPrice2 = DataConvertUtil.hexStr2Str(sTotalPrice);
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.df(String.format("netWeight:%s, tareWeight=%s, unitPrice=%s, totalPrice=%s",
                    sNetWeight2, sTareWeight2, sUnitPrice2, sTotalPrice2));
            }

            DS781A ds781A = new DS781A();
            ds781A.setNetWeight(sNetWeight2 != null ? Double.parseDouble(sNetWeight2) : null);
            ds781A.setTareWeight(sTareWeight2 != null ? Double.parseDouble(sTareWeight2) : null);
            ds781A.setUnitPrice(sUnitPrice2 != null ? Double.parseDouble(sUnitPrice2) : null);
            ds781A.setTotalPrice(sTotalPrice2 != null ? Double.parseDouble(sTotalPrice2) : null);
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.df(JSON.toJSONString(ds781A));
            }

            return ds781A;
        } catch (Exception e) {
            if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
                ZLogger.ef("generateDS781A failed, " + e.toString());
            }
            return null;
        }
    }

}
