package com.bingshanguxue.cashier.hardware;

/**
 * Created by kun on 15/9/7.
 */
public class SerialPortEvent {
    public static final int SERIAL_TYPE_DISPLAY = 0;//屏显（JOOYTEC）
    ////打印机(Gprinter)
    public static final int GPRINTER_SEND_DATA = 1;
    public static final int UPDATE_PORT_GPRINTER = 2;
    public static final int UPDATE_PORT_SMSCALE= 3;
    public static final int UPDATE_PORT_AHSCALE= 4;

    public static final int SERIAL_TYPE_VFD = 5;
    public static final int SERIAL_TYPE_VFD_INIT = 6;
    public static final int SERIAL_TYPE_VFD_BYTE = 7;

    int type;
    String cmd;
    byte[] cmdBytes;

    public SerialPortEvent(int type, String cmd) {
        this.type = type;
        this.cmd = cmd;
    }

    public SerialPortEvent(int type, byte[] cmdBytes) {
        this.type = type;
        this.cmdBytes = cmdBytes;
    }

    public int getType() {
        return type;
    }

    public String getCmd() {
        return cmd;
    }

    public byte[] getCmdBytes() {
        return cmdBytes;
    }
}
