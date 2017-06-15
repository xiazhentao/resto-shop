package com.resto.shop.web.dao;

import com.resto.brand.core.generic.GenericDao;
import com.resto.shop.web.model.ArticlePrice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticlePriceMapper extends GenericDao<ArticlePrice, String> {
    int deleteByPrimaryKey(String id);

    int insert(ArticlePrice record);

    int insertSelective(ArticlePrice record);

    ArticlePrice selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ArticlePrice record);

    int updateByPrimaryKey(ArticlePrice record);

    void deleteArticlePrices(String articleId);

    List<ArticlePrice> selectByArticleId(String articleId);

    List<ArticlePrice> selectList(@Param("shopId") String shopDetailId);

    ArticlePrice selectByArticle(@Param("articleId") String articleId, @Param("unitId") int unitId);

    Integer clearPriceStock(@Param("articleId") String articleId, @Param("emptyRemark") String emptyRemark);

    Integer clearPriceTotal(@Param("articleId") String articleId, @Param("emptyRemark") String emptyRemark);
}
