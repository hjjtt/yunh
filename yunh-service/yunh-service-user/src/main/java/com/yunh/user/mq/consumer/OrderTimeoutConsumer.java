package com.yunh.user.mq.consumer;

import com.yunh.common.constant.MqConstant;
import com.yunh.pojo.dto.CourseOrderMessage;
import com.yunh.user.service.UserCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * 订单超时取消消费者
 *
 * 核心原理：
 * 用户选课下单时，会发送一条延迟消息（30分钟后投递）到 order_cancel Tag
 * 30分钟后，RocketMQ 将消息投递给本消费者
 * 本消费者检查订单状态：
 *   - status=0（待支付）→ 自动取消订单 + 恢复库存
 *   - status=1（已支付）→ 忽略，不处理
 *   - status=2（已取消）→ 忽略，不处理
 *
 * 这就是"延迟消息实现订单超时取消"的经典方案
 */
@Slf4j
@Service
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = MqConstant.TOPIC_COURSE_ORDER,
        selectorExpression = MqConstant.TAG_ORDER_CANCEL,
        consumerGroup = MqConstant.GROUP_ORDER_TIMEOUT_CONSUMER
)
public class OrderTimeoutConsumer implements RocketMQListener<CourseOrderMessage> {

    private final UserCourseService userCourseService;

    @Override
    public void onMessage(CourseOrderMessage message) {
        log.info("====== 收到订单超时检查消息 ======");
        log.info("orderId={}, userId={}, courseId={}, createTime={}",
                message.getOrderId(), message.getUserId(),
                message.getCourseId(), message.getCreateTime());

        try {
            boolean cancelled = userCourseService.cancelTimeoutOrder(message.getOrderId());

            if (cancelled) {
                log.info("订单超时未支付，已自动取消：orderId={}", message.getOrderId());
            } else {
                log.info("订单已支付或已取消，无需处理：orderId={}", message.getOrderId());
            }

        } catch (Exception e) {
            log.error("订单超时取消处理失败：orderId={}", message.getOrderId(), e);
            throw new RuntimeException("订单超时取消失败", e);
        }
    }
}
