package com.mfh.framework.core.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;

import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;

/**
 * Android设备检测到一个Tag时，会创建一个Tag对象，将其放在Intent对象，然后发送到此Activity
 * @author yxm
 * @version 1.0
 */
public class NfcActivity extends Activity {
    private String Token;//Intent对象经处理后获取的特定标记

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        processIntent(this.getIntent());//调用处理Intent对象的方法

        Token = getToken(this.getIntent());

        if (Token != null){
            //发送广播
            Intent intent = new Intent();
            intent.putExtra("Token", Token);
            intent.setAction("com.mfh.nfc.NFC_BROADCAST");
            sendBroadcast(intent);
        }

        //结束Activity。因为只有Activity可以接收包含Tag的Intent对象，
        //故通过使NfcActivity快速消失以达到近似隐藏运行的效果，以增强用户体验。
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    /**
     * 字符序列转换为16进制字符串
     * @param src 字符序列
     * @return
     */
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (byte aSrc : src) {
            buffer[0] = Character.forDigit((aSrc >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(aSrc & 0x0F, 16);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    /**
     * 从Intent对象中解析 NDEF 消息（NFC Data Exchange Format，即 NFC 数据交换格式，
     * NDEF Message 为 NFC forum 定义的数据格式）
     * @param intent 包含Tag的Intent对象
     */
    private void processIntent(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);//取出封装在intent中的TAG

        //android.nfc.tech.NfcA
        for (String tech : tagFromIntent.getTechList()) {
            ZLogger.d(tech);
        }

        //读取TAG
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        if (mfc == null){
            ZLogger.d("MifareClassic} was not enumerated");
            return;
        }

        try {
            String metaInfo = "";
            boolean auth;

            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();

            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";

            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);

                int bCount;
                int bIndex;
                if (auth) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);

                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n";

                        String uid = bytesToHexString(data);
                        if (!StringUtils.isEmpty(uid)){
                            uid = uid.substring(8, 10) + uid.substring(6, 8) + uid.substring(4, 6) + uid.substring(2, 4);

                            metaInfo += "token: " + String.valueOf(Long.parseLong(uid, 16)) + "\n";
                        }

                        bIndex++;
                    }
                } else {
                    metaInfo += "Sector " + j + ":验证失败\n";
                }
            }

            ZLogger.d("metaInfo:" + metaInfo);
            Token = metaInfo;
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.d(e.toString());
        }
    }

    /**
     * 获取UID
     * @param intent 扫描到的NFC的Intent对象
     * @return
     */
    private String getToken(Intent intent){
        String token = null;
        byte[] bytesId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        String uid = bytesToHexString(bytesId);
        if (!StringUtils.isEmpty(uid)){
            ZLogger.d("NFC:uid 1=" + uid);
            uid = uid.substring(8, 10) + uid.substring(6, 8) + uid.substring(4, 6) + uid.substring(2, 4);
            ZLogger.d("NFC:uid 2=" + uid);

            token = String.valueOf(Long.parseLong(uid, 16));
            ZLogger.d("NFC:token=" + token);
        }
        return token;
    }

}
