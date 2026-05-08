# BE changes — so với commit `d00322d`

Tổng quan các thay đổi backend (3 service: `order-service`, `kitchen-service`, `table-service`, `payment-service`) áp dụng sau commit `d00322d fix: resolve inter-service communication URL mismatches`.

---

## 1. Bug fixes (BE không hoạt động được với FE)

### 1.1 `order-service` — Sai path gọi menu-service

**File**: `services/order-service/src/main/java/com/irms/order/infrastructure/client/MenuServiceClientImpl.java`

```diff
- String url = menuServiceUrl + "/api/menu/" + menuItemId;
+ String url = menuServiceUrl + "/api/menu/items/" + menuItemId;
```

**Lý do**: menu-service expose endpoint ở `/api/menu/items/{id}` (xem `MenuController.java`), nhưng client gọi `/api/menu/{id}` → 404 → wrap thành `ServiceUnavailableException` → POS thấy `503 "Menu service is currently unavailable"` khi tạo order.

---

### 1.2 `order-service` — `MenuItemDTO` không deserialize được response từ menu-service

**File**: `services/order-service/src/main/java/com/irms/order/dto/MenuItemDTO.java`

```diff
- private String category;
- private boolean available;
+ // (xóa hai field)
+ @JsonIgnoreProperties(ignoreUnknown = true)
```

**Lý do**: menu-service trả `category: { id, name, displayOrder, ... }` (object) và `isAvailable: true` (camelCase). DTO của order khai báo `category: String` → Jackson `MismatchedInputException`. Chỉ giữ `id`, `name`, `price` (3 field thực sự dùng) + `@JsonIgnoreProperties(ignoreUnknown = true)` cho forward-compat.

Tương tự cho **`TableResponseDTO`**: thêm `@JsonIgnoreProperties(ignoreUnknown = true)` để ổn định khi table-service mở rộng response.

---

### 1.3 `payment-service` — Sai tên query param khi notify order COMPLETED

**File**: `services/payment-service/src/main/java/com/irms/payment/infrastructure/client/OrderServiceClient.java`

```diff
- String url = orderServiceUrl + "/api/v1/orders/" + orderId + "/status?newStatus=COMPLETED";
+ String url = orderServiceUrl + "/api/v1/orders/" + orderId + "/status?status=COMPLETED";
```

**Lý do**: `OrderController.updateOrderStatus` khai báo `@RequestParam OrderStatus status` → tên param phải là `status` (mặc định Spring). Sai param → 400 → payment process throw 500.

---

### 1.4 `order-service` — `addOrderItem` ném `TransientObjectException`

**File**: `services/order-service/src/main/java/com/irms/order/service/OrderServiceImpl.java`

```diff
+ @PersistenceContext
+ private EntityManager entityManager;
...
- return orderMapper.toDto(orderRepository.save(order));
+ entityManager.persist(orderItem);
+ entityManager.flush();
+ return orderMapper.toDto(order);
```

**Lý do**: order là managed entity (load qua `findById`). `repository.save()` cho entity có `id != null` gọi `em.merge()`, không cascade-persist transient child theo cách mong đợi → flush time ném `TransientObjectException`. Persist item rõ ràng + flush thủ công để mapper có ID.

---

### 1.5 `order-service` — State machine quá strict, chặn thanh toán

**File**: `services/order-service/src/main/java/com/irms/order/validator/OrderStateValidator.java`

```diff
+ // Cho phép kết thúc đơn từ bất kỳ trạng thái không-terminal
+ if (currentStatus == COMPLETED || currentStatus == CANCELLED)
+   throw ...;
+ if (newStatus == COMPLETED || newStatus == CANCELLED) return;

  boolean isValid = switch (currentStatus) {
-   case DRAFT -> newStatus == PENDING || newStatus == CANCELLED;
-   case PENDING -> newStatus == COOKING || newStatus == CANCELLED;
+   case DRAFT -> newStatus == PENDING;
+   case PENDING -> newStatus == COOKING;
    case COOKING -> newStatus == READY_TO_SERVE;
    case READY_TO_SERVE -> newStatus == SERVED;
-   case SERVED -> newStatus == COMPLETED;
-   case COMPLETED, CANCELLED -> false;
+   case SERVED -> false; // đã handle COMPLETED ở trên
+   default -> false;
  };
```

