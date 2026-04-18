# Chi tiết Use Case: Menu Service

Tài liệu này đặc tả chi tiết các luồng xử lý cho các Use Case cốt lõi của Menu Service.

## 1. UC_M02: Thêm món mới (Add New Item)

| Thuộc tính | Chi tiết |
|:--- |:--- |
| **ID** | UC_M02 |
| **Tác nhân** | Quản lý (Manager) |
| **Mô tả** | Thêm một món ăn mới vào hệ thống. |
| **Luồng cơ bản** | 1. Chọn "Thêm món".<br>2. Nhập Tên, Giá, Loại, Mô tả.<br>3. Nhấn "Lưu".<br>4. Hệ thống kiểm tra dữ liệu.<br>5. Lưu DB và thông báo thành công. |
| **Ngoại lệ** | Dữ liệu trống hoặc giá âm -> Báo lỗi. |

## 2. UC_M05: Đánh dấu món hết/có lại (Update Availability)

| Thuộc tính | Chi tiết |
|:--- |:--- |
| **ID** | UC_M05 |
| **Tác nhân** | Quản lý (Manager) |
| **Mô tả** | Bật/tắt trạng thái kinh doanh của món ăn (Còn/Hết). |
| **Luồng cơ bản** | 1. Tìm món.<br>2. Chuyển trạng thái Toggle.<br>3. Hệ thống cập nhật DB.<br>4. Đồng bộ trạng thái sang POS/KDS qua Notification Service. |

## 3. UC_M08: Xác thực món (Validate Items)

| Thuộc tính | Chi tiết |
|:--- |:--- |
| **ID** | UC_M08 |
| **Tác nhân** | Order Service (Hệ thống) |
| **Mô tả** | Kiểm tra tính hợp lệ của món ăn khi khách đặt hàng. |
| **Luồng cơ bản** | 1. Nhận yêu cầu từ Order Service.<br>2. Kiểm tra ID và trạng thái `isAvailable` trong DB.<br>3. Trả về thông tin món hoặc báo lỗi nếu hết hàng. |
