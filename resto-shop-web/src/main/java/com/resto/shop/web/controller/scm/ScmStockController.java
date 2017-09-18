package com.resto.shop.web.controller.scm;

import com.resto.brand.core.entity.Result;
import com.resto.scm.web.dto.DocStockCountDetailDo;
import com.resto.scm.web.dto.DocStockInput;
import com.resto.scm.web.dto.MaterialStockDo;
import com.resto.scm.web.model.DocStockCountHeader;
import com.resto.scm.web.service.StockCountCheckService;
import com.resto.shop.web.controller.GenericController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by bruce on 2017-09-18 13:41
 */
public class ScmStockController extends GenericController{


    @Autowired
    private StockCountCheckService stockCountCheckService;

    @RequestMapping("/list_default")
    @ResponseBody
    public Result list(String shopId){
        List<MaterialStockDo> list = stockCountCheckService.findDefaultStock(shopId);
        return getSuccessResult(list);
    }

    @RequestMapping("/list_all")
    @ResponseBody
    public Result listData(String shopId){
        List<DocStockCountHeader> list = stockCountCheckService.findStockList(shopId);
        return getSuccessResult(list);
    }

    @RequestMapping("list_detail")
    @ResponseBody
    public Result listDeatil(Long stockId){
        List<DocStockCountDetailDo> list = stockCountCheckService.findStockDetailListById(stockId);
        return getSuccessResult(list);
    }

    @RequestMapping("create")
    @ResponseBody
    public Result create(DocStockInput docStockInput){
        docStockInput.setShopId(this.getCurrentShopId());
        docStockInput.setCreateId(this.getCurrentUserId());
        boolean isTrue = stockCountCheckService.saveStock(docStockInput);
        if(isTrue){
            return Result.getSuccess();
        }
        return new Result("保存失败", 5000,false);
    }

}
