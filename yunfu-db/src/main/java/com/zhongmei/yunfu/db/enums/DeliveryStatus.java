package com.zhongmei.yunfu.db.enums;

import com.zhongmei.yunfu.util.ValueEnum;


public enum DeliveryStatus implements ValueEnum<Integer> {


    WAITINT_DELIVERY(0),


    DELIVERYING(1),


    REAL_DELIVERY(2),


    SQUARE_UP(3),


    @Deprecated
    __UNKNOWN__;

    private final Helper<Integer> helper;

    private DeliveryStatus(Integer value) {
        helper = Helper.valueHelper(value);
    }

    private DeliveryStatus() {
        helper = Helper.unknownHelper();
    }

    @Override
    public Integer value() {
        return helper.value();
    }

    @Override
    public boolean equalsValue(Integer value) {
        return helper.equalsValue(this, value);
    }

    @Override
    public boolean isUnknownEnum() {
        return helper.isUnknownEnum();
    }

    @Override
    public void setUnknownValue(Integer value) {
        helper.setUnknownValue(value);
    }

    @Override
    public String toString() {
        return "" + value();
    }

}
