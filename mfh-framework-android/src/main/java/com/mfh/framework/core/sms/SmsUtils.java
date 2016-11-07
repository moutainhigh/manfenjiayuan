package com.mfh.framework.core.sms;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by shengkun on 15/6/8.
 */
public class SmsUtils {

    Context mContext;

    public SmsUtils(final Context context) {
        mContext = context;
    }

    //只检查收件箱的验证码信息
    public static final Uri MMSSMS_ALL_MESSAGE_URI = Uri.parse("content://sms/inbox");
    public static final Uri ALL_MESSAGE_URI = MMSSMS_ALL_MESSAGE_URI.buildUpon().
            appendQueryParameter("simple", "true").build();

    private static final String[] ALL_THREADS_PROJECTION = {
            "_id", "address", "person", "body",
            "date", "type", "thread_id"};

//    public List<Message> getAllCaptchMessages() {
//        List<String> dateGroups = new ArrayList<>();
//        ContentResolver contentResolver = mContext.getContentResolver();
//        Cursor cursor = contentResolver.query(ALL_MESSAGE_URI, ALL_THREADS_PROJECTION,
//                null, null, "date desc");
//        List<Message> smsMessages = new ArrayList<>();
//        while ((cursor.moveToNext())) {
//            int indexBody = cursor.getColumnIndex("body");
//            int indexAddress = cursor.getColumnIndex("address");
//            int indexThreadId = cursor.getColumnIndex("thread_id");
//            String strbody = cursor.getString(indexBody);
//            String strAddress = cursor.getString(indexAddress);
//            if (!RegularUtils.isMobile(strAddress)) {
//                boolean isCpatchasMessage = false;
//                if (CaptchaUtils.isCaptchasMessage(strbody) &&
//                        !CaptchaUtils.tryToGetCaptchas(strbody).equals("")) {
//                    isCpatchasMessage = true;
//                }
//
//                if (isCpatchasMessage) {
//                    int date = cursor.getColumnIndex("date");
//                    //格式化短信日期提示
//                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd hh:mm");
//                    Date formatDate = new Date(Long.parseLong(cursor.getString(date)));
//                    long threadId = cursor.getLong(indexThreadId);
//
//                    //获得短信的各项内容
//                    String dateMms = dateFormat.format(formatDate);
//                    Message message = new Message();
//                    String company = StringUtils.getContentInBracket(strbody, strAddress);
//                    if (company != null) {
//                        message.setCompanyName(company);
//                    }
//                    String captchas = StringUtils.tryToGetCaptchas(strbody);
//                    if (!captchas.equals("")) {
//                        message.setCaptchas(captchas);
//                    }
//                    int columnIndex = cursor.getColumnIndex("_id");
//                    String smsId = cursor.getString(columnIndex);
//                    message.setMessage(true);
//                    message.setDate(formatDate);
//                    message.setSender(strAddress);
//                    message.setThreadId(threadId);
//                    message.setContent(strbody);
//                    message.setSmsId(smsId);
//                    message.setReceiveDate(dateMms);
//                    String resultContent = StringUtils.getResultText(message, false);
//                    if (resultContent != null) {
//                        message.setResultContent(resultContent);
//                    }
//                    //检查收件箱地址把所有的验证码短信放到smsMessages
//                    smsMessages.add(message);
//                }
//            }
//        }
//
//
//        List<Message> localMessages = DataSupport.where("readStatus = ?", "0").order("date asc").find(Message.class);
//        for (Message message : localMessages) {
//            if (message.getDate() != null) {
//                message.setIsMessage(true);
//                boolean find = false;
//                for (int u = 0; u < smsMessages.size(); u++) {
//                    if (message.getDate().getTime() > smsMessages.get(u).getDate().getTime()) {
//                        smsMessages.add(u, message);
//                        find = true;
//                        break;
//                    }
//                }
//                if (!find) {
//                    smsMessages.add(message);
//                }
//
//            }
//        }
//
//
//        List<Message> unionMessages = new ArrayList<>();
//        for (Message message : smsMessages) {
//            String group = TimeUtils.getInstance().getDateGroup(message.getDate());
//            if (dateGroups.size() == 0) {
//                dateGroups.add(group);
//                Message dateMessage = new Message();
//                dateMessage.setReceiveDate(group);
//                dateMessage.setIsMessage(false);
//                unionMessages.add(dateMessage);
//            } else {
//                if (!group.equals(dateGroups.get(dateGroups.size() - 1))) {
//                    dateGroups.add(group);
//                    Message dateMessage = new Message();
//                    dateMessage.setReceiveDate(group);
//                    dateMessage.setIsMessage(false);
//                    unionMessages.add(dateMessage);
//                }
//            }
//
//            unionMessages.add(message);
//        }
//
//        cursor.close();
//        return unionMessages;
//    }

