# Hướng dẫn Tính năng Quản lý Người dùng

## Tổng quan
Tính năng này cho phép Admin quản lý người dùng hệ thống, bao gồm:
- Xem danh sách tất cả người dùng có role USER
- Chặn/mở chặn người dùng
- Kiểm tra trạng thái khi đăng nhập

## Backend Changes

### 1. User Entity
Đã thêm cột `status` với enum `UserStatus`:
- `ACTIVE`: Người dùng hoạt động bình thường
- `BANNED`: Người dùng bị chặn

File: `/hls-server/src/main/java/com/rin/hlsserver/model/User.java`

### 2. UserService
Các phương thức mới:
- `getAllRegularUsers()`: Lấy danh sách user (không bao gồm ADMIN)
- `banUser(Long userId)`: Chặn người dùng
- `unbanUser(Long userId)`: Mở chặn người dùng

File: `/hls-server/src/main/java/com/rin/hlsserver/service/UserService.java`

### 3. UserController
Các endpoint mới (chỉ ADMIN truy cập):
- `GET /users`: Lấy danh sách tất cả user
- `PUT /users/{id}/ban`: Chặn user
- `PUT /users/{id}/unban`: Mở chặn user

File: `/hls-server/src/main/java/com/rin/hlsserver/controller/UserController.java`

### 4. AuthService
Đã cập nhật logic login để kiểm tra trạng thái BANNED. Nếu user bị chặn, sẽ không cho phép đăng nhập.

File: `/hls-server/src/main/java/com/rin/hlsserver/service/AuthService.java`

### 5. Database Migration
File SQL để thêm cột status vào bảng users:

File: `/hls-server/src/main/resources/db/migration/add_user_status.sql`

## Frontend Changes

### 1. FormUserManagement
Giao diện quản lý người dùng cho Admin với:
- Bảng hiển thị danh sách user
- Cột trạng thái có màu sắc (xanh=hoạt động, đỏ=bị chặn)
- Nút Chặn/Mở chặn tương ứng với trạng thái hiện tại
- Confirm dialog trước khi thực hiện action

File: `/client-desktop/src/main/java/raven/modal/demo/forms/admin/FormUserManagement.java`

### 2. UserApi
API client để gọi các endpoint user management:

File: `/client-desktop/src/main/java/raven/modal/demo/api/UserApi.java`

### 3. Admin Menu
Đã thêm menu "Quản lý người dùng" vào menu Admin.

File: `/client-desktop/src/main/java/raven/modal/demo/menu/MyDrawerBuilder.java`

## Cách sử dụng

### Bước 1: Chạy Migration
Khi khởi động server, migration sẽ tự động chạy và thêm cột `status` vào bảng `users`.

### Bước 2: Test Backend
```bash
# Login với tài khoản admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password"}'

# Lấy danh sách users (cần token)
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer YOUR_TOKEN"

# Chặn user id=2
curl -X PUT http://localhost:8080/users/2/ban \
  -H "Authorization: Bearer YOUR_TOKEN"

# Mở chặn user id=2
curl -X PUT http://localhost:8080/users/2/unban \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Bước 3: Test Frontend
1. Đăng nhập bằng tài khoản ADMIN
2. Vào menu "Quản lý người dùng"
3. Xem danh sách users
4. Click nút "Chặn" để chặn user
5. Click nút "Mở chặn" để mở chặn user

### Bước 4: Test Login với User bị chặn
1. Chặn một user từ admin panel
2. Thử đăng nhập bằng user đó
3. Sẽ nhận được lỗi: "Your account has been banned. Please contact administrator."

## Lưu ý

### Security
- Chỉ tài khoản có role ADMIN mới có quyền truy cập các endpoint user management
- Không thể chặn chính mình (có thể thêm validation này nếu cần)
- User bị chặn không thể login vào hệ thống

### Database
- Mặc định tất cả user mới có status là `ACTIVE`
- Migration tự động set status cho các user hiện có thành `ACTIVE`

### UI/UX
- Trạng thái được hiển thị bằng màu sắc trực quan
- Có confirm dialog trước khi thực hiện chặn/mở chặn
- Toast notification sau khi thực hiện thành công
- Auto refresh danh sách sau khi cập nhật

## Troubleshooting

### Lỗi "User not found"
- Kiểm tra ID user có tồn tại không
- Đảm bảo user không phải là ADMIN (chỉ list user có role USER)

### Lỗi 403 Forbidden
- Kiểm tra token JWT có hợp lệ không
- Đảm bảo tài khoản đang login có role ADMIN

### Lỗi database migration
- Nếu migration fail, có thể chạy manual SQL trong file `add_user_status.sql`
- Kiểm tra xem cột `status` đã tồn tại chưa

## Mở rộng tương lai
- Thêm lý do chặn (ban reason)
- Thêm thời gian chặn tạm thời (ban duration)
- Log lịch sử chặn/mở chặn
- Gửi email thông báo khi bị chặn
- Thống kê số user bị chặn
