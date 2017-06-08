package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.FileUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.SystemUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.core.utils.ZipUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.rxapi.http.ClientLogHttpManager;
import com.mfh.framework.rxapi.http.ResHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * <h1>一键反馈</h1>
 * <p>首先判断是否已经日结过:<br>
 * 1.如果已经日结过,则不需要启动日结统计，可以直接查询统计数据。最后也不需要进行日结确认。<br>
 * 2.如果未日结，则需要启动日结统计，然后再查询统计数据，最后需要确认日结操作。</p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class OneKeyFeedbackFragment extends BaseProgressFragment {
    private static final String ONE_KEY_FEEDBACK_ZIP = "onekeyfeedback.zip";


    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @BindView(R.id.et_content)
    EditText etContent;

//    @BindView(R.id.button_header_close)
//    ImageButton btnClose;
    @BindView(R.id.ib_submit)
    ImageButton fabSubmit;


    public static OneKeyFeedbackFragment newInstance(Bundle args) {
        OneKeyFeedbackFragment fragment = new OneKeyFeedbackFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_onekey_feedback;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        tvHeaderTitle.setText("一键反馈");

        etContent.setFocusable(true);
        etContent.setFocusableInTouchMode(true);
        etContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        submit();
                    }
                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        etContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (SharedPrefesManagerFactory.isSoftKeyboardEnabled()) {
                    DeviceUtils.showSoftInput(getContext(), etContent);
//                    } else {
//                        DeviceUtils.hideSoftInput(getContext(), etContent);
//                    }
                }
                etContent.requestFocus();
                etContent.setSelection(etContent.length());
                //返回true,不再继续传递事件
                return true;
            }
        });
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 提交反馈
     */
    @OnClick(R.id.ib_submit)
    public void submit() {
        DeviceUtils.hideSoftInput(getContext(), etContent);

        String feedback = etContent.getText().toString();
        if (StringUtils.isEmpty(feedback)) {
            DialogUtil.showHint("请输入反馈内容");
            return;
        }

        uploadFile(feedback);
    }

    public void uploadFile(final String feedback) {
        try {
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
            File zipFile = FileUtil.getSaveFile("", ONE_KEY_FEEDBACK_ZIP);
            if (!zipFile.exists()) {
                zipFile.createNewFile();
            }
            //由于日志文件太大，所以这里只压缩最新的日志文件上传
            ZipUtils.zipFiles(FileUtil.getSavePath(ZLogger.CRASH_FOLDER_PATH),
                    zipFile);
//            String time = ZLogger.DATE_FORMAT.format(new Date());
//            String fileName = time + ".log";
//            ZipUtils.zipFile(FileUtil.getSaveFile(ZLogger.CRASH_FOLDER_PATH, fileName), zipFile);

            Map<String, String> options = new HashMap<>();
//                options.put("fileToUpload", zipFile.toString());
//                ZLogger.d(zipFile.toString());
            options.put("responseType", "1");
            options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

            File file = FileUtil.getSaveFile("", ONE_KEY_FEEDBACK_ZIP);//FileUtil.getSaveFile(ZLogger.CRASH_FOLDER_PATH, fileName);
            if (!file.exists()) {
                updateStackInfo(feedback, null);
                return;
            }
            ZLogger.d("file: " + file.getPath());

            ResHttpManager.getInstance().upload2(file,
                    new MValueSubscriber<Long>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            //retrofit2.adapter.rxjava.HttpException: HTTP 413 Request Entity Too Large
                            ZLogger.ef("上传一键反馈资源文件:" + e.toString());
                            updateStackInfo(feedback, null);
                        }

                        @Override
                        public void onValue(Long data) {
                            super.onValue(data);

                            updateStackInfo(feedback, data);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }
    }


    /**
     * //上传日志文件成功，再创建一个文件，将该资源文件的编号发送过来，方便下载文件
     * */
    private void updateStackInfo(String feedback, Long attachmentId) {
        Map<String, String> options = new HashMap<>();

        JSONObject jsonObject = new JSONObject();
        JSONObject stackObject = new JSONObject();
        stackObject.put("terminalId", SharedPrefesManagerFactory.getTerminalId());
        StringBuilder sb = new StringBuilder();
        sb.append("\n" +
                "一键反馈: \n");
        if (!StringUtils.isEmpty(feedback)) {
            sb.append(feedback).append("\n");
        }
        if (attachmentId != null) {
            sb.append(String.format("附件编号： %d\n",
                    attachmentId));
        }
        stackObject.put("feedback", sb.toString());
        jsonObject.put("stackInformation", stackObject.toJSONString());
        jsonObject.put("hardwareInformation", String.format("%s %s", Build.MANUFACTURER, Build.MODEL));
        jsonObject.put("androidLevel", String.format("%s(API %d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT));
        jsonObject.put("loginName", MfhLoginService.get().getLoginName());
        jsonObject.put("softVersion", SystemUtils.getVersion(MfhApplication.getAppContext()));
        jsonObject.put("errorTime", TimeUtil.format(TimeUtil.getCurrentDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));

        options.put("jsonStr", jsonObject.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ClientLogHttpManager.getInstance().create(options,
                new MValueSubscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef(e.toString());
                        hideProgressDialog();

                    }

                    @Override
                    public void onValue(Long data) {
                        super.onValue(data);
                        hideProgressDialog();
                        DialogUtil.showHint("反馈成功");
                        finishActivity();
                    }
                });

    }

    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
    }


}
