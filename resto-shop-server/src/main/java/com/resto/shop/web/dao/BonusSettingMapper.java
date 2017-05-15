package com.resto.shop.web.dao;

import com.resto.shop.web.model.BonusSetting;
import com.resto.brand.core.generic.GenericDao;

public interface BonusSettingMapper  extends GenericDao<BonusSetting,String> {
    int deleteByPrimaryKey(String id);

    int insert(BonusSetting record);

    int insertSelective(BonusSetting record);

    BonusSetting selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BonusSetting record);

    int updateByPrimaryKey(BonusSetting record);
}
