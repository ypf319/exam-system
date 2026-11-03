# 在线考试与题库管理系统

基于课程设计选题 K.4B「在线考试与题库管理」实现的 B/S 架构在线考试系统，覆盖题库维护、组卷、在线考试与成绩管理四大核心环节。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.5.6、MyBatis 3.0.4、PageHelper 2.1.0 |
| 数据库 | MySQL 8 |
| 前端 | Vue 2.7、ElementUI 2.15、Vue Router 3、Axios、Vite 5 |
| 构建 | Maven 3.6+、Node.js 18+ |
| 其他 | Lombok、Apache POI（Excel 导入导出） |

> 说明：原设计方案使用 Spring Boot 2.7，由于开发机为 JDK 25，Spring Boot 2.7 无法运行，因此升级到 Spring Boot 3.5.6。业务功能与设计方案保持一致。

## 目录结构

```text
exam-system/
├── backend/                    后端工程
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/exam/
│       │   ├── common/         统一响应、异常处理
│       │   ├── config/         Web 配置、跨域
│       │   ├── controller/     接口层
│       │   ├── dto/            请求参数对象
│       │   ├── entity/         实体类
│       │   ├── interceptor/    登录与角色权限拦截器
│       │   ├── mapper/         MyBatis Mapper 接口
│       │   ├── service/        业务层
│       │   ├── util/           MD5、判分工具
│       │   └── vo/             视图对象
│       └── resources/
│           ├── application.yml
│           └── mapper/         MyBatis XML
├── frontend/                   前端工程
│   └── src/
│       ├── api/                接口封装
│       ├── router/             路由与权限守卫
│       ├── utils/              登录状态工具
│       └── views/
│           ├── admin/          管理员页面
│           ├── teacher/        教师页面
│           └── student/        学生页面
├── sql/exam_system.sql         建库建表与初始化数据
└── start.ps1                   一键启动脚本
```

## 环境要求

- JDK 17 及以上（当前已在 JDK 25 验证）
- Maven 3.6+
- MySQL 8
- Node.js 18+（当前已在 Node 24 验证）

## 数据库初始化

1. 确认 MySQL 已启动。
2. 数据库连接支持通过环境变量覆盖（默认值 `root / root`）：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的数据库密码"
# 可选：$env:DB_URL = "jdbc:mysql://localhost:3306/exam_system?..."
```

3. 执行初始化脚本：

```powershell
mysql -uroot -proot --default-character-set=utf8mb4 -e "source D:/Projects/exam-system/sql/exam_system.sql"
```

脚本会创建数据库 `exam_system`、6 张数据表，并写入 3 门课程、15 道题目、1 套示例试卷及演示账号。

## 启动项目

### 方式一：一键启动

```powershell
powershell -ExecutionPolicy Bypass -File D:\Projects\exam-system\start.ps1
```

### 方式二：分别启动

后端：

```powershell
cd D:\Projects\exam-system\backend
mvn spring-boot:run
```

前端：

```powershell
cd D:\Projects\exam-system\frontend
npm install
npm run dev
```

启动后访问：<http://localhost:5173>

后端接口地址：<http://localhost:8080/api>

### 生产打包

```powershell
cd frontend
npm run build
cd ..\backend
mvn clean package
```

可将 `frontend/dist` 内容复制到 `backend/src/main/resources/static` 后重新打包，由 Spring Boot 统一托管前端页面。

## 演示账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | admin | 123456 |
| 教师 | teacher1 / teacher2 | 123456 |
| 学生 | student1 / student2 / student3 | 123456 |

## 功能清单

### 管理员

- 教师、学生账号的新增、编辑、启用/禁用、删除、重置密码
- 课程信息增删改查

### 教师

- 题库管理：单选、多选、判断题增删改查，按课程/题型/难度/关键词筛选
- 批量导入：下载 Excel 模板并按格式批量导入题目
- 自动组卷：按课程、题型数量、难度随机抽题，题目不足给出提示
- 手动组卷：从题库勾选题目生成试卷
- 试卷管理：设置总分（按题目分值汇总）、时长、考试时间，启用/关闭试卷
- 成绩管理：查看成绩列表、最高分/最低分/平均分/及格率统计、答卷详情、导出 Excel

### 学生

- 查看处于考试时间范围内的试卷
- 在线答题，倒计时、时间到自动交卷、离开页面提醒
- 提交后客观题自动判分并即时显示成绩
- 查询个人历史成绩与答卷详情（含正确答案对比）

## 核心设计说明

- **权限控制**：基于 Session 的登录拦截器，配合 `@RequireRole` 注解完成角色权限校验，未登录返回 401，越权返回 403。
- **密码存储**：MD5 加密后入库。
- **自动判分**：单选、判断题答案完全一致得分；多选题答案排序后与标准答案集合比较，完全一致才得分。
- **超时控制**：前端按时长倒计时并自动交卷；后端提交时二次校验试卷截止时间，超过截止时间拒绝提交。
- **随机组卷**：使用 `ORDER BY RAND() LIMIT` 按题型和难度抽题，抽题前校验题库数量。
- **成绩导出**：使用 Apache POI 生成 xlsx 文件，文件名支持中文。

## 主要接口

| 模块 | 接口 |
| --- | --- |
| 认证 | `POST /api/auth/login`、`POST /api/auth/logout`、`GET /api/auth/me` |
| 用户 | `GET/POST /api/users`、`PUT /api/users/{id}`、`PUT /api/users/{id}/status` |
| 课程 | `GET /api/courses`、`GET /api/courses/page`、`POST/PUT/DELETE /api/courses` |
| 题库 | `GET/POST /api/questions`、`PUT/DELETE /api/questions/{id}`、`POST /api/questions/import` |
| 试卷 | `GET/POST /api/papers`、`POST /api/papers/manual`、`POST /api/papers/auto` |
| 考试 | `GET /api/exam/available`、`POST /api/exam/{paperId}/start`、`POST /api/exam/{paperId}/submit` |
| 成绩 | `GET /api/records/my`、`GET /api/records`、`GET /api/records/statistics`、`GET /api/records/export` |

## 注意事项

- 首次启动 Maven 需要下载依赖，耗时取决于网络，请耐心等待。
- 若修改了数据库密码，请同步修改 `application.yml`。
- 后端默认端口 8080，前端开发端口 5173，前端通过 Vite 代理访问 `/api`。
- 数据库初始化脚本会删除并重建 6 张业务表，请勿在已有生产数据的环境中执行。
