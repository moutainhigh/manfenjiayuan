package org.century.schemas;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * 商品属性 Goods Property
 * <p>
 * The maps between property ID and its meaning as follows:
 * Property ID  Property Name   Mark
 * <p/>
 * <ul>
 * <li>
 * 1--Code of merchandise.店内码(商品编码)<br>
 * It’s a necessary property, and must be same as _goodscode field. It is the unique flag in the customer ’s retail system.
 * 必须有,而且与_goodscode 相同。该信息是指客户零售系统中商品的唯一标 识(比如条形码)
 * </li>
 * <li>
 * 2--Barcode.条形码
 * Unnecessary.非必须
 * </li>
 * </ul>
 * <p/>
 * </p>
 * <p/>
 * Created by bingshanguxue on 4/21/16.
 */
public class Property implements KvmSerializable, Serializable {
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";

//    public DataType DataType;
    public int ID; // Note: it’s not a random index, but it accordance with the above table.
//    public String PropertyName;
    /**
     * 必须有,而且与_goodscode 相同。该信息是指客户零售系统中商品的唯一标 识(比如条形码)
     */
    public String Value;

    public Property() {
    }


    public Property(int ID, String value) {
        this.ID = ID;
        this.Value = value;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return ID;
            case 1:
                return Value;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 2;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
                ID = Integer.parseInt(o.toString());
                break;
            case 1:
                Value = o.toString();
                break;
            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch (i) {
            case 0:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "ID";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "Value";
                propertyInfo.namespace = NAMESPACE;
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
//        return super.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append(ID).append("\t")
                .append(Value).append("}");
        return sb.toString();
    }
}
