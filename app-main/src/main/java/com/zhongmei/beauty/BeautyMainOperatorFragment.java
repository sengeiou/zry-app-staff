package com.zhongmei.beauty;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhongmei.beauty.entity.BeautyNotifyEntity;
import com.zhongmei.beauty.interfaces.IBeautyOperator;
import com.zhongmei.beauty.operates.BeautyNotifyCache;
import com.zhongmei.util.AsyncImage;
import com.zhongmei.yunfu.R;
import com.zhongmei.yunfu.ShopInfoManager;
import com.zhongmei.yunfu.context.util.SystemUtils;
import com.zhongmei.yunfu.ui.base.BasicFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;


@EFragment(R.layout.beauty_main_fragment_operator)
public class BeautyMainOperatorFragment extends BasicFragment{

    @ViewById(R.id.tv_version)
    TextView tv_version;

    @ViewById(R.id.iv_shop_logo)
    protected ImageView iv_shopLogo;

    @ViewById(R.id.tv_shop_name)
    protected TextView tv_shopName;

    @ViewById(R.id.tv_shopinfo)
    protected TextView tv_shopinfo;

    @ViewById(R.id.tv_customer_number)
    protected TextView tv_customerNumber;
    @ViewById(R.id.tv_reserver_number)
    protected TextView tv_reserverNumber;
    @ViewById(R.id.tv_trade_number)
    protected TextView tv_tradeNumber;
    @ViewById(R.id.tv_member_number)
    protected TextView tv_memberNumber;

    private IBeautyOperator iBeautyOperatorListener;

    @AfterViews
    public void init() {
        showShopInfo();
        setVersion(SystemUtils.getVersionName());
    }


    public void setiBeautyOperatorListener(IBeautyOperator iBeautyOperatorListener) {
        this.iBeautyOperatorListener = iBeautyOperatorListener;
    }


    private void showShopInfo() {
        String shopName = ShopInfoManager.getInstance().shopInfo.getShopName();
        tv_shopName.setText(shopName);
        tv_shopinfo.setText(String.format(getString(R.string.beauty_shopinfo), ShopInfoManager.getInstance().shopInfo.getShopAddress()));

        String logoUrl = ShopInfoManager.getInstance().shopInfo.getShopLogo();
        if (!TextUtils.isEmpty(logoUrl)) {

            AsyncImage.showImg(getActivity(), iv_shopLogo, logoUrl, R.drawable.shop_icon);
        }

    }


    private void setVersion(String versionName){
        if(TextUtils.isEmpty(versionName)){
            tv_version.setVisibility(View.GONE);
            return;
        }

        tv_version.setText("V"+versionName);

    }



    @Click({R.id.btn_create_trade, R.id.btn_create_card, R.id.btn_charge, R.id.btn_create_member, R.id.btn_create_reserver})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_trade:                if (iBeautyOperatorListener != null) {
                    iBeautyOperatorListener.toCreateTrade();
                }
                break;
            case R.id.btn_create_card:                if (iBeautyOperatorListener != null) {
                    iBeautyOperatorListener.toCreateCrad();
                }
                break;
            case R.id.btn_charge:                if (iBeautyOperatorListener != null) {
                    iBeautyOperatorListener.toCharge();
                }
                break;
            case R.id.btn_create_member:                if (iBeautyOperatorListener != null) {
                    iBeautyOperatorListener.toCreateMember();
                }
                break;
            case R.id.btn_create_reserver:                if (iBeautyOperatorListener != null) {
                    iBeautyOperatorListener.toCreateReserver();
                }
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
