package com.mfh.litecashier.hardware.SMScale;


import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.Encoding;

import java.io.File;
import java.io.IOException;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

/**
 * Created by bingshanguxue on 4/21/16.
 */
public class FTPManager {
    /*********  work only for Dedicated IP ***********/
    public static String FTP_HOST= "192.168.18.60";

    public static int FTP_PORT= 21;

    /*********  FTP USERNAME ***********/
    public static String FTP_USER = "bingshanguxue";

    /*********  FTP PASSWORD ***********/
    public static String FTP_PASS  ="123456";


    public static String ENCODING_CHARSET  = Encoding.CHARSET_GBK;

    /**
     * upload file to ftp
     * */
    public static void upload2Ftp(File file, FTPDataTransferListener transferListener){
        FTPClient ftpClient = new FTPClient();
        try {

            ZLogger.d(String.format("connect to %s:%d", FTPManager.FTP_HOST, FTPManager.FTP_PORT));
            ftpClient.connect(FTPManager.FTP_HOST, FTPManager.FTP_PORT);

            ZLogger.d(String.format("login %s:%s", FTPManager.FTP_USER, FTPManager.FTP_PASS));
            //it.sauronsoftware.ftp4j.FTPException [code=530, message= Login authentication failed]
            ftpClient.login(FTPManager.FTP_USER, FTPManager.FTP_PASS);

            ftpClient.setType(FTPClient.TYPE_BINARY);
            ftpClient.changeDirectory("/");
            ftpClient.upload(file, transferListener);
        } catch (IOException | FTPIllegalReplyException | FTPException |
                FTPDataTransferException | FTPAbortedException e1) {
            e1.printStackTrace();
            ZLogger.d("upload2Ftp failed :" + e1.toString());
            if (transferListener != null){
                transferListener.failed();
            }

            try {
                ftpClient.disconnect(true);
            } catch (Exception e2){
                e2.printStackTrace();
                ZLogger.d("ftpClient disconnect failed :" + e2.toString());
            }
        }
    }
}
