package com.usian.service;

import com.usian.mapper.TbItemMapper;
import com.usian.mapper.TbOrderItemMapper;
import com.usian.mapper.TbOrderMapper;
import com.usian.mapper.TbOrderShippingMapper;
import com.usian.pojo.*;
import com.usian.redis.RedisClient;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
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
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private TbItemMapper itemMapper ;

    @Value("${ORDER_ID_KEY}")
    private String ORDER_ID_KEY;

    @Value("${ORDER_ID_BEGIN}")
    private Long ORDER_ID_BEGIN;

    @Value("${ORDER_ITEM_ID_KEY}")
    private String ORDER_ITEM_ID_KEY;

    @Override
    public String insertOrder(OrderInfo orderInfo) {
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

        //发布消息到mq，完成扣减库存
        amqpTemplate.convertAndSend("order_exchange","order.add", orderId);
        //返回订单id
        return orderId.toString();
    }


    /**
     * 查询超时订单
     * @return
     */
    @Override
    public List<TbOrder> selectOvertimeOrder() {
        return orderMapper.selectOvertimeOrder();
    }

    /**
     * 关闭超时订单
     * @param tbOrder
     */
    @Override
    public void closeTimeOutOrder(TbOrder tbOrder) {
        orderMapper.updateByPrimaryKeySelective(tbOrder);
    }

    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Override
    public List<TbOrderItem> selectOrderItemByOrderId(String orderId) {
        TbOrderItemExample tbOrderItemExample = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = tbOrderItemExample.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<TbOrderItem> list = orderItemMapper.selectByExample(tbOrderItemExample);
        return list;
    }

    /**
     * 把删除的超时订单中商品数量加回去
     * @param itemId
     * @param num
     */
    @Override
    public void addItemNum(String itemId, Integer num) {
        TbItem tbItem = itemMapper.selectByPrimaryKey(Long.valueOf(itemId));
        //修改回原来库存数量
        tbItem.setNum(tbItem.getNum()+num);
        itemMapper.updateByPrimaryKeySelective(tbItem);
    }
}
