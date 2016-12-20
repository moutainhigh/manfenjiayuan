package com.mfh.litecashier.ui.fragment.message;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bingshanguxue.cashier.model.wrapper.CouponRule;
import com.manfenjiayuan.im.database.entity.EmbMsg;
import com.manfenjiayuan.im.database.service.EmbMsgService;
import com.manfenjiayuan.im.param.TextParam;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.FaceUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.ChatMessageAdapter;
import com.mfh.litecashier.ui.adapter.MaterialCouponAdapter;
import com.mfh.litecashier.ui.adapter.MaterialEmojiPageAdapter;
import com.mfh.litecashier.ui.dialog.QueryGoodsDialog;
import com.mfh.litecashier.ui.widget.LeftTabStrip;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 消息
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class MessageFragment extends BaseFragment {

    @BindView(R.id.message_list)
    RecyclerView messageRecyclerView;
    private ChatMessageAdapter chatMessageAdapter;

    @BindView(R.id.right_tab)
    LeftTabStrip rightTabStrip;

    @BindView(R.id.et_message)
    EditText etMessage;
    @BindView(R.id.et_query) EditText etQuery;

    @BindView(R.id.ib_emoji)
    ImageButton ibEmoji;
    @BindView(R.id.ib_goods)
    ImageButton ibGoods;
    @BindView(R.id.ib_coupon)
    ImageButton ibCoupon;
    @BindView(R.id.button_send)
    Button buttonSend;
    @BindView(R.id.frame_coupon)
    FrameLayout frameCoupon;
    @BindView(R.id.coupon_list)
    RecyclerViewEmptySupport couponRecyclerView;
    private GridLayoutManager couponLayoutManager;
    @BindView(R.id.no_coupon_view)
    TextView noCouponView;
    @BindView(R.id.emoji_viewpager)
    AdvertisementViewPager emojiViewPager;
    private MaterialEmojiPageAdapter emojiPageAdapter;

    private MaterialCouponAdapter couponAdapter;


    private QueryGoodsDialog queryGoodsDialog;


    private final class ViewPageInfo {
        public final String title;
        public final int resId;

        public ViewPageInfo(String title, int resId) {
            this.title = title;
            this.resId = resId;
        }
    }


    public static MessageFragment newInstance(Bundle args){
        MessageFragment fragment = new MessageFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_message;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initMessageRecyclerView();
        initCouponRecyclerView();
        initEmoji();
        initTabs();

//        frameEmoji.setOnEmojiClickListener(new EmojiView.EmojiClickListener() {
//            @Override
//            public void onItemSelected(int id) {
//                if (id == 20) {
//                    //删除，回退
//                    KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL);
//                    etMessage.dispatchKeyEvent(keyEvent);
//                    return;
//                }
////                etMessage.append(FaceUtil.getFaceString(id));
//
////                String inputStr = etMessage.getText().toString();
//                SpannableString spannableString = FaceUtil.getSpannable(getActivity(), FaceUtil.getFaceString(id), 25, 25);
//                etMessage.append(spannableString);
//                etMessage.requestFocus();
//                etMessage.setSelection(etMessage.getText().length());
//            }
//        });
        rightTabStrip.selectedTab(0);
        //TODO,加载联系人列表

        //加载表情

        //加载卡券测试数据
        List<CouponRule> couponEntityList = new ArrayList<>();
        for (int i = 0; i < 6; i++){
            CouponRule bean = new CouponRule();
            bean.setTitle(String.format("测试%d", i));
            bean.setDiscount(1D * i);
            bean.setType(CouponRule.TYPE_COUPON);
            couponEntityList.add(bean);
        }
        couponAdapter.setEntityList(couponEntityList);

        etMessage.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        etMessage.requestFocus();
    }

    @OnClick(R.id.ib_emoji)
    public void toggleEmoji(){
        hideCouponFrame();

        if (ibEmoji.isSelected()){
            hideEmojiFrame();
        }
        else{
            showEmojiFrame();
        }
    }

    @OnClick(R.id.ib_goods)
    public void toggleGoods(){
        hideCouponFrame();
        hideEmojiFrame();

        if (queryGoodsDialog == null) {
            queryGoodsDialog = new QueryGoodsDialog(getActivity());
            queryGoodsDialog.setCancelable(false);
            queryGoodsDialog.setCanceledOnTouchOutside(false);
        }
        queryGoodsDialog.init(QueryGoodsDialog.DIALOG_TYPE_SEND_GOODS);
        if (!queryGoodsDialog.isShowing()) {
            queryGoodsDialog.show();
        }
    }

    @OnClick(R.id.ib_coupon)
    public void toggleCoupon(){
        hideEmojiFrame();

        if (ibCoupon.isSelected()){
            hideCouponFrame();
        }
        else{
            showCouponFrame();
        }
    }

    @OnClick(R.id.button_send)
    public void sendText(){
        String message = etMessage.getText().toString();
        if (StringUtils.isEmpty(message)){
            DialogUtil.showHint("请输入文字");
            return;
        }

        //TODO 发送消息
//        DialogUtil.showHint("@开发君@ 失踪了...");
        NetProcessor.ComnProcessor processor = new NetProcessor.ComnProcessor<EmbMsg>(){
            @Override
            protected void processOperResult(EmbMsg result){
//                doAfterSendSuccess(result);
                ZLogger.d("消息发送成功");
                chatMessageAdapter.appendEntity(result);
            }
        };
        EmbMsgService.getInstance().sendMessageToPeople(MfhLoginService.get().getGuidLong(),
                MfhLoginService.get().getGuidLong(), new TextParam(message), processor);
    }

    private void initTabs() {
        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();
        mTabs.add(new ViewPageInfo("联系人", R.drawable.ic_message_contact));
        mTabs.add(new ViewPageInfo("商城", R.drawable.ic_message_mall));
        mTabs.add(new ViewPageInfo("购物车", R.drawable.ic_message_shopcart));
        mTabs.add(new ViewPageInfo("门店", R.drawable.ic_message_store));
        mTabs.add(new ViewPageInfo("对话", R.drawable.ic_message_chat));
        mTabs.add(new ViewPageInfo("会员", R.drawable.ic_message_mfh));

        for (ViewPageInfo viewPageInfo : mTabs){
            addTab(viewPageInfo);
        }

        rightTabStrip.setOnClickTabListener(new LeftTabStrip.OnClickTabListener() {
            @Override
            public void onClickTab(View tab, int index) {
                rightTabStrip.selectedTab(index);

//                TextDrawable drawable = TextDrawable.builder()
//                        .beginConfig()
//                        .textColor(Color.WHITE)
//                        .useFont(Typeface.DEFAULT)
//                        .toUpperCase()
//                        .width(DensityUtil.dip2px(getActivity(), 60))  // width in px
//                        .height(DensityUtil.dip2px(getActivity(), 60)) // height in px
//                        .fontSize(DensityUtil.sp2px(getActivity(), 50))/* size in px */
//                        .endConfig()
//                        .buildRect(String.valueOf(index + 95), Color.TRANSPARENT);
//                fabSend.setImageDrawable(drawable);
            }
        });
    }

    private void addTab(ViewPageInfo viewPageInfo){
        Context context = getActivity();
        if (context == null){
            return;
        }
        View v = LayoutInflater.from(context).inflate(
                R.layout.tabitem_message, null, false);
        //in pixel
        v.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(context, 70), DensityUtil.dip2px(context, 70)));
        ImageView icon = (ImageView) v.findViewById(R.id.iv_buttonImage);
        icon.setImageResource(viewPageInfo.resId);