**Lý do**: payment-service auto gọi `updateOrderStatusToCompleted` khi process payment, nhưng order còn ở `DRAFT/PENDING/COOKING/SERVED` (FE không đẩy state machine từng bước). Trước đó chỉ cho `SERVED → COMPLETED` → 409 → payment 500. Relax: bất kỳ trạng thái không-terminal đều có thể `→ COMPLETED` hoặc `→ CANCELLED`.

Tương tự ở **`OrderItemServiceImpl.updateOrderItemStatusInternal`**: cho phép `* → CANCELLED` (waiter có thể hủy bất kỳ lúc nào) và cho phép skip-step (PENDING → READY_TO_SERVE/SERVED).

---

## 2. Cross-service sync (kitchen ↔ order)

Bug: KDS update kitchen item nhưng `order_items` không đổi → POS thấy lệch. POS hủy món nhưng kitchen không biết → chef vẫn nấu.

### 2.1 Kitchen → Order (sau action ở KDS)

**Files mới**:
- `services/kitchen-service/src/main/java/com/irms/kitchen/infrastructure/config/RestTemplateConfig.java` — bean RestTemplate
- `services/kitchen-service/src/main/java/com/irms/kitchen/infrastructure/client/OrderServiceClient.java` — gọi internal endpoint của order

**Files sửa**:
- `KitchenServiceImpl.java`: tách `updateItemStatusInternal(itemId, newStatus, propagateToOrder)`. Public `updateItemStatus` → propagate=true. Sau khi save, gọi `orderServiceClient.syncItemStatus(orderId, menuItemId, mappedStatus)`.
- Mapping: `KITCHEN.PENDING→ORDER.PENDING`, `KITCHEN.COOKING→ORDER.COOKING`, `KITCHEN.READY→ORDER.READY_TO_SERVE`, `KITCHEN.CANCELLED→ORDER.CANCELLED`.

### 2.2 Order → Kitchen (sau action ở POS)

**Files mới**:
- `services/order-service/src/main/java/com/irms/order/infrastructure/client/KitchenSyncClient.java` — gọi internal endpoint của kitchen
- `services/order-service/src/main/java/com/irms/order/controller/InternalSyncController.java` — endpoint internal `PUT /api/v1/internal/orders/{orderId}/items/by-menu/{menuItemId}/status?status=...`
- `services/kitchen-service/src/main/java/com/irms/kitchen/controller/InternalSyncController.java` — endpoint internal `PUT /api/v1/internal/kitchen/orders/{orderId}/menu/{menuItemId}/status?status=...`

**Files sửa**:
- `OrderItemServiceImpl.java`: tách `updateOrderItemStatusInternal(itemId, newStatus, propagateToKitchen)`. Sau khi update, nếu CANCELLED hoặc SERVED → gọi `kitchenSyncClient.syncItemStatus(orderId, menuItemId, status)`.
- `KitchenServiceImpl.syncStatusByMenuItem(orderId, menuItemId, status)` (mới): nhận từ order-service, không propagate ngược.
- `OrderItemServiceImpl.syncStatusByMenuItem(orderId, menuItemId, status)` (mới): nhận từ kitchen-service, không propagate ngược.

**Repository finders mới**:
- `OrderItemRepository.findByOrder_IdAndMenuItemId(orderId, menuItemId)`
- `KitchenTicketItemRepository.findByTicket_OrderIdAndMenuItemId(orderId, menuItemId)`

### 2.3 Loop prevention

| Public endpoint | propagate | Internal endpoint | propagate |
|---|---|---|---|
| `PUT /api/v1/kds/items/{id}/status` | true (→ order) | `PUT /api/v1/internal/orders/{oid}/items/by-menu/{mid}/status` | false |
| `PUT /api/v1/orders/{oid}/items/{iid}/status` | true (→ kitchen) | `PUT /api/v1/internal/kitchen/orders/{oid}/menu/{mid}/status` | false |

