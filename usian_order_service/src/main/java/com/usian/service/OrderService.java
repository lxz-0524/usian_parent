package com.usian.service;

import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderItem;

import java.util.List;

public interface OrderService {

     String insertOrder(OrderInfo orderInfo) ;

     List<TbOrder> selectOvertimeOrder() ;

     void closeTimeOutOrder(TbOrder tbOrder) ;

     List<TbOrderItem> selectOrderItemByOrderId(String orderId) ;

     void addItemNum(String itemId, Integer num) ;

}
