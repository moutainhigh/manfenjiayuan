package org.century.schemas;


import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * 读写器信息
 * Created by bingshanguxue on 4/21/16.
 */
public class ReaderInfoEX implements KvmSerializable, Serializable {
    public static final String NAMESPACE = "http://schemas.datacontract.org/2004/07/CENTURY_ESL.EntityEX";

    private String GetWay;
    private String MAC;
    private String ModuleName;
    private int ReaderID;
    private String ReaderIP;
    private String ReaderNo;
    private int ReaderPort;
    private String ServerIP;
    private int ServerPort;
//    private ReaderStatus Status;
//    private String SubNetMask;
//    private TagInfoEX[] tagInfos;


    public String getGetWay() {
        return GetWay;
    }

    public void setGetWay(String getWay) {
        GetWay = getWay;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public String getModuleName() {
        return ModuleName;
    }

    public void setModuleName(String moduleName) {
        ModuleName = moduleName;
    }

    public int getReaderID() {
        return ReaderID;
    }

    public void setReaderID(int readerID) {
        ReaderID = readerID;
    }

    public String getReaderIP() {
        return ReaderIP;
    }

    public void setReaderIP(String readerIP) {
        ReaderIP = readerIP;
    }

    public String getReaderNo() {
        return ReaderNo;
    }

    public void setReaderNo(String readerNo) {
        ReaderNo = readerNo;
    }

    public int getReaderPort() {
        return ReaderPort;
    }

    public void setReaderPort(int readerPort) {
        ReaderPort = readerPort;
    }

    public String getServerIP() {
        return ServerIP;
    }

    public void setServerIP(String serverIP) {
        ServerIP = serverIP;
    }

    public int getServerPort() {
        return ServerPort;
    }

    public void setServerPort(int serverPort) {
        ServerPort = serverPort;
    }

//    public ReaderStatus getStatus() {
//        return Status;
//    }
//
//    public void setStatus(ReaderStatus status) {
//        Status = status;
//    }
//
//    public String getSubNetMask() {
//        return SubNetMask;
//    }
//
//    public void setSubNetMask(String subNetMask) {
//        SubNetMask = subNetMask;
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
                return GetWay;
            case 1:
                return MAC;
            case 2:
                return ModuleName;
            case 3:
                return ReaderID;
            case 4:
                return ReaderIP;
            case 5:
                return ReaderNo;
            case 6:
                return ReaderPort;
            case 7:
                return ServerIP;
            case 8:
                return ServerPort;
//            case 9:
//                return Status;
//            case 10:
//                return SubNetMask;
//            case 11:
//                return tagInfos;
            default:
                return null;
        }
    }

    @Override
    public int getPropertyCount() {
        return 9;
    }

    @Override
    public void setProperty(int i, Object o) {
        switch (i) {
            case 0:
                GetWay = o.toString();
                break;
            case 1:
                MAC = o.toString();
                break;
            case 2:
                ModuleName = o.toString();
                break;
            case 3:
                ReaderID = Integer.parseInt(o.toString());
                break;
            case 4:
                ReaderIP = o.toString();
                break;
            case 5:
                ReaderNo = o.toString();
                break;
            case 6:
                ReaderPort = Integer.parseInt(o.toString());
                break;
            case 7:
                ServerIP = o.toString();
                break;
            case 8:
                ServerPort = Integer.parseInt(o.toString());
                break;
//            case 9:
//                Status = (ReaderStatus) o;
//                break;
//            case 10:
//                SubNetMask = (String) o;
//                break;
//            case 11:
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
                propertyInfo.name = "GetWay";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 1:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "MAC";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 2:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "ModuleName";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 3:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "ReaderID";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 4:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "ReaderIP";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 5:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "ReaderNo";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 6:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "ReaderPort";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 7:
                propertyInfo.type = PropertyInfo.STRING_CLASS;
                propertyInfo.name = "ServerIP";
                propertyInfo.namespace = NAMESPACE;
                break;
            case 8:
                propertyInfo.type = PropertyInfo.INTEGER_CLASS;
                propertyInfo.name = "ServerPort";
                propertyInfo.namespace = NAMESPACE;
                break;
//            case 9:
//                propertyInfo.type = ReaderStatus.class;
//                propertyInfo.name = "Status";
//                break;
//            case 10:
//                propertyInfo.type = PropertyInfo.STRING_CLASS;
//                propertyInfo.name = "SubNetMask";
//                propertyInfo.namespace = NAMESPACE;
//                break;
//            case 11:
//                propertyInfo.type = PropertyInfo.OBJECT_CLASS;
//                propertyInfo.name = "tagInfos";
//                propertyInfo.namespace = NAMESPACE;
//                break;
            default:
                break;
        }
    }
}


