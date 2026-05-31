# NexusPay 未实现功能清单

## 汇总来源
- ROADMAP.md (原有路线图)
- nexuspay-elements-roadmap.md (Elements SDK 路线图)
- nexuspay-rbac-design.md (RBAC 设计)

---

## 一、核心平台 (v1.0.0 遗留)

### 1.1 安全功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| AES-256-GCM 凭证加密 | 🔴 高 | Provider API Key 加密存储 |
| 密码重置流程 | 🟡 中 | 邮箱验证码重置密码 |
| MFA 备用码 | 🟡 中 | TOTP 备用恢复码 |
| SMTP 邮件服务 | 🟡 中 | 发送验证邮件、通知 |

### 1.2 测试与质量

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 测试覆盖率 80%+ | 🔴 高 | 当前约 60% |
| Integration tests | 🔴 高 | Testcontainers 集成测试 |
| E2E tests | 🟡 中 | Playwright/Cypress |
| Mutation testing | 🟢 低 | PIT 代码变异测试 |
| Load testing | 🟢 低 | JMeter/Gatling 压力测试 |

---

## 二、Element SDK (v1.1.0)

### 2.1 核心功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| Payment Element 动态 UI | 🔴 高 | 动态渲染支付方式选项卡 |
| 3DS 自动处理 | 🔴 高 | 自动弹出 3DS 认证 iframe |
| 样式深度定制 | 🟡 中 | CSS 变量级别定制 |
| React 组件封装 | 🟡 中 | `@nexuspay/react-elements` |
| Vue 组件封装 | 🟡 中 | `@nexuspay/vue-elements` |
| npm 发布 | 🟡 中 | 发布到 npm registry |
| CDN 部署 | 🟡 中 | 静态资源 CDN |

### 2.2 增强功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 表单验证增强 | 🟢 低 | 详细错误提示 |
| BIN 检测增强 | 🟢 低 | 返回更多卡信息 |
| 地址自动完成 | 🟢 低 | 集成地址 API |
| 移动端优化 | 🟢 低 | 触控优化 |
| Link 支付 | 🟢 低 | 保存卡片快捷支付 |
| iDEAL/Bancontact | 🟢 低 | 欧洲 APM |

---

## 三、订阅模块 (v1.1.0)

### 3.1 后端

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 定时扣款任务 | 🔴 高 | Scheduler 自动续费 |
| Invoice 实体 | 🟡 中 | 账单记录 |
| Invoice API | 🟡 中 | 账单 CRUD |
| 订阅 Webhook 事件 | 🟡 中 | 订阅状态变更通知 |

### 3.2 前端

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 商户后台订阅页面路由 | 🔴 高 | 添加到 router/index.ts |
| 客户页面路由 | 🔴 高 | 添加到 router/index.ts |

---

## 四、运营管理端 (v1.2.0)

### 4.1 前端项目

| 功能 | 优先级 | 说明 |
|------|--------|------|
| frontend-admin 初始化 | 🔴 高 | 独立 Vue 项目 |
| 登录页面 | 🔴 高 | AdminAuth 登录 |
| 商户管理页面 | 🟡 中 | 独立商户列表 |
| 系统监控页面 | 🟡 中 | 图表可视化 |
| 支付方式配置页面 | 🟡 中 | 全局开关 |
| 数据报表页面 | 🟡 中 | 图表+导出 |
| 操作审计页面 | 🟡 中 | 操作日志 |

### 4.2 后端

| 功能 | 优先级 | 说明 |
|------|--------|------|
| AdminAuthController | 🔴 高 | 运营端登录 API |
| AdminJwtFilter | 🔴 高 | 运营端 JWT 认证 |
| 真实监控数据 | 🟡 中 | 替换 mock 数据 |
| 统计报表 API | 🟡 中 | 数据聚合 |
| 操作审计日志 | 🟡 中 | 记录操作 |
| 数据导出 API | 🟢 低 | CSV/Excel |

---

## 五、RBAC 权限 (v1.1.0)

### 5.1 数据库