Cross-service luôn dùng internal endpoint → flag propagate = false → không loop.

---

## 3. SSE realtime (Server-Sent Events)

Mục tiêu: thay polling 5–10s bằng push event → FE refetch ngay khi BE có thay đổi.

### 3.1 Mỗi service: SseBroadcaster + EventsController

**Files mới** (3 service: order/kitchen/table — code copy/paste, không share lib):

- `infrastructure/sse/SseBroadcaster.java` — `CopyOnWriteArrayList<SseEmitter>`, method `register()` + `broadcast(eventName, payload)` + `@Scheduled(fixedRate = 25000)` heartbeat ping
- `controller/EventsController.java` — endpoint `GET .../events/stream` produces `text/event-stream`

**Path SSE** (đi qua Next rewrite, không cần config thêm):
| Service | URL |
|---|---|
| order-service | `/api/v1/orders/events/stream` |
| kitchen-service | `/api/v1/kitchen/events/stream` |
| table-service | `/api/tables/events/stream` |

### 3.2 Bật `@EnableScheduling`

Cần cho `@Scheduled` heartbeat. Sửa 3 file Application:
- `OrderServiceApplication.java`
- `KitchenServiceApplication.java`
- `TableServiceApplication.java`

```diff
+ import org.springframework.scheduling.annotation.EnableScheduling;
  @SpringBootApplication
+ @EnableScheduling
```

### 3.3 Wire broadcaster vào mọi mutation

| Service | Method | Event |
|---|---|---|
| order | `createOrder` | `order.created` |
| order | `updateOrderStatus` | `order.status` |
| order | `deleteOrder` | `order.deleted` |
| order | `addOrderItem` | `order.itemAdded` |
| order item | `updateOrderItemStatus` | `order.itemStatus` |
| order item | `syncStatusByMenuItem` | `order.itemStatus.sync` |
| kitchen | `createTicket` | `ticket.created` |
| kitchen | `updateItemStatus` | `ticket.itemStatus` |
| kitchen | `syncStatusByMenuItem` | `ticket.itemStatus.sync` |
| table | `createTable` | `table.created` |
| table | `updateTableStatus` | `table.status` |
| table | `seatGuest` | `table.seated` |
| table | `moveTable` | `table.moved` |
| reservation | (mọi save) | `reservation.changed` |
| waitlist | `addToWaitlist`, `seatFromWaitlist`, `removeFromWaitlist`, `notifyGuest` | `waitlist.changed` |

Mỗi event payload là Map nhỏ hoặc DTO đầy đủ — FE chỉ dùng làm trigger refetch, không dùng payload trực tiếp.

---

## 4. Khác

### 4.1 `RestaurantTable` — Hibernate lazy proxy serialization

**File**: `services/table-service/src/main/java/com/irms/table/domain/RestaurantTable.java`

```diff
+ import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
+ @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  public class RestaurantTable extends BaseEntity {
```

**Lý do**: `Reservation.table` là `@ManyToOne(fetch = LAZY)`. Khi serialize qua `GET /api/reservations`, Jackson đụng Hibernate proxy → cố đọc field nội bộ `hibernateLazyInitializer` → 500. Annotation này skip 2 field proxy.

---

## 5. Seed data

**Files mới** (root repo): 6 file SQL để khởi tạo dữ liệu mẫu hệ thống nhà hàng đặt-ăn-tại-chỗ:

- `seed_auth.sql` — 5 roles, 8 permissions, 24 role↔permission, 6 users (admin/manager1/waiter1/waiter2/chef1/cashier1, password chung `123456`)
- `seed_menu.sql` — 6 categories, 27 menu items, 22 allergens
- `seed_table.sql` — 12 bàn (sections A/B/C, status đa dạng), 6 reservations, 3 waitlist
- `seed_order.sql` — 8 orders trải đều mọi trạng thái, 26 order items
- `seed_kitchen.sql` — 6 kitchen tickets khớp với orders, 23 items phân về 5 stations
- `seed_payment.sql` — 4 payments (COMPLETED + REFUNDED + PENDING)

