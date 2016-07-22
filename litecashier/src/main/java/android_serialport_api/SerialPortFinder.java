/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import android.util.Log;

import com.mfh.framework.core.logger.ZLogger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

public class SerialPortFinder {

	public class Driver {
		public Driver(String name, String root) {
			mDriverName = name;
			mDeviceRoot = root;
		}
		private String mDriverName;
		private String mDeviceRoot;
		Vector<File> mDevices = null;
		public Vector<File> getDevices() {
			ZLogger.d("getDevices start");
			if (mDevices == null) {
				try{
					mDevices = new Vector<>();
					File dev = new File("/dev");
					File[] files = dev.listFiles();
					if (files != null && files.length > 0){
						for (File file : files){
							if (file.getAbsolutePath().startsWith(mDeviceRoot)) {
								ZLogger.d(TAG, "Found new device: " + file);
								mDevices.add(file);
							}
						}
					}
				}
				catch (Exception e){
					ZLogger.e("getDevices failed" + e.toString());
				}
			}

			ZLogger.d("getDevices start");
			return mDevices;
		}
		public String getName() {
			return mDriverName;
		}
	}

	private static final String TAG = "SerialPort";

	private Vector<Driver> mDrivers = null;

	Vector<Driver> getDrivers() throws IOException {
		ZLogger.d("getDrivers start");
		if (mDrivers == null) {
			mDrivers = new Vector<>();
			try{
				LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
				String l;
				while((l = r.readLine()) != null) {
					// Issue 3:
					// Since driver name may contain spaces, we do not extract driver name with split()
					String drivername = l.substring(0, 0x15).trim();
					String[] w = l.split(" +");
					if ((w.length >= 5) && (w[w.length-1].equals("serial"))) {
						ZLogger.d(TAG, "Found new driver " + drivername + " on " + w[w.length-4]);
						mDrivers.add(new Driver(drivername, w[w.length-4]));
					}
				}
				r.close();
			}
			catch (Exception e){
				ZLogger.e("getDrivers failed" + e.toString());
				return new Vector<>();
			}

		}

		ZLogger.d("getDrivers end");
		return mDrivers;
	}

	public String[] getAllDevices() {
		ZLogger.d("getAllDevices start");
		Vector<String> devices = new Vector<>();
		// Parse each driver
		Iterator<Driver> itdriv;
		try {
			Vector<Driver> drivers = getDrivers();
			if (drivers != null && drivers.size() > 0){
				for (Driver driver : drivers){
					Vector<File> driverDevices = driver.getDevices();
					if (driverDevices != null && driverDevices.size() > 0){
						for (File file : driverDevices) {
							String device = file.getName();
							String value = String.format("%s (%s)", device, driver.getName());
							devices.add(value);
						}
					}
				}
			}
//			itdriv = getDrivers().iterator();
//			while(itdriv.hasNext()) {
//				Driver driver = itdriv.next();
//				Vector<File> driverDevices = driver.getDevices();
//				if (driverDevices != null && driverDevices.size() > 0){
//					for (File file : driverDevices) {
//						String device = file.getName();
//						String value = String.format("%s (%s)", device, driver.getName());
//						devices.add(value);
//					}
//				}
//			}
		} catch (IOException e) {
			ZLogger.e("getAllDevices failed: " + e.toString());
			e.printStackTrace();
		}

		ZLogger.d("getAllDevices end");
		return devices.toArray(new String[devices.size()]);
	}

	public String[] getAllDevicesPath() {
		Vector<String> devices = new Vector<>();
		// Parse each driver
		Iterator<Driver> itdriv;
		try {
			itdriv = getDrivers().iterator();
			while(itdriv.hasNext()) {
				Driver driver = itdriv.next();
				for (File file : driver.getDevices()) {
					String device = file.getAbsolutePath();
					devices.add(device);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
	}
}
