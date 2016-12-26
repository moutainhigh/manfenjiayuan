package com.mfh.litecashier.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.bingshanguxue.cashier.model.wrapper.ResMenu;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.mfh.framework.uikit.base.ResultCode;
import com.manfenjiayuan.im.IMClient;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.UserMixInfo;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.login.MfhUserManager;
import com.mfh.framework.login.logic.Callback;
import com.mfh.framework.login.logic.LoginCallback;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.Constants;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.CompanyHumanEntity;
import com.mfh.litecashier.event.AffairEvent;
import com.mfh.litecashier.ui.ActivityRoute;
import com.mfh.litecashier.ui.adapter.AdministratorMenuAdapter;
import com.mfh.litecashier.ui.dialog.AccountDialog;
import com.mfh.litecashier.ui.dialog.ResumeMachineDialog;
import com.mfh.litecashier.ui.dialog.SelectCompanyHumanDialog;
import com.mfh.litecashier.utils.AppHelper;
import com.tencent.bugly.beta.Beta;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 管理员页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class AdministratorActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_avatar)
    AvatarView mAvatarView;
    @BindView(R.id.tv_username)
    TextView tvUsername;

    @BindView(R.id.menulist)
    RecyclerView menuRecyclerView;
    private GridLayoutManager mRLayoutManager;
    private AdministratorMenuAdapter menuAdapter;

    private AccountDialog mAccountDialog = null;
    private ResumeMachineDialog resumeMachineDialog = null;
    private SelectCompanyHumanDialog selectCompanyHumanDialog = null;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, AdministratorActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_administrator_splash;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();

        toolbar.setTitle(MfhLoginService.get().getCurOfficeName());
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AdministratorActivity.this.onBackPressed();
                    }
                });
