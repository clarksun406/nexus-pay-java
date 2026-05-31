# NexusPay RBAC 设计文档

## 一、当前状态

### 1.1 现有用户体系

| 实体 | 说明 | 角色 |
|------|------|------|
| `User` | 用户基础信息（共享） | - |
| `MerchantUser` | 商户-用户关联 | OWNER, ADMIN, DEVELOPER, FINANCE, VIEWER |
| `OrganizationUser` | 组织-用户关联 | ORG_OWNER, ORG_ADMIN, ORG_MEMBER |

### 1.2 当前问题

| 问题 | 说明 |
|------|------|
| 权限粒度粗 | 角色枚举，无细粒度权限 |
| 无统一权限检查 | 缺少 `@RequirePermission` 注解 |
| 运营端登录缺失 | 无 `/api/v1/admin/auth/login` |
| 运营端认证缺失 | 无 Admin JWT Filter |

---

## 二、设计方案

### 2.1 统一 RBAC 模型

```
User (用户)
├── UserRole (用户-角色关联)
│   ├── roleId
│   ├── scopeType: MERCHANT, ORGANIZATION, SYSTEM
│   └── scopeId: merchantId 或 organizationId
│
Role (角色)
├── name: MERCHANT_OWNER, ORG_ADMIN...
├── type: MERCHANT, ORG, SYSTEM
└── permissions: List<Permission>
│
Permission (权限)
├── code: MERCHANT_READ, PAYMENT_CREATE...
└── description
```

### 2.2 权限矩阵

**商户端权限：**

| 角色 | 权限 |
|------|------|
| MERCHANT_OWNER | 所有商户权限 + 成员管理 + API Key 管理 |
| MERCHANT_ADMIN | 商户配置 + 连接器管理 + 路由规则 |
| MERCHANT_DEVELOPER | 支付查看 + Webhook 配置 + 测试模式 |
| MERCHANT_FINANCE | 退款管理 + 结算查看 + 争议处理 |
| MERCHANT_VIEWER | 只读访问 |

**运营端权限：**

| 角色 | 权限 |
|------|------|
| ORG_OWNER | 组织所有权限 + 商户管理 + 成员管理 |
| ORG_ADMIN | 商户审核 + 系统监控 + 配置管理 |
| ORG_MEMBER | 只读访问 |

### 2.3 权限代码定义

```java
public enum Permission {
    // 商户权限
    MERCHANT_READ,
    MERCHANT_WRITE,
    MERCHANT_DELETE,
    
    PAYMENT_READ,
    PAYMENT_CREATE,
    PAYMENT_REFUND,
    
    CONNECTOR_MANAGE,
    ROUTING_RULE_MANAGE,
    API_KEY_MANAGE,
    WEBHOOK_MANAGE,
    
    MEMBER_MANAGE,
    CUSTOMER_MANAGE,
    SUBSCRIPTION_MANAGE,
    
    // 运营权限
    ORG_READ,
    ORG_MANAGE,
    
    MERCHANT_APPROVE,
    MERCHANT_SUSPEND,
    
    SYSTEM_MONITOR,
    SYSTEM_CONFIG,
    AUDIT_LOG_READ
}
```

---

## 三、需要实现的内容

### 3.1 数据库

| 表 | 说明 | 状态 |
|----|------|------|
| `permissions` | 权限定义表 | ❌ 未实现 |
| `roles` | 角色定义表 | ❌ 未实现 |
| `role_permissions` | 角色-权限关联表 | ❌ 未实现 |
| `user_roles` | 用户-角色关联表 | ❌ 未实现 |

**迁移脚本：** `V6__create_rbac_tables.sql`

### 3.2 后端实体

| 实体 | 文件 | 状态 |
|------|------|------|
| `Permission` | `entity/Permission.java` | ❌ 未实现 |
| `Role` | `entity/Role.java` | ❌ 未实现 |
| `UserRole` | `entity/UserRole.java` | ❌ 未实现 |

### 3.3 后端 Repository

| Repository | 文件 | 状态 |
|------------|------|------|
| `PermissionRepository` | `repository/PermissionRepository.java` | ❌ 未实现 |
| `RoleRepository` | `repository/RoleRepository.java` | ❌ 未实现 |
| `UserRoleRepository` | `repository/UserRoleRepository.java` | ❌ 未实现 |

### 3.4 后端 Service

| Service | 文件 | 功能 | 状态 |
|---------|------|------|------|
| `PermissionService` | `service/PermissionService.java` | 权限检查 | ❌ 未实现 |
| `RoleService` | `service/RoleService.java` | 角色管理 | ❌ 未实现 |

### 3.5 权限注解

| 组件 | 文件 | 功能 | 状态 |
|------|------|------|------|
| `@RequirePermission` | `annotation/RequirePermission.java` | 权限注解 | ❌ 未实现 |
| `PermissionAspect` | `aop/PermissionAspect.java` | AOP 权限检查 | ❌ 未实现 |

### 3.6 认证增强