**Cách chạy**:
```bash
docker cp seed_auth.sql archsoft_252-irms-postgres-db-1:/tmp/
docker cp seed_menu.sql archsoft_252-irms-postgres-db-1:/tmp/
docker cp seed_table.sql archsoft_252-irms-postgres-db-1:/tmp/
docker cp seed_order.sql archsoft_252-irms-postgres-db-1:/tmp/
docker cp seed_kitchen.sql archsoft_252-irms-postgres-db-1:/tmp/
docker cp seed_payment.sql archsoft_252-irms-postgres-db-1:/tmp/

for f in seed_auth seed_menu seed_table seed_order seed_kitchen seed_payment; do
  MSYS_NO_PATHCONV=1 docker exec archsoft_252-irms-postgres-db-1 \
    psql -U irms_user -d postgres -v ON_ERROR_STOP=1 -f /tmp/$f.sql
done
```

> Trên Windows: cần `MSYS_NO_PATHCONV=1` để Git Bash không convert `/tmp/...` thành `C:/...`.

---

## 6. Hướng dẫn rebuild

Sau khi pull/apply các file:

```bash
cd archsoft_252-IRMS
docker compose build order-service kitchen-service table-service payment-service
docker compose up -d order-service kitchen-service table-service payment-service
```

Kiểm tra healthy:
```bash
curl http://localhost:8083/api/v1/orders | head -c 100
curl http://localhost:8084/api/v1/kds/tickets | head -c 100
curl http://localhost:8085/api/tables | head -c 100
curl http://localhost:8086/api/v1/payments/aaaaaaaa-1111-0000-0000-000000000001 | head -c 100
```

Test SSE:
```bash
curl -N http://localhost:8085/api/tables/events/stream
# Mở terminal khác, đổi status 1 bàn → terminal đầu sẽ in event
```

---

## 7. Tổng số file thay đổi

| Loại | Số lượng |
|---|---|
| File mới (BE) | 11 |
| File sửa (BE) | 16 |
| File seed SQL | 6 |
| **Tổng** | **33** |

### Danh sách đầy đủ

**order-service** — sửa 7, mới 4:
- M `OrderServiceApplication.java` (+@EnableScheduling)
- M `dto/MenuItemDTO.java`
- M `dto/TableResponseDTO.java`
- M `infrastructure/client/MenuServiceClientImpl.java`
- M `repository/OrderItemRepository.java`
- M `service/OrderItemService.java`
- M `service/OrderItemServiceImpl.java`
- M `service/OrderServiceImpl.java`
- M `validator/OrderStateValidator.java`
- A `controller/EventsController.java`
- A `controller/InternalSyncController.java`
- A `infrastructure/client/KitchenSyncClient.java`
- A `infrastructure/sse/SseBroadcaster.java`

**kitchen-service** — sửa 4, mới 5:
- M `KitchenServiceApplication.java` (+@EnableScheduling)
- M `repository/KitchenTicketItemRepository.java`
- M `service/KitchenService.java`
- M `service/KitchenServiceImpl.java`
- A `controller/EventsController.java`
- A `controller/InternalSyncController.java`
- A `infrastructure/client/OrderServiceClient.java`
- A `infrastructure/config/RestTemplateConfig.java`
- A `infrastructure/sse/SseBroadcaster.java`

**table-service** — sửa 4, mới 2:
- M `TableServiceApplication.java` (+@EnableScheduling)
- M `domain/RestaurantTable.java`
- M `service/TableService.java`
- M `service/ReservationService.java`
- M `service/WaitlistService.java`
- A `controller/EventsController.java`
- A `infrastructure/sse/SseBroadcaster.java`

**payment-service** — sửa 1:
- M `infrastructure/client/OrderServiceClient.java`

**Root** — mới 6 seed SQL.
