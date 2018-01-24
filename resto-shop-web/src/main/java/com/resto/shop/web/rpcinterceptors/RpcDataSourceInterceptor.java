package com.resto.shop.web.rpcinterceptors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.resto.shop.web.config.SessionKey;

import cn.restoplus.rpc.common.bean.RpcRequest;
import cn.restoplus.rpc.common.listener.SendInterceptor;



public class RpcDataSourceInterceptor implements SendInterceptor{
	Logger log = LoggerFactory.getLogger(getClass());

	@Override
    public void beforeSend(RpcRequest request) {
        String interfaceName = request.getInterfaceName();
        if(interfaceName.matches("^com.resto.shop.web.service.*") || interfaceName.matches("^com.resto.scm.web.service.*")){
            HttpServletRequest httpRequest = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
            String brandId = (String) httpRequest.getSession().getAttribute(SessionKey.CURRENT_BRAND_ID);
            //for scm pos2.0 测试
//            if(StringUtils.isEmpty(brandId)){
//                 brandId = StringUtils.isEmpty(httpRequest.getParameter("brandId"))?"31946c940e194311b117e3fff5327215":httpRequest.getParameter("brandId");
//            }
            request.setRequestHead(brandId);
            if(log.isInfoEnabled()){
                log.info(request.getInterfaceName()+" add head:"+request.getRequestHead());
            }
        }
    }


	public static void main(String[] args) {
		boolean b = "com.resto.shop.web.service.AdvertService".matches("^com.resto.shop.web.service.*");
					 
		System.out.println(b);
	}
	
}
