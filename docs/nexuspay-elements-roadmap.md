# NexusPay Elements SDK 实现状态与路线图

## 项目概述

NexusPay Elements SDK 是一个类似 Stripe Elements 的前端支付组件库，支持银行卡、APM 和订阅功能。本文档记录已实现、未实现的功能及后续路线图。

---

## 一、已实现功能

### 1.1 前端 SDK (nexuspay-js)

| 模块 | 功能 | 文件 | 状态 |
|------|------|------|------|
| **核心架构** |
| 入口类 | Nexuspay 主类，初始化 SDK | `core/Nexuspay.ts` | ✅ |
| 容器类 | Elements 容器，创建 Element | `core/Elements.ts` | ✅ |
| 基类 | Element 抽象类，事件系统 | `core/Element.ts` | ✅ |
| 类型定义 | TypeScript 类型 | `types.ts` | ✅ |
| iframe 通信 | postMessage 工具 | `utils/postMessage.ts` | ✅ |
| **Element 类型** |
| Card Element | 银行卡支付 | `elements/CardElement.ts` | ✅ |
| Payment Element | 统一支付组件 | `elements/PaymentElement.ts` | ⚠️ 框架 |
| Setup Element | 保存卡片 | `elements/SetupElement.ts` | ✅ |
| Apple Pay Element | Apple Pay | `elements/ApplePayElement.ts` | ✅ |
| Google Pay Element | Google Pay | `elements/GooglePayElement.ts` | ✅ |
| Alipay Element | 支付宝 | `elements/AlipayElement.ts` | ✅ |
| WeChatPay Element | 微信支付 | `elements/WeChatPayElement.ts` | ✅ |
| **构建配置** |
| TypeScript | 类型系统 | `tsconfig.json` | ✅ |
| Vite | 构建工具 | `vite.config.ts` | ✅ |
| Package | npm 包配置 | `package.json` | ✅ |

### 1.2 iframe 页面 (frontend/elements)

| 页面 | 功能 | 状态 |
|------|------|------|
| `card.html` | 卡片输入 UI（卡号/有效期/CVC） | ✅ |
| `card.js` | Luhn 校验、格式化、品牌识别、postMessage | ✅ |
| `payment.html` | Payment Element iframe（tabs/accordion 布局） | ⚠️ 基础 |
| `demo.html` | 完整演示页面 | ✅ |

### 1.3 后端 - 订阅模块

| 层级 | 文件 | 功能 | 状态 |
|------|------|------|------|
| **实体** |
| `Customer.java` | 客户实体 | ✅ |
| `PaymentMethod.java` | 支付方式实体 | ✅ |
| `Subscription.java` | 订阅实体 | ✅ |
| **Repository** |
| `CustomerRepository.java` | 客户 CRUD | ✅ |
| `PaymentMethodRepository.java` | 支付方式 CRUD | ✅ |
| `SubscriptionRepository.java` | 订阅 CRUD | ✅ |
| **Service** |
| `CustomerService.java` | 客户管理、支付方式管理 | ✅ |
| `SubscriptionService.java` | 订阅创建、激活、取消 | ✅ |
| **Controller** |
| `CustomerController.java` | `/api/v1/merchants/{id}/customers` | ✅ |
| `SubscriptionController.java` | `/api/v1/merchants/{id}/subscriptions` | ✅ |
| **数据库** |
| `V5__create_subscription_tables.sql` | customers, payment_methods, subscriptions 表 | ✅ |

### 1.4 后端 - 运营管理模块

| 层级 | 文件 | 功能 | 状态 |
|------|------|------|------|
| **Service** |
| `OrganizationService.java` | 组织管理、商户管理、成员管理 | ✅ |
| **Controller** |
| `AdminController.java` | 全局概览、监控、审核 API | ✅ |
| `OrganizationController.java` | 组织 CRUD API | ✅ |

### 1.5 前端 - 商户后台

