VO 和 TO 都属于 POJO（简单Java对象）的不同场景变种，本质都是用来传递数据的纯Java类，**核心区别就是使用场景不一样**，我结合你的项目实际场景来解释就很好懂：

***

## 🎯 核心区分表

| 类型        | 全称                                | 中文    | 作用场景                 | 字段规则                        | 存放位置                                 |
| :-------- | :-------------------------------- | :---- | :------------------- | :-------------------------- | :----------------------------------- |
| **TO**    | Transfer Object                   | 传输对象  | **微服务与微服务之间调用时传输数据** | 按需裁剪，不需要和数据库表对应，只保留调用方需要的字段 | `yunh-api-xxx` 模块（因为要被其他服务依赖）        |
| **VO**    | View Object                       | 视图对象  | **后端返回给前端页面展示用**     | 完全和前端页面需求对应，前端要什么字段就加什么字段   | `yunh-service-xxx` 模块（只在当前服务用，不需要共享） |
| **PO/DO** | Persistent Object / Domain Object | 持久化对象 | **和数据库表一一对应**        | 字段和数据库表完全一致                 | `yunh-pojo-xxx` 模块（全局共享）             |

***

### 举你项目的实际例子

#### 1. PO/DO 例子（`yunh-pojo-user` 里的 `User.java`）

和数据库`t_user`表完全对应，包含所有字段：

```java
public class User {
    private Long id;
    private String username;
    private String password; // 敏感字段，不能给前端/其他服务
    private String nickname;
    private String phone; // 敏感字段
    private String email;
    private String avatar;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

这个对象是**绝对不能直接返回给前端或者给其他服务用的**，会泄露密码、手机号这些敏感信息。

***

#### 2. TO 例子（`yunh-api-user` 里的 `UserTO.java`）

课程服务调用用户服务的时候，只需要知道用户ID、昵称、头像就行，不需要其他字段，所以定义TO：

```java
// 服务之间传输用的TO
public class UserTO {
    private Long id;
    private String nickname;
    private String avatar;
    // 没有password、phone、createTime这些不需要的字段
}
```

✅ 好处：

- 不会泄露敏感字段
- 传输的数据量更小，性能更好
- 就算数据库User表加了字段，只要调用方不需要，TO不用改，不影响服务之间的调用

***

#### 3. VO 例子（`yunh-service-course` 里的 `CourseDetailVO.java`）

前端课程详情页需要展示：课程信息 + 讲师昵称 + 讲师头像，所以专门定义VO：

```java
// 返回给前端课程详情页的VO
public class CourseDetailVO {
    private Long id;
    private String name; // 课程名
    private String description; // 课程描述
    private BigDecimal price; // 课程价格
    private String teacherName; // 讲师昵称（来自UserTO）
    private String teacherAvatar; // 讲师头像（来自UserTO）
    // 完全按照前端页面需要的字段来定义
}
```

✅ 好处：

- 前端要什么字段就返回什么字段，不会返回多余数据
- 前端需求变了（比如要加讲师的简介），只需要改VO就行，不用动Course、User这些底层实体
- 避免把整个Course和User对象返回给前端，减少无用字段的传输

***

## 🎯 怎么判断该用哪个？

记住一个原则：**看这个对象要传给谁**

- 传给数据库 → 用PO/DO
- 传给其他微服务 → 用TO/DTO
- 传给前端页面 → 用VO

***

### 常见误区

1. **不要混用**：千万不要直接把PO（数据库实体）返回给前端，否则如果不小心加了`password`字段没处理，就会出现安全问题
2. **不要怕多建类**：哪怕TO和PO字段差不多，也建议分开，以后需求变化时改起来非常方便，不会互相影响
3. **名字不用纠结**：有的公司把服务之间传输的叫DTO（Data Transfer Object），和TO是一个东西，不用纠结名字，只要团队统一就行，VO的叫法是行业统一的，专门指返回给前端的视图对象。

你现在的项目里`yunh-pojo`层目前只有PO，后续业务复杂了，就可以在`yunh-api`层加TO，在`service`层加VO，架构会更清晰。
