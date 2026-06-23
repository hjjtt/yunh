package com.yunh.common.constant;

/**
 * RocketMQ 常量定义
 * 统一管理 Topic、Tag、Consumer Group 名称
 */
public class MqConstant {

    /**
     * Topic：选课订单
     */
    public static final String TOPIC_COURSE_ORDER = "course_order_topic";

    /**
     * Tag：订单创建
     */
    public static final String TAG_ORDER_CREATE = "order_create";

    /**
     * Tag：订单支付
     */
    public static final String TAG_ORDER_PAY = "order_pay";

    /**
     * Tag：订单取消
     */
    public static final String TAG_ORDER_CANCEL = "order_cancel";

    /**
     * 消费者组：短信通知
     */
    public static final String GROUP_SMS_CONSUMER = "yunh-sms-consumer-group";

    /**
     * 消费者组：邮件通知
     */
    public static final String GROUP_EMAIL_CONSUMER = "yunh-email-consumer-group";

    /**
     * 消费者组：日志记录
     */
    public static final String GROUP_LOG_CONSUMER = "yunh-log-consumer-group";

    /**
     * 消费者组：订单超时取消
     */
    public static final String GROUP_ORDER_TIMEOUT_CONSUMER = "yunh-order-timeout-consumer-group";

    /**
     * 生产者组：事务消息
     */
    public static final String GROUP_TX_PRODUCER = "yunh-transaction-producer-group";

    /**
     * 延迟级别：订单超时检查（30分钟）
     * 级别对照：1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * 级别 16 = 30分钟
     * 测试时可用级别 3 = 10秒
     */
    public static final int DELAY_LEVEL_ORDER_TIMEOUT = 3;

    /**
     * 延迟级别：测试用（10秒）
     */
    public static final int DELAY_LEVEL_TEST = 3;
}
