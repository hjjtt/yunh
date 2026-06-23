package com.yunh.user.service;

import com.yunh.common.constant.MqConstant;
import com.yunh.pojo.dto.CourseOrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * MQ 消息发送服务
 * 支持同步发送、异步发送、单向发送、延迟消息、事务消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqMessageService {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送消息（重要消息，需要确认）
     *
     * @param topic   Topic
     * @param tag     Tag
     * @param message 消息体
     * @return 是否发送成功
     */
    public boolean sendSync(String topic, String tag, CourseOrderMessage message) {
        try {
            String destination = topic + ":" + tag;
            Message<CourseOrderMessage> msg = MessageBuilder
                    .withPayload(message)
                    .build();

            SendResult sendResult = rocketMQTemplate.syncSend(destination, msg);

            log.info("同步发送消息成功：topic={}, tag={}, messageId={}, orderId={}",
                    topic, tag, sendResult.getMsgId(), message.getOrderId());

            return sendResult.getSendStatus().equals(SendStatus.SEND_OK);

        } catch (Exception e) {
            log.error("同步发送消息失败：topic={}, tag={}, orderId={}",
                    topic, tag, message.getOrderId(), e);
            return false;
        }
    }

    /**
     * 异步发送消息（耗时敏感场景）
     *
     * @param topic   Topic
     * @param tag     Tag
     * @param message 消息体
     */
    public void sendAsync(String topic, String tag, CourseOrderMessage message) {
        String destination = topic + ":" + tag;
        Message<CourseOrderMessage> msg = MessageBuilder
                .withPayload(message)
                .build();

        rocketMQTemplate.asyncSend(destination, msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("异步发送消息成功：topic={}, tag={}, messageId={}",
                        topic, tag, sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("异步发送消息失败：topic={}, tag={}, orderId={}",
                        topic, tag, message.getOrderId(), e);
            }
        });
    }

    /**
     * 单向发送消息（日志等不重要消息，不需要确认）
     *
     * @param topic   Topic
     * @param tag     Tag
     * @param message 消息体
     */
    public void sendOneway(String topic, String tag, CourseOrderMessage message) {
        String destination = topic + ":" + tag;
        Message<CourseOrderMessage> msg = MessageBuilder
                .withPayload(message)
                .build();

        rocketMQTemplate.sendOneWay(destination, msg);
        log.debug("单向发送消息：topic={}, tag={}, orderId={}", topic, tag, message.getOrderId());
    }

    /**
     * 发送选课成功消息（同步发送）
     *
     * @param message 消息体
     */
    public void sendCourseOrderSuccess(CourseOrderMessage message) {
        sendSync(MqConstant.TOPIC_COURSE_ORDER, MqConstant.TAG_ORDER_CREATE, message);
    }

    /**
     * 发送延迟消息
     * 延迟级别：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * 对应级别 1-18
     *
     * @param topic      Topic
     * @param tag        Tag
     * @param message    消息体
     * @param delayLevel 延迟级别（1-18）
     */
    public void sendDelayMessage(String topic, String tag, CourseOrderMessage message, int delayLevel) {
        String destination = topic + ":" + tag;
        Message<CourseOrderMessage> msg = MessageBuilder
                .withPayload(message)
                .build();

        rocketMQTemplate.syncSend(destination, msg, 3000, delayLevel);
        log.info("发送延迟消息：topic={}, tag={}, delayLevel={}, orderId={}",
                topic, tag, delayLevel, message.getOrderId());
    }

    /**
     * 发送事务消息
     *
     * @param topic          Topic
     * @param tag            Tag
     * @param message        消息体
     * @param transactionId  事务 ID
     */
    public void sendTransactionMessage(String topic, String tag,
                                       CourseOrderMessage message, String transactionId) {
        String destination = topic + ":" + tag;

        Message<CourseOrderMessage> msg = MessageBuilder
                .withPayload(message)
                .setHeader(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, transactionId)
                .build();

        rocketMQTemplate.sendMessageInTransaction(destination, msg, null);

        log.info("事务消息已发送：topic={}, tag={}, transactionId={}, orderId={}",
                topic, tag, transactionId, message.getOrderId());
    }

    /**
     * 发送选课订单事务消息
     *
     * @param message 消息体
     */
    public void sendCourseOrderTransaction(CourseOrderMessage message) {
        String transactionId = "order_" + message.getOrderId() + "_" + System.currentTimeMillis();
        sendTransactionMessage(MqConstant.TOPIC_COURSE_ORDER, MqConstant.TAG_ORDER_CREATE,
                message, transactionId);
    }
}
