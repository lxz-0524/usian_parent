package com.usian.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

/**
 * 限流器
 */
@Component
public class RateLimitFilter extends ZuulFilter {
    // 创建令牌桶
    //RateLimiter.create(1)1: 是每秒生成令牌的数量
    // 数值越大代表处理请求量月多，数值越小代表处理请求量越少
    private static final RateLimiter RATE_LIMIT = RateLimiter.create(1);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    //限流器的优先级应为最高
    @Override
    public int filterOrder() {
        return FilterConstants.SERVLET_DETECTION_FILTER_ORDER;//-3  数值越小，优先级越高
    }
    //是否启用限流器，默认是关闭
    @Override
    public boolean shouldFilter() {
        return true;
    }

    //判断是否从令牌桶获取令牌
    @Override
    public Object run() throws ZuulException {
        if (!RATE_LIMIT.tryAcquire()){
            RequestContext currentContext = RequestContext.getCurrentContext();
            currentContext.setSendZuulResponse(false);
            currentContext.setResponseBody(JsonUtils.objectToJson(Result.error("访问太过频繁，请稍候访问！")));
            currentContext.getResponse().setContentType("application/json;charset=utf-8");
        }
        return null;
    }
}
