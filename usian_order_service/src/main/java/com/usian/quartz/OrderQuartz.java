package com.usian.quartz;

import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderItem;
import com.usian.service.OrderService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class OrderQuartz implements Job {

    @Autowired
    private OrderService orderService ;

    /**
     * 关闭超时订单
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("执行关闭超时订单任务...."+new Date());
        //查询超时订单
        List<TbOrder> overtimeOrder = orderService.selectOvertimeOrder();
        //关闭超时订单 status、updatetime、endtime、closetime
        for (TbOrder order:overtimeOrder) {
            order.setStatus(6);
            order.setUpdateTime(new Date());
            order.setEndTime(new Date());
            order.setCloseTime(new Date());
            orderService.closeTimeOutOrder(order);
            //把订单中的商品数量加回库存数量中
            List<TbOrderItem> orderItemList = orderService.selectOrderItemByOrderId(order.getOrderId());
            for (TbOrderItem orderItem:orderItemList) {
                orderService.addItemNum(orderItem.getItemId(),orderItem.getNum());
            }
        }
    }


}
