# User Center 用户中心

前后端分离的 Java 用户中心示例，聚焦常见后端工程能力：用户注册登录、JWT 鉴权、个人资料、密码管理、邮箱验证、头像上传与操作日志。

## 技术栈

### 后端

- Java 21
- Spring Boot 4.0.7
- Spring Web MVC
- MyBatis-Plus 3.5.17
- MySQL
- JJWT 0.12.6
- PBKDF2-HMAC-SHA256

### 前端

- React 19
- Umi Max 4
- Ant Design 6
- TypeScript
- Vitest

## 核心功能

- 用户注册、登录、退出
- JWT Bearer Token 鉴权
- 当前用户查询与资料修改
- 密码修改 / 重置
- 邮箱验证码
- 头像上传
- 操作日志
- 登录失败限制与 Token 失效处理
- MyBatis-Plus 数据访问

## 安全说明

仓库不包含可用的数据库密码、JWT secret 或其他运行凭据。

运行时通过环境变量注入敏感配置：

```text
DATABASE_URL=jdbc:mysql://localhost:3306/chenyouxin
DATABASE_USERNAME=root
DATABASE_PASSWORD=your-password
JWT_SECRET=replace-with-a-random-secret
JWT_EXPIRATION=86400000
FILE_UPLOAD_DIR=uploads/avatars
```

生产环境请使用环境变量、密钥管理服务或平台 Secret，不要把真实凭据写入源码或 README。

如果历史提交曾包含真实凭据，应在删除文件内容后继续检查 Git 历史，并轮换相关凭据。

## 本地启动

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS chenyouxin
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 2. 设置环境变量

PowerShell：

```powershell
$env:DATABASE_URL="jdbc:mysql://localhost:3306/chenyouxin"
$env:DATABASE_USERNAME="root"
$env:DATABASE_PASSWORD="你的数据库密码"
$env:JWT_SECRET="随机生成的高强度密钥"
```

### 3. 启动后端

```powershell
.\mvnw.cmd spring-boot:run
```

默认监听：

```text
http://localhost:8080
```

### 4. 启动前端

```bash
npm install
npm run dev
```

前端开发服务器会将 `/api` 请求代理到后端。

## API

统一前缀：`/api`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/users/register` | 用户注册 |
| POST | `/api/users/login` | 用户登录 |
| GET | `/api/users/me` | 当前用户 |
| POST | `/api/users/logout` | 退出登录 |
| PUT | `/api/users/{id}` | 修改本人资料 |
| PUT | `/api/users/{id}/password` | 修改本人密码 |
| POST | `/api/users/password/reset` | 密码重置 |
| POST | `/api/users/avatar` | 上传头像 |
| POST | `/api/users/email/verify/send` | 发送验证码 |
| POST | `/api/users/email/verify` | 校验验证码 |

除公共接口外，其余接口使用：

```http
Authorization: Bearer <JWT>
```

## 数据模型

主要数据表：

- `user`：用户信息、密码摘要、状态与时间字段
- `operation_log`：登录、退出、资料修改、密码修改等操作记录

SQL 脚本位于 `src/main/resources/sql/`。

## 测试与构建

后端：

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

前端：

```bash
npm test
npm run build
```

## 项目定位

这是一个用于展示 Java 后端基础工程能力的练习项目，与 ChenManus 的 AI Agent Runtime 项目形成互补：前者侧重认证、数据访问与业务接口，后者侧重 AI Agent 编排、任务运行时与分布式能力。

## License

本项目用于学习与作品展示。
