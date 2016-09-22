package com.resto.shop.web.service;

import com.resto.brand.core.generic.GenericService;
import com.resto.shop.web.model.Unit;

import java.util.List;

/**
 * Created by KONATA on 2016/9/11.
 */
public interface UnitService extends GenericService<Unit, String> {

   List<Unit> getUnits(String shopId);

   List<Unit> getUnitsByArticleId(String shopId,String articleId);

   void insertDetail(Unit unit);

   Unit getUnitById(String id);

   void initUnit(Unit unit);

   List<Unit> getUnitByArticleid(String articleId);

   void insertArticleRelation(String articleId,List<Unit> units);

   void updateArticleRelation(String articleId,List<Unit> units);


}