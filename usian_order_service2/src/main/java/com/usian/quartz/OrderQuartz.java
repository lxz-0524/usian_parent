package com.usian.quartz;

import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderItem;
import com.usian.redis.RedisClient;
import com.usian.service.OrderService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;

public class OrderQuartz implements Job {
    //在把OrderQuartz放到Spring容器中的时候，给OrderService赋值，就是在OrderQuartz上加一个component注解的时候
    @Autowired
    private OrderService orderService ;

    @Autowired
    private RedisClient redisClient ;

    /**
     * 关闭超时订单
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("执行关闭超时订单任务...."+new Date());
        String ip = null ;
       try{
            ip = InetAddress.getLocalHost().getHostAddress();
       }catch (Exception e){
           e.printStackTrace();
       }
       //解决quartz集群任务重复执行  加分布式锁setnx
        if (redisClient.setnx("SETNX_ORDER_LOCK_KEY:",ip,30)){
            //查询超时订单
            List<TbOrder> overtimeOrder = orderService.selectOvertimeOrder();
            //关闭超时订单 status、updatetime、endtime、closetime
            for (TbOrder order:overtimeOrder) {
                orderService.closeTimeOutOrder(order);
                //把订单中的商品数量加回库存数量中
                List<TbOrderItem> orderItemList = orderService.selectOrderItemByOrderId(order.getOrderId());
                for (TbOrderItem orderItem:orderItemList) {
                    orderService.addItemNum(orderItem.getItemId(),orderItem.getNum());
                }
            }
            //删除释放分布式锁  避免造成死锁
            redisClient.del("SETNX_ORDER_LOCK_KEY");
        }else {
            System.out.println("=======机器："+ip+"占用分布式锁，任务正在执行======");
        }
    }


}
