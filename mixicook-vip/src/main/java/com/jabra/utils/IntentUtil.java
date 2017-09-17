package com.jabra.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
//import android.provider.MediaStore.Audio.Media;
//import android.provider.MediaStore.Images.Media;
//import android.provider.MediaStore.Video.Media;
import java.io.File;
import java.util.List;

public class IntentUtil {
    public static Intent createExplicitFromImplicitIntent(Context paramContext, Intent paramIntent) {
        List localList = paramContext.getPackageManager().queryIntentServices(paramIntent, 0);
        if ((localList == null) || (localList.size() != 1)) {
            return null;
        }
        ResolveInfo localResolveInfo = (ResolveInfo) localList.get(0);
        ComponentName localComponentName = new ComponentName(localResolveInfo.serviceInfo.packageName, localResolveInfo.serviceInfo.name);
        Intent localIntent = new Intent(paramIntent);
        localIntent.setComponent(localComponentName);
        return localIntent;
    }

    public static void callTelephone(Context paramContext, String paramString) {
        paramContext.startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + paramString)));
    }

    public static String getDataColumn(Context paramContext, Uri paramUri, String paramString, String[] paramArrayOfString) {
        Cursor localCursor = null;
        String[] arrayOfString = {"_data"};
        try {
            localCursor = paramContext.getContentResolver().query(paramUri, arrayOfString, paramString, paramArrayOfString, null);
            if ((localCursor != null) && (localCursor.moveToFirst())) {
                String str = localCursor.getString(localCursor.getColumnIndexOrThrow("_data"));
                return str;
            }
        } finally {
            if (localCursor != null) {
                localCursor.close();
            }
        }
        if (localCursor != null) {
            localCursor.close();
        }
        return null;
    }

    @SuppressLint({"NewApi"})
    public static String getPath(Context paramContext, Uri paramUri) {
        int i;
        String str1 = null;
        if (Build.VERSION.SDK_INT >= 19) {
            i = 1;
            if ((i == 0) || (!DocumentsContract.isDocumentUri(paramContext, paramUri))) {
//        break;
            }
            if (!isExternalStorageDocument(paramUri)) {
//        break;
            }

            String[] arrayOfString3 = DocumentsContract.getDocumentId(paramUri).split(":");
            boolean bool4 = "primary".equalsIgnoreCase(arrayOfString3[0]);
            if (bool4) {
                str1 = Environment.getExternalStorageDirectory() + "/" + arrayOfString3[1];
            }
        }

        boolean bool1;
        do {
            boolean bool2;
            do {
//        return str1;
                i = 0;
//                break;
                if (isDownloadsDocument(paramUri)) {
                    String str3 = DocumentsContract.getDocumentId(paramUri);
                    return getDataColumn(paramContext, ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(str3).longValue()), null, null);
                }
                bool2 = isMediaDocument(paramUri);
                str1 = null;
            } while (!bool2);

            String[] arrayOfString1 = DocumentsContract.getDocumentId(paramUri).split(":");
            String str2 = arrayOfString1[0];
            Uri localUri = null;
            if ("image".equals(str2)) {
                localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            else if ("video".equals(str2)) {
                localUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else {
                boolean bool3 = "audio".equals(str2);
                localUri = null;
                if (bool3) {
                    localUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
            }

            if (arrayOfString1.length > 1) {
                String[] arrayOfString2 = new String[1];
                arrayOfString2[0] = arrayOfString1[1];
                return getDataColumn(paramContext, localUri, "_id=?", arrayOfString2);
            }

            if ("content".equalsIgnoreCase(paramUri.getScheme())) {
                if (isGooglePhotosUri(paramUri)) {
                    return paramUri.getLastPathSegment();
                }
                return getDataColumn(paramContext, paramUri, null, null);
            }
            bool1 = "file".equalsIgnoreCase(paramUri.getScheme());
            str1 = null;
        } while (!bool1);
        return paramUri.getPath();
    }

    public static boolean isDownloadsDocument(Uri paramUri) {
        return "com.android.providers.downloads.documents".equals(paramUri.getAuthority());
    }

    public static boolean isExternalStorageDocument(Uri paramUri) {
        return "com.android.externalstorage.documents".equals(paramUri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri paramUri) {
        return "com.google.android.apps.photos.content".equals(paramUri.getAuthority());
    }

    public static boolean isLocalUri(Uri paramUri) {
        return paramUri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
    }

    public static boolean isMediaDocument(Uri paramUri) {
        return "com.android.providers.media.documents".equals(paramUri.getAuthority());
    }

    public static void openAlbum(Activity paramActivity, int paramInt) {
        paramActivity.startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), paramInt);
    }

    public static void openTelephone(Context paramContext, String paramString) {
        paramContext.startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:" + paramString)));
    }

    public static void startActivity(Context paramContext, Class<?> paramClass) {
        paramContext.startActivity(new Intent(paramContext, paramClass));
    }

    public static void takePhoto(Activity paramActivity, int paramInt) {
        paramActivity.startActivityForResult(new Intent("android.media.action.IMAGE_CAPTURE"), paramInt);
    }

    public static void takePhoto(Activity paramActivity, int paramInt, String paramString) {
        Intent localIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        localIntent.putExtra("output", Uri.fromFile(new File(paramString)));
        paramActivity.startActivityForResult(localIntent, paramInt);
    }
}
