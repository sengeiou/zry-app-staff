package com.zhongmei.beauty.order;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.zhongmei.beauty.order.action.AppletRemoveAction;
import com.zhongmei.beauty.order.adapter.BeautyShopcartAdapter;
import com.zhongmei.beauty.order.event.BeautyUpdateUserInfo;
import com.zhongmei.beauty.order.util.IChangeMiddlePageListener;
import com.zhongmei.beauty.utils.TradeUserUtil;
import com.zhongmei.bty.basemodule.discount.bean.CouponPrivilegeVo;
import com.zhongmei.bty.basemodule.discount.entity.ExtraCharge;
import com.zhongmei.bty.basemodule.discount.manager.ExtraManager;
import com.zhongmei.bty.basemodule.inventory.bean.InventoryItem;
import com.zhongmei.bty.basemodule.inventory.utils.InventoryUtils;
import com.zhongmei.bty.basemodule.orderdish.bean.DishDataItem;
import com.zhongmei.bty.basemodule.orderdish.bean.DishDataItem.DishCheckStatus;
import com.zhongmei.bty.basemodule.orderdish.bean.ISetmealShopcartItem;
import com.zhongmei.bty.basemodule.orderdish.bean.IShopcartItem;
import com.zhongmei.bty.basemodule.orderdish.bean.IShopcartItemBase;
import com.zhongmei.bty.basemodule.orderdish.bean.ShopcartItem;
import com.zhongmei.bty.basemodule.orderdish.enums.ItemType;
import com.zhongmei.bty.basemodule.shopmanager.interfaces.ChangePageListener;
import com.zhongmei.bty.basemodule.shoppingcart.DinnerShoppingCart;
import com.zhongmei.bty.basemodule.shoppingcart.bean.ShoppingCartVo;
import com.zhongmei.bty.basemodule.trade.bean.TradeVo;
import com.zhongmei.bty.basemodule.trade.manager.DinnerCashManager;
import com.zhongmei.bty.basemodule.trade.manager.DinnerShopManager;
import com.zhongmei.bty.cashier.shoppingcart.ShoppingCartListener;
import com.zhongmei.bty.commonmodule.util.manager.ClickManager;
import com.zhongmei.bty.dinner.action.ActionDinnerBatchDiscount;
import com.zhongmei.bty.dinner.action.ActionSeparateDeleteCoupon;
import com.zhongmei.bty.dinner.action.ActionSeparateDeleteIntegral;
import com.zhongmei.bty.dinner.manager.DinnerTradeItemManager;
import com.zhongmei.bty.dinner.shopcart.adapter.SuperShopCartAdapter;
import com.zhongmei.bty.snack.event.EventChoice;
import com.zhongmei.bty.snack.event.EventSelectDish;
import com.zhongmei.yunfu.R;
import com.zhongmei.yunfu.context.util.Utils;
import com.zhongmei.yunfu.db.entity.discount.TradePrivilege;
import com.zhongmei.yunfu.db.entity.dish.TradeItemOperation;
import com.zhongmei.yunfu.db.enums.Bool;
import com.zhongmei.yunfu.db.enums.BusinessType;
import com.zhongmei.yunfu.db.enums.DishType;
import com.zhongmei.yunfu.db.enums.PrivilegeType;
import com.zhongmei.yunfu.db.enums.StatusFlag;
import com.zhongmei.yunfu.orm.DBHelperManager;
import com.zhongmei.yunfu.orm.DatabaseHelper;
import com.zhongmei.yunfu.ui.base.BasicFragment;
import com.zhongmei.yunfu.util.DensityUtil;
import com.zhongmei.yunfu.util.ToastUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.beauty_tradeinfo_fragment)
public class BeautyTradeInfoFragment extends BasicFragment {

    private static final String TAG = BeautyTradeInfoFragment.class.getSimpleName();
    //购物车界面所处页面
    public static final String DISHSHOPCART_PAGE = "shopcart_page";

