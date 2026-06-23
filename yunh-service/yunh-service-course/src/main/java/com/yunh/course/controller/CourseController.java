package com.yunh.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yunh.api.client.UserFeignClient;
import com.yunh.common.constant.InternalCallConstant;
import com.yunh.common.result.Result;
import com.yunh.course.config.CourseProperties;
import com.yunh.course.mapper.CourseMapper;
import com.yunh.course.pojo.Course;
import com.yunh.course.service.CourseStockService;
import com.yunh.course.vo.CourseDetailVO;
import com.yunh.user.vo.UserSafeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "课程管理", description = "课程相关接口")
@Slf4j
@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private CourseProperties courseProperties;

    @Autowired
    private CourseStockService courseStockService;

    @Autowired
    private CourseMapper courseMapper;

    @ApiOperation(value = "查询所有课程", notes = "获取系统中所有课程列表")
    @GetMapping("/list")
    public Result<List<Course>> list() {
        List<Course> courses = courseMapper.selectList(null);
        return Result.success(courses);
    }

    @ApiOperation(value = "根据ID查询课程", notes = "查询指定课程的详细信息")
    @GetMapping("/{id}")
    public Result<Course> getById(
            @ApiParam(value = "课程ID", example = "1") @PathVariable Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        return Result.success(course);
    }

    @ApiOperation(value = "查询课程详情", notes = "包含讲师信息，通过 Feign 调用 User 服务获取")
    @GetMapping("/{id}/detail")
    public Result<CourseDetailVO> getCourseDetail(
            @ApiParam(value = "课程ID", example = "1") @PathVariable Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }

        Result<UserSafeVO> userResult = userFeignClient.getById(course.getTeacherId());

        CourseDetailVO detailVO = new CourseDetailVO();
        detailVO.setCourse(course);
        if (userResult.isSuccess() && userResult.getData() != null) {
            detailVO.setTeacher(userResult.getData());
        }

        return Result.success(detailVO);
    }

    @ApiOperation(value = "创建课程", notes = "新增课程，仅管理员可操作")
    @PostMapping("/create")
    public Result<Course> create(
            @RequestBody Course course,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可创建课程");
        }
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            return Result.error("课程名称不能为空");
        }
        courseMapper.insert(course);
        return Result.success(course);
    }

    @ApiOperation(value = "更新课程信息", notes = "根据ID更新课程信息，仅管理员可操作")
    @PutMapping("/{id}")
    public Result<Course> update(
            @ApiParam(value = "课程ID") @PathVariable Long id,
            @RequestBody Course course,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可修改课程");
        }
        Course existing = courseMapper.selectById(id);
        if (existing == null) {
            return Result.error("课程不存在");
        }
        course.setId(id);
        courseMapper.updateById(course);
        return Result.success(course);
    }

    @ApiOperation(value = "删除课程", notes = "根据ID删除课程，仅管理员可操作")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @ApiParam(value = "课程ID") @PathVariable Long id,
            @RequestHeader(value = "X-Role", required = false) String role) {
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可删除课程");
        }
        int rows = courseMapper.deleteById(id);
        if (rows > 0) {
            return Result.success();
        }
        return Result.error("课程不存在");
    }

    @ApiOperation(value = "健康检查", notes = "检查课程服务是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("course service is running");
    }

    @ApiOperation(value = "获取配置信息", notes = "获取 Nacos 动态配置，修改后无需重启即可生效")
    @GetMapping("/config")
    public Result<Map<String, String>> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("welcome", courseProperties.getWelcome());
        config.put("version", courseProperties.getVersion());
        config.put("message", "配置来自 Nacos，修改后无需重启即可生效");
        return Result.success(config);
    }

    @ApiOperation(value = "根据讲师ID查询课程", notes = "查询指定讲师的所有课程")
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Course>> getByTeacherId(
            @ApiParam(value = "讲师ID", example = "1") @PathVariable Long teacherId) {
        List<Course> courses = courseMapper.selectList(
                new QueryWrapper<Course>().eq("teacher_id", teacherId));
        return Result.success(courses);
    }

    @ApiOperation(value = "扣减课程库存", notes = "供 Feign 调用，分布式事务中使用")
    @PutMapping("/stock/decrease")
    public Result<Void> decreaseStock(
            @ApiParam(value = "课程ID") @RequestParam Long courseId,
            @ApiParam(value = "扣减数量") @RequestParam Integer count,
            @RequestHeader(value = InternalCallConstant.HEADER_NAME, required = false) String internalCall) {
        if (!isInternalCall(internalCall)) {
            return Result.error(403, "仅内部服务可扣减课程库存");
        }
        if (courseId == null || count == null || count <= 0) {
            return Result.error("参数错误：courseId 和 count 不能为空，count 必须 > 0");
        }
        courseStockService.decreaseStock(courseId, count);
        return Result.success();
    }

    @ApiOperation(value = "恢复课程库存", notes = "订单超时取消时调用")
    @PutMapping("/stock/increase")
    public Result<Void> increaseStock(
            @ApiParam(value = "课程ID") @RequestParam Long courseId,
            @ApiParam(value = "恢复数量") @RequestParam Integer count,
            @RequestHeader(value = InternalCallConstant.HEADER_NAME, required = false) String internalCall) {
        if (!isInternalCall(internalCall)) {
            return Result.error(403, "仅内部服务可恢复课程库存");
        }
        if (courseId == null || count == null || count <= 0) {
            return Result.error("参数错误：courseId 和 count 不能为空，count 必须 > 0");
        }
        courseStockService.increaseStock(courseId, count);
        return Result.success();
    }

    private boolean isInternalCall(String internalCall) {
        return InternalCallConstant.HEADER_VALUE.equals(internalCall);
    }
}
