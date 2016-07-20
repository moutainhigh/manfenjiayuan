package com.mfh.enjoycity.ui.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.ui.fragments.ShoppingCartFragment;
import com.mfh.framework.uikit.base.BaseActivity;

import butterknife.Bind;


/**
 * 购物车
 * */
public class ShoppingCartActivity extends BaseActivity {

    @Bind(R.id.tool_bar)
    Toolbar toolbar;

    public static void actionStart(Context context, int animationType) {
        Intent intent = new Intent(context, ShoppingCartActivity.class);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, animationType);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_cart);
        toolbar.setTitleTextAppearance(this, R.style.toolbar_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.abc_ic_clear_mtrl_alpha);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShoppingCartActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        ShoppingCartFragment shoppingCartFragment = new ShoppingCartFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, shoppingCartFragment)
                .show(shoppingCartFragment)
                .commit();
    }

    /**
     * */
    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);

            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if(animType == ANIM_TYPE_NEW_FLOW){
                this.setTheme(R.style.AppTheme_NewFlow);
            }
        }
    }

}
