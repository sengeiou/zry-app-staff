package com.zhongmei.beauty.order;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhongmei.beauty.BeautyShopCartActivity;
import com.zhongmei.beauty.order.util.BeautyPrePrintUtil;
import com.zhongmei.bty.basemodule.shoppingcart.utils.MathShoppingCartTool;
import com.zhongmei.yunfu.ShopInfoManager;
import com.zhongmei.yunfu.db.enums.BusinessType;
import com.zhongmei.yunfu.MainApplication;
import com.zhongmei.beauty.BeautyOrderActivity;
import com.zhongmei.yunfu.R;
import com.zhongmei.bty.basemodule.auth.application.BeautyApplication;
import com.zhongmei.bty.basemodule.customer.manager.CustomerManager;
import com.zhongmei.bty.basemodule.discount.bean.MarketRuleVo;
import com.zhongmei.bty.basemodule.orderdish.bean.DishDataItem;
import com.zhongmei.bty.basemodule.orderdish.bean.ISetmealShopcartItem;
import com.zhongmei.bty.basemodule.orderdish.bean.IShopcartItem;
import com.zhongmei.bty.basemodule.orderdish.bean.IShopcartItemBase;
import com.zhongmei.bty.basemodule.orderdish.bean.ShopcartItem;
import com.zhongmei.yunfu.context.session.core.auth.Auth;
import com.zhongmei.yunfu.context.session.core.user.User;
import com.zhongmei.bty.basemodule.session.support.VerifyHelper;
import com.zhongmei.bty.basemodule.shopmanager.interfaces.ChangePageListener;
import com.zhongmei.bty.basemodule.shoppingcart.DinnerShoppingCart;
import com.zhongmei.bty.basemodule.trade.bean.TradeVo;
import com.zhongmei.bty.basemodule.trade.manager.DinnerShopManager;
import com.zhongmei.beauty.order.BeautyOrderManager;
import com.zhongmei.beauty.order.adapter.BeautyShopcartAdapter;
import com.zhongmei.beauty.order.event.BeautyCustmoerEvent;
import com.zhongmei.beauty.order.event.MarketActivityEvent;
import com.zhongmei.beauty.utils.BeautyOrderConstants;
import com.zhongmei.beauty.order.util.IChangeMiddlePageListener;
import com.zhongmei.bty.cashier.shoppingcart.ShoppingCartListener;
import com.zhongmei.bty.cashier.shoppingcart.ShoppingCartListerTag;
import com.zhongmei.yunfu.ui.base.BasicFragment;
import com.zhongmei.yunfu.db.entity.trade.Tables;
import com.zhongmei.yunfu.db.enums.TradeType;
import com.zhongmei.yunfu.util.ToastUtil;
import com.zhongmei.yunfu.context.util.Utils;
import com.zhongmei.yunfu.ui.view.CommonDialogFragment;
import com.zhongmei.bty.dinner.vo.LoadingFinish;
import com.zhongmei.bty.snack.event.EventEditModle;
import com.zhongmei.yunfu.db.entity.trade.Trade;
import com.zhongmei.yunfu.util.ValueEnums;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.internal.Util;


@EFragment(R.layout.beauty_order_left)
public class BeautyOrderLeftFragment extends BasicFragment {
    private final static String TAG = BeautyOrderLeftFragment.class.getSimpleName();

    private BeautyTradeInfoFragment tradeInfoFragment;

    protected LoadingFinish mLoadingFinish;

    DinnerShoppingCart mShopcarting;
    private MarketRuleVo marketRuleVo;
    @ViewById(R.id.cancel_choose_dish_btn)
    Button btn_market_cancel;

    @ViewById(R.id.ok_choose_dish_btn)
    Button btn_ok_cancel;

    @ViewById(R.id.beauty_total_price)
    TextView total_price;

    @ViewById(R.id.beauty_btn_save)
    Button btn_save;

    @ViewById(R.id.beauty_btn_pay)
    Button btn_pay;

    @ViewById(R.id.beauty_btn_print)
    Button btn_print;

    private boolean isEdit = false;
    private BusinessType mBusinessType = BusinessType.BEAUTY;

    private ChangePageListener mChangePageListener;

    private IChangeMiddlePageListener middleChangeListener;
    private boolean isEditMode = false;

    public void registerLoadingListener(LoadingFinish mLoadingFinish) {
        this.mLoadingFinish = mLoadingFinish;
    }

