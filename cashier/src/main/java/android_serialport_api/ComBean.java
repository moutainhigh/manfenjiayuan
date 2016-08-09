package android_serialport_api;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author benjaminwan
 * 串口数据
 */
public class ComBean {
	private static SimpleDateFormat sDateFormat = new SimpleDateFormat("hh:mm:ss", Locale.US);
		public byte[] bRec=null;
		public String sRecTime="";
		public String sComPort="";
		public ComBean(String sPort,byte[] buffer,int size){
			sComPort=sPort;
			bRec=new byte[size];
			System.arraycopy(buffer, 0, bRec, 0, size);
			sRecTime = sDateFormat.format(new java.util.Date()); 
		}
}