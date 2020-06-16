package com.usian.service;

import com.usian.mapper.TbOrderItemMapper;
import com.usian.mapper.TbOrderMapper;
import com.usian.mapper.TbOrderShippingMapper;
import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderItem;
import com.usian.pojo.TbOrderShipping;
import com.usian.redis.RedisClient;
import com.usian.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private TbOrderItemMapper orderItemMapper;
    @Autowired
    private TbOrderShippingMapper orderShippingMapper;
    @Autowired
    private RedisClient redisClient;

    @Value("${ORDER_ID_KEY}")
    private String ORDER_ID_KEY;

    @Value("${ORDER_ID_BEGIN}")
    private Long ORDER_ID_BEGIN;

    @Value("${ORDER_ITEM_ID_KEY}")
    private String ORDER_ITEM_ID_KEY;

    @Override
    public Long insertOrder(OrderInfo orderInfo) {
        /************1、向订单表插入数据。********/
        TbOrder tbOrder = orderInfo.getTbOrder();
        if (!redisClient.keys(ORDER_ID_KEY)){
            //设置初始值
            redisClient.set(ORDER_ID_KEY,ORDER_ID_BEGIN);
        }
        Long orderId = redisClient.incr(ORDER_ID_KEY, 1);
        tbOrder.setOrderId(orderId.toString());
        tbOrder.setPostFee("0");
        //1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭
        tbOrder.setStatus(1);
        Date date = new Date();
        tbOrder.setCreateTime(date);
        tbOrder.setUpdateTime(date);
        orderMapper.insertSelective(tbOrder);
        /************2、向订单明细表插入数据********/
        List<TbOrderItem> orderItemList = JsonUtils.jsonToList(orderInfo.getOrderItem(), TbOrderItem.class);
        for (TbOrderItem tbOrderItem:orderItemList) {
            //生成明细id
            tbOrderItem.setId(((Long)redisClient.incr(ORDER_ITEM_ID_KEY,1)).toString());
            tbOrderItem.setOrderId(orderId.toString());
            //插入数据
            orderItemMapper.insertSelective(tbOrderItem);
        }
        //向订单物流表中插入数据
        TbOrderShipping tbOrderShipping = orderInfo.getTbOrderShipping();
        tbOrderShipping.setOrderId(orderId.toString());
        tbOrderShipping.setCreated(date);
        tbOrderShipping.setUpdated(date);
        orderShippingMapper.insertSelective(tbOrderShipping);
        //返回订单id
        return orderId;
    }
}
