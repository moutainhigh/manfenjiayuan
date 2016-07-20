package org.century.schemas;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 *
 * 输入参数包括6个参数，string类型(简单类型)的endTime,QueryType类型（复杂类型）的queryType；
 * minOccurs为最少出现次数；
 * <xs:complexType name="QueryCondition">
 *     <xs:sequence>
 *         <xs:element minOccurs="0" name="endTime" nillable="true" type="xs:string"/>
 *         <xs:element minOccurs="0" name="itemCount" type="xs:unsignedInt"/>
 *         <xs:element minOccurs="0" name="queryConditionSql" nillable="true" type="xs:string"/>
 *         <xs:element minOccurs="0" name="queryType" type="tns:QueryType"/>
 *         <xs:element minOccurs="0" name="startIndex" type="xs:unsignedInt"/>
 *         <xs:element minOccurs="0" name="startTime" nillable="true" type="xs:string"/>
 *     </xs:sequence>
 * </xs:complexType>
 * <xs:element name="QueryCondition" nillable="true" type="tns:QueryCondition"/>
 *
 * <xs:simpleType name="QueryType">
 *     <xs:restriction base="xs:string">
 *         <xs:enumeration value="Tag"/>
 *         <xs:enumeration value="TagHistory"/>
 *         <xs:enumeration value="Goods"/>
 *         <xs:enumeration value="GoodsHistory"/>
 *         <xs:enumeration value="Reader"/>
 *         <xs:enumeration value="ReaderHistory"/>
 *         <xs:enumeration value="Log"/>
 *         <xs:enumeration value="User"/>
 *     </xs:restriction>
 * </xs:simpleType>
 * <xs:element name="QueryType" nillable="true" type="tns:QueryType"/>
 *
 *
 * Created by bingshanguxue on 5/3/16.
 */
public class QueryCondition  implements KvmSerializable{
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";


    /**格式："1 like '%***%', 2 like '%***%', …其他条件…."
     <ul>
     注意以下几点：
     <li>多个条件用逗号“,”隔开；</li>
     <li>每个条件只能用like,（这是个BUG,还没有解决）</li>
     <li>每个条件的主语都是属性的ID号</li>
     </ul>
     */
    private String queryConditionSql;
    private int startIndex;
    private int itemCount;
    private QueryType queryType;
    private String startTime;
    private String endTime;


    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getQueryConditionSql() {
        return queryConditionSql;
    }

    public void setQueryConditionSql(String queryConditionSql) {
        this.queryConditionSql = queryConditionSql;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public Object getProperty(int i) {
        switch (i) {
            case 0:
                return queryConditionSql;
            case 1:
                return startIndex;
            case 2:
                return itemCount;
            case 3:
                return queryType;
            case 4:
                return startTime;
            case 5:
                return endTime;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 6;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
                queryConditionSql = o.toString();
                break;
            case 1:
                startIndex = Integer.parseInt(o.toString());
                break;
            case 2:
                itemCount =  Integer.parseInt(o.toString());
                break;
            case 3:
                queryType = (QueryType) o;
                break;
            case 4:
                startTime = o.toString();
                break;
            case 5:
                endTime = o.toString();
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
                propertyInfo.name = "queryConditionSql";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 1:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "startIndex";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 2:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "itemCount";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 3:
                propertyInfo.type = QueryType.class;
                propertyInfo.name = "queryType";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 4:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "startTime";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 5:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "endTime";
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
            } if(prop instanceof String) {
                sb.append(prop);
            } else {
                sb.append(prop.toString());
            }
        }
        sb.append("}");

        return sb.toString();
    }
}

