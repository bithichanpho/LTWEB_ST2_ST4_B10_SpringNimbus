# JWT_springboot3 — Demo JWT với Spring Boot 3 + Security 6 (CSDL MSSQL)

Bài tập theo bài giảng "Json Web Token" (ThS. Nguyễn Hữu Trung) — Spring Boot 3, Spring Security 6, JWT dùng thư viện **jjwt (io.jsonwebtoken) 0.12.6**. CSDL: **Microsoft SQL Server** (thay cho MySQL của slide).

## 1. Yêu cầu môi trường
- JDK 17+ (đã kiểm thử với JDK 24)
- Maven 3.8+
- SQL Server đang chạy ở `localhost:1433`

## 2. Cấu hình CSDL (MSSQL)
1. Tạo database (SSMS hoặc sqlcmd):

   ```sql
   CREATE DATABASE jwt_springboot3;
   ```

2. Mở `src/main/resources/application.properties`, sửa `username`/`password` theo tài khoản SQL Server của bạn (mặc định đang để `sa` / `123` cho khớp các project khác trên máy):

   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=jwt_springboot3;encrypt=false;trustServerCertificate=true
   ```

3. Bảng `users` sẽ tự được tạo nhờ `spring.jpa.hibernate.ddl-auto=update` khi chạy lần đầu.

## 3. Chạy ứng dụng
```bash
mvn spring-boot:run
```
hoặc mở project bằng IDE (IntelliJ/Eclipse/VS Code) và Run file `JwtSpringboot3Application`.

Ứng dụng chạy tại: http://localhost:8005

## 4. Luồng demo (theo Bước 9–10 của slide)
1. Tạo tài khoản trước bằng API (Postman/curl):

   ```bash
   curl -X POST http://localhost:8005/auth/signup -H "Content-Type: application/json" -d "{\"fullName\":\"Nguyen Van A\",\"email\":\"a@test.com\",\"password\":\"123456\"}"
   ```

2. Đăng nhập: `POST http://localhost:8005/auth/login` với `{"email":"a@test.com","password":"123456"}` → nhận `token`.
3. Mở http://localhost:8005/login → điền email/password → bấm **Login** (AJAX tự lưu token vào `localStorage` và chuyển sang trang profile).
4. Trang http://localhost:8005/user/profile tự gọi `GET /users/me` kèm header `Authorization: Bearer <token>` → hiển thị `fullName` + ảnh.
5. Các endpoint khác: `GET /users` (danh sách user, cần token).

## 5. Cấu trúc mã nguồn (theo cấu trúc của giảng viên)
```
src/main/java/web/programming
├── configs        → ApplicationConfiguration, SecurityConfiguration
├── controllers    → AuthenticationController, UserController, AuthController, GlobalExceptionHandler
├── entity         → User
├── filter         → JwtAuthenticationFilter
├── models         → LoginResponse, LoginUserModel, RegisterUserModel
├── repository     → UserRepository
└── services       → AuthenticationService, JwtService, UserService
src/main/resources
├── static/js      → mainjs.js
├── static/images  → avatar.svg (ảnh mặc định)
├── templates      → login.html, profile.html
├── META-INF       → additional-spring-configuration-metadata.json
└── application.properties
```

## 6. Ghi chú
- Slide gốc dùng package `vn.iotstar`; bản này đổi thành `web.programming` theo yêu cầu của bạn.
- Slide gốc dùng MySQL; bản này dùng SQL Server (dependency `mssql-jdbc`, URL `jdbc:sqlserver://...`).
- Entity có cột `images` là NOT NULL còn API signup mẫu không gửi ảnh → `AuthenticationService.signup()` đã set ảnh mặc định `images/avatar.svg` để đăng ký chạy được. (Muốn bám slide 100%: xóa dòng `user.setImages(...)` và đổi cột sang cho phép NULL.)
- Token hết hạn sau 30 giờ (theo `buildToken()` trong slide); `expiresIn` trả về giá trị `security.jwt.expiration-time`.
