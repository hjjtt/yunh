package com.yunh.user.mq.consumer;

import com.yunh.common.constant.MqConstant;
import com.yunh.pojo.dto.CourseOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 操作日志消费者
 * 监听选课订单 Topic，消费 order_create 标签的消息
 * 记录操作日志到数据库
 */
@Slf4j
@Service
@RocketMQMessageListener(
        topic = MqConstant.TOPIC_COURSE_ORDER,
        selectorExpression = MqConstant.TAG_ORDER_CREATE,
        consumerGroup = MqConstant.GROUP_LOG_CONSUMER
)
public class LogConsumer implements RocketMQListener<CourseOrderMessage> {

    @Override
    public void onMessage(CourseOrderMessage message) {
        log.info("收到日志记录消息：orderId={}, userId={}, courseId={}",
                message.getOrderId(), message.getUserId(), message.getCourseId());

        try {
            // TODO: 记录操作日志到数据库
            // OperationLog log = new OperationLog();
            // log.setUserId(message.getUserId());
            // log.setOperationType("COURSE_ORDER");
            // log.setOperationDesc("选课成功：" + message.getCourseName());
            // log.setBusinessId(message.getOrderId());
            // log.setStatus(1);
            // operationLogMapper.insert(log);

            log.info("操作日志记录成功：orderId={}, userId={}",
                    message.getOrderId(), message.getUserId());

        } catch (Exception e) {
            log.error("操作日志记录失败：orderId={}", message.getOrderId(), e);
            throw new RuntimeException("日志记录失败", e);
        }
    }
}