| 页面 | 文件 | 功能 | 状态 |
|------|------|------|------|
| 客户管理 | `pages/Customers.vue` | 客户列表、详情、支付方式管理 | ✅ |
| 订阅管理 | `pages/Subscriptions.vue` | 订阅列表、创建、取消、统计 | ✅ |

### 1.6 前端 - 运营管理端

| 页面 | 文件 | 功能 | 状态 |
|------|------|------|------|
| 布局 | `pages/admin/AdminLayout.vue` | 侧边栏导航 | ✅ |
| 概览 | `pages/admin/AdminOverview.vue` | 全局统计、待审核、Provider 状态 | ✅ |
| 组织管理 | `pages/admin/Organizations.vue` | 组织 CRUD、商户管理 | ✅ |

---

## 二、未实现功能

### 2.1 Element SDK 缺失功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| **Payment Element 动态渲染** | 🔴 高 | 根据支付方式动态渲染选项卡，当前仅有框架 |
| **3DS 自动处理** | 🔴 高 | 自动弹出 3DS 认证 iframe，当前需手动处理 |
| **样式深度定制** | 🟡 中 | CSS 变量级别定制，当前仅支持基础 style |
| **React 组件封装** | 🟡 中 | `@nexuspay/react-elements` 包 |
| **Vue 组件封装** | 🟡 中 | `@nexuspay/vue-elements` 包 |
| **表单验证增强** | 🟢 低 | 实时详细错误提示、BIN 检测增强 |
| **地址自动完成** | 🟢 低 | 集成地址 API |
| **移动端优化** | 🟢 低 | 触控优化、响应式增强 |
| **iDEAL/Bancontact 等 APM** | 🟢 低 | 欧洲本地支付方式 |
| **Link 支付** | 🟢 低 | Stripe Link 类似的保存卡片功能 |

### 2.2 后端缺失功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| **定时扣款任务** | 🔴 高 | 订阅自动续费 Scheduler |
| **Invoice 实体** | 🟡 中 | 订阅账单记录 |
| **Webhook 通知增强** | 🟡 中 | 订阅事件通知 |
| **数据导出** | 🟢 低 | CSV/Excel 报表导出 |
| **操作日志** | 🟢 低 | 审计日志记录 |

### 2.3 前端缺失功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| **商户后台 - 退款页面完善** | 🟡 中 | 当前仅有骨架 |
| **商户后台 - Webhook 配置** | 🟡 中 | 当前仅有骨架 |
| **运营管理端 - 商户管理页面** | 🟡 中 | 独立商户列表页面 |
| **运营管理端 - 监控页面** | 🟡 中 | 详细监控图表 |
| **运营管理端 - 支付方式配置页面** | 🟡 中 | 全局支付方式开关 |
| **路由配置** | 🔴 高 | 需要添加到 router/index.ts |

### 2.4 运营管理端缺失功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| **权限控制** | 🔴 高 | RBAC 权限检查 |
| **数据报表** | 🟡 中 | 图表可视化 |
| **操作审计** | 🟡 中 | 操作日志页面 |
| **系统配置** | 🟢 低 | 全局配置管理 |

---

## 三、路线图

### Phase 1: 核心完善 (v1.1.0)

**目标：补齐核心功能缺口**

**Element SDK：**
- [ ] Payment Element 动态 UI 完善
- [ ] 3DS 自动处理流程
- [ ] 样式定制增强（CSS 变量）

**后端：**
- [ ] 订阅定时扣款任务
- [ ] Invoice 实体和 API

**前端：**
- [ ] 路由配置（添加新页面到路由）
- [ ] 商户后台退款页面完善
- [ ] 商户后台 Webhook 配置完善

**预计完成：2 周**

---

### Phase 2: 框架组件 (v1.2.0)

**目标：提升开发者体验**

**Element SDK：**
- [ ] React 组件封装 (`@nexuspay/react-elements`)
- [ ] Vue 组件封装 (`@nexuspay/vue-elements`)
- [ ] npm 发布和 CDN 部署

**后端：**
- [ ] Invoice API 完善
- [ ] 订阅 Webhook 事件

