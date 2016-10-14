package com.resto.shop.web.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by carl on 2016/10/14.
 */
public class GetNumber {

    private String id;

    private String shopDetailId;

    private String brandId;

    private Integer state;

    private Date createTime;

    private Date eatTime;

    private Date passNumberTime;

    private Integer personNumber;

    private String phone;

    private Integer waitNumber;

    private String tableType;

    private Integer callNumber;

    private BigDecimal flowMoney;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getEatTime() {
        return eatTime;
    }

    public void setEatTime(Date eatTime) {
        this.eatTime = eatTime;
    }

    public Date getPassNumberTime() {
        return passNumberTime;
    }

    public void setPassNumberTime(Date passNumberTime) {
        this.passNumberTime = passNumberTime;
    }

    public Integer getPersonNumber() {
        return personNumber;
    }

    public void setPersonNumber(Integer personNumber) {
        this.personNumber = personNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getWaitNumber() {
        return waitNumber;
    }

    public void setWaitNumber(Integer waitNumber) {
        this.waitNumber = waitNumber;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public Integer getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(Integer callNumber) {
        this.callNumber = callNumber;
    }

    public BigDecimal getFlowMoney() {
        return flowMoney;
    }

    public void setFlowMoney(BigDecimal flowMoney) {
        this.flowMoney = flowMoney;
    }
}
