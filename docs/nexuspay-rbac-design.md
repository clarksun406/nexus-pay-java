# NexusPay RBAC 璁捐鏂囨。

## 涓€銆佸綋鍓嶇姸鎬?
### 1.1 鐜版湁鐢ㄦ埛浣撶郴

| 瀹炰綋 | 璇存槑 | 瑙掕壊 |
|------|------|------|
| `User` | 鐢ㄦ埛鍩虹淇℃伅锛堝叡浜級 | - |
| `MerchantUser` | 鍟嗘埛-鐢ㄦ埛鍏宠仈 | OWNER, ADMIN, DEVELOPER, FINANCE, VIEWER |
| `OrganizationUser` | 缁勭粐-鐢ㄦ埛鍏宠仈 | ORG_OWNER, ORG_ADMIN, ORG_MEMBER |

### 1.2 褰撳墠闂

| 闂 | 璇存槑 |
|------|------|
| 鏉冮檺绮掑害绮?| 瑙掕壊鏋氫妇锛屾棤缁嗙矑搴︽潈闄?|
| 鏃犵粺涓€鏉冮檺妫€鏌?| 缂哄皯 `@RequirePermission` 娉ㄨВ |
| 杩愯惀绔櫥褰曠己澶?| 鏃?`/api/v1/admin/auth/login` |
| 杩愯惀绔璇佺己澶?| 鏃?Admin JWT Filter |

---

## 浜屻€佽璁℃柟妗?
### 2.1 缁熶竴 RBAC 妯″瀷

```
User (鐢ㄦ埛)
鈹溾攢鈹€ UserRole (鐢ㄦ埛-瑙掕壊鍏宠仈)
鈹?  鈹溾攢鈹€ roleId
鈹?  鈹溾攢鈹€ scopeType: MERCHANT, ORGANIZATION, SYSTEM
鈹?  鈹斺攢鈹€ scopeId: merchantId 鎴?organizationId
鈹?Role (瑙掕壊)
鈹溾攢鈹€ name: MERCHANT_OWNER, ORG_ADMIN...
鈹溾攢鈹€ type: MERCHANT, ORG, SYSTEM
鈹斺攢鈹€ permissions: List<Permission>
鈹?Permission (鏉冮檺)
鈹溾攢鈹€ code: MERCHANT_READ, PAYMENT_CREATE...
鈹斺攢鈹€ description
```

### 2.2 鏉冮檺鐭╅樀

**鍟嗘埛绔潈闄愶細**

| 瑙掕壊 | 鏉冮檺 |
|------|------|
| MERCHANT_OWNER | 鎵€鏈夊晢鎴锋潈闄?+ 鎴愬憳绠＄悊 + API Key 绠＄悊 |
| MERCHANT_ADMIN | 鍟嗘埛閰嶇疆 + 杩炴帴鍣ㄧ鐞?+ 璺敱瑙勫垯 |
| MERCHANT_DEVELOPER | 鏀粯鏌ョ湅 + Webhook 閰嶇疆 + 娴嬭瘯妯″紡 |
| MERCHANT_FINANCE | 閫€娆剧鐞?+ 缁撶畻鏌ョ湅 + 浜夎澶勭悊 |
| MERCHANT_VIEWER | 鍙璁块棶 |

**杩愯惀绔潈闄愶細**

| 瑙掕壊 | 鏉冮檺 |
|------|------|
| ORG_OWNER | 缁勭粐鎵€鏈夋潈闄?+ 鍟嗘埛绠＄悊 + 鎴愬憳绠＄悊 |
| ORG_ADMIN | 鍟嗘埛瀹℃牳 + 绯荤粺鐩戞帶 + 閰嶇疆绠＄悊 |
| ORG_MEMBER | 鍙璁块棶 |

### 2.3 鏉冮檺浠ｇ爜瀹氫箟

