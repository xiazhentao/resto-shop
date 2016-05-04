package com.resto.shop.web.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.resto.brand.core.generic.GenericDao;
import com.resto.brand.core.generic.GenericServiceImpl;
import com.resto.brand.core.util.ApplicationUtils;
import com.resto.shop.web.dao.ArticleMapper;
import com.resto.shop.web.model.Article;
import com.resto.shop.web.model.ArticlePrice;
import com.resto.shop.web.model.MealAttr;
import com.resto.shop.web.model.SupportTime;
import com.resto.shop.web.service.ArticlePriceService;
import com.resto.shop.web.service.ArticleService;
import com.resto.shop.web.service.KitchenService;
import com.resto.shop.web.service.MealAttrService;
import com.resto.shop.web.service.SupportTimeService;

import cn.restoplus.rpc.common.util.StringUtil;
import cn.restoplus.rpc.server.RpcService;

/**
 *
 */
@RpcService
public class ArticleServiceImpl extends GenericServiceImpl<Article, String> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticlePriceService articlePriceServer;
    
    @Resource
    private SupportTimeService supportTimeService;
    
    @Resource
    private KitchenService kitchenService;
    
    @Resource
    private MealAttrService mealAttrService;
    
    @Override
    public GenericDao<Article, String> getDao() {
        return articleMapper;
    }

	@Override
	public List<Article> selectList(String currentShopId) {
		return articleMapper.selectList(currentShopId);
	}

	@Override
	public Article save(Article article) {
		article.setId(ApplicationUtils.randomUUID());
		this.insert(article);
		if(article.getArticleType()==Article.ARTICLE_TYPE_SIGNLE){
			articlePriceServer.saveArticlePrices(article.getId(),article.getArticlePrices());
			kitchenService.saveArticleKitchen(article.getId(), article.getKitchenList());
		}else if(article.getArticleType()==Article.ARTICLE_TYPE_MEALS){
			mealAttrService.insertBatch(article.getMealAttrs(),article.getId());
		}
		supportTimeService.saveSupportTimes(article.getId(),article.getSupportTimes());
		return article;
	} 
	
	@Override
	public int update(Article article) {
		if(article.getArticleType()==Article.ARTICLE_TYPE_SIGNLE){
			articlePriceServer.saveArticlePrices(article.getId(),article.getArticlePrices());
			kitchenService.saveArticleKitchen(article.getId(), article.getKitchenList());
		}else if(article.getArticleType()==Article.ARTICLE_TYPE_MEALS){
			mealAttrService.insertBatch(article.getMealAttrs(),article.getId());
		}
		supportTimeService.saveSupportTimes(article.getId(),article.getSupportTimes());
		return super.update(article);
	}

	@Override
	public Article selectFullById(String id) {
		Article article  = selectById(id);
		if(article.getArticleType()==Article.ARTICLE_TYPE_SIGNLE){
			List<ArticlePrice> prices = articlePriceServer.selectByArticleId(id);
			article.setArticlePrices(prices);
			List<Integer> kitchenList = kitchenService.selectIdsByArticleId(id);
			article.setKitchenList(kitchenList.toArray(new Integer[0]));
		}else{
			List<MealAttr> mealAttrs = mealAttrService.selectFullByArticleId(id);
			article.setMealAttrs(mealAttrs);
		}
		List<Integer> supportTimesIds = supportTimeService.selectByIdsArticleId(id);
		article.setSupportTimes(supportTimesIds.toArray(new Integer[0]));
		return article;
	}

	@Override
	public List<Article> selectListFull(String currentShopId, Integer distributionModeId) {
		List<Article> articleList = articleMapper.selectListByShopIdAndDistributionId(currentShopId,distributionModeId);
		Map<String,Article> articleMap = selectAllSupportArticle(currentShopId);
		for (Article a: articleList) {
			if(!StringUtil.isEmpty(a.getHasUnit())){
				List<ArticlePrice> prices = articlePriceServer.selectByArticleId(a.getId());
				a.setArticlePrices(prices);
			}
			if(!articleMap.containsKey(a.getId())){
				a.setIsEmpty(true);
			}
		}
		return articleList;
	}

	private Map<String, Article> selectAllSupportArticle(String currentShopId) {
		List<SupportTime> supportTime = supportTimeService.selectNowSopport(currentShopId);
		if(supportTime.isEmpty()){
			return new HashMap<>();
		}
		List<Integer> list = new ArrayList<>(ApplicationUtils.convertCollectionToMap(Integer.class, supportTime).keySet());
		List<Article> article = articleMapper.selectBySupportTimeId(list,currentShopId);
		Map<String,Article> articleMap = ApplicationUtils.convertCollectionToMap(String.class, article);
		return articleMap;
	}

	@Override
	public List<Article> selectListByIsEmpty(Integer isEmpty,String shopId) {
		return articleMapper.selectListByIsEmpty(isEmpty,shopId);
	}

	@Override
	public void setEmpty(Integer isEmpty,String articleId) {
		articleMapper.setEmpty(isEmpty, articleId);
	}
	
}
