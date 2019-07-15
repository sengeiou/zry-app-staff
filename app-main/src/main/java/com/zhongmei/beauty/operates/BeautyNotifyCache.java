package com.zhongmei.beauty.operates;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.zhongmei.yunfu.bean.YFResponseList;
import com.zhongmei.yunfu.context.util.Utils;
import com.zhongmei.yunfu.data.LoadingYFResponseListener;
import com.zhongmei.yunfu.db.entity.TaskRemind;
import com.zhongmei.yunfu.db.entity.booking.Booking;
import com.zhongmei.beauty.entity.BeautyNotifyEntity;
import com.zhongmei.beauty.entity.UnpaidTradeVo;
import com.zhongmei.yunfu.net.volley.VolleyError;
import com.zhongmei.yunfu.orm.DBHelperManager;
import com.zhongmei.yunfu.orm.DatabaseHelper;
import com.zhongmei.yunfu.db.entity.trade.Trade;
import com.zhongmei.yunfu.db.enums.BusinessType;
import com.zhongmei.yunfu.db.enums.StatusFlag;
import com.zhongmei.yunfu.db.enums.TradeStatus;
import com.zhongmei.yunfu.db.enums.TradeType;
import com.zhongmei.yunfu.resp.YFResponseListener;
import com.zhongmei.yunfu.util.ToastUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by demo on 2018/12/15
 * 管理美业通知
 * 如：到店人数，预约单数，订单数等
 */

public class BeautyNotifyCache {

    //刷新模块-预定
    public static final int MODULE_RESERVER = 0x01;
    //刷新模块-订单
    public static final int MODULE_TRADES = 0x02;

    private static BeautyNotifyCache mNotifyCache;
    private Set<BeautyNotifyListener> mNotifyListenerSet; //通知回掉监听集合
    private WorkerHandler mWorkerHandler; //工作线程
    private HandlerThread mHandlerThread;
    private BeautyTradeChangeObserver mObserver; //数据改变监听
    private BeautyTradeDataManager mTradeManager; //订单查询管理
    private BeautyDataListener mBeautyDataListener;//数据刷新

    private int mRefreshModule = MODULE_RESERVER;

    private BeautyNotifyEntity mNotifyEntity = new BeautyNotifyEntity();

    private BeautyNotifyCache() {
        if (mWorkerHandler == null) {
            mHandlerThread = new HandlerThread("wrokHandler");
            mHandlerThread.start();
            mWorkerHandler = new WorkerHandler(mHandlerThread.getLooper());
        }

        mObserver = new BeautyTradeChangeObserver();
        DatabaseHelper.Registry.register(mObserver);

        mTradeManager = new BeautyTradeDataManager();
    }

    public static BeautyNotifyCache getInstance() {
        if (mNotifyCache == null) {
            mNotifyCache = new BeautyNotifyCache();
        }
        return mNotifyCache;
    }

    public void addNotifyListener(BeautyNotifyListener listener) {
        if (mNotifyListenerSet == null) {
            mNotifyListenerSet = new HashSet<>();
        }
        mNotifyListenerSet.add(listener);
    }

    public void removeNotifyListener(BeautyNotifyListener listener) {
        if (mNotifyListenerSet != null) {
            mNotifyListenerSet.remove(listener);
        }
    }

    public void setmBeautyDataListener(BeautyDataListener mBeautyDataListener) {
        this.mBeautyDataListener = mBeautyDataListener;
    }

    /**
     * 启动查询
     */
    public void start() {
        if (mWorkerHandler != null) {
            mWorkerHandler.sendEmptyMessage(WorkerHandler.WHAT_QUERY_NOTIFY);
        }
        getTodayTaskNumber();
    }

    public void startModuleCache(int module) {
        this.mRefreshModule = module;
        //刷新数据
        switch (module) {
            case MODULE_RESERVER:
                break;
            case MODULE_TRADES:
                mWorkerHandler.sendEmptyMessage(mWorkerHandler.WHAT_REFRESH_TRADE);
                break;
        }
    }

