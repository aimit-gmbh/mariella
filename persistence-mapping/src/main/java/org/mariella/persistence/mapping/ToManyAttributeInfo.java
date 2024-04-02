package org.mariella.persistence.mapping;

import java.io.PrintStream;


public abstract class ToManyAttributeInfo extends RelationAttributeInfo {

    private JoinTableInfo joinTableInfo;
    private OrderByInfo orderByInfo;
    private Class<?> targetEntity;

    public ToManyAttributeInfo() {
    }


    @Override
    void initializeAdoptionCopy(AttributeInfo copy) {
        super.initializeAdoptionCopy(copy);
        ((ToManyAttributeInfo) copy).setJoinTableInfo(joinTableInfo);
    }

    @Override
    public void overrideWith(AttributeInfo overriddenAttrInfo) {
        super.overrideWith(overriddenAttrInfo);
        if (this.joinTableInfo == null)
            this.joinTableInfo = ((ToManyAttributeInfo) overriddenAttrInfo).getJoinTableInfo();
    }

    public JoinTableInfo getJoinTableInfo() {
        return joinTableInfo;
    }

    public void setJoinTableInfo(JoinTableInfo joinTableInfo) {
        this.joinTableInfo = joinTableInfo;
    }

    @Override
    void debugPrintAttributes(PrintStream out) {
        super.debugPrintAttributes(out);
        if (getJoinTableInfo() != null)
            getJoinTableInfo().debugPrint(out);
    }

    public OrderByInfo getOrderByInfo() {
        return orderByInfo;
    }

    public void setOrderByInfo(OrderByInfo orderByInfo) {
        this.orderByInfo = orderByInfo;
    }

    public Class<?> getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Class<?> targetEntity) {
        this.targetEntity = targetEntity;
    }


}