| 功能 | 优先级 | 说明 |
|------|--------|------|
| permissions 表 | 🔴 高 | 权限定义 |
| roles 表 | 🔴 高 | 角色定义 |
| role_permissions 表 | 🔴 高 | 角色-权限关联 |
| user_roles 表 | 🔴 高 | 用户-角色关联 |
| V6__create_rbac_tables.sql | 🔴 高 | 迁移脚本 |

### 5.2 后端

| 功能 | 优先级 | 说明 |
|------|--------|------|
| Permission 实体 | 🔴 高 | 权限实体 |
| Role 实体 | 🔴 高 | 角色实体 |
| UserRole 实体 | 🔴 高 | 用户角色关联 |
| PermissionService | 🔴 高 | 权限检查服务 |
| RoleService | 🟡 中 | 角色管理服务 |
| @RequirePermission 注解 | 🔴 高 | 权限注解 |
| PermissionAspect | 🔴 高 | AOP 权限检查 |
| SecurityContext | 🟡 中 | 权限上下文 |
| 权限初始化脚本 | 🟡 中 | 默认权限数据 |
| 数据迁移脚本 | 🟡 中 | 现有角色迁移 |

### 5.3 前端

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 权限路由守卫 | 🟡 中 | 路由权限检查 |
| v-permission 指令 | 🟡 中 | 元素级权限控制 |
| usePermission 组合式函数 | 🟡 中 | 权限检查工具 |

---

## 六、商户后台完善 (v1.1.0)

### 6.1 页面

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 退款页面完善 | 🟡 中 | 当前仅有骨架 |
| Webhook 配置完善 | 🟡 中 | 当前仅有骨架 |

---

## 七、可观测性 (v1.2.0)

### 7.1 监控

| 功能 | 优先级 | 说明 |
|------|--------|------|
| Prometheus metrics | 🟡 中 | 指标采集 |
| Grafana dashboards | 🟡 中 | 可视化仪表盘 |
| Structured logging | 🟡 中 | 结构化日志 |
| Distributed tracing | 🟢 低 | OpenTelemetry |
| Health check 增强 | 🟢 低 | 详细健康检查 |

### 7.2 数据库

| 功能 | 优先级 | 说明 |
|------|--------|------|
| Connection pooling | 🟡 中 | HikariCP 调优 |
| Read replicas | 🟢 低 | 读写分离 |
| Migration rollback | 🟢 低 | 迁移回滚策略 |

---

## 八、缓存与性能 (v1.2.0)

### 8.1 Redis 集成

| 功能 | 优先级 | 说明 |
|------|--------|------|
| Redis 集成 | 🟡 中 | Redis 客户端 |
| 缓存路由规则 | 🟡 中 | 减少数据库查询 |
| 缓存 Provider 配置 | 🟡 中 | 减少数据库查询 |
| Session storage | 🟡 中 | JWT 黑名单 |
| Rate limiting with Redis | 🟡 中 | 分布式限流 |

### 8.2 性能优化

| 功能 | 优先级 | 说明 |
|------|--------|------|
| API response caching | 🟢 低 | 响应缓存 |
| Query optimization | 🟢 低 | SQL 优化 |
| Async processing | 🟢 低 | 异步支付处理 |
| Webhook queue | 🟢 低 | 消息队列 |

---

## 九、新增 Provider (v1.3.0)

| 功能 | 优先级 | 说明 |
|------|--------|------|
| PayPal 集成 | 🟡 中 | PayPal SDK |
| Adyen 集成 | 🟢 低 | Adyen SDK |
| Checkout.com 集成 | 🟢 低 | Checkout SDK |
| Provider 抽象层 | 🟡 中 | SDK 抽象接口 |

---

## 十、企业功能 (v2.0.0)

### 10.1 多租户

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 组织级隔离 | 🟡 中 | 数据隔离 |
| 自定义品牌 | 🟢 低 | 白标支持 |
| 租户配置 | 🟢 低 | 独立配置 |

### 10.2 高级路由

| 功能 | 优先级 | 说明 |
|------|--------|------|
| ML 路由优化 | 🟢 低 | 机器学习 |
| 成本路由 | 🟢 低 | 费用优化 |
| 成功率预测 | 🟢 低 | 预测模型 |
| A/B 测试 | 🟢 低 | 路由测试 |

