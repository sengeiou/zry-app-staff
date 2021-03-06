package com.zhongmei.bty.basemodule.orderdish.enums;

import com.zhongmei.yunfu.util.ValueEnum;


public enum WmType implements ValueEnum<Integer> {


    PREFABRICATE(1),


    CURRENT(2),


    OUTSOURSING(3),


    COMMODITIES(4),


    SEMI(5),


    @Deprecated
    __UNKNOWN__;

    private final Helper<Integer> helper;

    private WmType(Integer value) {
        helper = Helper.valueHelper(value);
    }

    private WmType() {
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
