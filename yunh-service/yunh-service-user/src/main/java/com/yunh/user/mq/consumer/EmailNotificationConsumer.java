package com.yunh.user.mq.consumer;

import com.yunh.common.constant.MqConstant;
import com.yunh.pojo.dto.CourseOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 邮件通知消费者
 * 监听选课订单 Topic，消费 order_create 标签的消息
 */
@Slf4j
@Service
@RocketMQMessageListener(
        topic = MqConstant.TOPIC_COURSE_ORDER,
        selectorExpression = MqConstant.TAG_ORDER_CREATE,
        consumerGroup = MqConstant.GROUP_EMAIL_CONSUMER
)
public class EmailNotificationConsumer implements RocketMQListener<CourseOrderMessage> {

    @Override
    public void onMessage(CourseOrderMessage message) {
        log.info("收到邮件通知消息：orderId={}, userId={}",
                message.getOrderId(), message.getUserId());

        try {
            String email = getUserEmailByUserId(message.getUserId());
            String subject = "选课成功通知";
            String content = buildEmailContent(message);

            // TODO: 调用邮件服务发送邮件
            // sendEmail(email, subject, content);

            log.info("邮件发送成功：orderId={}, email={}", message.getOrderId(), email);

        } catch (Exception e) {
            log.error("邮件发送失败：orderId={}", message.getOrderId(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    private String getUserEmailByUserId(Long userId) {
        // TODO: 从数据库查询用户邮箱
        return "user@example.com";
    }

    private String buildEmailContent(CourseOrderMessage message) {
        return String.format(
                "<html><body>" +
                        "<h2>选课成功通知</h2>" +
                        "<p>亲爱的用户，您已成功选课：</p>" +
                        "<ul>" +
                        "<li>课程名称：%s</li>" +
                        "<li>订单号：%d</li>" +
                        "<li>订单金额：%.2f 元</li>" +
                        "</ul>" +
                        "<p>祝您学习愉快！</p>" +
                        "</body></html>",
                message.getCourseName(),
                message.getOrderId(),
                message.getAmount()
        );
    }
}
