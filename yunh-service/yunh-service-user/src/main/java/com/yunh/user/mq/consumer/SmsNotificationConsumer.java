package com.yunh.user.mq.consumer;

import com.yunh.common.constant.MqConstant;
import com.yunh.pojo.dto.CourseOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 短信通知消费者
 * 监听选课订单 Topic，消费 order_create 标签的消息
 */
@Slf4j
@Service
@RocketMQMessageListener(
        topic = MqConstant.TOPIC_COURSE_ORDER,
        selectorExpression = MqConstant.TAG_ORDER_CREATE,
        consumerGroup = MqConstant.GROUP_SMS_CONSUMER
)
public class SmsNotificationConsumer implements RocketMQListener<CourseOrderMessage> {

    @Override
    public void onMessage(CourseOrderMessage message) {
        log.info("收到短信通知消息：orderId={}, userId={}, courseId={}",
                message.getOrderId(), message.getUserId(), message.getCourseId());

        try {
            String phoneNumber = getPhoneNumberByUserId(message.getUserId());
            String content = String.format("恭喜您成功选课：%s，订单号：%d",
                    message.getCourseName(), message.getOrderId());

            // TODO: 调用短信服务发送短信
            // sendSms(phoneNumber, content);

            log.info("短信发送成功：orderId={}, phone={}", message.getOrderId(), phoneNumber);

        } catch (Exception e) {
            log.error("短信发送失败：orderId={}", message.getOrderId(), e);
            throw new RuntimeException("短信发送失败", e);
        }
    }

    private String getPhoneNumberByUserId(Long userId) {
        // TODO: 从数据库查询用户手机号
        return "138****0000";
    }
}
