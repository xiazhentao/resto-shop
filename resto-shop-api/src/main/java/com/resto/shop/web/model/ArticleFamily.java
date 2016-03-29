package com.resto.shop.web.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class ArticleFamily {
    private String id;

    @NotBlank(message="{类型名称   不能为空}")
    private String name;

    @NotNull(message="{序号   不能为空}")
    private Integer peference;

    private String parentId;

    private Integer level;

    private String shopDetailId;
    
    @NotNull(message="{就餐模式   不能为空}")
    private Integer distributionModeId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getPeference() {
        return peference;
    }

    public void setPeference(Integer peference) {
        this.peference = peference;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getShopDetailId() {
        return shopDetailId;
    }

    public void setShopDetailId(String shopDetailId) {
        this.shopDetailId = shopDetailId == null ? null : shopDetailId.trim();
    }

    public Integer getDistributionModeId() {
        return distributionModeId;
    }

    public void setDistributionModeId(Integer distributionModeId) {
        this.distributionModeId = distributionModeId;
    }
}