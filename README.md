# IRMS: Intelligent Restaurant Management System

Hệ thống Quản lý Nhà hàng Thông minh (IRMS) được xây dựng trên kiến trúc **Service-Based**, sử dụng Docker để đồng bộ môi trường phát triển và vận hành.

## 🚀 Tính năng chính
- Quản lý đặt món, thực đơn, bếp (KDS), bàn và thanh toán.
- Phân tách theo domain services (Order, Menu, Kitchen, v.v.).
- Tích hợp Database độc lập, Redis Cache và RabbitMQ Message Broker.

## 📂 Cấu trúc dự án
- `gateway/`: API Gateway & Auth Service (Node.js).
- `services/`: Các dịch vụ nghiệp vụ (Java Spring Boot, Python).
- `frontend/`: Ứng dụng POS, KDS, Admin (React, React Native).
- `infrastructure/`: Cấu hình Docker, PostgreSQL init scripts.

## 🛠 Hướng dẫn bắt đầu

### 1. Chuẩn bị
- Đã cài đặt **Docker Desktop**.
- Copy file cấu hình môi trường:
  ```powershell
  cp .env.example .env
  ```

### 2. Chạy hạ tầng (Database, Cache, MQ)
```powershell
docker-compose up -d postgres-db redis-cache rabbitmq
```
*Lưu ý: Các database `irms_order`, `irms_menu`,... sẽ được tự động tạo trong PostgreSQL.*

### 3. Chạy toàn bộ hệ thống (Sau khi đã có code)
```powershell
docker-compose up -d
```

## 📜 Kiến trúc & Báo cáo
Chi tiết về thiết kế hệ thống có thể xem tại file `report.md`.