**预计完成：2 周**

---

### Phase 3: 监控与运营 (v1.3.0)

**目标：完善运营能力**

**运营管理端：**
- [ ] 商户管理独立页面
- [ ] 系统监控页面（图表）
- [ ] 支付方式配置页面
- [ ] 数据报表导出

**后端：**
- [ ] 操作审计日志
- [ ] 统计报表 API

**预计完成：2 周**

---

### Phase 4: 高级功能 (v2.0.0)

**目标：企业级能力**

**Element SDK：**
- [ ] 地址自动完成
- [ ] Link 支付
- [ ] iDEAL/Bancontact 等 APM
- [ ] 移动端优化

**后端：**
- [ ] 多租户数据隔离
- [ ] 高可用架构

**运营管理端：**
- [ ] 多维度数据分析
- [ ] 自定义报表

**预计完成：4 周**

---

## 四、里程碑时间线

```
v1.0.0 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 2026-05-31 ✅
        SDK 核心架构、7 种 Element、订阅模块、运营管理端基础

v1.1.0 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 2026-06-14 🎯
        Payment Element 完善、3DS 自动处理、定时扣款

v1.2.0 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 2026-06-28 📋
        React/Vue 组件封装、npm 发布

v1.3.0 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 2026-07-12 📋
        监控完善、报表导出、审计日志

v2.0.0 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ 2026-08-09 📋
        企业级功能、更多 APM、高可用
```

---

## 五、优先级矩阵

```
                    高价值
                      │
                      │  ┌─────────────────┐
                      │  │ Payment Element │
                      │  │ 动态 UI         │
                      │  └─────────────────┘
                      │  ┌─────────────────┐
         ┌────────────┼──│ 3DS 自动处理    │
         │            │  └─────────────────┘
         │            │  ┌─────────────────┐
         │   React    │  │ 订阅定时扣款    │
         │   组件     │  └─────────────────┘
         │            │
低投入 ──┼────────────┼────────────────────────── 高投入
         │            │
         │            │  ┌─────────────────┐
         │   样式     │  │ 监控图表        │
         │   定制     │  └─────────────────┘
         │            │  ┌─────────────────┐
         │            │  │ 数据报表        │
         │            │  └─────────────────┘
                      │
                      │
                    低价值
```

---

## 六、技术债务

| 项目 | 说明 | 影响 |
|------|------|------|
| 路由配置缺失 | 新页面未添加到 router | 页面无法访问 |
| Provider 监控数据 | AdminController 返回 mock 数据 | 需要对接真实数据 |
| 错误处理 | Element SDK 错误处理不够完善 | 用户体验 |
| 单元测试 | 测试覆盖不足 | 代码质量 |
| 文档 | API 文档、集成文档缺失 | 开发者体验 |

---

## 七、依赖关系

```
路由配置 ─────────────────┐
                          │
                          ▼
Payment Element ◄─────── 3DS 处理
                          │
                          │
订阅定时扣款 ◄────────────┘
                          │
                          ▼
React/Vue 组件 ◄───────── npm 发布
```

---

## 八、风险评估

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|----------|
| 3DS 兼容性问题 | 中 | 高 | 充分测试各银行 3DS 流程 |
| Provider API 变更 | 低 | 高 | 抽象层隔离，版本锁定 |
| 性能问题 | 低 | 中 | 压力测试，缓存策略 |
| 安全漏洞 | 低 | 高 | 安全审计，渗透测试 |

---

## 九、下一步行动

### 立即行动（本周）

1. **添加路由配置** - 使新页面可访问
2. **完善 Payment Element iframe** - 动态支付方式切换
3. **实现 3DS 自动处理** - 核心合规要求

### 短期行动（2周内）

4. **实现订阅定时扣款** - 核心订阅功能
5. **React 组件封装** - 开发者体验
6. **对接真实监控数据** - 运营能力

### 中期行动（1月内）

7. **Vue 组件封装**
8. **数据报表导出**
9. **审计日志**
