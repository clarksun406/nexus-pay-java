# NexusPay 閮ㄧ讲鎸囧崡

## 鐜瑕佹眰

| 缁勪欢 | 鐗堟湰 |
|------|------|
| Java | 17+ |
| PostgreSQL | 16+ |
| Redis | 7+ (鍙€? |
| Docker | 20+ |
| Docker Compose | 2+ |

---

## 涓€銆丏ocker Compose 閮ㄧ讲锛堟帹鑽愶級

### 1. 鍏嬮殕椤圭洰

```bash
git clone https://github.com/nexuspay/nexus-pay-java.git
cd nexus-pay-java
```

### 2. 閰嶇疆鐜鍙橀噺

```bash
cp .env.example .env
```

缂栬緫 `.env` 鏂囦欢锛?
```env
# 鏁版嵁搴?DATABASE_URL=jdbc:postgresql://db:5432/nexuspay
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_at_least_256_bits

# Provider Keys (鍙€?
STRIPE_SECRET_KEY=sk_test_xxx
SQUARE_ACCESS_TOKEN=xxx
BRAINTREE_MERCHANT_ID=xxx
BRAINTREE_PUBLIC_KEY=xxx
BRAINTREE_PRIVATE_KEY=xxx
```

### 3. 鍚姩鏈嶅姟

```bash
docker compose up -d
```

### 4. 璁块棶鏈嶅姟

| 鏈嶅姟 | 鍦板潃 |
|------|------|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| 鍟嗘埛鍚庡彴 | http://localhost:5173 |
| 杩愯惀绠＄悊绔?| http://localhost:5174 |

---

## 浜屻€佹湰鍦板紑鍙戦儴缃?
### 1. 鏁版嵁搴?
```bash
# 鍒涘缓鏁版嵁搴?createdb nexuspay

# 鎴栦娇鐢?Docker
docker run -d --name nexuspay-db \
  -e POSTGRES_DB=nexuspay \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16
```

### 2. 鍚庣

```bash
# 瀹夎渚濊禆
mvn clean install

# 鍚姩
cd nexuspay-web
mvn spring-boot:run
```

### 3. 鍓嶇

**鍟嗘埛鍚庡彴锛?*
```bash
cd frontend-dashboard
npm install
npm run dev
```

**杩愯惀绠＄悊绔細**
```bash
cd frontend-admin
npm install
npm run dev
```

**Element SDK锛?*
```bash
cd frontend-nexuspay-js
npm install
npm run build
```

---

## 涓夈€佺敓浜ч儴缃?
### 1. 鏋勫缓鐢熶骇闀滃儚

```bash
# 鍚庣
docker build -t nexuspay-api:latest .

# 鍓嶇
cd frontend-dashboard && docker build -t nexuspay-dashboard:latest .
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

### 3. 鍚姩

```bash
docker compose -f docker-compose.prod.yml up -d
```

---

## 鍥涖€並ubernetes 閮ㄧ讲

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

## 浜斻€丼SL/TLS 閰嶇疆

### 1. 浣跨敤 Nginx 鍙嶅悜浠ｇ悊

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

### 2. 浣跨敤 Let's Encrypt

```bash
certbot --nginx -d api.nexuspay.com -d dashboard.nexuspay.com
```

---

## 鍏€佺洃鎺?
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

瀵煎叆 Spring Boot Dashboard 妯℃澘锛岀洃鎺э細
- JVM 鍐呭瓨
- HTTP 璇锋眰
- 鏁版嵁搴撹繛鎺ユ睜
- 鑷畾涔夋寚鏍?
---

## 涓冦€佸浠戒笌鎭㈠

### 1. 鏁版嵁搴撳浠?
```bash
# 澶囦唤
docker exec nexuspay-db pg_dump -U postgres nexuspay > backup.sql

# 鎭㈠
docker exec -i nexuspay-db psql -U postgres nexuspay < backup.sql
```

### 2. 鑷姩澶囦唤鑴氭湰

```bash
#!/bin/bash
DATE=$(date +%Y%m%d)
docker exec nexuspay-db pg_dump -U postgres nexuspay > /backups/nexuspay_$DATE.sql
find /backups -name "*.sql" -mtime +7 -delete
```

---

## 鍏€佹晠闅滄帓鏌?
### 1. 鏌ョ湅鏃ュ織

```bash
# Docker logs
docker compose logs api

# 搴旂敤鏃ュ織
tail -f /var/log/nexuspay/application.log
```

### 2. 鍋ュ悍妫€鏌?
```bash
curl http://localhost:8080/actuator/health
```

### 3. 鏁版嵁搴撹繛鎺?
```bash
docker exec -it nexuspay-db psql -U postgres -d nexuspay
```

---

## 涔濄€佹€ц兘璋冧紭

### 1. JVM 鍙傛暟

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