//        icon.setImageDrawable(null);
//        icon.setBackgroundResource(viewPageInfo.resId);
//        icon.setBackground(DrawableUtils.tintDrawable(icon.getBackground(), getResources().getColorStateList(R.color.maintab_tint_colors)));
        rightTabStrip.addTab(v);
    }

    private void initMessageRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messageRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        messageRecyclerView.setHasFixedSize(true);
        //添加分割线
//        couponRecyclerView.addItemDecoration(new VerticalBlockItemDecoration(
//                this, VerticalBlockItemDecoration.HORIZONTAL_LIST));

        chatMessageAdapter = new ChatMessageAdapter(getActivity(), null);
//        messageRecyclerView.setOnAdapterListener(new MaterialCouponAdapter.OnAdapterListener() {
//
//            @Override
//            public void onDataSetChanged() {
//            }
//
//            @Override
//            public void onToggleItem(CouponRule couponRule) {
//                //TODO,发送卡券
//            }
//        });
        messageRecyclerView.setAdapter(chatMessageAdapter);
    }

    private void initCouponRecyclerView() {
        couponLayoutManager = new GridLayoutManager(getActivity(), 3);
        couponRecyclerView.setLayoutManager(couponLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        couponRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        couponRecyclerView.setEmptyView(noCouponView);
        //添加分割线
//        couponRecyclerView.addItemDecoration(new VerticalBlockItemDecoration(
//                this, VerticalBlockItemDecoration.HORIZONTAL_LIST));

        couponAdapter = new MaterialCouponAdapter(getActivity(), null);
        couponAdapter.setOnAdapterListener(new MaterialCouponAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged() {
            }

            @Override
            public void onToggleItem(CouponRule couponRule) {
                //TODO,发送卡券
            }
        });
        couponRecyclerView.setAdapter(couponAdapter);
    }

    private void initEmoji(){
        emojiPageAdapter = new MaterialEmojiPageAdapter(getActivity(), new MaterialEmojiPageAdapter.OnEmojiClickListener() {
            @Override
            public void onItemSelected(int id) {

                if (id == 20) {
                    //删除，回退
                    KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL);
                    etMessage.dispatchKeyEvent(keyEvent);
                    return;
                }
//                etMessage.append(FaceUtil.getFaceString(id));

//                String inputStr = etMessage.getText().toString();
                SpannableString spannableString = FaceUtil.getSpannable(getActivity(),
                        FaceUtil.getFaceString(id), 25, 25);
                etMessage.append(spannableString);
                etMessage.requestFocus();
                etMessage.setSelection(etMessage.getText().length());
            }
        });
        emojiViewPager.setAdapter(emojiPageAdapter);
    }

    /**
     * 隐藏Emoji
     * */
    private void hideEmojiFrame(){
        ibEmoji.setSelected(false);
        emojiViewPager.setVisibility(View.GONE);
    }

    /**
     * 显示Emoji
     * */
    private void showEmojiFrame(){
        ibEmoji.setSelected(true);
        emojiViewPager.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏卡券
     * */
    private void hideCouponFrame(){
        ibCoupon.setSelected(false);
        frameCoupon.setVisibility(View.GONE);
    }

    /**
     * 显示卡券
     * */
    private void showCouponFrame(){
        ibCoupon.setSelected(true);
        frameCoupon.setVisibility(View.VISIBLE);
    }

    public void refresh(){
        etMessage.requestFocus();
    }
}
