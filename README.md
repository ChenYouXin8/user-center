# User Center 用户中心

这是一个前后端分离的用户中心项目，提供用户注册、登录、JWT 鉴权、个人信息查询、退出登录、密码修改、密码重置、头像上传、邮箱验证和操作日志等能力。

项目由两个独立 Git 仓库组成：

- **后端**：`D:\projectt\user-center`
- **前端**：`D:\programmer\antpro`

> 本 README 以 Windows PowerShell 为例。Linux/macOS 用户将 `./mvnw.cmd` 替换为 `./mvnw`，路径命令按实际目录调整。

## 1. 技术栈

### 后端

- Java 21
- Spring Boot 4.0.7
- Spring Web MVC
- MyBatis-Plus 3.5.17
- MySQL Connector/J
- JJWT 0.12.6
- Maven Wrapper
- PBKDF2-HMAC-SHA256 密码摘要

### 前端

- Node.js >= 22
- React 19
- Umi Max 4
- Ant Design 6
- Ant Design Pro Components
- TypeScript
- Vitest

## 2. 环境要求

启动前请准备：

1. **Java 21**，确认：

   ```powershell
   java -version
   ```

2. **MySQL 8.0 或更高版本**，确认 MySQL 服务已经启动。
3. **Node.js 22 或更高版本**，确认：

   ```powershell
   node --version
   npm --version
   ```

4. 能够访问 Maven/npm 依赖源，或者使用 IntelliJ IDEA 已配置好的 Maven。

## 3. 第一次配置 MySQL

当前后端默认连接配置位于：

```text
D:\projectt\user-center\src\main\resources\application.yml
```

默认值如下：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chenyouxin
    username: root
    password: 1442411359abc
```

### 3.1 创建数据库

在 MySQL 客户端、DataGrip 或 IntelliJ IDEA 的 Database 窗口中执行：

```sql
CREATE DATABASE IF NOT EXISTS `chenyouxin`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

如果你的 MySQL 版本不支持 `utf8mb4_0900_ai_ci`，可以改用：

```sql
CREATE DATABASE IF NOT EXISTS `chenyouxin`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3.2 新数据库执行 SQL

新环境按以下顺序执行：

1. `src/main/resources/sql/user-registration.sql`
2. `src/main/resources/sql/operation-log.sql`

PowerShell 中也可以使用 MySQL 命令行：

```powershell
mysql -u root -p chenyouxin < .\src\main\resources\sql\user-registration.sql
mysql -u root -p chenyouxin < .\src\main\resources\sql\operation-log.sql
```

### 3.3 已有数据库执行 SQL

如果数据库中已经存在 `user` 表，不要只执行建表脚本。请先检查用户名和邮箱是否重复：

```sql
SELECT username, COUNT(*)
FROM `user`
GROUP BY username
HAVING COUNT(*) > 1;

SELECT email, COUNT(*)
FROM `user`
GROUP BY email
HAVING COUNT(*) > 1;
```

确认两个查询都没有结果后，再执行：

1. `src/main/resources/sql/migration-user-schema.sql`
2. `src/main/resources/sql/operation-log.sql`

`migration-user-schema.sql` 会修改字段定义并增加唯一索引，重复执行前请先确认索引是否已经存在。

### 3.4 数据表说明

#### `user`

主要字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 用户 ID，自增主键 |
| `username` | 用户名，唯一，最长 64 个字符 |
| `email` | 邮箱，唯一 |
| `password` | PBKDF2 密码摘要，不保存明文密码 |
| `avatarUrl` | 头像地址 |
| `gender` | 性别 |
| `phone` | 手机号 |
| `isValid` | 用户是否有效 |
| `isDelete` | 逻辑删除标记 |
| `createTime` / `updateTime` | 创建和更新时间 |

#### `operation_log`

记录登录、退出、修改资料、删除用户、修改密码等操作，当前实现使用 MySQL 表保存。

## 4. 后端配置

配置文件：

```text
D:\projectt\user-center\src\main\resources\application.yml
```

完整关键配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chenyouxin
    username: root
    password: 1442411359abc
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

jwt:
  secret: mySecretKeyForJwtTokenGenerationAndValidation123456
  expiration: 86400000

file:
  upload-dir: uploads/avatars
```

### 配置其他数据库

可以直接修改 `application.yml`，也可以在启动时通过 Spring 参数覆盖：

```powershell
.\mvnw.cmd spring-boot:run `
  "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:mysql://localhost:3306/chenyouxin;--spring.datasource.username=root;--spring.datasource.password=你的密码"