### 10.3 分析

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 收入分析 | 🟡 中 | 收入仪表盘 |
| 成功指标 | 🟡 中 | 支付成功率 |
| Provider 对比 | 🟡 中 | 性能对比 |
| 自定义报表 | 🟢 低 | 灵活报表 |

---

## 十一、Cloud Native (v3.0.0)

### 11.1 Kubernetes

| 功能 | 优先级 | 说明 |
|------|--------|------|
| Helm charts | 🟢 低 | K8s 部署 |
| K8s operators | 🟢 低 | 自动化运维 |
| Auto-scaling | 🟢 低 | 自动扩缩容 |
| Blue-green deployments | 🟢 低 | 零停机部署 |

### 11.2 云平台

| 功能 | 优先级 | 说明 |
|------|--------|------|
| AWS 部署 | 🟢 低 | EKS + RDS + ElastiCache |
| GCP 部署 | 🟢 低 | GKE + Cloud SQL |
| Azure 部署 | 🟢 低 | AKS + Azure DB |

### 11.3 容灾

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 多区域部署 | 🟢 低 | 跨区域 |
| 数据库复制 | 🟢 低 | 主从复制 |
| 自动备份 | 🟢 低 | 定时备份 |
| 恢复流程 | 🟢 低 | 灾难恢复 |

---

## 十二、优先级汇总

### 🔴 高优先级 (v1.1.0)

| # | 功能 | 模块 |
|---|------|------|
| 1 | AES-256-GCM 凭证加密 | 安全 |
| 2 | 测试覆盖率 80%+ | 质量 |
| 3 | Integration tests | 质量 |
| 4 | Payment Element 动态 UI | Element SDK |
| 5 | 3DS 自动处理 | Element SDK |
| 6 | 定时扣款任务 | 订阅 |
| 7 | 商户后台路由配置 | 前端 |
| 8 | frontend-admin 初始化 | 运营端 |
| 9 | AdminAuthController | 运营端 |
| 10 | AdminJwtFilter | 运营端 |
| 11 | RBAC 数据库表 | 权限 |
| 12 | RBAC 实体/Service | 权限 |
| 13 | @RequirePermission 注解 | 权限 |

### 🟡 中优先级 (v1.2.0)

| # | 功能 | 模块 |
|---|------|------|
| 1 | 密码重置流程 | 安全 |
| 2 | MFA 备用码 | 安全 |
| 3 | SMTP 邮件服务 | 安全 |
| 4 | E2E tests | 质量 |
| 5 | React 组件封装 | Element SDK |
| 6 | Vue 组件封装 | Element SDK |
| 7 | npm 发布 | Element SDK |
| 8 | Invoice 实体/API | 订阅 |
| 9 | 订阅 Webhook | 订阅 |
| 10 | 运营端完整页面 | 运营端 |
| 11 | 监控数据对接 | 运营端 |
| 12 | RBAC 数据迁移 | 权限 |
| 13 | 前端权限控制 | 权限 |
| 14 | 商户后台完善 | 前端 |
| 15 | Prometheus/Grafana | 可观测性 |
| 16 | Redis 集成 | 性能 |
| 17 | PayPal 集成 | Provider |

### 🟢 低优先级 (v1.3.0+)

| # | 功能 | 模块 |
|---|------|------|
| 1 | Mutation testing | 质量 |
| 2 | Load testing | 质量 |
| 3 | 地址自动完成 | Element SDK |
| 4 | 移动端优化 | Element SDK |
| 5 | Link 支付 | Element SDK |
| 6 | iDEAL/Bancontact | Element SDK |
| 7 | 数据导出 API | 运营端 |
| 8 | Distributed tracing | 可观测性 |
| 9 | Read replicas | 数据库 |
| 10 | Async processing | 性能 |
| 11 | Adyen/Checkout.com | Provider |
| 12 | 企业功能 | v2.0.0 |
| 13 | Cloud Native | v3.0.0 |

---

## 十三、统计

| 优先级 | 数量 |
|--------|------|
| 🔴 高 | 13 项 |
| 🟡 中 | 17 项 |
| 🟢 低 | 13 项 |
| **总计** | **43 项** |