    @AfterViews
    public void initView() {
        registerEventBus();
        mShopcarting = DinnerShoppingCart.getInstance();
        mShopcarting.registerListener(ShoppingCartListerTag.ORDER_DISH_LEFT, mModifyShoppingCartListener);
        tradeInfoFragment = new BeautyTradeInfoFragment_();
        tradeInfoFragment.registerListener(mChangePageListener, middleChangeListener);
        replaceFragment(R.id.beauty_left_content, tradeInfoFragment, "tradeInfoFragment");
        tradeInfoFragment.setDishCheckMode(false);
        initView(mBusinessType);
        initData();
    }


    private void initData() {
//        isEdit = getActivity().getIntent().getBooleanExtra(BeautyOrderConstants.IS_ORDER_EDIT, false);
//        final Tables tables = (Tables) getActivity().getIntent().getSerializableExtra(BeautyOrderConstants.ORDER_EDIT_TABLE);
//        final Long tradeId = getActivity().getIntent().getLongExtra(BeautyOrderConstants.ORDER_EDIT_TRADEID, -1);
//        Integer businessTypeValue = getActivity().getIntent().getIntExtra(BeautyOrderConstants.ORDER_BUSINESSTYPE, ValueEnums.toValue(BusinessType.BEAUTY));
//        mBusinessType = ValueEnums.toEnum(BusinessType.class, businessTypeValue);
//        initTask = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void[] objects) {
//                BeautyOrderManager.initOrder(isEdit, tradeId, tables, mBusinessType);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                updateShopCartView(mShopcarting.getShoppingCartDish(), mShopcarting.getOrder());
//                initTableView();
//                mLoadingFinish.loadingFinish();
//                EventBus.getDefault().post(new BeautyCustmoerEvent(CustomerManager.getInstance().getDinnerLoginCustomer()));
//            }
//        }.execute();


        mLoadingFinish.loadingFinish();
        initTableView();
        updateView(mShopcarting.getOrder().getTrade(),mShopcarting.getShoppingCartDish());
        EventBus.getDefault().post(new BeautyCustmoerEvent(CustomerManager.getInstance().getDinnerLoginCustomer()));
    }


    private void initView(BusinessType busType) {
        if (isBuyServer(busType)) {
            btn_save.setVisibility(View.GONE);
            btn_print.setVisibility(View.GONE);
        }
    }

    private boolean isBuyServer(BusinessType busType) {
        return ValueEnums.equalsValue(busType, ValueEnums.toValue(BusinessType.CARD_TIME));
    }

    private void initTableView() {
        FragmentActivity activity = getActivity();
        if (DinnerShoppingCart.getInstance().getOrder().getTradeTableList() != null && activity instanceof BeautyShopCartActivity) {
            ((BeautyShopCartActivity) activity).setTables(DinnerShoppingCart.getInstance().getOrder().getTradeTableList());
        }
    }

    private void updateShopCartHint(Trade trade, List<IShopcartItem> list) {
        FragmentActivity activity = getActivity();
        if (DinnerShoppingCart.getInstance().getOrder().getTradeTableList() != null && activity instanceof BeautyShopCartActivity) {
            ((BeautyShopCartActivity) activity).updateShopCartHint(trade, list, isEditMode);
        }
    }


    public void loginOrOut() {
        BeautyOrderCustomerLoginFragment loginFragment = (BeautyOrderCustomerLoginFragment) getChildFragmentManager().findFragmentById(R.id.beauty_left_customer);
        if (loginFragment != null) {
            loginFragment.loginOrOut();
        }
    }


    public DishDataItem doSelected(IShopcartItemBase item) {
        if (item == null) {
            return null;
        }
        DishDataItem dishDataItem = null;
        try {
            dishDataItem = tradeInfoFragment.getSelectedDishAdapter().doSelectedItem(item.getUuid());
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return dishDataItem;
    }

    public DishDataItem doSelected(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return null;
        }
        return tradeInfoFragment.getSelectedDishAdapter().doSelectedItem(uuid);
    }


    public void goDefaultDiscountMode() {
        clearAllSelected();
        tradeInfoFragment.goDefaultMode();
        tradeInfoFragment.getSelectedDishAdapter().notifyDataSetChanged();
    }


    public void goAllDiscountMode() {
        tradeInfoFragment.goAllDiscountMode();
    }

    public void cancelMarketPage() {
        tradeInfoFragment.setDishCheckMode(false);
        showMarketingCampaignDishCheckMode(false, null);
    }


    public void goBatchDiscountMode() {
        tradeInfoFragment.goBatchDiscountMode();
    }


    public void clearAllSelected() {
        tradeInfoFragment.getSelectedDishAdapter().clearAllSelected();
    }


