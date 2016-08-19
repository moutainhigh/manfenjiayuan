package com.mfh.litecashier.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mfh.framework.ZIntent;
import com.mfh.framework.core.logger.ZLogger;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;

/**
 * Created by bingshanguxue on 16/2/24.
 */
public class KeepAlarmLiveReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null){
            return;
        }

        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            AlarmManagerHelper.triggleNextDailysettle(-1);
            AlarmManagerHelper.registerBuglyUpgrade(context);
        }
        else if (ZIntent.ACTION_BETA_BUGLY_CHECKUPDATE.equals(intent.getAction())) {
            StringBuilder info = new StringBuilder();

            UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
            if (upgradeInfo != null){
                info.append("id: ").append(upgradeInfo.id).append("\n");
                info.append("标题: ").append(upgradeInfo.title).append("\n");
                info.append("升级说明: ").append(upgradeInfo.newFeature).append("\n");
                info.append("versionCode: ").append(upgradeInfo.versionCode).append("\n");
                info.append("versionName: ").append(upgradeInfo.versionName).append("\n");
                info.append("发布时间: ").append(upgradeInfo.publishTime).append("\n");
                info.append("安装包Md5: ").append(upgradeInfo.apkMd5).append("\n");
                info.append("安装包下载地址: ").append(upgradeInfo.apkUrl).append("\n");
                info.append("安装包大小: ").append(upgradeInfo.fileSize).append("\n");
                info.append("弹窗间隔（ms）: ").append(upgradeInfo.popInterval).append("\n");
                info.append("弹窗次数: ").append(upgradeInfo.popTimes).append("\n");
                info.append("发布类型（0:测试 1:正式）: ").append(upgradeInfo.publishType).append("\n");
                info.append("弹窗类型（1:建议 2:强制 3:手工）: ").append(upgradeInfo.upgradeType);
            }
            else{
                info.append("无升级信息");
            }

            ZLogger.df(String.format("%s-自动检测更新\n%s", intent.getAction(), info.toString()));
            Beta.checkUpgrade(false, false);
        }
    }
}
