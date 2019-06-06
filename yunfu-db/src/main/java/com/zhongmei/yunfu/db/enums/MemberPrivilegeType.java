package com.zhongmei.yunfu.db.enums;

import com.zhongmei.yunfu.util.ValueEnum;

/**
 * 会员优惠类型
 */
public enum MemberPrivilegeType implements ValueEnum<Integer> {

    /**
     * 折扣
     */
    DISCOUNT(1),

    /**
     * 折让
     */
    REBATE(2),

    /**
     * 特价
     */
    PRICE(3),


    /**
     * 未知的值
     *
     * @deprecated 为了避免转为enum出错而设置，不应直接使用
     */
    @Deprecated
    __UNKNOWN__;

    private final Helper<Integer> helper;

    private MemberPrivilegeType(Integer value) {
        helper = Helper.valueHelper(value);
    }

    private MemberPrivilegeType() {
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

    public MemberPrivilegeType createPrivilegeType(int value) {
        for (MemberPrivilegeType privilegeType : MemberPrivilegeType.values()) {
            if (privilegeType.value() == value) {
                return privilegeType;
            }
        }
        return __UNKNOWN__;
    }

}