```java
public enum Permission {
    // 鍟嗘埛鏉冮檺
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
    
    // 杩愯惀鏉冮檺
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

## 涓夈€侀渶瑕佸疄鐜扮殑鍐呭

### 3.1 鏁版嵁搴?
| 琛?| 璇存槑 | 鐘舵€?|
|----|------|------|
| `permissions` | 鏉冮檺瀹氫箟琛?| 鉂?鏈疄鐜?|
| `roles` | 瑙掕壊瀹氫箟琛?| 鉂?鏈疄鐜?|
| `role_permissions` | 瑙掕壊-鏉冮檺鍏宠仈琛?| 鉂?鏈疄鐜?|
| `user_roles` | 鐢ㄦ埛-瑙掕壊鍏宠仈琛?| 鉂?鏈疄鐜?|

**杩佺Щ鑴氭湰锛?* `V6__create_rbac_tables.sql`

### 3.2 鍚庣瀹炰綋

| 瀹炰綋 | 鏂囦欢 | 鐘舵€?|
|------|------|------|
| `Permission` | `entity/Permission.java` | 鉂?鏈疄鐜?|
| `Role` | `entity/Role.java` | 鉂?鏈疄鐜?|
| `UserRole` | `entity/UserRole.java` | 鉂?鏈疄鐜?|

### 3.3 鍚庣 Repository

| Repository | 鏂囦欢 | 鐘舵€?|
|------------|------|------|
| `PermissionRepository` | `repository/PermissionRepository.java` | 鉂?鏈疄鐜?|
| `RoleRepository` | `repository/RoleRepository.java` | 鉂?鏈疄鐜?|
| `UserRoleRepository` | `repository/UserRoleRepository.java` | 鉂?鏈疄鐜?|

### 3.4 鍚庣 Service

| Service | 鏂囦欢 | 鍔熻兘 | 鐘舵€?|
|---------|------|------|------|
| `PermissionService` | `service/PermissionService.java` | 鏉冮檺妫€鏌?| 鉂?鏈疄鐜?|
| `RoleService` | `service/RoleService.java` | 瑙掕壊绠＄悊 | 鉂?鏈疄鐜?|

### 3.5 鏉冮檺娉ㄨВ

| 缁勪欢 | 鏂囦欢 | 鍔熻兘 | 鐘舵€?|
|------|------|------|------|
| `@RequirePermission` | `annotation/RequirePermission.java` | 鏉冮檺娉ㄨВ | 鉂?鏈疄鐜?|
| `PermissionAspect` | `aop/PermissionAspect.java` | AOP 鏉冮檺妫€鏌?| 鉂?鏈疄鐜?|

### 3.6 璁よ瘉澧炲己

| 缁勪欢 | 鏂囦欢 | 鍔熻兘 | 鐘舵€?|
|------|------|------|------|
| 杩愯惀绔櫥褰?API | `controller/AdminAuthController.java` | `/api/v1/admin/auth/login` | 鉂?鏈疄鐜?|
| Admin JWT Filter | `security/AdminJwtFilter.java` | 杩愯惀绔?JWT 璁よ瘉 | 鉂?鏈疄鐜?|
| 鏉冮檺涓婁笅鏂?| `security/SecurityContext.java` | 褰撳墠鐢ㄦ埛鏉冮檺 | 鉂?鏈疄鐜?|

### 3.7 鍓嶇

| 鍔熻兘 | 椤甸潰 | 鐘舵€?|
|------|------|------|
| 鍟嗘埛鐧诲綍 | `frontend-dashboard/src/pages/Login.vue` | 鉁?宸叉湁 |
| 杩愯惀绔櫥褰?| `frontend-admin/src/pages/Login.vue` | 鉂?鏈疄鐜?|
| 鏉冮檺璺敱瀹堝崼 | `router/guards.ts` | 鉂?鏈疄鐜?|
| 鏉冮檺鎸囦护 | `directives/permission.ts` | 鉂?鏈疄鐜?|

---

## 鍥涖€佸疄鐜拌矾绾垮浘

### Phase 1: 鍩虹 RBAC (v1.1.0)

**浼樺厛绾э細馃敶 楂?*

**鍚庣锛?*
- [ ] 鍒涘缓 Permission, Role, UserRole 瀹炰綋
- [ ] 鍒涘缓鏁版嵁搴撹縼绉昏剼鏈?- [ ] 瀹炵幇 PermissionService
- [ ] 瀹炵幇 @RequirePermission 娉ㄨВ
- [ ] 瀹炵幇 PermissionAspect

**棰勮锛? 澶?*

### Phase 2: 杩愯惀绔璇?(v1.1.0)

**浼樺厛绾э細馃敶 楂?*

**鍚庣锛?*
- [ ] 瀹炵幇 AdminAuthController
- [ ] 瀹炵幇 AdminJwtFilter
- [ ] 鍖哄垎鍟嗘埛绔?杩愯惀绔?JWT Claims

**鍓嶇锛?*
- [ ] 瀹炵幇 frontend-admin 鐧诲綍椤甸潰
- [ ] 瀹炵幇鐧诲綍鐘舵€佺鐞?
**棰勮锛? 澶?*

### Phase 3: 鏉冮檺鍒濆鍖?(v1.2.0)

**浼樺厛绾э細馃煛 涓?*

- [ ] 鏉冮檺鏁版嵁鍒濆鍖栬剼鏈?- [ ] 榛樿瑙掕壊鍒濆鍖?- [ ] 鏁版嵁杩佺Щ锛堢幇鏈?MerchantUser 鈫?UserRole锛?
**棰勮锛? 澶?*

### Phase 4: 鍓嶇鏉冮檺 (v1.2.0)

**浼樺厛绾э細馃煛 涓?*

- [ ] 鏉冮檺璺敱瀹堝崼
- [ ] v-permission 鎸囦护
- [ ] 鏉冮檺妫€鏌ュ伐鍏峰嚱鏁?
**棰勮锛? 澶?*

---

## 浜斻€丄PI 璁捐

### 5.1 杩愯惀绔璇?API

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

### 5.2 鏉冮檺妫€鏌?API

```
GET /api/v1/admin/me/permissions
Response: { permissions: ["MERCHANT_APPROVE", "SYSTEM_MONITOR"] }