    /**
     * 删除手机短信
     */
    public int deleteSms(String smsId) {
        final Uri SMS_URI = Uri.parse("content://sms/");
        return mContext.getContentResolver().delete(SMS_URI, "_id=?", new String[]{smsId});
    }

    public String getContactNameFromPhoneBook(String phoneNum) {
        String contactName = null;
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER};
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNum));
        try {
            Cursor cursor = mContext.getContentResolver().query(uri, projection,
                    null, null, null);

            if (cursor.moveToFirst()) {
                contactName = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
            cursor.close();
        } catch (IllegalArgumentException e) {
            return null;
        }
        return contactName;
    }

    // 根据号码获得联系人头像
    public Bitmap getPeopleImage(String x_number) {

        // 获得Uri
        Uri uriNumber2Contacts = Uri.parse("content://com.android.contacts/"
                + "data/phones/filter/" + x_number);
        // 查询Uri，返回数据集
        Cursor cursorCantacts = mContext.getContentResolver().query(
                uriNumber2Contacts,
                null,
                null,
                null,
                null);
        // 如果该联系人存在
        if (cursorCantacts.getCount() > 0) {
            // 移动到第一条数据
            cursorCantacts.moveToFirst();
            // 获得该联系人的contact_id
            Long contactID = cursorCantacts.getLong(cursorCantacts.getColumnIndex("contact_id"));
            cursorCantacts.close();
            // 获得contact_id的Uri
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactID);
            // 打开头像图片的InputStream
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(mContext.getContentResolver(), uri);
            // 从InputStream获得bitmap
            return BitmapFactory.decodeStream(input);
        }
        cursorCantacts.close();
        return null;
    }

    public static String getContentInBracket(String str, String address) {
        Pattern pattern = Pattern.compile("\\【(.*?)\\】");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            if (matcher.group(1) != null && matcher.group(1).length() < 10) {

                return analyseSpecialCompany(matcher.group(1), str, address);
            }
        }
        Pattern pattern1 = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher1 = pattern1.matcher(str);
        while (matcher1.find()) {
            if (matcher1.group(1) != null && matcher1.group(1).length() < 10) {

                return analyseSpecialCompany(matcher1.group(1), str, address);
            }
        }
        Pattern pattern2 = Pattern.compile("\\((.*?)\\)");
        Matcher matcher2 = pattern2.matcher(str);
        while (matcher2.find()) {
            if (matcher2.group(1) != null && matcher2.group(1).length() < 10) {

                return analyseSpecialCompany(matcher2.group(1), str, address);
            }
        }
        return null;
    }

    private static String analyseSpecialCompany(String company, String content, String address) {
        String companyName = company;
        if (company.equals("掌淘科技")) {
            int index = content.indexOf("的验证码");
            companyName = content.substring(0, index);
            companyName = companyName.replaceAll("【掌淘科技】", "").trim();
        } else {
            if (content.contains("贝壳单词的验证码")) {
                companyName = "贝壳单词";
            }
        }
        if (address.equals("10010")) {
            companyName = "中国联通";
        }
        if (address.equals("10086")) {
            companyName = "中国移动";
        }
        if (address.equals("10000")) {
            companyName = "中国电信";
        }
        return companyName;
    }

    /**
     * 根据短信获取描述文字
     *
     * @return
     */
    public static String getResultText(Message message, Boolean isNotificationText) {
        String resultStr = "";
        if (message.getCompanyName() != null && !isNotificationText) {
            resultStr += "来自" + message.getCompanyName() + "的验证码：";
        } else {
            resultStr += "当前验证码为：";
        }
        if (message.getCaptchas() != null) {
            resultStr += message.getCaptchas();
        } else {
            resultStr += "点击查看详情.";
        }
        return resultStr;
    }

}
