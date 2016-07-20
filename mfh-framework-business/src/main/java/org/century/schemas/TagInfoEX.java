package org.century.schemas;


import com.mfh.framework.core.utils.StringUtils;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * 标签信息
 * Created by bingshanguxue on 4/20/16.
 * <pre>
 *     {
 * int _tagid;
 * string _tagno; // The actual identity of the Tag.
 * TagType _tagtype;
 * int? _tagtypeid;
 * TagStatus _tagstatus;
 * int _templeteid;
 * ReaderInfoEX _readerinfoex;
 * GoodsInfoEX _goodsinfoex;
 * }
 * </pre>
 */
public class TagInfoEX implements KvmSerializable , Serializable {
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";

//    private byte[] FnState;
//    private int Humidity;
//    private boolean LineStatus;
//    private int LowpowerStatus;
//    private ModelType modelType;
//    private byte[] OKState;
//    private TagStatus Status;
//    private int TagID;
    private String TagNo;
//    private int Temperature;
    private int TempleteID;
//    private int Voltage;
//    private GoodsInfoEX goodsInfoEX;
//    private ReaderInfoEX readerInfoEX;


    public String getTagNo() {
        return TagNo;
    }

    public void setTagNo(String tagNo) {
        TagNo = tagNo;
    }

    public int getTempleteID() {
        return TempleteID;
    }

    public void setTempleteID(int templeteID) {
        TempleteID = templeteID;
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
//            case 0:
//                return Humidity;
//            case 1:
//                return LineStatus;
//            case 2:
//                return LowpowerStatus;
//            case 3:
//                return modelType;
//            case 4:
//                return OKState;
//            case 6:
//                return status;
//            case 0:
//                return TagID;
            case 0:
                return TagNo;
//            case 5:
//                return Temperature;
            case 1:
                return TempleteID;
//            case 7:
//                return Voltage;
//            case 8:
//                return goodsInfoEX;
//            case 13:
//                return readerInfoEX;
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
//            case 0:
//                fnState = Base64.decode(o.toString(), Base64.DEFAULT);
////                break;
//            case 0:
//                Humidity = Integer.parseInt(o.toString());
//                break;
//            case 1:
//                LineStatus = (Boolean) o;
//                break;
//            case 2:
//                LowpowerStatus = Integer.parseInt(o.toString());
//                break;
//            case 3:
//                modelType = (ModelType) o;
//                break;
//            case 5:
//                OKState = Base64.decode(o.toString(), Base64.DEFAULT);
//                break;
//            case 6:
//                status = (TagStatus) o;
//                break;
//            case 3:
//                TagID = (Integer) o;
//                break;
            case 0:
                TagNo = o.toString();
                break;
//            case 5:
//                Temperature = (Integer) o;
//                break;
            case 1:
                TempleteID = Integer.parseInt(o.toString());
                break;
//            case 7:
//                Voltage = (Integer) o;
//                break;
//            case 8:
//                goodsInfoEX = (GoodsInfoEX) o;
//                break;
//            case 13:
//                readerInfoEX = (ReaderInfoEX) o;
//                break;
            default:
                break;
        }
    }

    @Override
    public void getPropertyInfo(int i, Hashtable hashtable, PropertyInfo propertyInfo) {

        switch (i) {
//            case 0:
//                propertyInfo.type = MarshalBase64.BYTE_ARRAY_CLASS;
//                propertyInfo.name = "fnState";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 0:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "Humidity";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 1:
//                propertyInfo.type = PropertyInfo.BOOLEAN_CLASS;
//                propertyInfo.name = "LineStatus";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 2:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "LowpowerStatus";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 3:
//                propertyInfo.type = ModelType.class;
//                propertyInfo.name = "ModelType";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 5:
//                propertyInfo.type = MarshalBase64.BYTE_ARRAY_CLASS;//PropertyInfo.VECTOR_CLASS;
//                propertyInfo.name = "OKState";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 6:
//                propertyInfo.type = TagStatus.class;
//                propertyInfo.name = "status";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 3:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "TagID";
//                propertyInfo.namespace = NAMESPACE;
//                break;
            case 0:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "TagNo";
                propertyInfo.namespace = NAMESPACE;
                break;
//            case 5:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "Temperature";
//                propertyInfo.namespace = NAMESPACE;
//                break;
            case 1:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "TempleteID";
                propertyInfo.namespace = NAMESPACE;
                break;
//            case 7:
//                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
//                propertyInfo.name = "Voltage";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 8:
//                propertyInfo.type = GoodsInfoEX.class;
//                propertyInfo.name = "goodsInfoEX";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 13:
//                propertyInfo.type = ReaderInfoEX.class;
//                propertyInfo.name = "readerInfoEX";
//                propertyInfo.namespace = NAMESPACE;
//                break;
            default:
                break;
        }
    }

    public static TagInfoEX createDefault(String tagNo) {
        if (StringUtils.isEmpty(tagNo)) {
            tagNo = StringUtils.getNonceDecimalString(6);
        }
        TagInfoEX tagInfoEX = new TagInfoEX();
        tagInfoEX.setTagNo(tagNo);
        tagInfoEX.setTempleteID(-1);
//        tagInfoEX.setModelType(ModelType.UNKNOWN);
//
//        GoodsInfoEX googsInfoEX = new GoodsInfoEX();
//        googsInfoEX.setGoodsCode(barcode);
//
//        //更新商品属性
//        ArrayOfProperty propertyList = new ArrayOfProperty();
//        propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_GOODSCODE, barcode));
//        propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_NAME, StringUtils.genNonceChinease(4)));
//        propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_PRICE, StringUtils.getNonceDecimalString(3)));
////        propertyList.add(new Property(TableDefinition.TB_GOODS_COLOUMNINDEX_NAME, StringUtils.genNonceChinease(4)));
////        propertyList.add(new Property(DataType.Text, TableDefinition.TB_GOODS_COLOUMNINDEX_GOODSCODE,
////                "GoodsCode", barcode));
////        propertyList.add(new Property(DataType.Text, TableDefinition.TB_GOODS_COLOUMNINDEX_NAME,
////                "name", StringUtils.genNonceChinease(4)));
////        propertyList.add(new Property(2, "origin", StringUtils.genNonceChinease(4)));
//        googsInfoEX.setProperties(propertyList);
////        googsInfoEX.setStatus(GoodStatus.ALLCOMPLETE);

        return tagInfoEX;
    }
}
