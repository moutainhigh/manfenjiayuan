package com.bingshanguxue.cashier.hardware;

import android.os.Bundle;

/**
 * Created by bingshanguxue on 15/9/7.
 */
public class SerialPortEvent {
    public static final int SERIAL_TYPE_DISPLAY = 0;//屏显（JOOYTEC）
    ////打印机
    public static final int RINTER_PRINT_PRIMITIVE = 1;//byte[]
    public static final int UPDATE_PORT_GPRINTER = 2;
    public static final int UPDATE_PORT_SCALE= 3;

    public static final int SERIAL_TYPE_VFD = 5;
    public static final int SERIAL_TYPE_VFD_INIT = 6;
    public static final int SERIAL_TYPE_VFD_BYTE = 7;


    int type;
    String cmd;
    byte[] cmdBytes;

    private Bundle args;

    public SerialPortEvent(int type, String cmd) {
        this.type = type;
        this.cmd = cmd;
    }

    public SerialPortEvent(int type, byte[] cmdBytes) {
        this.type = type;
        this.cmdBytes = cmdBytes;
    }

    public SerialPortEvent(int type, Bundle args) {
        this.type = type;
        this.args = args;
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
