package com.zkc.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zkc.Service.CaptureService;


public class ShutdownReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.i("ShutdownReceiver", "关机消息,关闭条码电源.........");
		CaptureService.scanGpio.closePower();
	}
}

