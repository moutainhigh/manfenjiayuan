package org.century.schemas;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * 商品属性 Goods Property
 * <p>
 *     The maps between property ID and its meaning as follows:
 *     Property ID  Property Name   Mark

 <ul>
 <li>
 1--Code of merchandise.店内码(商品编码)<br>
 It’s a necessary property, and must be same as _goodscode field. It is the unique flag in the customer ’s retail system.
 必须有,而且与_goodscode 相同。该信息是指客户零售系统中商品的唯一标 识(比如条形码)
 </li>
 <li>
 2--Barcode.条形码
 Unnecessary.非必须
 </li>
 </ul>

 * </p>
 *
 * Created by bingshanguxue on 4/21/16.
 */
public class ModelType implements KvmSerializable, Serializable {
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";

    public static final String _UNKNOWN = "UNKNOWN";
    public static final String _TE836N_3B = "TE836N_3B";
    public static final String _TB835N_3B = "TB835N_3B";
    public static final String _TE819N_MP_3B = "TE819N_MP_3B";
    public static final String _TE839N_3B = "TE839N_3B";
    public static final String _TE836L_4B = "TE836L_4B";
    public static final String _TS818N_3B = "TS818N_3B";
    public static final String _TS822N_3B = "TS822N_3B";
    public static final String _TS800N_3B = "TS800N_3B";
    public static final String _TS817N_3B = "TS817N_3B";
    public static final String _TE819N_3B = "TE819N_3B";
    public static final String _TE832N_3B = "TE832N_3B";
    public static final String _TS819N_3B = "TS819N_3B";
    public static final String _TS823L_4B = "TS823L_4B";
    public static final String _TE836N_4B = "TE836N_4B";
    public static final String _TE836C_4B = "TE836C_4B";
    public static final String _TS824L_4B = "TS824L_4B";
    public static final String _TE837N_4B = "TE837N_4B";
    public static final String _TE832N_4B = "TE832N_4B";
    public static final String _TE832N_NFC = "TE832N_NFC";
    public static final String _TE819N_4B = "TE819N_4B";
    public static final String _TB835N_4B = "TB835N_4B";
    public static final String _TS817N_4B = "TS817N_4B";
    public static final String _TS818N_4B = "TS818N_4B";
    public static final String _TS800N_4B = "TS800N_4B";
    public static final String _TS820N_4B = "TS820N_4B";
    public static final String _TS822N_4B = "TS822N_4B";
    public static final String _TS870L_4B = "TS870L_4B";
    public static final String _TF880N_4B = "TF880N_4B";
    public static final String _TF880C_4B = "TF880C_4B";
    public static final String _TS825N_4B = "TS825N_4B";
    public static final String _TE843N_4B = "TE843N_4B";
    public static final String _TS829L_4B = "TS829L_4B";
    public static final String _TB851N_4B = "TB851N_4B";
    public static final String _TE832N_A_4B = "TE832N_A_4B";
    public static final String _TS826L_4B = "TS826L_4B";
    public static final String _TE840N_4B = "TE840N_4B";
    public static final String _LT154EB = "LT154EB";
    public static final String _LT154EA = "LT154EA";
    public static final String _LT154A = "LT154A";
    public static final String _LG213EA = "LG213EA";
    public static final String _LG290EA = "LG290EA";
    public static final String _LT420A_4B = "LT420A_4B";
    public static final String _LT290A_V = "LT290A_V";
    public static final String _LT750A = "LT750A";
    public static final String _LT750C = "LT750C";
    public static final ModelType UNKNOWN = new ModelType(_UNKNOWN);
    public static final ModelType TE836N_3B = new ModelType(_TE836N_3B);
    public static final ModelType TB835N_3B = new ModelType(_TB835N_3B);
    public static final ModelType TE819N_MP_3B = new ModelType(_TE819N_MP_3B);
    public static final ModelType TE839N_3B = new ModelType(_TE839N_3B);
    public static final ModelType TE836L_4B = new ModelType(_TE836L_4B);
    public static final ModelType TS818N_3B = new ModelType(_TS818N_3B);
    public static final ModelType TS822N_3B = new ModelType(_TS822N_3B);
    public static final ModelType TS800N_3B = new ModelType(_TS800N_3B);
    public static final ModelType TS817N_3B = new ModelType(_TS817N_3B);
    public static final ModelType TE819N_3B = new ModelType(_TE819N_3B);
    public static final ModelType TE832N_3B = new ModelType(_TE832N_3B);
    public static final ModelType TS819N_3B = new ModelType(_TS819N_3B);
    public static final ModelType TS823L_4B = new ModelType(_TS823L_4B);
    public static final ModelType TE836N_4B = new ModelType(_TE836N_4B);
    public static final ModelType TE836C_4B = new ModelType(_TE836C_4B);
    public static final ModelType TS824L_4B = new ModelType(_TS824L_4B);
    public static final ModelType TE837N_4B = new ModelType(_TE837N_4B);
    public static final ModelType TE832N_4B = new ModelType(_TE832N_4B);
    public static final ModelType TE832N_NFC = new ModelType(_TE832N_NFC);
    public static final ModelType TE819N_4B = new ModelType(_TE819N_4B);
    public static final ModelType TB835N_4B = new ModelType(_TB835N_4B);
    public static final ModelType TS817N_4B = new ModelType(_TS817N_4B);
    public static final ModelType TS818N_4B = new ModelType(_TS818N_4B);
    public static final ModelType TS800N_4B = new ModelType(_TS800N_4B);
    public static final ModelType TS820N_4B = new ModelType(_TS820N_4B);
    public static final ModelType TS822N_4B = new ModelType(_TS822N_4B);
    public static final ModelType TS870L_4B = new ModelType(_TS870L_4B);
    public static final ModelType TF880N_4B = new ModelType(_TF880N_4B);
    public static final ModelType TF880C_4B = new ModelType(_TF880C_4B);
    public static final ModelType TS825N_4B = new ModelType(_TS825N_4B);
    public static final ModelType TE843N_4B = new ModelType(_TE843N_4B);
    public static final ModelType TS829L_4B = new ModelType(_TS829L_4B);
    public static final ModelType TB851N_4B = new ModelType(_TB851N_4B);
    public static final ModelType TE832N_A_4B = new ModelType(_TE832N_A_4B);
    public static final ModelType TS826L_4B = new ModelType(_TS826L_4B);
    public static final ModelType TE840N_4B = new ModelType(_TE840N_4B);
    public static final ModelType LT154EB = new ModelType(_LT154EB);
    public static final ModelType LT154EA = new ModelType(_LT154EA);
    public static final ModelType LT154A = new ModelType(_LT154A);
    public static final ModelType LG213EA = new ModelType(_LG213EA);
    public static final ModelType LG290EA = new ModelType(_LG290EA);
    public static final ModelType LT420A_4B = new ModelType(_LT420A_4B);
    public static final ModelType LT290A_V = new ModelType(_LT290A_V);
    public static final ModelType LT750A = new ModelType(_LT750A);
    public static final ModelType LT750C = new ModelType(_LT750C);
    
    public String value;

    public ModelType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
//                return id;
//            case 1:
                return value;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
//                id = (Integer) o;
//                break;
//            case 1:
                value = (String) o;
                break;
            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch (i) {
            case 0:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "id";
//                break;
//            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "value";
                propertyInfo.namespace = NAMESPACE;
                break;
            default:
                break;
        }
    }
}