| 组件 | 文件 | 功能 | 状态 |
|------|------|------|------|
| 运营端登录 API | `controller/AdminAuthController.java` | `/api/v1/admin/auth/login` | ❌ 未实现 |
| Admin JWT Filter | `security/AdminJwtFilter.java` | 运营端 JWT 认证 | ❌ 未实现 |
| 权限上下文 | `security/SecurityContext.java` | 当前用户权限 | ❌ 未实现 |

### 3.7 前端

| 功能 | 页面 | 状态 |
|------|------|------|
| 商户登录 | `frontend/src/pages/Login.vue` | ✅ 已有 |
| 运营端登录 | `frontend-admin/src/pages/Login.vue` | ❌ 未实现 |
| 权限路由守卫 | `router/guards.ts` | ❌ 未实现 |
| 权限指令 | `directives/permission.ts` | ❌ 未实现 |

---

## 四、实现路线图

### Phase 1: 基础 RBAC (v1.1.0)

**优先级：🔴 高**

**后端：**
- [ ] 创建 Permission, Role, UserRole 实体
- [ ] 创建数据库迁移脚本
- [ ] 实现 PermissionService
- [ ] 实现 @RequirePermission 注解
- [ ] 实现 PermissionAspect

**预计：3 天**

### Phase 2: 运营端认证 (v1.1.0)

**优先级：🔴 高**

**后端：**
- [ ] 实现 AdminAuthController
- [ ] 实现 AdminJwtFilter
- [ ] 区分商户端/运营端 JWT Claims

**前端：**
- [ ] 实现 frontend-admin 登录页面
- [ ] 实现登录状态管理

**预计：2 天**

### Phase 3: 权限初始化 (v1.2.0)

**优先级：🟡 中**

- [ ] 权限数据初始化脚本
- [ ] 默认角色初始化
- [ ] 数据迁移（现有 MerchantUser → UserRole）

**预计：2 天**

### Phase 4: 前端权限 (v1.2.0)

**优先级：🟡 中**

- [ ] 权限路由守卫
- [ ] v-permission 指令
- [ ] 权限检查工具函数

**预计：2 天**

---

## 五、API 设计

### 5.1 运营端认证 API

```
POST /api/v1/admin/auth/login
Request:  { email, password }
Response: { accessToken, refreshToken, user }

POST /api/v1/admin/auth/logout
Request:  { refreshToken }
Response: { success }

POST /api/v1/admin/auth/refresh
Request:  { refreshToken }
Response: { accessToken }
```

### 5.2 权限检查 API

```
GET /api/v1/admin/me/permissions
Response: { permissions: ["MERCHANT_APPROVE", "SYSTEM_MONITOR"] }

GET /api/v1/merchants/{id}/me/permissions
Response: { permissions: ["PAYMENT_CREATE", "CONNECTOR_MANAGE"] }
```

---

## 六、使用示例

### 6.1 后端权限检查

```java
@RestController
@RequestMapping("/api/v1/admin/merchants")
public class AdminMerchantController {
    
    @PostMapping("/{id}/approve")
    @RequirePermission("MERCHANT_APPROVE")
    public ResponseEntity<?> approveMerchant(@PathVariable UUID id) {
        // 只有拥有 MERCHANT_APPROVE 权限的用户可以访问
    }
}
```

### 6.2 前端权限检查

```vue
<template>
  <!-- 按钮级权限控制 -->
  <button v-permission="'MERCHANT_APPROVE'" @click="approve">
    审核通过
  </button>
  
  <!-- 条件渲染 -->
  <div v-if="hasPermission('SYSTEM_MONITOR')">
    系统监控数据...
  </div>
</template>

<script setup>
import { usePermission } from '@/composables/usePermission'
const { hasPermission } = usePermission()
</script>
```

---

## 七、数据迁移

### 7.1 迁移策略

现有数据：
- `MerchantUser` → 迁移到 `UserRole`
- `OrganizationUser` → 迁移到 `UserRole`

**迁移脚本逻辑：**

```sql
-- 1. 创建权限
INSERT INTO permissions (code, description) VALUES
('MERCHANT_READ', 'Read merchant data'),
('PAYMENT_CREATE', 'Create payments'),
-- ...

-- 2. 创建角色
INSERT INTO roles (name, type) VALUES
('MERCHANT_OWNER', 'MERCHANT'),
('MERCHANT_ADMIN', 'MERCHANT'),
-- ...

-- 3. 关联角色权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MERCHANT_OWNER';

-- 4. 迁移用户角色
INSERT INTO user_roles (user_id, role_id, scope_type, scope_id)
SELECT mu.user_id, r.id, 'MERCHANT', mu.merchant_id
FROM merchant_users mu, roles r
WHERE r.name = CONCAT('MERCHANT_', mu.role);
```

---

## 八、风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 数据迁移失败 | 用户无法登录 | 先备份，事务回滚 |
| 权限遗漏 | 功能无法访问 | 权限测试用例 |
| 性能影响 | 权限检查慢 | 缓存用户权限 |

---

## 九、测试计划

| 测试项 | 说明 |
|--------|------|
| 权限检查单元测试 | 验证 @RequirePermission 注解 |
| 认证集成测试 | 商户登录 + 运营端登录 |
| 权限边界测试 | 无权限访问返回 403 |
| 数据迁移测试 | 验证迁移后权限正确 |