    ShoppingCartListener mModifyShoppingCartListener = new ShoppingCartListener() {

        @Override
        public void addToShoppingCart(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo, ShopcartItem mShopcartItem) {
            updateShopCartView(listOrderDishshopVo, mTradeVo, mShopcartItem, OperationStatus.addToShoppingCart);
        }

        public void updateDish(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void removeShoppingCart(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo,
                                       IShopcartItemBase mShopcartItemBase) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);

        }

        @Override
        public void removeSetmealRemark(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo,
                                        ISetmealShopcartItem setmeal) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }


        @Override
        public void clearShoppingCart() {
            updateShopCartView(mShopcarting.getShoppingCartDish(), mShopcarting.getOrder());
        }

        @Override
        public void orderDiscount(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void removeDiscount(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo,
                                   IShopcartItem mShopcartItem) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void setRemark(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void removeRemark(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo, IShopcartItem mShopcartItem) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void batchPrivilege(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {

            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void addMarketActivity(TradeVo mTradeVo) {
            super.addMarketActivity(mTradeVo);
            updateShopCartView(mShopcarting.getShoppingCartDish(), mTradeVo);
        }

        @Override
        public void removeMarketActivity(TradeVo mTradeVo) {
            super.removeMarketActivity(mTradeVo);
            updateShopCartView(mShopcarting.getShoppingCartDish(), mTradeVo);
        }

        @Override
        public void setIntegralCash(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void removeIntegralCash(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void setCouponPrivi1lege(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void removeCouponPrivilege(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void addWeiXinCouponsPrivilege(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void removeWeiXinCouponsPrivilege(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void doBanquet(TradeVo mTradeVo) {
            updateShopCartView(mShopcarting.getShoppingCartDish(), mShopcarting.getOrder());
        }

        @Override
        public void removeCustomerPrivilege(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void resetOrder(List<IShopcartItem> listOrderDishshopVo, TradeVo mTradeVo) {
            updateShopCartView(listOrderDishshopVo, mTradeVo);
        }

        @Override
        public void updateUserInfo() {
            updateShopCartView(mShopcarting.getShoppingCartDish(), mShopcarting.getOrder());
        }

        @Override
        public void exception(String message) {
            new CommonDialogFragment.CommonDialogFragmentBuilder(MainApplication.getInstance()).title(getResources().getString(R.string.invalidLogin))
                    .iconType(R.drawable.commonmodule_dialog_icon_warning)
                    .negativeText(R.string.reLogin)
                    .negativeLisnter(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Intent intent = new Intent();
                            intent.setClassName("com.zhongmei.bty",
                                    "com.zhongmei.bty.splash.login.LoginActivity_");
                            startActivity(intent);
                        }
                    })
                    .build()
                    .show(getActivity().getSupportFragmentManager(), TAG);
        }

        ;

    };

    public void updateShopCartView(List<IShopcartItem> list, TradeVo tradeVo) {
        updateShopCartView(list, tradeVo, null, ShoppingCartListener.OperationStatus.None);
    }


    public void updateShopCartView(List<IShopcartItem> list, TradeVo tradeVo, ShopcartItem shopcartItem, ShoppingCartListener.OperationStatus operationStatus) {
        try {
            if (this.isAdded()) {
                if (tradeInfoFragment != null) {
                    tradeInfoFragment.updateOrderDishList(list, tradeVo, shopcartItem, operationStatus);
                    updateView(tradeVo.getTrade(), list);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateView(Trade trade, List<IShopcartItem> list) {
        updateShopCartHint(trade, mShopcarting.getAllValidShopcartItem(list));

        BigDecimal price = trade.getTradeAmount();
        if (price == null) {
            total_price.setText(Utils.formatPrice(0.00));
        } else {
            total_price.setText(Utils.formatPrice(price.doubleValue()));
        }

        List<IShopcartItem> shopcartItemList = mShopcarting.getShoppingCartDish();
        if (isEditMode || !BeautyOrderManager.isShopcartCanDisplay(mShopcarting.getOrder(), shopcartItemList) && !mShopcarting.getOrder().isHasValidTable()) {
            btn_save.setEnabled(false);
            btn_print.setEnabled(false);
        } else {
            if (trade.getTradeType() != TradeType.SELL_FOR_REPEAT) {
                btn_save.setEnabled(true);
                btn_print.setEnabled(true);
            } else {
                btn_save.setEnabled(false);
                btn_print.setVisibility(View.GONE);
            }
        }

        if (isEditMode || Utils.isEmpty(shopcartItemList)) {
            btn_pay.setEnabled(false);
        } else {
            btn_pay.setEnabled(true);
        }
    }

    @Click({R.id.beauty_btn_save, R.id.beauty_btn_pay, R.id.beauty_btn_print})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.beauty_btn_save:
                BeautyOrderManager.saveTrade(this.getActivity(), mShopcarting);
                break;
            case R.id.beauty_btn_pay:
                if (isBuyServer(mBusinessType) && DinnerShopManager.getInstance().getLoginCustomer() == null) {
                    ToastUtil.showShortToast(R.string.beauty_no_login_customer);
                    return;
                }

                VerifyHelper.verifyAlert(getActivity(), BeautyApplication.PERMISSION_BEAUTY_CASH,
                        new VerifyHelper.Callback() {
                            @Override
                            public void onPositive(User user, String code, Auth.Filter filter) {
                                super.onPositive(user, code, filter);
                                mShopcarting.refreshDish();
                                BeautyOrderManager.gotoPay(getActivity(), mShopcarting.createOrder());
                            }
                        });

                break;
            case R.id.beauty_btn_print:
                BeautyPrePrintUtil.doWillPrint(getActivity());
                break;
        }
    }

    public void onEventMainThread(EventEditModle event) {
        if (event != null && event.isEditModle) {
            isEditMode = true;
            btn_save.setEnabled(false);
            btn_pay.setEnabled(false);
        } else {
            isEditMode = false;
            btn_save.setEnabled(true);
            btn_pay.setEnabled(true);
            middleChangeListener.closePage(null);
        }
    }

    public void onEventMainThread(MarketActivityEvent event) {
        showMarketingCampaignDishCheckMode(event.isSelected, event.marketRuleVo);
    }


    public void showMarketingCampaignDishCheckMode(boolean show, MarketRuleVo marketRuleVo) {
        BeautyShopcartAdapter selectedDishAdapter = tradeInfoFragment.getSelectedDishAdapter();
        if (show) {
            tradeInfoFragment.setDishCheckMode(true);
            selectedDishAdapter.setMarketRuleVo(marketRuleVo);
            selectedDishAdapter.setMarketActivityItemsCheckStatus(marketRuleVo, DinnerShopManager.getInstance().getShoppingCart().getOrder().getTradeItemPlanActivityList());
            DinnerShopManager.getInstance().getShoppingCart().doDishActivityIsCheck(selectedDishAdapter.getUnMarketActivityItems(), marketRuleVo);
            selectedDishAdapter.notifyDataSetChanged();

            this.marketRuleVo = marketRuleVo;
            btn_market_cancel.setVisibility(View.VISIBLE);
            btn_ok_cancel.setVisibility(View.VISIBLE);
        } else {
            selectedDishAdapter.setDishCheckMode(false);
            selectedDishAdapter.setMarketRuleVo(null);
            selectedDishAdapter.notifyDataSetChanged();
            btn_market_cancel.setVisibility(View.GONE);
            btn_ok_cancel.setVisibility(View.GONE);
        }

    }


    @Click({R.id.cancel_choose_dish_btn, R.id.ok_choose_dish_btn})
    void clickMarketingCampaignButtons(View view) {
        switch (view.getId()) {
            case R.id.cancel_choose_dish_btn:
                showMarketingCampaignDishCheckMode(false, null);
                cancelSelectInMarketActivityFragment();
                break;
            case R.id.ok_choose_dish_btn:
                if (tradeInfoFragment.getSelectedDishAdapter().getCheckedNumber() == 0) {
                    ToastUtil.showShortToast(R.string.beauty_marketing_campaign_addfail_notchoose);
                    return;
                }

                if (DinnerShopManager.getInstance().getShoppingCart().addMarketActivity(marketRuleVo,
                        tradeInfoFragment.getSelectedDishAdapter().getCheckedIShopcartItems())) {
                    showMarketingCampaignDishCheckMode(false, null);
                    cancelSelectInMarketActivityFragment();
                } else {
                    ToastUtil.showShortToast(R.string.beauty_marketing_campaign_addfail);
                }
                break;

            default:
                break;
        }
    }

    public void registerListener(ChangePageListener changePageListener, IChangeMiddlePageListener middleChangeListener) {
        this.mChangePageListener = changePageListener;
        this.middleChangeListener = middleChangeListener;
    }


    private void cancelSelectInMarketActivityFragment() {
        if (mChangePageListener == null) {
            return;
        }
        mChangePageListener.changePage(ChangePageListener.PAGE_CANCEL_MARKET, null);
    }


    public void setTableName(String roomName) {
    }

    public void clearShopCart() {
//        initTask.cancel(true);
        DinnerShoppingCart.getInstance().unRegisterListenerByTag(ShoppingCartListerTag.ORDER_DISH_LEFT);
//        BeautyOrderManager.clearShopcart();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity().isFinishing()) {
            clearShopCart();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
