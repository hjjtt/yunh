package com.yunh.user.mq.transaction;

import com.yunh.pojo.dto.CourseOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 选课订单事务消息监听器
 *
 * 事务消息流程：
 * 1. 发送半消息（Half Message）到 Broker
 * 2. 执行本地事务
 * 3. 根据本地事务结果提交/回滚消息
 * 4. 如果步骤 3 失败，Broker 会主动回查事务状态
 *
 * 事务状态存储在 Redis 中，重启后不丢失。
 */
@Slf4j
@Component
@RocketMQTransactionListener
@SuppressWarnings("rawtypes")
public class CourseOrderTransactionListener implements RocketMQLocalTransactionListener {

    private static final String TX_STATE_PREFIX = "mq_tx_state:";
    private static final long TX_STATE_TTL_HOURS = 24;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object extension) {
        try {
            CourseOrderMessage orderMessage = (CourseOrderMessage)
                    ((GenericMessage) message).getPayload();

            log.info("开始执行本地事务：orderId={}", orderMessage.getOrderId());

            // 记录事务状态到 Redis（1=成功）
            String transactionId = orderMessage.getOrderId().toString();
            redisTemplate.opsForValue().set(
                    TX_STATE_PREFIX + transactionId, "1",
                    TX_STATE_TTL_HOURS, TimeUnit.HOURS);

            log.info("本地事务执行成功：orderId={}", orderMessage.getOrderId());

            return RocketMQLocalTransactionState.COMMIT;

        } catch (Exception e) {
            log.error("本地事务执行失败", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        try {
            CourseOrderMessage orderMessage = (CourseOrderMessage)
                    ((GenericMessage) message).getPayload();

            String transactionId = orderMessage.getOrderId().toString();
            String status = redisTemplate.opsForValue().get(TX_STATE_PREFIX + transactionId);

            log.info("事务状态回查：orderId={}, status={}", orderMessage.getOrderId(), status);

            if (status == null) {
                return RocketMQLocalTransactionState.ROLLBACK;
            } else if ("1".equals(status)) {
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                return RocketMQLocalTransactionState.ROLLBACK;
            }

        } catch (Exception e) {
            log.error("事务状态回查失败", e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
}