```

也可以在 IntelliJ IDEA 的 Run/Debug Configuration 中添加 VM options 或 Program arguments。

### 生产环境安全要求

当前配置中的数据库密码和 JWT secret 只是本地开发默认值，**不要直接用于生产环境**。生产部署至少应：

- 通过环境变量、密钥管理服务或启动参数注入数据库密码。
- 更换长度足够且随机的 JWT secret。
- 关闭或重写“重置密码直接返回新密码”的开发逻辑。
- 邮箱验证码应通过真实邮件服务发送，不应在接口响应中返回验证码。
- Token 黑名单当前保存在单机内存中，多实例部署应改为 Redis 等共享存储。
- 上传目录应配置访问权限，并校验图片真实格式、扩展名和文件内容。

## 5. 启动后端

### 5.1 使用 Maven Wrapper

打开 PowerShell：

```powershell
cd D:\projectt\user-center
.\mvnw.cmd spring-boot:run
```

首次执行可能会下载 Maven 和项目依赖，需要等待一段时间。

### 5.2 使用 IntelliJ IDEA

1. 使用 IDEA 打开 `D:\projectt\user-center`。
2. 等待 Maven 项目导入完成。
3. 确认 Project SDK 为 Java 21。
4. 运行主类：

   ```text
   io.github.chenyouxin8.usercenter.UserCenterApplication
   ```

5. 后端默认监听：

   ```text
   http://localhost:8080
   ```

### 5.3 使用已安装 Maven

如果电脑已经安装 Maven：

```powershell
cd D:\projectt\user-center
mvn spring-boot:run
```

如果 `mvnw.cmd` 报 Maven 下载或 Wrapper 启动错误，优先使用 IDEA 运行主类，或者安装 Maven 后执行上面的 `mvn spring-boot:run`。

## 6. 启动前端

打开另一个 PowerShell 窗口：

```powershell
cd D:\programmer\antpro
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:8000
```

常用页面：

- 登录：`http://localhost:8000/user/login`
- 注册：`http://localhost:8000/user/register`
- 注册结果：`http://localhost:8000/user/register-result`

首次安装依赖时建议使用 Node.js 22 或更高版本；项目当前 `package.json` 已声明 `node >= 22.0.0`。

## 7. 前后端代理关系

开发环境前端代理配置位于：

```text
D:\programmer\antpro\config\proxy.ts
```

所有 `/api/` 请求会从前端开发服务器代理到后端：

```text
http://localhost:8000/api/*  ->  http://localhost:8080/api/*
```

因此前端代码请求 `/api/users/login` 时，不需要把后端地址写进页面代码，也不会产生本地开发时的跨域问题。

> Umi 开发代理只在开发服务器生效。执行 `npm run build` 后，生产环境需要由 Nginx、网关或部署平台配置 `/api` 反向代理，或者改为使用正式后端地址。

## 8. 登录与 JWT 流程

1. 注册页面提交用户名、邮箱、密码和确认密码。
2. 登录页面调用 `POST /api/users/login`。
3. 登录成功后，后端在 `data.token` 返回 JWT。
4. 前端根据“自动登录”选项，将 Token 保存到 `localStorage` 或 `sessionStorage`。
5. 后续 API 请求自动添加：

   ```http
   Authorization: Bearer <JWT>
   ```

6. 前端启动或刷新页面时调用 `GET /api/users/me` 获取当前用户。
7. 如果收到 401、Token 已过期、Token 已退出或 Token 格式无效，前端会清理本地 Token，并跳转到登录页。
8. 调用 `POST /api/users/logout` 后，后端将当前 Token 加入黑名单，前端同时清理本地登录状态。

## 9. API 接口

统一前缀：`/api`

### 9.1 公共接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/users/register` | 注册用户 |
| POST | `/api/users/login` | 用户名密码登录 |
| POST | `/api/users/password/reset` | 根据邮箱重置密码 |
| GET | `/api/files/avatars/{filename}` | 获取头像文件 |

#### 注册请求示例

```json
{
  "username": "demo",
  "email": "demo@example.com",
  "password": "Password123",
  "confirmPassword": "Password123"
}
```

#### 登录请求示例

```json
{
  "username": "demo",
  "password": "Password123"
}
```

登录成功响应示例：

```json
{
  "success": true,
  "code": 0,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "demo",
    "email": "demo@example.com",
    "avatarUrl": null,
    "token": "eyJ..."
  }
}
```

### 9.2 需要 JWT 的接口

除公共接口外，以下接口均需要请求头：

```http
Authorization: Bearer <JWT>
```

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/users/me` | 查询当前登录用户 |
| POST | `/api/users/logout` | 退出登录并使 Token 失效 |
| GET | `/api/users/{id}` | 查询指定用户 |
| GET | `/api/users?pageNum=1&pageSize=10` | 分页查询用户 |
| PUT | `/api/users/{id}` | 修改用户资料，仅允许修改本人 |
| DELETE | `/api/users/{id}` | 逻辑删除用户，仅允许删除本人 |
| PUT | `/api/users/{id}/password` | 修改本人密码 |
| POST | `/api/users/avatar` | 上传本人头像，字段名为 `file` |
| POST | `/api/users/email/verify/send` | 生成邮箱验证码 |
| POST | `/api/users/email/verify` | 校验邮箱验证码 |

#### 查询当前用户

```powershell
curl.exe http://localhost:8080/api/users/me `
  -H "Authorization: Bearer 你的JWT"
