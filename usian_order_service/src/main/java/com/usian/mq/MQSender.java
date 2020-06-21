package com.usian.mq;

import com.usian.mapper.LocalMessageMapper;
import com.usian.pojo.LocalMessage;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQSender implements ReturnCallback ,ConfirmCallback{

    @Autowired
    private LocalMessageMapper localMessageMapper ;

    @Autowired
    private AmqpTemplate amqpTemplate ;

    public void sendMessage(LocalMessage localMessage){
        RabbitTemplate rabbbitTemplate = (RabbitTemplate)this.amqpTemplate;
        rabbbitTemplate.setMandatory(true);//设置为true，保证消息消费失败后继续回调再次发送
        rabbbitTemplate.setConfirmCallback(this);//确认回调
        rabbbitTemplate.setReturnCallback(this);//失败退回
        //用于确认之后更改本地消息状态或删除本地消息--根据本地消息id
        CorrelationData correlationData = new CorrelationData(localMessage.getTxNo());
        rabbbitTemplate.convertAndSend("order_exchange","order.add",
                                        JsonUtils.objectToJson(localMessage),correlationData);
    }

    @Override//确认回调
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId();
        if (ack){
            //消息发送成功，则更新本地消息为已发送成功或者直接删除该本地消息记录
            LocalMessage localMessage = localMessageMapper.selectByPrimaryKey(id);
            localMessage.setState(1);
            localMessageMapper.updateByPrimaryKeySelective(localMessage);
        }
    }

    @Override//失败回调
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("return-message:"+message.getBody().toString()+",exchange:" + exchange + ",routingKey:" + routingKey);
    }
}