//        // Set an OnMenuItemClickListener to handle menu item clicks
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_canary) {
//                    ActivityRoute.redirect2Canary(AdministratorActivity.this);
//                }
//                return true;
//            }
//        });
//
//        // Inflate a menu to be displayed in the toolbar
//        toolbar.inflateMenu(R.menu.menu_administrator);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(R.style.NewFlow);

        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mAvatarView.setBorderWidth(2);
        mAvatarView.setBorderColor(Color.parseColor("#e8e8e8"));
//        mAvatarView.setAvatarUrl("");
        mAvatarView.setAvatarUrl(MfhLoginService.get().getHeadimage());
        tvUsername.setText(MfhLoginService.get().getHumanName());

        initMenuRecyclerView();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_administrator, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public void onPause() {
        super.onPause();

        if (mAccountDialog != null) {
            mAccountDialog.dismiss();
        }

        if (resumeMachineDialog != null) {
            resumeMachineDialog.dismiss();
        }

        if (selectCompanyHumanDialog != null) {
            selectCompanyHumanDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAccountDialog != null) {
            mAccountDialog.dismiss();
            mAccountDialog = null;
        }

        if (resumeMachineDialog != null) {
            resumeMachineDialog.dismiss();
            resumeMachineDialog = null;
        }

        if (selectCompanyHumanDialog != null) {
            selectCompanyHumanDialog.dismiss();
            selectCompanyHumanDialog = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case ResultCode.ARC_NATIVE_SIGNIN: {
                if (resultCode == Activity.RESULT_OK) {
                    DialogUtil.showHint("登录成功");
                    mAvatarView.setAvatarUrl(MfhLoginService.get().getHeadimage());
                    tvUsername.setText(MfhLoginService.get().getHumanName());

                    //初始化收银,createdBy(humanId)已经改变
                    EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_RESET_CASHIER));
                }
            }
            break;
            case Constants.ARC_HANDOVER: {
                if (resultCode == Activity.RESULT_OK) {
                    handoverSelectAccount();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 单击头像
     */
    @OnClick(R.id.iv_avatar)
    public void clickAvatarView() {
        if (mAccountDialog == null) {
            mAccountDialog = new AccountDialog(this);
            mAccountDialog.setCancelable(false);
            mAccountDialog.setCanceledOnTouchOutside(true);
        }
        mAccountDialog.init(1, new AccountDialog.DialogClickListener() {
            @Override
            public void onLock() {
                lockMachine();
            }

            @Override
            public void onHandOver() {
                handoverAnalysis();
            }

            @Override
            public void onLogout() {
                logout();
            }
        });

        mAccountDialog.show();
    }

    /**
     * 锁定机器
     */
    private void lockMachine() {
        CompanyHumanEntity human = new CompanyHumanEntity();
        human.setPassword(MfhLoginService.get().getPassword());
        human.setHeaderUrl(MfhLoginService.get().getHeadimage());
        human.setName(MfhLoginService.get().getHumanName());

        if (resumeMachineDialog == null) {
            resumeMachineDialog = new ResumeMachineDialog(this);
            resumeMachineDialog.setCancelable(false);
            resumeMachineDialog.setCanceledOnTouchOutside(false);
        }
        resumeMachineDialog.init(ResumeMachineDialog.DTYPE_LOCK, human, null);
        resumeMachineDialog.show();
    }

    /**
     * 交接班－－统计交接班数据
     */
    private void handoverAnalysis() {
        Intent intent = new Intent(this, SimpleDialogActivity.class);
        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_HANDOVER);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
//        extras.putString(SelectPlatformGoodsFragment.EXTRA_KEY_BARCODE, barcode);

        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_HANDOVER);
    }


    /**
     * 交接班－－确认交接班账号
     */
    private void handoverConfirmAccount(CompanyHumanEntity entity) {
        if (resumeMachineDialog == null) {
            resumeMachineDialog = new ResumeMachineDialog(this);
            resumeMachineDialog.setCancelable(false);
            resumeMachineDialog.setCanceledOnTouchOutside(false);
        }

        resumeMachineDialog.init(ResumeMachineDialog.DTYPE_HANDOVER, entity,
                new ResumeMachineDialog.DialogClickListener() {
                    @Override
                    public void onChangeHuman() {
                        mAvatarView.setAvatarUrl(MfhLoginService.get().getHeadimage());
                        tvUsername.setText(MfhLoginService.get().getHumanName());

                        retryLogin(true);
                    }
                });
        resumeMachineDialog.show();
    }

    /**
     * 交接班－－选择交接班账号
     */
    private void handoverSelectAccount() {
        if (selectCompanyHumanDialog == null) {
            selectCompanyHumanDialog = new SelectCompanyHumanDialog(this);
            selectCompanyHumanDialog.setCancelable(false);
            selectCompanyHumanDialog.setCanceledOnTouchOutside(false);
        }
        selectCompanyHumanDialog.setOnDialogClickListener(new SelectCompanyHumanDialog.DialogClickListener() {
            @Override
            public void onSelectHuman(CompanyHumanEntity entity) {
                handoverConfirmAccount(entity);
            }
        });
        selectCompanyHumanDialog.show();
    }

    /**
     * 日结
     */
    private void dailySettle() {
//        ZLogger.df(String.format("准备日结：datetime = %s, cancelable = %b", datetime, cancelable));
        Intent intent = new Intent(this, SimpleDialogActivity.class);
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_DAILY_SETTLE);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
//        extras.putString(DailySettleFragment.EXTRA_KEY_DATETIME, datetime);
        intent.putExtras(extras);
        startActivity(intent);
    }


    private void initMenuRecyclerView() {
        mRLayoutManager = new GridLayoutManager(this, 8);
        menuRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        menuRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        menuRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
//        menuRecyclerView.addItemDecoration(new GridItemDecoration2(getActivity(), 1,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.5f,
//                getResources().getColor(R.color.mf_dividerColorPrimary), 0.1f));
//        menuRecyclerView.addItemDecoration(new GridItemDecoration(
//                4, 2, false));

        menuAdapter = new AdministratorMenuAdapter(CashierApp.getAppContext(), null);
        menuAdapter.setOnAdapterLitener(new AdministratorMenuAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                ResMenu entity = menuAdapter.getEntity(position);
                if (entity != null) {
                    responseMenu(entity.getId());
                }
            }
        });
        menuRecyclerView.setAdapter(menuAdapter);
        menuAdapter.setEntityList(getAdminMenus());
    }


    /**
     * 获取菜单
     */
    private synchronized List<ResMenu> getAdminMenus() {
        List<ResMenu> functionalList = new ArrayList<>();
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_PURCHASE_MANUAL,
                "手动订货", R.mipmap.ic_admin_purchase_manual));
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_INVENTORY,
                "库存", R.mipmap.ic_admin_menu_inventory));
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_RECEIPT,
                "单据", R.mipmap.ic_admin_menu_receipt));
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_ANALYSIS,
                "统计", R.mipmap.ic_admin_menu_analysis));
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_CASHQUOTA,
                "授信", R.mipmap.ic_admin_menu_cashquota));
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_SETTINGS,
                "设置", R.mipmap.ic_admin_menu_settings));
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_FACTORYDATA_RESET,
                "恢复出厂设置", R.mipmap.ic_admin_factorydatareset));
        functionalList.add(new ResMenu(ResMenu.ADMIN_MENU_SYSTEM_UPGRADE,
                "系统升级", R.mipmap.ic_admin_system_upgrade));
        if (SharedPrefesManagerFactory.isSuperPermissionGranted()) {
            functionalList.add(new ResMenu(ResMenu.CANARY_MENU_ORDERFLOW,
                    "流水", R.mipmap.ic_admin_menu_orderflow));
            functionalList.add(new ResMenu(ResMenu.CANARY_MENU_GOODS,
                    "商品档案", R.mipmap.ic_admin_menu_inventory));
            functionalList.add(new ResMenu(ResMenu.CANARY_MENU_MESSAGE_MGR,
                    "消息管理器", R.mipmap.ic_admin_menu_settings));
        }

        return functionalList;
    }

    /**
     * 响应菜单
     */
    private void responseMenu(Long id) {
        if (id == null) {
            return;
        }

        if (id.compareTo(ResMenu.ADMIN_MENU_PURCHASE_MANUAL) == 0) {
            ActivityRoute.redirect2Purchase(this);
        } else if (id.compareTo(ResMenu.ADMIN_MENU_INVENTORY) == 0) {
            ActivityRoute.redirect2Inventory(this);
        } else if (id.compareTo(ResMenu.ADMIN_MENU_RECEIPT) == 0) {
            ActivityRoute.redirect2Receipt(this);
        } else if (id.compareTo(ResMenu.ADMIN_MENU_ANALYSIS) == 0) {
            dailySettle();
        } else if (id.compareTo(ResMenu.ADMIN_MENU_SETTINGS) == 0) {
            ActivityRoute.redirect2Settings(this);
        } else if (id.compareTo(ResMenu.ADMIN_MENU_CASHQUOTA) == 0) {
            ActivityRoute.redirect2CashQuota(this);
        } else if (id.compareTo(ResMenu.ADMIN_MENU_FACTORYDATA_RESET) == 0) {
            factoryDataReset();
        } else if (id.compareTo(ResMenu.ADMIN_MENU_SYSTEM_UPGRADE) == 0) {
            /**
             * @param isManual  用户手动点击检查，非用户点击操作请传false
             * @param isSilence 是否显示弹窗等交互，[true:没有弹窗和toast] [false:有弹窗或toast]
             */
//        Beta.checkUpgrade(false, false);
            Beta.checkUpgrade();
        } else if (id.compareTo(ResMenu.CANARY_MENU_ORDERFLOW) == 0) {
            ActivityRoute.redirect2CanaryOrderflow(this);
        } else if (id.compareTo(ResMenu.CANARY_MENU_GOODS) == 0) {
            ActivityRoute.redirect2CanaryGoods(this);
        } else if (id.compareTo(ResMenu.CANARY_MENU_MESSAGE_MGR) == 0) {
            ActivityRoute.redirect2MsgMgr(this);
        } else {
            DialogUtil.showHint(R.string.coming_soon);
        }
    }


    /**
     * 退出当前账号
     */
    private void logout() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在退出当前账号...", false);