```

#### 上传头像

```powershell
curl.exe -X POST http://localhost:8080/api/users/avatar `
  -H "Authorization: Bearer 你的JWT" `
  -F "file=@C:\path\to\avatar.png"
```

### 9.3 常见 HTTP 状态码

| 状态码 | 含义 |
| --- | --- |
| 200 | 请求成功 |
| 201 | 注册成功并创建用户 |
| 400 | 请求参数错误 |
| 401 | 未登录、Token 无效或已过期 |
| 403 | 无权操作其他用户 |
| 404 | 用户或资源不存在 |
| 409 | 用户名或邮箱已存在 |
| 413 | 上传文件超过限制 |
| 429 | 登录失败次数过多，账号暂时锁定 |
| 500 | 服务器内部错误 |

## 10. 测试、检查和构建

### 后端

```powershell
cd D:\projectt\user-center
.\mvnw.cmd test
.\mvnw.cmd clean package
```

也可以使用 IDEA 的 Maven 面板执行 `test` 和 `package`。

### 前端

```powershell
cd D:\programmer\antpro
npm run tsc
npm run biome:lint
npm test
npm run build
```

其他常用命令：

```powershell
npm run lint          # Biome + TypeScript
npm run test:coverage # 生成测试覆盖率
npm run preview:build # 构建后启动预览服务
```

## 11. 项目目录

### 后端

```text
D:\projectt\user-center
├─ src/main/java/io/github/chenyouxin8/usercenter
│  ├─ config          # MyBatis、MVC、跨域和拦截器配置
│  ├─ controller      # 用户和文件接口
│  ├─ dto             # 请求和响应对象
│  ├─ entity          # user、operation_log 实体
│  ├─ exception       # 业务异常和统一异常处理
│  ├─ interceptor     # JWT 鉴权拦截器
│  ├─ mapper          # MyBatis-Plus Mapper
│  ├─ service         # 用户、密码、Token、日志等服务
│  └─ util            # JWT 工具类
├─ src/main/resources
│  ├─ application.yml
│  └─ sql             # 建表和迁移脚本
├─ pom.xml
└─ mvnw.cmd
```

### 前端

```text
D:\programmer\antpro
├─ config/proxy.ts                  # /api 代理到后端 8080
├─ src/pages/user/login             # 登录页面
├─ src/pages/user/register          # 注册页面
├─ src/pages/user/components        # 登录注册公共布局
├─ src/services/ant-design-pro/api.ts # 用户接口适配
├─ src/utils/auth.ts                # Token 保存、读取和清理
├─ src/requestErrorConfig.ts        # 请求拦截和 401 处理
└─ package.json
```

## 12. 常见问题

### Q1：打开前端页面提示请求失败

按以下顺序检查：

1. 后端是否已启动并监听 `8080`。
2. MySQL 是否启动。
3. `application.yml` 中数据库名称、用户名和密码是否正确。
4. `chenyouxin` 数据库是否已经执行建表脚本。
5. 前端是否通过 `npm run dev` 启动，而不是直接双击 HTML 文件。

### Q2：返回 401

确认：

- 是否先登录并拿到了 `data.token`。
- 请求头是否为 `Authorization: Bearer <JWT>`。
- Token 是否过期。
- 是否已经调用过退出登录，导致 Token 被加入黑名单。
- 前端浏览器是否清除了站点存储。

### Q3：注册时报用户名或邮箱已存在

这是后端唯一索引和业务校验的正常结果。换一个用户名或邮箱即可；如果是迁移旧库，请先检查重复数据。

### Q4：Maven Wrapper 无法启动

可以尝试：

```powershell
.\mvnw.cmd -version
```

如果 Wrapper 需要下载 Maven 但当前网络不可用：

- 使用 IntelliJ IDEA 直接运行 `UserCenterApplication`；或
- 安装 Maven 后使用 `mvn spring-boot:run`；或
- 配置可访问 Maven Central 的镜像后再执行 Wrapper。

### Q5：生产环境为什么前端请求不到后端

`config/proxy.ts` 只服务于 Umi 开发服务器。生产部署必须在 Nginx、网关或云平台中配置：

```text
/api/* -> 后端服务地址
```

## 13. Git 提交说明

这是两个独立仓库，需要分别执行 Git 命令：

```powershell
cd D:\projectt\user-center
git status
git log -1 --oneline

cd D:\programmer\antpro
git status
git log -1 --oneline
```

本地 Maven 缓存、上传目录和调试错误日志不应提交到 Git。
