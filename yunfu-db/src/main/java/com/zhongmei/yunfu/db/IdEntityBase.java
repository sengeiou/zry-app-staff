package com.zhongmei.yunfu.db;

import com.j256.ormlite.field.DatabaseField;
import com.zhongmei.yunfu.db.EntityBase;


public abstract class IdEntityBase extends EntityBase<Long> {


    private static final long serialVersionUID = 1L;

    public interface $ {


        String id = "_id";

    }

    @DatabaseField(columnName = $.id, id = true, canBeNull = false)
    protected Long id;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long pkValue() {
        return id;
    }

    @Override
    public boolean checkNonNull() {
        return checkNonNull(id);
    }

    @Override
    public String toString() {
        return "IdEntityBase{" +
                "id=" + id +
                '}';
    }
}
