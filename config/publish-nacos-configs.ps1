# Nacos 配置中心批量发布脚本
# 使用方式：在 PowerShell 中执行 .\publish-nacos-configs.ps1

$nacosAddr = "http://127.0.0.1:8848"
$group = "DEFAULT_GROUP"
$successCount = 0
$failCount = 0

function Publish-Config {
    param(
        [string]$DataId,
        [string]$Content
    )
    $encodedContent = [System.Web.HttpUtility]::UrlEncode($Content)
    $url = "$nacosAddr/nacos/v1/cs/configs?dataId=$DataId&group=$group&content=$encodedContent"
    try {
        $result = Invoke-RestMethod -Uri $url -Method POST -ContentType "application/x-www-form-urlencoded"
        if ($result -eq "True") {
            Write-Host "[OK] $DataId 发布成功" -ForegroundColor Green
            return $true
        } else {
            Write-Host "[FAIL] $DataId 发布失败: $result" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "[ERROR] $DataId 发布异常: $_" -ForegroundColor Red
        return $false
    }
}

# ============ 1. 网关服务配置 ============
$gatewayConfig = @"
server:
  port: 9000

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-gateway.log

yunh:
  jwt:
    secret: your_jwt_secret_here

spring:
  cloud:
    gateway:
      routes:
        - id: yunh-auth
          uri: lb://yunh-auth
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1

        - id: yunh-service-user
          uri: lb://yunh-service-user
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY

        - id: yunh-service-course
          uri: lb://yunh-service-course
          predicates:
            - Path=/api/course/**
          filters:
            - StripPrefix=1

        - id: yunh-service-order
          uri: lb://yunh-service-order
          predicates:
            - Path=/api/order/**,/api/cart/**
          filters:
            - StripPrefix=1

        - id: yunh-service-pay
          uri: lb://yunh-service-pay
          predicates:
            - Path=/api/pay/**
          filters:
            - StripPrefix=1

        - id: yunh-service-video
          uri: lb://yunh-service-video
          predicates:
            - Path=/api/video/**,/api/chapter/**
          filters:
            - StripPrefix=1

        - id: yunh-service-interaction
          uri: lb://yunh-service-interaction
          predicates:
            - Path=/api/comment/**,/api/question/**,/api/answer/**,/api/note/**
          filters:
            - StripPrefix=1

        - id: yunh-service-search
          uri: lb://yunh-service-search
          predicates:
            - Path=/api/search/**
          filters:
            - StripPrefix=1

        - id: yunh-service-statistics
          uri: lb://yunh-service-statistics
          predicates:
            - Path=/api/statistics/**
          filters:
            - StripPrefix=1

        - id: yunh-gateway-actuator
          uri: lb://yunh-service-gateway
          predicates:
            - Path=/actuator/**
"@

# ============ 2. 认证服务配置 ============
# Auth 服务不使用数据库（已排除 DataSource 自动配置），仅依赖 Redis
$authConfig = @"
server:
  port: 9001

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-auth.log

spring:
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
"@

# ============ 3. 用户服务配置（含 Seata + RocketMQ） ============
$userConfig = @"
server:
  port: 9010

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-user.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_user?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8719
      eager: true

seata:
  enabled: true
  application-id: yunh-service-user
  tx-service-group: yunh_tx_group
  service:
    vgroup-mapping:
      yunh_tx_group: default
  registry:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here
      application: seata-server
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here

rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: yunh-producer-group
    send-message-timeout: 30000
    retry-times-when-send-failed: 3

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 4. 课程服务配置（含 Seata） ============
$courseConfig = @"
server:
  port: 9002

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-course.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_course?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8720
      eager: true

seata:
  enabled: true
  application-id: yunh-service-course
  tx-service-group: yunh_tx_group
  service:
    vgroup-mapping:
      yunh_tx_group: default
  registry:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here
      application: seata-server
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 5. 订单服务配置（含 Seata + RocketMQ） ============
$orderConfig = @"
server:
  port: 9004

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-order.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_order?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8728
      eager: true

seata:
  enabled: true
  application-id: yunh-service-order
  tx-service-group: yunh_tx_group
  service:
    vgroup-mapping:
      yunh_tx_group: default
  registry:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here
      application: seata-server
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here

rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: yunh-producer-group
    send-message-timeout: 30000
    retry-times-when-send-failed: 3

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 6. 支付服务配置（含 Seata + RocketMQ） ============
$payConfig = @"
server:
  port: 9005

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-pay.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_pay?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8723
      eager: true

seata:
  enabled: true
  application-id: yunh-service-pay
  tx-service-group: yunh_tx_group
  service:
    vgroup-mapping:
      yunh_tx_group: default
  registry:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here
      application: seata-server
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: public
      group: SEATA_GROUP
      username: your_nacos_username
      password: your_password_here

rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: yunh-producer-group
    send-message-timeout: 30000
    retry-times-when-send-failed: 3

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 7. 视频服务配置 ============
$videoConfig = @"
server:
  port: 9006

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-video.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_video?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8727
      eager: true

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 8. 互动服务配置 ============
$interactionConfig = @"
server:
  port: 9007

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-interaction.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_interaction?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8724
      eager: true

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 9. 搜索服务配置 ============
$searchConfig = @"
server:
  port: 9008

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-search.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_course?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8725
      eager: true

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 10. 统计服务配置 ============
$statisticsConfig = @"
server:
  port: 9009

logging:
  file:
    name: D:/vis/yunh/log/app/yunh-service-statistics.log

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yunh_statistics?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password_here
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    timeout: 5000
  cloud:
    sentinel:
      transport:
        dashboard: 127.0.0.1:8098
        port: 8726
      eager: true

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
"@

# ============ 11. Seata Server 配置 ============
$seataConfig = @"
store.mode=db
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true&characterEncoding=utf8&connectTimeout=5000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai
store.db.user=root
store.db.password=your_nacos_password_here
store.db.minConn=5
store.db.maxConn=30
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.lockTable=lock_table
store.db.queryLimit=100
store.db.maxWait=5000
service.vgroupMapping.yunh_tx_group=default
client.rm.asyncCommitBufferLimit=10000
client.rm.lock.retryInterval=10
client.rm.lock.retryTimes=30
client.rm.lock.retryPolicyBranchRollbackOnConflict=true
client.rm.reportRetryCount=5
client.rm.tableMetaCheckEnable=false
client.rm.sqlParserType=druid
client.rm.reportSuccessEnable=false
client.rm.sagaBranchRegisterEnable=false
client.rm.tccActionInterceptorOrder=-2147482648
client.tm.commitRetryCount=5
client.tm.rollbackRetryCount=5
client.tm.defaultGlobalTransactionTimeout=60000
client.tm.degradeCheck=false
client.tm.degradeCheckAllowTimes=10
client.tm.degradeCheckPeriod=2000
transport.type=TCP
transport.server=NIO
transport.heartbeat=true
transport.enableClientBatchSendRequest=false
transport.shutdown.wait=3
transport.threadFactory.bossThreadPrefix=NettyBoss
transport.threadFactory.workerThreadPrefix=NettyServerNIOWorker
transport.threadFactory.serverExecutorThreadPrefix=NettyServerBizHandler
transport.threadFactory.shareBossWorker=false
transport.threadFactory.clientSelectorThreadPrefix=NettyClientSelector
transport.threadFactory.clientSelectorThreadSize=1
transport.threadFactory.clientWorkerThreadPrefix=NettyClientWorkerThread
transport.threadFactory.bossThreadSize=1
transport.threadFactory.workerThreadSize=default
transport.adaptor.netty.workerThreadSize=default
"@

# ============ 开始发布 ============
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Nacos 配置中心批量发布" -ForegroundColor Cyan
Write-Host "  Nacos 地址: $nacosAddr" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$configs = @(
    @{ DataId = "yunh-service-gateway.yml"; Content = $gatewayConfig; Desc = "网关服务" },
    @{ DataId = "yunh-auth.yml"; Content = $authConfig; Desc = "认证服务" },
    @{ DataId = "yunh-service-user.yml"; Content = $userConfig; Desc = "用户服务" },
    @{ DataId = "yunh-service-course.yml"; Content = $courseConfig; Desc = "课程服务" },
    @{ DataId = "yunh-service-order.yml"; Content = $orderConfig; Desc = "订单服务" },
    @{ DataId = "yunh-service-pay.yml"; Content = $payConfig; Desc = "支付服务" },
    @{ DataId = "yunh-service-video.yml"; Content = $videoConfig; Desc = "视频服务" },
    @{ DataId = "yunh-service-interaction.yml"; Content = $interactionConfig; Desc = "互动服务" },
    @{ DataId = "yunh-service-search.yml"; Content = $searchConfig; Desc = "搜索服务" },
    @{ DataId = "yunh-service-statistics.yml"; Content = $statisticsConfig; Desc = "统计服务" },
    @{ DataId = "seataServer.properties"; Content = $seataConfig; Desc = "Seata Server (SEATA_GROUP)" }
)

foreach ($cfg in $configs) {
    Write-Host "正在发布: $($cfg.Desc) [$($cfg.DataId)]" -ForegroundColor Yellow
    $groupToUse = $group
    $type = "yaml"
    if ($cfg.DataId -eq "seataServer.properties") {
        $groupToUse = "SEATA_GROUP"
        $type = "properties"
    }
    $body = "dataId=$($cfg.DataId)&group=$groupToUse&type=$type&content=$([Uri]::EscapeDataString($cfg.Content))"
    $url = "$nacosAddr/nacos/v1/cs/configs"
    try {
        $result = Invoke-RestMethod -Uri $url -Method POST -Body $body -ContentType "application/x-www-form-urlencoded"
        if ($result -eq "True") {
            Write-Host "  [OK] $($cfg.DataId) 发布成功" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "  [FAIL] $($cfg.DataId) 发布失败: $result" -ForegroundColor Red
            $failCount++
        }
    } catch {
        Write-Host "  [ERROR] $($cfg.DataId) 发布异常: $_" -ForegroundColor Red
        $failCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  发布完成！成功: $successCount, 失败: $failCount" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
