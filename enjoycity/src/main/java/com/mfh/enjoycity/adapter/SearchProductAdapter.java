package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.HistorySearchEntity;
import com.mfh.enjoycity.database.HistorySearchService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.uikit.widget.ChildGridView;
import com.mfh.framework.uikit.widget.FlowLayout;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 搜索商品
 * Created by NAT.ZZN on 15/8/11.
 */
public class SearchProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final DecimalFormat DECIMAL_FORMAT =new DecimalFormat("0.00");

    public enum ITEM_TYPE {
        ITEM_TYPE_HOT,
        ITEM_TYPE_HISTORY,
        ITEM_TYPE_OPERATION
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private SearchProductBean adapterData;

    private int hotCount, historyCount;

    public interface OnAdapterListener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onSearch(String queryText);
    }
    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener)
    {
        this.adapterListener = adapterListener;
    }


    public SearchProductAdapter(Context context, SearchProductBean adapterData) {
        this.adapterData = adapterData;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);

        hotCount = this.adapterData.getHotCount();
        historyCount = this.adapterData.getHistoryCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_HOT.ordinal()){
            return new HotSearchViewHolder(mLayoutInflater.inflate(R.layout.view_item_search_hot, parent, false));
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_HISTORY.ordinal()){
            return new HistoryViewHolder(mLayoutInflater.inflate(R.layout.view_item_search_history, parent, false));
        }
        else if (viewType == ITEM_TYPE.ITEM_TYPE_OPERATION.ordinal()){
            return new OperationViewHolder(mLayoutInflater.inflate(R.layout.view_item_search_operation, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_HOT.ordinal()) {
            List<String> hotData = adapterData.getHotData();
            for(final String text : hotData){
                TextView tv = new TextView(mContext);
                tv.setHeight(DensityUtil.dip2px(mContext, 32));
                tv.setTextSize(14);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(ContextCompat.getColorStateList(mContext, R.color.material_black));
//                tv.setBackgroundResource(R.drawable.btn_recharge_alipay_bg);
                tv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.orangered));
                tv.setText(text);
                tv.setPadding(5, 5, 5, 5);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adapterListener != null) {
                            adapterListener.onSearch(text);
                        }
                    }
                });
                        ((HotSearchViewHolder) holder).flowLayout.addView(tv);
            }
        }
        else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_HISTORY.ordinal()) {
//            ((HistoryViewHolder)holder).listView.setAdapter(new HistorySearchAdapter(mContext, adapterData.getHistoryData()));
            ((HistoryViewHolder)holder).gridView.setAdapter(new HistorySearchAdapter(mContext, adapterData.getHistoryData()));
        }
        else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_OPERATION.ordinal()) {
        }
    }

    @Override
    public int getItemCount() {
        return adapterData == null ? 0 : (hotCount + historyCount + 1);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == hotCount-1){
            return ITEM_TYPE.ITEM_TYPE_HOT.ordinal();
        }
        else if(position == hotCount + historyCount-1){
            return ITEM_TYPE.ITEM_TYPE_HISTORY.ordinal();
        }
        else{
            return ITEM_TYPE.ITEM_TYPE_OPERATION.ordinal();
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public SearchProductBean getData(){
        return adapterData;
    }

    public class HotSearchViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.flow_layout)
        FlowLayout flowLayout;

        public HotSearchViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.listview_history)
        ListView listView;
        @BindView(R.id.grid_query)
        ChildGridView gridView;

        public HistoryViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try{
                        HistorySearchAdapter adapter = (HistorySearchAdapter)parent.getAdapter();
                        HistorySearchEntity entity = (HistorySearchEntity)adapter.getItem(position);
                        if (adapterListener != null){
                            adapterListener.onSearch(entity.getQueryContent());
                        }
                    }
                    catch (Exception ex){
                        ZLogger.e(ex.toString());
                    }

                }
            });
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (adapterListener != null){
                        adapterListener.onSearch("dd");
                    }
                }
            });
        }
    }

    public class OperationViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.button_clear_history)
        Button btnClearHistory;

        public OperationViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }

        @OnClick(R.id.button_clear_history)
        public void clearHistory(){
            HistorySearchService dbService = HistorySearchService.get();
            dbService.clear();
            if (adapterData != null){
                adapterData.setHistoryData(null);
                notifyDataSetChanged();
            }
        }
    }



}
