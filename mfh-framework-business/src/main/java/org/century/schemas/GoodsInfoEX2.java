package org.century.schemas;


import com.mfh.framework.core.utils.StringUtils;

import org.century.TableDefinition;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 商品信息
 * Created by bingshanguxue on 4/21/16.
 */
public class GoodsInfoEX2 implements KvmSerializable, Serializable {

    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";


    private String GoodsCode;//Code of merchandise [店内码(商品编码)]
    //    private int GoodsID;
    private Vector<Property> Properties;//Pair of property ID and property value [属性值对]
//    private GoodStatus Status;
//    private TagInfoEX[] labels;
//    private TagInfoEX[] tagInfos;


    public String getGoodsCode() {
        return GoodsCode;
    }

    public void setGoodsCode(String goodsCode) {
        GoodsCode = goodsCode;
    }
//
//    public int getGoodsID() {
//        return GoodsID;
//    }
//
//    public void setGoodsID(int goodsID) {
//        GoodsID = goodsID;
//    }

    public Vector<Property> getProperties() {
        return Properties;
    }

    public void setProperties(Vector<Property> properties) {
        Properties = properties;
    }

//    public GoodStatus getStatus() {
//        return Status;
//    }
//
//    public void setStatus(GoodStatus status) {
//        Status = status;
//    }

    //
//    public TagInfoEX[] getLabels() {
//        return labels;
//    }
//
//    public void setLabels(TagInfoEX[] labels) {
//        this.labels = labels;
//    }
//
//    public TagInfoEX[] getTagInfos() {
//        return tagInfos;
//    }
//
//    public void setTagInfos(TagInfoEX[] tagInfos) {
//        this.tagInfos = tagInfos;
//    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return getGoodsCode();
            case 1:
//                return getGoodsID();
//            case 2:
                return getProperties();
//            case 2:
//                return Status;
//            case 4:
//                return labels;
//            case 5:
//                return tagInfos;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 3;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
                GoodsCode = o.toString();
                break;
            case 1:
//                GoodsID = Integer.parseInt(o.toString());
//                break;
//            case 2:
                Properties = (Vector<Property>) o;
                break;
//            case 2:
//            Status = (GoodStatus) o;
//                break;
//            case 4:
//                labels = (TagInfoEX[]) o;
//                break;
//            case 5:
//                tagInfos = (TagInfoEX[]) o;
//                break;
            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {
        switch (i) {
            case 0:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "GoodsCode";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 1:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "GoodsID";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 2:
                propertyInfo.type = PropertyInfo.VECTOR_CLASS;
                propertyInfo.name = "Properties";
                propertyInfo.namespace = NAMESPACE;
                break;
//            case 2:
//                propertyInfo.type = PropertyInfo.OBJECT_CLASS;
//                propertyInfo.name = "Status";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 4:
//                propertyInfo.type = PropertyInfo.VECTOR_CLASS;
//                propertyInfo.name = "labels";
//            propertyInfo.namespace = NAMESPACE;
//                break;
//            case 5:
//                propertyInfo.type = PropertyInfo.VECTOR_CLASS;
//                propertyInfo.name = "tagInfos";
//            propertyInfo.namespace = NAMESPACE;
//                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
//        return super.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\tGoodsCode:").append(GoodsCode).append("\n");
//        sb.append("\tGoodsID:").append(GoodsID).append("\n");
        sb.append("\tProperties:[\n");
//        sb.append("\tStatus:").append(Status.value).append("\n");
        Vector<Property> properties = getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                sb.append("\t\t");
                Property property = properties.get(i);
                sb.append(property.toString());
                if (i < properties.size()-1) {
                    sb.append(",\n");
                }
            }
        }
        sb.append("\t]\n");
        sb.append("}\n");
        return sb.toString();
    }

    public static GoodsInfoEX2 createDefault(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            barcode = StringUtils.getNonceDecimalString(6);
        }
        GoodsInfoEX2 googsInfoEX = new GoodsInfoEX2();
        googsInfoEX.setGoodsCode(barcode);

        //更新商品属性
        Vector<Property> propertyList = new Vector<>();
        propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_GOODSCODE, barcode));
//        propertyList.add(new Property(DataType.Text, TableDefinition.TB_GOODS_COLOUMNINDEX_GOODSCODE,
//                "GoodsCode", barcode));
//        propertyList.add(new Property(DataType.Text, TableDefinition.TB_GOODS_COLOUMNINDEX_NAME,
//                "name", StringUtils.genNonceChinease(4)));
//        propertyList.add(new Property(2, "origin", StringUtils.genNonceChinease(4)));
        googsInfoEX.setProperties(propertyList);
//        googsInfoEX.setStatus(GoodStatus.ALLCOMPLETE);

        return googsInfoEX;
    }
}