    @ViewById(R.id.lv_selected_dish)
    SwipeMenuListView orderDishListView;//

    @ViewById(R.id.haveNoDishLayout)
    RelativeLayout haveNoDishLayout;

    @ViewById(R.id.haveNoDishImage)
    ImageView haveNoDishImage;

    @ViewById(R.id.tv_select_all)
    TextView tvSelectAll;

    protected BeautyShopcartAdapter selectedDishAdapter;// 适配器

    private int mCurrentPosition;// 当前定位

    protected DinnerShoppingCart mShoppingCart;

    private TradeVo tradeVo;

    //订单中的催菜数目
    private long tradeItemOperationCount = 0;

    // 是否显示退菜、改菜后无效的数据
    private boolean isShowDelete = true;

    private ChangePageListener mChangePageListener;

    private DatabaseHelper.DataChangeObserver observer;

    private boolean enableSlide = true;//是否允许滑动

    private BusinessType mBusinessType = BusinessType.DINNER;
    //初始菜品缓存
    private Map<String, List<TradeItemOperation>> mUnOpItemOperation = new HashMap<>();

    List<IShopcartItem> currentShopcartList = null;
    private List<IShopcartItem> mListOrderDishshopVo;
    private boolean isInit = true;

    private boolean isDiscountPage = false;
    private boolean isBatchDiscountMode = false;
    private boolean isAllSelect = false;
    private IChangeMiddlePageListener middleChangeListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShoppingCart = DinnerShoppingCart.getInstance();
        observer = new TradeItemOperationDataChangeObserver();
        registerEventBus();
    }


    public void registerListener(ChangePageListener mChangePageListener, IChangeMiddlePageListener middleChangeListener) {
        this.mChangePageListener = mChangePageListener;
        this.middleChangeListener = middleChangeListener;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unregisterEventBus();
        super.onDestroy();
    }

    @AfterViews
    void init() {
        selectedDishAdapter = new BeautyShopcartAdapter(getActivity());
        orderDishListView.setAdapter(selectedDishAdapter);
        orderDishListView.setMenuCreator(mSwipeMenuCreator);
        orderDishListView.setItemsCanFocus(false);
        orderDishListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        selectedDishAdapter.isShowMemeberDiscount(true);
        //可以移除营销活动
        selectedDishAdapter.setCanRemoveMarketActivity(true);
        bindItemListener();
        bindMenuListener();

    }

    private SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {

        @Override
        public void create(SwipeMenu menu) {
            switch (menu.getViewType()) {
                case SuperShopCartAdapter.MARKET_TYPE:
                case SuperShopCartAdapter.MEMO_TYPE:
                case SuperShopCartAdapter.CARD_SERVICE_LABEL:
                case SuperShopCartAdapter.CARD_SERVICE_ITEM:
                    break;
                default:
                    createMenu0(menu, 0);
            }

        }

        private void createMenu0(SwipeMenu menu, int type) {
            SwipeMenuItem item = new SwipeMenuItem(getActivity().getApplicationContext());
            item.setBackground(new ColorDrawable(getActivity().getResources().getColor(R.color.bg_red)));
            item.setWidth(DensityUtil.dip2px(getActivity(), 100));

            item.setIcon(getActivity().getResources().getDrawable(R.drawable.beauty_order_swipemenu_delete_icon));
            menu.addMenuItem(item);
        }
    };

    private void bindMenuListener() {
        orderDishListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                deleteItem(position);
                return false;
            }
        });
    }

    /**
     * 删除品项
     *
     * @param position
     */
    private void deleteItem(int position) {
        DishDataItem dishDataItem = selectedDishAdapter.getItem(position);
        if (dishDataItem == null) {
            return;
        }
        switch (dishDataItem.getType()) {
            case EXCISE_TAX:
                ToastUtil.showLongToast(R.string.tax_cannot_delete);
                break;
            // 优惠券
            case COUPONS:
                if (dishDataItem.getCouponPrivilegeVo().isUsed()) {
                    ToastUtil.showLongToast(R.string.dinner_privilege_used);
                    return;
                }
                DinnerShopManager.getInstance().getShoppingCart().removeCouponPrivilege(dishDataItem.getCouponPrivilegeVo(), true);
                sendCouponAction(dishDataItem.getCouponPrivilegeVo());
                break;
            // 积分
            case INTERGRAL:
                if (dishDataItem.getIntegralCashPrivilegeVo().isUsed()) {
                    ToastUtil.showLongToast(R.string.dinner_privilege_used);
                    return;
                }
                DinnerShopManager.getInstance().getShoppingCart().removeIntegralCash();
                sendIntegralAction();
                break;
            // 微信卡券
            case WECHAT_CARD_COUPONS:
                if (dishDataItem.getWeiXinCouponsVo() != null
                        && dishDataItem.getWeiXinCouponsVo().getmTradePrivilege() != null) {
                    if (dishDataItem.getWeiXinCouponsVo().isUsed()) {
                        ToastUtil.showLongToast(R.string.dinner_privilege_used);
                        return;
                    }
                    DinnerShopManager.getInstance().getShoppingCart()
                            .removeWeiXinCouponsPrivilege(dishDataItem.getWeiXinCouponsVo().getmTradePrivilege());
                }
                break;
            case COMBO_DISCOUNT:
            case SINGLE_DISCOUNT:
                IShopcartItem shopcartItem = dishDataItem.getItem();
                if (shopcartItem != null && shopcartItem.getPrivilege() != null) {
                    TradePrivilege tradePrivilege = shopcartItem.getPrivilege();
                    DinnerShopManager.getInstance().getShoppingCart().removeDishPrivilege(shopcartItem);
                    if (tradePrivilege.getPrivilegeType() != PrivilegeType.AUTO_DISCOUNT
                            && tradePrivilege.getPrivilegeType() != PrivilegeType.MEMBER_PRICE) {
                        // 如果是会员登录并且移除的不是会员折扣 恢复会员折扣
                        if (DinnerShopManager.getInstance().getLoginCustomer() != null) {
                            DinnerShopManager.getInstance().getShoppingCart().memberPrivilege(shopcartItem, true, true);
                        }
                    }
                }
                break;
            case CHILD_MEMO:
            case SINGLE_MEMO:
            case COMBO_MEMO:
            case ALL_MEMO:
                break;
            // 整单折扣移除
            case ALL_DISCOUNT:
                DinnerShopManager.getInstance().getShoppingCart().removeOrderPrivilege();
                break;
            case ADDITIONAL:
                ExtraCharge extraCharge = dishDataItem.getExtraCharge();
                if (extraCharge != null) {
                    if (extraCharge.getCode().equals(ExtraManager.BUFFET_OOUTTIME_CODE)) {
                        ToastUtil.showShortToast(R.string.buffet_outtimefee_delete);
                    } else if (extraCharge.getCode().equals(ExtraManager.BUFFET_MIN_CONSUM)) {
                        DinnerShopManager.getInstance().getShoppingCart().removeMinconsumExtra();
                    } else if (extraCharge.getCode().equals(ExtraManager.SERVICE_CONSUM)) {
                        ToastUtil.showShortToast(R.string.server_consum_delete);
                    } else {
                        DinnerShopManager.getInstance().getShoppingCart().removeExtraCharge(extraCharge.getId());
                    }
                }

                break;
            case BANQUET_PRIVILIGE://移除宴请
                DinnerShopManager.getInstance().getShoppingCart().removeBanquet();
                break;
            case GIFT_COUPON:
                //移除礼品劵
                IShopcartItem item = dishDataItem.getItem();
                if (item != null && item.getCouponPrivilegeVo() != null && item.getCouponPrivilegeVo().getTradePrivilege() != null) {
                    ShoppingCartVo shoppingCartVo = DinnerShopManager.getInstance().getShoppingCart().getShoppingCartVo();
                    DinnerShopManager.getInstance().getShoppingCart().removeGiftCouponePrivilege(item.getCouponPrivilegeVo().getTradePrivilege().getPromoId(), shoppingCartVo, true);
                    // 移除礼品劵后,恢复会员折扣
                    if (DinnerShopManager.getInstance().getLoginCustomer() != null) {
                        DinnerShopManager.getInstance().getShoppingCart().memberPrivilege(item, true, true);
                    }
                    ActionSeparateDeleteCoupon coupon = new ActionSeparateDeleteCoupon();
                    coupon.id = item.getCouponPrivilegeVo().getTradePrivilege().getPromoId();
                    EventBus.getDefault().post(coupon);
                }
                break;
            case SINGLE:
            case COMBO:
            case CHILD:
                detailInventy(dishDataItem);
                // 这一项要在最后
                DinnerTradeItemManager.getInstance().deleteItem(dishDataItem.getBase(),
                        dishDataItem.getItem().getUuid(), mChangePageListener, getActivity());
                EventBus.getDefault().post(new AppletRemoveAction(dishDataItem.getBase()));
                if (middleChangeListener != null) {
                    middleChangeListener.closePage(null);
                }
                break;
            case ITEM_USER:
                removeItemUser(dishDataItem);
                break;
            case TRADE_USER:
                removeTradeUser(dishDataItem);
                break;
        }
    }

    /**
     * 处理退库存
     * @param dishDataItem
     */
    private void detailInventy(DishDataItem dishDataItem){
        if (dishDataItem == null||dishDataItem.getBase()==null) {
            return;
        }
        if(dishDataItem.getBase().getStatusFlag() != StatusFlag.VALID
                ||!DinnerTradeItemManager.getInstance().isSaved(dishDataItem)){
            return;
        }
        List<InventoryItem> inventoryItemList = new ArrayList<>();
        InventoryItem inventoryItem = InventoryUtils.transformInventoryItem(dishDataItem, dishDataItem.getBase().getTotalQty());
        inventoryItemList.add(inventoryItem);
        DinnerShoppingCart.getInstance().addReturnInventoryList(inventoryItemList);
    }

    private void removeItemUser(DishDataItem item) {
        TradeUserUtil.removeTradeItemUserByUser(item.getTradeItemUser(), item.getBase());
        DinnerShoppingCart.getInstance().updateUserInfo();
        EventBus.getDefault().post(new BeautyUpdateUserInfo());
    }

    private void removeTradeUser(DishDataItem item) {
        TradeUserUtil.removeTradeUser(DinnerShopManager.getInstance().getShoppingCart().getOrder().getTradeUsers(), item.getTradeUser());
        DinnerShoppingCart.getInstance().updateUserInfo();
        EventBus.getDefault().post(new BeautyUpdateUserInfo());
    }

    private void sendCouponAction(CouponPrivilegeVo couponPrivilegeVo) {
        ActionSeparateDeleteCoupon coupon = new ActionSeparateDeleteCoupon();
        if (couponPrivilegeVo != null) {
            coupon.id = couponPrivilegeVo.getTradePrivilege().getPromoId();
        }
        EventBus.getDefault().post(coupon);
    }

    private void sendIntegralAction() {
        ActionSeparateDeleteIntegral integralAction = new ActionSeparateDeleteIntegral();
        EventBus.getDefault().post(integralAction);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseHelper.Registry.register(observer);//add 20170209
        Log.i(TAG, "onResume: DatabaseHelper.Registry.register");
    }

    @Override
    public void onPause() {
        super.onPause();
        DatabaseHelper.Registry.unregister(observer);//add  20170209
        Log.i(TAG, "onPause: DatabaseHelper.Registry.unregister");
    }

    public void onEventMainThread(EventSelectDish select) {
        if (mListOrderDishshopVo == null) {
            return;
        }
        List<IShopcartItem> list = DinnerShopManager.getInstance().getCanDiscountData(mListOrderDishshopVo);
        List<IShopcartItemBase> selectedList = DinnerShopManager.getInstance().getAllSelectData(selectedDishAdapter.getAllData());
        setSelectView(list, selectedList);
        DinnerShopManager.getInstance().getShoppingCart().batchDishPrivilege(selectedList);
    }


    /**
     * 全选操作
     *
     * @Title: doSelectAll
     * @Description:
     * @Param
     * @Return void 返回类型
     */
    @Click(R.id.tv_select_all)
    void doSelectAll() {
        if (!ClickManager.getInstance().isClicked()) {
            if (selectedDishAdapter == null || selectedDishAdapter.getAllData().size() == 0) {
                return;
            }
            if (DinnerShopManager.getInstance().getCanDiscountData(mListOrderDishshopVo).size() <= 0) {
                ToastUtil.showShortToast(R.string.no_discount_dish);
                return;
            }
            isAllSelect = !isAllSelect;
            setSelectBtn(isAllSelect);
            doSelectAll(isAllSelect);
        }
    }

    /**
     * 全选操作
     *
     * @Title: setSelectAll
     * @Return void 返回类型
     */
    public void doSelectAll(boolean isSelect) {
        if (selectedDishAdapter == null) {
            return;
        }
        if (!isSelect) {
            for (DishDataItem dish : selectedDishAdapter.getAllData()) {
                if (dish.getType() == ItemType.SINGLE
                        || dish.getType() == ItemType.COMBO) {
                    if (dish.getBase() != null) {
                        dish.getBase().setSelected(false);
                    }
                }
            }
        } else {
            for (DishDataItem dish : selectedDishAdapter.getAllData()) {
                if (dish.getType() == ItemType.SINGLE
                        || dish.getType() == ItemType.COMBO) {
                    // && dish.getBase().getEnableWholePrivilege() == Bool.YES
                    if (dish.getBase() != null) {
                        if (selectedDishAdapter.isDiscountModle()
                                && DinnerCashManager.hasMarketActivity(selectedDishAdapter.getTradeItemPlanActivityMap(), dish.getBase()) || dish.getBase().getEnableWholePrivilege() == Bool.NO) {
                            continue;
                        }
                        BigDecimal currentPrice = dish.getBase().getPrice();
                        TradePrivilege tradePrivilege = DinnerShopManager.getInstance().getShoppingCart().getShoppingCartVo().getDishTradePrivilege();
                        if (tradePrivilege != null && tradePrivilege.getPrivilegeType() == PrivilegeType.REBATE) {
                            if (currentPrice.subtract(tradePrivilege.getPrivilegeValue()).floatValue() >= 0) {
                                dish.getBase().setSelected(true);
                            } else {
                                ToastUtil.showLongToast(getResources().getString(R.string.privilegeError));
                                setSelectBtn(false);
                                return;
                            }
                        } else {
                            dish.getBase().setSelected(true);
                        }
                    }
                }
            }
        }
        batchSetDiscount();
        selectedDishAdapter.notifyDataSetChanged();
    }

    /**
     * 先设置折扣再在订单列表中全选
     *
     * @Title: batchSetDiscount
     * @Return void 返回类型
     */
    public void batchSetDiscount() {
        ArrayList<IShopcartItemBase> listDishData = new ArrayList<IShopcartItemBase>();
        listDishData.clear();
        for (DishDataItem dish : selectedDishAdapter.getAllData()) {
            // && dish.getBase().getEnableWholePrivilege() == Bool.YES
            if ((dish.getBase() != null && dish.getBase().isSelected())
                    && (dish.getType() == ItemType.SINGLE
                    || dish.getType() == ItemType.COMBO)) {
                IShopcartItemBase mShopcartItemBase = dish.getBase();
                if (mShopcartItemBase != null) {
                    listDishData.add(mShopcartItemBase);
                }
            }
        }
        DinnerShopManager.getInstance().getShoppingCart().batchDishPrivilege(listDishData);
    }


    /**
     * 更新全选 取消全选文字
     *
     * @Title: setSelectView
     * @Param @param srcList
     * @Return void 返回类型
     */
    private void setSelectView(List<IShopcartItem> srcList, List<IShopcartItemBase> selectedList) {
        if (srcList != null) {
            if (selectedList != null) {
                if (srcList.size() != selectedList.size() || selectedList.size() == 0) {
                    isAllSelect = false;
                } else {
                    isAllSelect = true;
                }
            } else if (selectedList == null) {
                isAllSelect = false;
            }
            setSelectBtn(isAllSelect);
        }
    }

    /**
     * 改变全选按钮选择
     *
     * @Title: setSelectBtn
     * @Description:
     * @Param @param isUsed
     * @Return void 返回类型
     */
    private void setSelectBtn(boolean isSelected) {
        if (!isSelected) {
            tvSelectAll.setText(getResources().getString(R.string.dinner_select_all));
            tvSelectAll.setTextColor(getResources().getColor(R.color.text_blue));
        } else {
            tvSelectAll.setText(getResources().getString(R.string.dinner_select_all_cancel));
            tvSelectAll.setTextColor(getResources().getColor(R.color.text_select_yellow));
        }
    }

    public void onEventMainThread(ActionDinnerBatchDiscount action) {
        boolean isMoveToBottom = false;
        selectedDishAdapter.setDiscountModle(action.isEditModle);
        if (action.getIsEditModle() && !action.isAllDiscount) {
            goBatchDiscountMode();
        } else if (!action.getIsEditModle() && action.isAllDiscount) {
            goAllDiscountMode();
            getSelectedDishAdapter().clearAllSelected();
            isMoveToBottom = true;
        } else {
            goAllDiscountMode();
            getSelectedDishAdapter().clearAllSelected();
        }
        selectedDishAdapter.notifyDataSetChanged();
        if (isMoveToBottom)
            gotoListViewBottom();
    }

    /**
     * 整单折扣
     */
    public void goAllDiscountMode() {
        isDiscountPage = true;
        tvSelectAll.setVisibility(View.GONE);
        getSelectedDishAdapter().setIsDiscountAll(true);
        getSelectedDishAdapter().setDiscountModle(false);
    }

    /**
     * 批量打折模式
     *
     * @Title: goBatchDiscountMode
     * @Return void 返回类型
     */
    public void goBatchDiscountMode() {
        isDiscountPage = true;
        isBatchDiscountMode = true;
        tvSelectAll.setVisibility(View.VISIBLE);
        getSelectedDishAdapter().setIsDiscountAll(false);
        getSelectedDishAdapter().setDiscountModle(true);
    }

    public void goDefaultMode() {
        tvSelectAll.setVisibility(View.GONE);
        isDiscountPage = false;
        isBatchDiscountMode = false;
        getSelectedDishAdapter().setIsDiscountAll(false);
        getSelectedDishAdapter().setDiscountModle(false);
    }

    //滑动到购物车底部
    private void gotoListViewBottom() {
        if (isInit) {
            return;
        }
        if (selectedDishAdapter != null)
            orderDishListView.setSelection(selectedDishAdapter.getCount() - 1);
    }


    public void updateOrderDishList(List<IShopcartItem> list, TradeVo tradeVo, ShopcartItem shopcartItem, ShoppingCartListener.OperationStatus operationStatus) {
        updateOrderDishList(list, tradeVo);
    }


    public void updateOrderDishList(List<IShopcartItem> list, TradeVo tradeVo) {
        mListOrderDishshopVo = list;
        updateOrderDishList(list, tradeVo, true, false, false);
    }

    /**
     * 购物车有变更时更新界面
     *
     * @Title: updateView
     * @Description: TODO
     * @Param @param list
     * @Param @param tradeVo TODO
     * @Param isSlide 是否允许滑动
     * @Param isGroupCheckMode  是否是分组显示
     * @Return void 返回类型
     */
    public void updateOrderDishList(List<IShopcartItem> list, TradeVo tradeVo, boolean isSlide, boolean isGroupCheckMode, boolean isInit) {
        this.tradeVo = tradeVo;
        currentShopcartList = new ArrayList<>();
        if (selectedDishAdapter == null) {
            return;
        }

        if (list != null && list.size() != 0) {
            if (isShowDelete) {
                currentShopcartList = mShoppingCart.getAllValidShopcartItem(list);
            } else {
                currentShopcartList = mShoppingCart.filterDishList(list);
            }
        }

        if (BeautyOrderManager.isShopcartCanDisplay(mShoppingCart.getOrder(), currentShopcartList)) {
            haveNoDishLayout.setVisibility(View.GONE);
            orderDishListView.setVisibility(View.VISIBLE);
        } else {
            haveNoDishLayout.setVisibility(View.VISIBLE);
            orderDishListView.setVisibility(View.GONE);
        }
        selectedDishAdapter.updateData(currentShopcartList, tradeVo, true);
        orderDishListView.setSelection(this.mCurrentPosition);
        selectedDishAdapter.refreshSelectedItems();
        selectedDishAdapter.notifyDataSetChanged();
        orderDishListView.postInvalidate();
        if (isInit) {
            mUnOpItemOperation.clear();
            if (Utils.isNotEmpty(currentShopcartList)) {
                for (IShopcartItem iShopcartItem : currentShopcartList) {
                    if (Utils.isNotEmpty(iShopcartItem.getTradeItemOperations()))
                        mUnOpItemOperation.put(iShopcartItem.getUuid(), new ArrayList<>(iShopcartItem.getTradeItemOperations()));
                    else
                        mUnOpItemOperation.put(iShopcartItem.getUuid(), new ArrayList<TradeItemOperation>());

                    if (iShopcartItem.getType() == DishType.COMBO && Utils.isNotEmpty(iShopcartItem.getSetmealItems())) {
                        List<? extends ISetmealShopcartItem> iSetmealShopcartItems = iShopcartItem.getSetmealItems();
                        for (ISetmealShopcartItem iSetmealShopcartItem : iSetmealShopcartItems) {
                            if (iSetmealShopcartItem != null && !mUnOpItemOperation.containsKey(iSetmealShopcartItem.getUuid())) {
                                if (Utils.isNotEmpty(iSetmealShopcartItem.getTradeItemOperations()))
                                    mUnOpItemOperation.put(iSetmealShopcartItem.getUuid(), new ArrayList<>(iSetmealShopcartItem.getTradeItemOperations()));
                                else
                                    mUnOpItemOperation.put(iSetmealShopcartItem.getUuid(), new ArrayList<TradeItemOperation>());
                            }
                        }
                    }
                }
            }
        }
    }


    public void setDishCheckMode(boolean isCheckMode) {
        selectedDishAdapter.setDishCheckMode(isCheckMode);
    }


    private void itemClicked(int position) {
        //批量操作时，不允许点击其他菜品

        mCurrentPosition = position;
        DishDataItem dishDataItem = selectedDishAdapter.getItem(position);
        if (dishDataItem == null || (dishDataItem.getBase() == null) && !dishDataItem.isCategory()) {
            return;
        }

        if (dishDataItem.getType() != ItemType.SINGLE && dishDataItem.getType() != ItemType.COMBO && dishDataItem.getType() != ItemType.CHILD
                ) {
            return;
        }
        if (isBatchDiscountMode) {
            //针对批量折扣点击处理
            selectedDishAdapter.doEditModeItemClick(dishDataItem, position);
        } else if (selectedDishAdapter.isDishCheckMode()) {
//                        针对营销活动点击处理
            if (dishDataItem.getCheckStatus() == DishCheckStatus.CHECKED) {
                dishDataItem.setCheckStatus(DishCheckStatus.NOT_CHECK);
                selectedDishAdapter.notifyDataSetChanged();
            } else if (dishDataItem.getCheckStatus() == DishCheckStatus.NOT_CHECK) {
                dishDataItem.setCheckStatus(DishCheckStatus.CHECKED);
                selectedDishAdapter.notifyDataSetChanged();
            }
        } else {
            //选中条目
            if (dishDataItem.isSelected()) {
                return;
            }
            if (dishDataItem.getBase() != null) {
                selectedDishAdapter.doSelectedItem(dishDataItem.getBase().getUuid());
            }
            EventBus.getDefault().post(dishDataItem);
        }

    }

    private void bindItemListener() {
        orderDishListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int position, long paramLong) {
                itemClicked(position);
            }
        });
    }


    public void setShowDelete(boolean isShowDelete) {
        this.isShowDelete = isShowDelete;
        if (mShoppingCart != null)
            updateOrderDishList(mShoppingCart.getShoppingCartDish(), mShoppingCart.getOrder());
    }

    private class TradeItemOperationDataChangeObserver implements DatabaseHelper.DataChangeObserver {

        @Override
        public void onChange(Collection<Uri> uris) {
            if (uris.contains(DBHelperManager.getUri(TradeItemOperation.class))) {
                if (tradeVo != null && tradeVo.getTrade() != null) {
                    /*PrintOperationDal dal = OperatesFactory.create(PrintOperationDal.class);
                    try {
                        List<TradeItemOperation> tradeItemOperations = dal.findTradeItemOperation(tradeVo.getTrade().getUuid());
                        if (tradeItemOperationCount != tradeItemOperations.size()) {
                            Map<Long, List<TradeItemOperation>> tradeItemOperationIdMap = new HashMap<Long, List<TradeItemOperation>>();
                            for (TradeItemOperation tradeItemOperation : tradeItemOperations) {
                                List<TradeItemOperation> tios = tradeItemOperationIdMap.get(tradeItemOperation.getTradeItemId());
                                if (tios == null) {
                                    tios = new ArrayList<TradeItemOperation>();
                                    tradeItemOperationIdMap.put(tradeItemOperation.getTradeItemId(), tios);
                                }
                                tios.add(tradeItemOperation);
                            }

                            //将id转换成uuid
                            final Map<String, List<TradeItemOperation>> tradeItemOperationUuidMap = new HashMap<String, List<TradeItemOperation>>();
                            TradeDal tradeDal = OperatesFactory.create(TradeDal.class);
                            for (Entry<Long, List<TradeItemOperation>> entry : tradeItemOperationIdMap.entrySet()) {
                                TradeItem tradeItem = tradeDal.findTradeItem(entry.getKey());
                                tradeItemOperationUuidMap.put(tradeItem.getUuid(), entry.getValue());
                            }
                            if (!tradeItemOperationUuidMap.isEmpty()) {
                                getActivity().runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mShoppingCart.refreshTradeItemOperations(tradeItemOperationUuidMap);
                                    }
                                });
                            }
                        }
                        tradeItemOperationCount = tradeItemOperations.size();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }*/
                }
            }
        }

    }

    public BeautyShopcartAdapter getSelectedDishAdapter() {
        return selectedDishAdapter;
    }


    /**
     * 点选一项后改变全选按钮的选中状态
     */
    private void chnageCheckAllText() {
        EventChoice eventChoice = new EventChoice();
        eventChoice.setCount(getString(R.string.dinner_orderdish_dishcheck_number, String.valueOf(selectedDishAdapter.getCheckedNumber())));
//        eventChoice.setCheckAll(selectedDishAdapter.isCheckedAll());
        EventBus.getDefault().post(eventChoice);
    }


    public void goToPosition(int position) {
        if (orderDishListView == null) {
            return;
        }
        if (orderDishListView.getChildCount() > 0) {
            orderDishListView.setSelection(position);
        }
    }


}
