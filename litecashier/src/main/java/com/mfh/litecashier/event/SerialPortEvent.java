package com.mfh.litecashier.event;

/**
 * Created by kun on 15/9/7.
 */
public class SerialPortEvent {
    public static final int SERIAL_TYPE_DISPLAY = 0;//屏显（JOOYTEC）
    ////打印机(Gprinter)
    public static final int SERIAL_TYPE_PRINTER = 2;
    public static final int SERIAL_TYPE_PRINTER_INIT = 3;
    public static final int SERIAL_TYPE_PRINTER_OPEN = 4;
    public static final int SERIAL_TYPE_PRINTER_CLOSE = 5;
    ////秤(ACS-P215计价秤)
    public static final int SERIAL_TYPE_SCALE= 6;
    public static final int SERIAL_TYPE_SCALE_INIT= 7;
    public static final int SERIAL_TYPE_SCALE_OPEN= 8;
    public static final int SERIAL_TYPE_SCALE_CLOSE= 9;

    public static final int SERIAL_TYPE_VFD = 10;
    public static final int SERIAL_TYPE_VFD_INIT = 11;
    public static final int SERIAL_TYPE_VFD_OPEN = 12;
    public static final int SERIAL_TYPE_VFD_CLOSE = 13;
    public static final int SERIAL_TYPE_VFD_BYTE = 14;

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
