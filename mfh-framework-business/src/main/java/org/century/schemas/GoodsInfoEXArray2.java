package org.century.schemas;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;

/**
 *
 * Created by bingshanguxue on 5/3/16.
 */
public class GoodsInfoEXArray2 implements KvmSerializable, Serializable{
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";

    private ArrayOfGoodsInfoEX ArrayGoodsInfoEx;

    public ArrayOfGoodsInfoEX getArrayGoodsInfoEx() {
        return ArrayGoodsInfoEx;
    }

    public void setArrayGoodsInfoEx(ArrayOfGoodsInfoEX arrayGoodsInfoEx) {
        ArrayGoodsInfoEx = arrayGoodsInfoEx;
    }

    @Override
    public Object getProperty(int i) {
        if (i == 0){
            return ArrayGoodsInfoEx;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 2;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
                ArrayGoodsInfoEx =(ArrayOfGoodsInfoEX)o;
                break;
            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch (i) {

            case 0:
                propertyInfo.type = ArrayOfGoodsInfoEX.class;
                propertyInfo.name = "ArrayGoodsInfoEx";
                propertyInfo.namespace = NAMESPACE;
                break;
            default:
                break;
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        for(int i = 0; i < this.getPropertyCount(); ++i) {
            Object prop = getProperty(i);
            if (prop == null){
                continue;
            }
            if(prop instanceof PropertyInfo) {
                sb.append("").append(((PropertyInfo)prop).getName()).append("=").append(this.getProperty(i)).append("; ");
            } else {
                sb.append(prop.toString());
            }
        }
        sb.append("}");

        return sb.toString();
    }
}

