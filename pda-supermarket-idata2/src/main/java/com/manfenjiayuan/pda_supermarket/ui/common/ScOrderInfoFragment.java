package com.manfenjiayuan.pda_supermarket.ui.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.InstockTempEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.InstockTempService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.uikit.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;


/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ScOrderInfoFragment extends BaseFragment {

    @BindView(R.id.label_barcode)
    TextLabelView labelBarcode;
    @BindView(R.id.label_receiveName)
    TextLabelView labelReceiveName;
    @BindView(R.id.label_receivePhone)
    TextLabelView labelReceivePhone;
    @BindView(R.id.label_addr)
    TextLabelView labelAddr;
    @BindView(R.id.label_bcount)
    TextLabelView labelBcount;
    @BindView(R.id.label_amout)
    TextLabelView labelAmount;

    @BindView(R.id.label_commitAmount)
    TextLabelView labelCommitAmount;
    @BindView(R.id.label_diffAmount)
    TextLabelView labelDiffAmount;
    @BindView(R.id.label_refundAmount)
    TextLabelView labelRefundAmount;
    @BindView(R.id.label_payAmount)
    TextLabelView labelPayAmount;


    private ScOrder mScOrder = null;

    public static ScOrderInfoFragment newInstance(Bundle args) {
        ScOrderInfoFragment fragment = new ScOrderInfoFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invfind_info;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        refresh(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 验证
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ScOrderEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ScOrderEvent(%d)", eventId));
        switch (eventId) {
            case ScOrderEvent.EVENT_ID_UPDATE: {
                ScOrder scOrder = (ScOrder) args.getSerializable(ScOrderEvent.EXTRA_KEY_SCORDER);
                refresh(scOrder);
            }
            break;
            case ScOrderEvent.EVENT_ID_DATASETCHANGED: {
                refresh(mScOrder);
            }
            break;
        }
    }

    /**
     * 刷新信息
     */
    private void refresh(ScOrder scOrder) {
        mScOrder = scOrder;

        if (scOrder == null) {
            labelBarcode.setTvSubTitle("");
            labelReceiveName.setTvSubTitle("");
            labelReceivePhone.setTvSubTitle("");
            labelAddr.setTvSubTitle("");
            labelBcount.setTvSubTitle("");
            labelAmount.setTvSubTitle("");
            labelCommitAmount.setTvSubTitle("");
        } else {
            labelBarcode.setTvSubTitle(scOrder.getBarcode());
            labelReceiveName.setTvSubTitle(scOrder.getReceiveName());
            labelReceivePhone.setTvSubTitle(scOrder.getReceivePhone());
            labelAddr.setTvSubTitle(scOrder.getAddress());
            labelBcount.setTvSubTitle(MUtils.formatDouble(scOrder.getBcount(), ""));
            labelAmount.setTvSubTitle(MUtils.formatDouble(scOrder.getAmount(), ""));

            labelCommitAmount.setEndText(MUtils.formatDouble(scOrder.getCommitAmount(), ""));

            //返回差额，正值代表需要退钱给用户，负值代表需要用户补钱
            Double diffAmount = MathCompact.sub(scOrder.getAmount(), scOrder.getCommitAmount());
            if (diffAmount != null){
                if (diffAmount > 0){
                    labelDiffAmount.setEndText(MUtils.formatDouble("-", "", Math.abs(diffAmount),
                            "", null, null),
                            ContextCompat.getColor(getContext(), R.color.material_green_500));
                }
                else{
                    labelDiffAmount.setEndText(MUtils.formatDouble("+", "", Math.abs(diffAmount),
                            "", null, null),
                            ContextCompat.getColor(getContext(), R.color.material_red_500));
                }
            }
            else{
                labelDiffAmount.setEndText(MUtils.formatDouble(diffAmount, ""),
                        ContextCompat.getColor(getContext(), R.color.material_black));
            }

            //退款金额，正值代表需要退钱给用户，负值代表需要用户补钱
            Double totalRefund = null, refundAmount = null;
            if (ScOrder.MFHORDER_STATUS_SENDED.equals(scOrder.getStatus())){
                // TODO: 24/10/2016 遍历明细，检查退款金额
                List<InstockTempEntity> entities = InstockTempService.get().queryAll();
                if (entities != null && entities.size() > 0){
                    Double temp = 0D;
                    for (InstockTempEntity entity : entities){
                        if (entity.getIsEnable() == 1){
                            temp += entity.getCommitAmount();
                        }
                    }
                    refundAmount = MathCompact.sub(scOrder.getCommitAmount(), temp);
                    totalRefund = MathCompact.sub(scOrder.getAmount(), temp);
                }
            }
            labelRefundAmount.setEndText(MUtils.formatDouble(refundAmount, ""));

            //退款金额，正值代表需要退钱给用户，负值代表需要用户补钱
//            Double totalRefund = MathCompact.sub(diffAmount, refundAmount);
            //正数表示退款
            if (totalRefund != null && totalRefund > 0){
                labelPayAmount.setEndText(MUtils.formatDouble("-", "", Math.abs(totalRefund),
                        "", null, null),
                        ContextCompat.getColor(getContext(), R.color.material_green_500));
            }
            else{
                labelPayAmount.setEndText(MUtils.formatDouble("+", "", Math.abs(totalRefund),
                        "", null, null),
                        ContextCompat.getColor(getContext(), R.color.material_red_500));
            }
       }
    }
}