    /**
     * 查询美业通知
     *
     * @return
     */
    private BeautyNotifyEntity queryBeautyNotify() {
        DatabaseHelper helper = DBHelperManager.getHelper();
        try {
            mNotifyEntity.setCustomerNumber(mTradeManager.queryCustomerNumber(helper));
            mNotifyEntity.setReserverNumber(mTradeManager.queryReserverNumber(helper));
            mNotifyEntity.setUnDealReserverNumber(mTradeManager.queryUnDealReserverNumber(helper));
            mNotifyEntity.setTradeNumber(mTradeManager.queryTradeNumber(helper));
            mNotifyEntity.setMemberNumber(mTradeManager.queryMemberNumber());
            mNotifyEntity.setTodayReserverNumber(mTradeManager.queryTodayReserverNumber(helper));
            mNotifyEntity.setUnpaidTradeNumber(mTradeManager.queryUnPaidTradeNumber(helper));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBHelperManager.releaseHelper(helper);
        }

        return mNotifyEntity;
    }

    /**
     * 获取今日任务数量
     */
    private void getTodayTaskNumber(){
        YFResponseListener listener = new YFResponseListener<YFResponseList<TaskRemind>>() {

            @Override
            public void onResponse(YFResponseList<TaskRemind> response) {
                if (YFResponseList.isOk(response)) {
                    int taskCount=response.getContent().size();
                    refreshTaskNotify(taskCount);
                }
            }

            @Override
            public void onError(VolleyError error) {
                ToastUtil.showShortToast(error.getMessage());
            }
        };

        mTradeManager.getTaskNumberSync(listener);
    }

    /**
     * 刷新通知数据
     * 例如订单数量，预约数量等
     */
    private void refreshNotify() {
        BeautyNotifyEntity notifyEntity = queryBeautyNotify();
        if (mNotifyListenerSet != null) {
            Iterator iter = mNotifyListenerSet.iterator();
            while (iter.hasNext()) {
                ((BeautyNotifyListener) iter.next()).refreshNotifyNumbers(notifyEntity);
            }
        }
    }

    /**
     * 刷新任务数量
     * @param taskNumber
     */
    private void refreshTaskNotify(int taskNumber){
        mNotifyEntity.setTaskNumber(taskNumber);
        if (mNotifyListenerSet != null) {
            Iterator iter = mNotifyListenerSet.iterator();
            while (iter.hasNext()) {
                ((BeautyNotifyListener) iter.next()).refreshNotifyNumbers(mNotifyEntity);
            }
        }
    }

    /**
     * 刷新订单数据
     */
    private void refreshTrades() {
        List<UnpaidTradeVo> listTrades = mTradeManager.queryUnpaidTrades(BusinessType.BEAUTY);
        if (mBeautyDataListener != null) {
            mBeautyDataListener.refreshTrade(listTrades);
        }
    }




    public class WorkerHandler extends Handler {
        public static final int WHAT_QUERY_NOTIFY = 0x01;
        public static final int WHAT_REFRESH_TRADE = 0x02;

        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_QUERY_NOTIFY:
                    refreshNotify();
                    break;
                case WHAT_REFRESH_TRADE:
                    refreshTrades();
                    break;
            }
        }
    }


    public class BeautyTradeChangeObserver implements DatabaseHelper.DataChangeObserver {

        @Override
        public void onChange(Collection<Uri> uris) {
            if (uris.contains(DBHelperManager.getUri(Trade.class)) || uris.contains(DBHelperManager.getUri(Booking.class))) {
                if (mWorkerHandler != null) {
                    mWorkerHandler.sendEmptyMessage(mWorkerHandler.WHAT_QUERY_NOTIFY);
                    mWorkerHandler.sendEmptyMessage(mWorkerHandler.WHAT_REFRESH_TRADE);
                }
            }
        }
    }

    public void onDestory() {
        if (mObserver != null) {
            DatabaseHelper.Registry.unregister(mObserver);
            mObserver = null;
        }

        if (mWorkerHandler != null) {
            mWorkerHandler.removeCallbacks(null);
            mHandlerThread.quit();
            mWorkerHandler = null;
            mHandlerThread = null;
        }

        if (mNotifyListenerSet != null) {
            mNotifyListenerSet.clear();
            mNotifyListenerSet = null;
        }

        if (mNotifyCache != null) {
            mNotifyCache = null;
        }

        if (mTradeManager != null) {
            mTradeManager = null;
        }
    }


    public interface BeautyNotifyListener {
        public void refreshNotifyNumbers(BeautyNotifyEntity notifyEntity);
    }

    public interface BeautyDataListener {
        /**
         * 刷新预定订单信息
         */
        public void refreshReserverTrade();

        /**
         * 刷新订单信息
         */
        public void refreshTrade(List<UnpaidTradeVo> beautyTradeVos);
    }
}
