package android.serialport.api;

public interface SerialPortDataReceived {
	void onDataReceivedListener(final byte[] buffer, final int size);
	void onDataReceivedStringListener(final String barcodeStr);

}
