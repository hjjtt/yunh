package com.yunh.course.service;

/**
 * 课程库存服务接口
 */
public interface CourseStockService {

    /**
     * 扣减课程库存
     *
     * @param courseId 课程 ID
     * @param count    扣减数量
     */
    void decreaseStock(Long courseId, Integer count);

    /**
     * 恢复课程库存（订单超时取消时调用）
     *
     * @param courseId 课程 ID
     * @param count    恢复数量
     */
    void increaseStock(Long courseId, Integer count);
}
