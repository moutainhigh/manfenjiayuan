package com.mfh.litecashier.ui.fragment.goods.frontend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosLocalCategoryEntity;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 前台类目
 * Created by bingshanguxue on 17/07/03.
 */
public class FrontendCategoryAdapterV2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_CATEGORY,
        ITEM_TYPE_ACTION
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<PosLocalCategoryEntity> entityList;
    private PosLocalCategoryEntity curEntity;

    public interface OnAdapterListener {
        void onDataSetChanged();

        void onCategoryClick(PosLocalCategoryEntity categoryEntity);

        void onCategoryLongclick(PosLocalCategoryEntity categoryEntity);

        void onClickAction();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public FrontendCategoryAdapterV2(Context context, List<PosLocalCategoryEntity> entityList) {
        this.entityList = entityList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_CATEGORY.ordinal()) {
            return new GoodsViewHolder(mLayoutInflater.inflate(R.layout.itemview_one_text, parent, false));
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_ACTION.ordinal()) {
            return new ActionViewHolder(mLayoutInflater.inflate(R.layout.itemview_frontend_category_add, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        PosLocalCategoryEntity entity = entityList.get(position);

        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_CATEGORY.ordinal()) {

            if (curEntity != null && curEntity.getType() == 0 && curEntity.getId().compareTo(entity.getId()) == 0) {
                holder.itemView.setSelected(true);
            } else {
                holder.itemView.setSelected(false);
            }
            ((GoodsViewHolder) holder).tvContent.setText(entity.getName());
        }
    }


    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    @Override
    public int getItemViewType(int position) {
        PosLocalCategoryEntity entity = entityList.get(position);
        if (entity.getType() == 0) {
            return ITEM_TYPE.ITEM_TYPE_CATEGORY.ordinal();
        }
        return ITEM_TYPE.ITEM_TYPE_ACTION.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public PosLocalCategoryEntity getEntity(int position) {
        if (this.entityList == null || position < 0 || position >= entityList.size()) {
            return null;
        }

        return entityList.get(position);
    }

    public class GoodsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_content)
        TextView tvContent;

        public GoodsViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    PosLocalCategoryEntity goods = getEntity(position);
                    curEntity = goods;

                    notifyDataSetChanged();

                    if (goods != null && adapterListener != null) {
                        adapterListener.onCategoryClick(goods);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    PosLocalCategoryEntity goods = getEntity(position);

                    if (goods != null && adapterListener != null) {
                        adapterListener.onCategoryLongclick(goods);
                    }
                    return false;
                }
            });
        }
    }

    public class ActionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ib_action)
        ImageButton ibAction;

        public ActionViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getLayoutPosition()
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onClickAction();
                    }
                }
            });
        }

        @OnClick(R.id.ib_action)
        public void clickAction() {
            if (adapterListener != null) {
                adapterListener.onClickAction();
            }
        }
    }

    public void setEntityList(List<PosLocalCategoryEntity> goodsList) {
        if (this.entityList == null) {
            this.entityList = new ArrayList<>();
        } else {
            this.entityList.clear();
        }
        if (goodsList != null && goodsList.size() > 0) {
            this.entityList.addAll(goodsList);
            if (curEntity == null) {
                curEntity = this.entityList.get(0);
            }
        }

        PosLocalCategoryEntity action = new PosLocalCategoryEntity();
        action.setType(1);
        this.entityList.add(action);
//        ZLogger.d("entityList.size=" + entityList.size());

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public PosLocalCategoryEntity getCurEntity() {
        return curEntity;
    }


}