GET /api/v1/merchants/{id}/me/permissions
Response: { permissions: ["PAYMENT_CREATE", "CONNECTOR_MANAGE"] }
```

---

## 鍏€佷娇鐢ㄧず渚?
### 6.1 鍚庣鏉冮檺妫€鏌?
```java
@RestController
@RequestMapping("/api/v1/admin/merchants")
public class AdminMerchantController {
    
    @PostMapping("/{id}/approve")
    @RequirePermission("MERCHANT_APPROVE")
    public ResponseEntity<?> approveMerchant(@PathVariable UUID id) {
        // 鍙湁鎷ユ湁 MERCHANT_APPROVE 鏉冮檺鐨勭敤鎴峰彲浠ヨ闂?    }
}
```

### 6.2 鍓嶇鏉冮檺妫€鏌?
```vue
<template>
  <!-- 鎸夐挳绾ф潈闄愭帶鍒?-->
  <button v-permission="'MERCHANT_APPROVE'" @click="approve">
    瀹℃牳閫氳繃
  </button>
  
  <!-- 鏉′欢娓叉煋 -->
  <div v-if="hasPermission('SYSTEM_MONITOR')">
    绯荤粺鐩戞帶鏁版嵁...
  </div>
</template>

<script setup>
import { usePermission } from '@/composables/usePermission'
const { hasPermission } = usePermission()
</script>
```

---

## 涓冦€佹暟鎹縼绉?
### 7.1 杩佺Щ绛栫暐

鐜版湁鏁版嵁锛?- `MerchantUser` 鈫?杩佺Щ鍒?`UserRole`
- `OrganizationUser` 鈫?杩佺Щ鍒?`UserRole`

**杩佺Щ鑴氭湰閫昏緫锛?*

```sql
-- 1. 鍒涘缓鏉冮檺
INSERT INTO permissions (code, description) VALUES
('MERCHANT_READ', 'Read merchant data'),
('PAYMENT_CREATE', 'Create payments'),
-- ...

-- 2. 鍒涘缓瑙掕壊
INSERT INTO roles (name, type) VALUES
('MERCHANT_OWNER', 'MERCHANT'),
('MERCHANT_ADMIN', 'MERCHANT'),
-- ...

-- 3. 鍏宠仈瑙掕壊鏉冮檺
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'MERCHANT_OWNER';

-- 4. 杩佺Щ鐢ㄦ埛瑙掕壊
INSERT INTO user_roles (user_id, role_id, scope_type, scope_id)
SELECT mu.user_id, r.id, 'MERCHANT', mu.merchant_id
FROM merchant_users mu, roles r
WHERE r.name = CONCAT('MERCHANT_', mu.role);
```

---

## 鍏€侀闄╀笌缂撹В

| 椋庨櫓 | 褰卞搷 | 缂撹В鎺柦 |
|------|------|----------|
| 鏁版嵁杩佺Щ澶辫触 | 鐢ㄦ埛鏃犳硶鐧诲綍 | 鍏堝浠斤紝浜嬪姟鍥炴粴 |
| 鏉冮檺閬楁紡 | 鍔熻兘鏃犳硶璁块棶 | 鏉冮檺娴嬭瘯鐢ㄤ緥 |
| 鎬ц兘褰卞搷 | 鏉冮檺妫€鏌ユ參 | 缂撳瓨鐢ㄦ埛鏉冮檺 |

---

## 涔濄€佹祴璇曡鍒?
| 娴嬭瘯椤?| 璇存槑 |
|--------|------|
| 鏉冮檺妫€鏌ュ崟鍏冩祴璇?| 楠岃瘉 @RequirePermission 娉ㄨВ |
| 璁よ瘉闆嗘垚娴嬭瘯 | 鍟嗘埛鐧诲綍 + 杩愯惀绔櫥褰?|
| 鏉冮檺杈圭晫娴嬭瘯 | 鏃犳潈闄愯闂繑鍥?403 |
| 鏁版嵁杩佺Щ娴嬭瘯 | 楠岃瘉杩佺Щ鍚庢潈闄愭纭?|

