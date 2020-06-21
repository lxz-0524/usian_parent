package com.usian.listener;

import com.rabbitmq.client.Channel;
import com.usian.pojo.LocalMessage;
import com.usian.service.ItemService;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemMQListener {

    @Autowired
    private ItemService itemService ;

   /* @Autowired
    private DeDuplicationService duplicationService;*/


    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "item_queue",durable = "true"),
                        exchange = @Exchange(value = "order_exchange",type = ExchangeTypes.TOPIC),
                        key = {"*.*"}))
    public void listen(String msg, Channel channel, Message message){
        LocalMessage localMessage = JsonUtils.jsonToPojo(msg, LocalMessage.class);
        String txNo = localMessage.getTxNo();
        //进行幂等性判断防止因为网络问题MQ没有收到ACK确认导致重发消息重复操作

       /* System.out.println("接收到消息 ："+orderId);
        Integer result = itemService.updateTbItemByOrderId(orderId);
        if (!(result>0)){
            throw new RuntimeException("扣减失败");
        }*/
    }
}
