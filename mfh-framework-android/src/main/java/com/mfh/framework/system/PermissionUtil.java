/*
* Copyright 2015 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.mfh.framework.system;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
public abstract class PermissionUtil {

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    public static boolean checkSelfPermissions(Context context, String[] permissions) {
        // At least one result must be checked.
        if(permissions.length < 1){
            return false;
        }


//        PackageManager packageManager = context.getPackageManager();
//        //java.lang.SecurityException: ConnectivityService: Neither user 10103 nor current process has android.permission.ACCESS_NETWORK_STATE.
//        if (packageManager.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,
//                context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
//            ZLogger.wf("Neither user 10103 nor current process has android.permission.ACCESS_NETWORK_STATE");
//            return false;
//        }
        // Verify that each required permission has been granted, otherwise return false.
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}