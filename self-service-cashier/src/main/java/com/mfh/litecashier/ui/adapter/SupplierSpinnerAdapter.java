package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 供应商
 * Created by Nat.ZZN(bingshanguxue) on 2015/4/20.
 */
public class SupplierSpinnerAdapter extends BaseAdapter {

    private Context context;
    private List<CompanyInfo> entityList = new ArrayList<>();

    static class ViewHolder {
        @Bind(R.id.tv_title) TextView tvTitle;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public SupplierSpinnerAdapter(Context context) {
        super();
        this.context = context;
    }

    public SupplierSpinnerAdapter(Context context, List<CompanyInfo> entityList) {
        super();
        this.context = context;
        this.entityList = entityList;
    }

    @Override
    public int getCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    @Override
    public Object getItem(int i) {
        if(entityList != null){
            return entityList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(context, R.layout.itemview_supplier, null);

            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        CompanyInfo entity = entityList.get(i);

        viewHolder.tvTitle.setText(entity.getName());
        return view;
    }


    public List<CompanyInfo> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<CompanyInfo> entityList) {
        this.entityList = entityList;
        this.notifyDataSetChanged();
    }
}
