# IRMS: Intelligent Restaurant Management System

Hệ thống Quản lý Nhà hàng Thông minh (IRMS) được xây dựng trên kiến trúc **Microservices/Service-Based**, sử dụng Docker để đồng bộ môi trường phát triển và vận hành.

## 🚀 Tính năng chính
- Quản lý đặt món, thực đơn, bếp (KDS), bàn và thanh toán.
- Phân tách theo domain services (Order, Menu, Kitchen, v.v.).
- Tích hợp Database độc lập cho từng service, Redis Cache và RabbitMQ Message Broker.

## 📂 Cấu trúc dự án
- `gateway/`: API Gateway & Auth Service (Node.js).
- `services/`: Các dịch vụ nghiệp vụ (Java Spring Boot, Python).
- `shared/`: Các thư viện dùng chung (Common Lib cho Java).
- `frontend/`: Ứng dụng POS, KDS, Admin (React, React Native).
- `infrastructure/`: Cấu hình Docker, PostgreSQL init scripts.

## 🛠 Hướng dẫn vận hành với Docker

### 1. Chuẩn bị
- Đã cài đặt **Docker Desktop** & **Docker Compose**.
- Thiết lập file môi trường:
  ```powershell
  cp .env.example .env
  ```

### 2. Quản lý Hạ tầng (Database, Cache, MQ)
Chạy các dịch vụ nền tảng trước khi khởi động các service nghiệp vụ:
```powershell
# Khởi động DB, Redis, RabbitMQ
docker-compose up -d postgres-db redis-cache rabbitmq

# Kiểm tra trạng thái các container
docker-compose ps
```
*Lưu ý: Script tại `infrastructure/docker/postgres-init` sẽ tự động khởi tạo các database `irms_menu`, `irms_order`,...*

### 3. Xây dựng và Chạy Service (Build & Run)

#### Xây dựng lại Service sau khi thay đổi code:
Khi bạn chỉnh sửa mã nguồn (ví dụ trong `menu-service`), cần build lại image:
```powershell
# Build một service cụ thể
docker-compose build menu-service

# Build lại toàn bộ hệ thống
docker-compose build
```

#### Khởi chạy Service:
```powershell
# Chạy toàn bộ hệ thống (Hạ tầng + Services)
docker-compose up -d

# Chạy và xem log của một service cụ thể
docker-compose up menu-service
```

#### Xem Log và Debug:
```powershell
# Xem log realtime của menu-service
docker-compose logs -f menu-service

# Kiểm tra log của Database nếu có lỗi kết nối
docker-compose logs postgres-db
```

### 4. Dừng hệ thống
```powershell
# Dừng các container nhưng giữ lại dữ liệu volume
docker-compose down

# Dừng và xóa sạch dữ liệu (Reset Database)
docker-compose down -v
```

## 📝 Lưu ý cho Nhà phát triển (Java/Spring Boot)
- **Cấu hình Dockerfile**: Các service Java sử dụng `Multi-stage build` để tối ưu kích thước image và bảo mật (chạy với user `spring` không có quyền root).
- **Build Cache**: Thứ tự `COPY` trong Dockerfile đã được tối ưu. Nếu chỉ sửa code Java mà không thêm thư viện mới, quá trình build sẽ cực kỳ nhanh nhờ Docker Layer Cache.
- **JVM Optimization**: Hệ thống tự động cấu hình `-XX:MaxRAMPercentage=75.0` để tối ưu RAM trong môi trường container.

## 📜 Kiến trúc & Báo cáo
Chi tiết về thiết kế hệ thống và sơ đồ thực thể có thể xem tại file `report.md` hoặc thư mục `docs/architecture/`.
