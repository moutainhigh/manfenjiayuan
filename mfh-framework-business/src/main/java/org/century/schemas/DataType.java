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
public class DataType implements KvmSerializable, Serializable {
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";

    public static final String _Text = "Text";
    public static final String _Blob = "Blob";
    public static final String _Numeric = "Numeric";
    public static final String _DataTime = "DataTime";
    public static final String _ImageDescriptionIndex = "ImageDescriptionIndex";
    public static final DataType Text = new DataType(_Text);
    public static final DataType Blob = new DataType(_Blob);
    public static final DataType Numeric = new DataType(_Numeric);
    public static final DataType DataTime = new DataType(_DataTime);
    public static final DataType ImageDescriptionIndex = new DataType(_ImageDescriptionIndex);


    public String value;

    public DataType(String value) {
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
                return getValue();
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
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "value";
                propertyInfo.namespace = NAMESPACE;
                break;
            default:
                break;
        }
    }
}
