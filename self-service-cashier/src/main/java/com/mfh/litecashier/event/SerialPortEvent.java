package com.mfh.litecashier.event;

/**
 * Created by kun on 15/9/7.
 */
public class SerialPortEvent {
    public static final int SERIAL_TYPE_DISPLAY = 0;//屏显（JOOYTEC）
    ////打印机(Gprinter)
    public static final int SERIAL_TYPE_PRINTER = 1;
    public static final int SERIAL_TYPE_PRINTER_INIT = 2;
    ////秤(ACS-P215计价秤)
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