//                    // 保存统计数据
//                    MobclickAgent.onKillProcess(CashierApp.getAppContext());
//
//                    //退出程序
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    System.exit(0);
        MfhUserManager.getInstance().logout(new Callback() {
            @Override
            public void onSuccess() {
//                showProgressDialog(ProgressDialog.STATUS_DONE, "正在退出当前账号...", false);
                hideProgressDialog();
                redirect2Login();
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
//                showProgressDialog(ProgressDialog.STATUS_ERROR, "正在退出当前账号...", true);
                hideProgressDialog();
                redirect2Login();
            }
        });
    }

    /**
     * 尝试登录
     *
     * @param bSlient 是否静默重试登录
     */
    private void retryLogin(final boolean bSlient) {
        MfhLoginService.get().doLoginAsync(MfhLoginService.get().getLoginName(),
                MfhLoginService.get().getPassword(), new LoginCallback() {
                    @Override
                    public void loginSuccess(UserMixInfo user) {
                        //登录成功
                        ZLogger.df("重登录成功：");

                        //注册到消息桥
                        IMClient.getInstance().registerBridge();

                        //初始化收银,createdBy(humanId)已经改变
                        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_RESET_CASHIER));
                    }

                    @Override
                    public void loginFailed(String errMsg) {
                        //登录失败
                        ZLogger.df("重登录失败：" + errMsg);
                        //初始化收银,createdBy(humanId)已经改变
                        EventBus.getDefault().post(new AffairEvent(AffairEvent.EVENT_ID_RESET_CASHIER));

                        if (!bSlient) {
                            redirect2Login();
                        }
                    }
                });
    }


    /**
     * 跳转至登录页面,清空账号信息
     */
    private void redirect2Login() {
        MfhLoginService.get().clear();

        Bundle extras = new Bundle();
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
//        extras.putInt(ServiceActivity.EXTRA_KEY_SERVICE_TYPE, ServiceActivity.FRAGMENT_TYPE_OFFICELIST);
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ResultCode.ARC_NATIVE_SIGNIN);
    }

    /**
     * 恢复出厂设置
     */
    private void factoryDataReset() {
        showConfirmDialog("此操作会清楚您设备中的所有数据，确定要恢复出厂设置吗？\n(恢复出厂设置后会自动重启)",
                "恢复出厂设置", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        AppHelper.resetFactoryData(CashierApp.getAppContext());
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

}
