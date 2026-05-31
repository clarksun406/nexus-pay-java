# NexusPay 部署指南

## 环境要求

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| PostgreSQL | 16+ |
| Redis | 7+ (可选) |
| Docker | 20+ |
| Docker Compose | 2+ |

---

## 一、Docker Compose 部署（推荐）

### 1. 克隆项目

```bash
git clone https://github.com/nexuspay/nexus-pay-java.git
cd nexus-pay-java
```

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 文件：

```env
# 数据库
DATABASE_URL=jdbc:postgresql://db:5432/nexuspay
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_at_least_256_bits

# Provider Keys (可选)
STRIPE_SECRET_KEY=sk_test_xxx
SQUARE_ACCESS_TOKEN=xxx
BRAINTREE_MERCHANT_ID=xxx
BRAINTREE_PUBLIC_KEY=xxx
BRAINTREE_PRIVATE_KEY=xxx
```

### 3. 启动服务

```bash
docker compose up -d
```

### 4. 访问服务

| 服务 | 地址 |
|------|------|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| 商户后台 | http://localhost:5173 |
| 运营管理端 | http://localhost:5174 |

---

## 二、本地开发部署

### 1. 数据库

```bash
# 创建数据库
createdb nexuspay

# 或使用 Docker
docker run -d --name nexuspay-db \
  -e POSTGRES_DB=nexuspay \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16
```

### 2. 后端

```bash
# 安装依赖
mvn clean install

# 启动
cd nexuspay-web
mvn spring-boot:run
```

### 3. 前端

**商户后台：**
```bash
cd frontend
npm install
npm run dev
```

**运营管理端：**
```bash
cd frontend-admin
npm install
npm run dev
```

**Element SDK：**
```bash
cd nexuspay-js
npm install
npm run build
```

---

## 三、生产部署

### 1. 构建生产镜像

```bash
# 后端
docker build -t nexuspay-api:latest .

# 前端
cd frontend && docker build -t nexuspay-dashboard:latest .
cd frontend-admin && docker build -t nexuspay-admin:latest .
```

### 2. docker-compose.prod.yml

```yaml
version: '3.8'

services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: nexuspay
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

  api:
    image: nexuspay-api:latest
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://db:5432/nexuspay
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: ${DATABASE_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      REDIS_URL: redis://redis:6379
    depends_on:
      db:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  dashboard:
    image: nexuspay-dashboard:latest
    ports:
      - "80:80"
    depends_on:
      - api

  admin:
    image: nexuspay-admin:latest
    ports:
      - "81:80"
    depends_on:
      - api

volumes:
  postgres_data:
  redis_data:
```

### 3. 启动

```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## 四、Kubernetes 部署

### 1. Namespace

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: nexuspay
```

### 2. ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: nexuspay-config
  namespace: nexuspay
data:
  DATABASE_URL: "jdbc:postgresql://postgres:5432/nexuspay"
  REDIS_URL: "redis://redis:6379"
```

### 3. Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: nexuspay-secret
  namespace: nexuspay
type: Opaque
stringData:
  DATABASE_PASSWORD: "your_password"
  JWT_SECRET: "your_jwt_secret"
```

### 4. Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nexuspay-api
  namespace: nexuspay
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nexuspay-api
  template:
    metadata:
      labels:
        app: nexuspay-api
    spec:
      containers:
      - name: api
        image: nexuspay-api:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: nexuspay-config
        - secretRef:
            name: nexuspay-secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

### 5. Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nexuspay-api
  namespace: nexuspay
spec:
  selector:
    app: nexuspay-api
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

---

## 五、SSL/TLS 配置

### 1. 使用 Nginx 反向代理

```nginx
server {
    listen 443 ssl;
    server_name api.nexuspay.com;

    ssl_certificate /etc/letsencrypt/live/api.nexuspay.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.nexuspay.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 2. 使用 Let's Encrypt

```bash
certbot --nginx -d api.nexuspay.com -d dashboard.nexuspay.com
```

---

## 六、监控

### 1. Prometheus

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'nexuspay'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api:8080']
```

### 2. Grafana Dashboard

导入 Spring Boot Dashboard 模板，监控：
- JVM 内存
- HTTP 请求
- 数据库连接池
- 自定义指标

---

## 七、备份与恢复

### 1. 数据库备份

```bash
# 备份
docker exec nexuspay-db pg_dump -U postgres nexuspay > backup.sql

# 恢复
docker exec -i nexuspay-db psql -U postgres nexuspay < backup.sql
```

### 2. 自动备份脚本

```bash
#!/bin/bash
DATE=$(date +%Y%m%d)
docker exec nexuspay-db pg_dump -U postgres nexuspay > /backups/nexuspay_$DATE.sql
find /backups -name "*.sql" -mtime +7 -delete
```

---

## 八、故障排查

### 1. 查看日志

```bash
# Docker logs
docker compose logs api

# 应用日志
tail -f /var/log/nexuspay/application.log
```

### 2. 健康检查

```bash
curl http://localhost:8080/actuator/health
```

### 3. 数据库连接

```bash
docker exec -it nexuspay-db psql -U postgres -d nexuspay
```

---

## 九、性能调优

### 1. JVM 参数

```bash
java -Xms512m -Xmx1g -XX:+UseG1GC -jar nexuspay-web.jar
```

### 2. PostgreSQL

```sql
-- postgresql.conf
shared_buffers = 256MB
effective_cache_size = 768MB
max_connections = 200
```

### 3. Nginx

```nginx
worker_processes auto;
events {
    worker_connections 1024;
}
```
