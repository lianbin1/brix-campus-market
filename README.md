# Brix Campus Market 校园二手交易平台

Spring Boot 3 + Vue3 的校园二手交易平台（本地开发态双进程：后端 8080 + 前端 5173）。

## 环境要求

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 17+ | 编译 `--release 17`，JDK 21 / 24 运行均可 |
| Maven | 3.9+ | 也可直接用 IDEA 内置 Maven |
| Node.js | 18+ | 推荐 20 / 22 |
| MySQL | 8.0+ | 默认端口 3306 |

## 项目结构

```
brix-campus-market/
├── backend/               # SpringBoot 3.5.3 + MyBatis-Plus 3.5.17（183 个 Java 源文件）
│   └── src/main/java/com/brix/campus/
│       ├── common/        # R / ResultCode / BusinessException / 全局异常
│       ├── config/        # MyBatis-Plus / Security / WebSocket / Jackson
│       ├── controller/    # 用户端 + admin 后台
│       ├── service/       # 业务层（接口 + impl）
│       ├── model/         # entity / query / dto / vo / enums
│       ├── ai/            # LangChain4j 估价智能体 + 规则引擎降级
│       ├── ws/            # 私信 WebSocket
│       └── job/           # 待付款超时关单
├── frontend/              # Vue3 + TypeScript + Vite + Element Plus
│   └── src/{api,components,layout,router,stores,styles,utils,views}
├── db/                    # schema.sql / data.sql / init-db.bat
├── docs/                  # PRD.md（69 条需求）、ARCHITECTURE.md（11 表 DDL + 57 接口）
├── start-backend.bat
└── start-frontend.bat
```

## 功能一览

**用户端**：注册登录、首页筛选搜索、商品详情、发布商品（AI 估价 + 一键文案）、下单与收银台、订单流转（发货 / 收货 / 取消）、面交确认码、评价、收藏、私信（WebSocket 实时）、举报、个人中心。

**管理端**：数据大屏（ECharts 趋势与分布）、商品审核（单条 / 批量 / 拒绝原因）、举报处理、用户管理、订单管理（含锁定单重置与强制取消）、分类管理、私信取证。

**技术要点**：统一响应体 `R{code,msg,data,timestamp}`、业务失败 HTTP 仍 200、分页用 MyBatis-Plus `IPage`、`Long` 序列化为字符串防精度丢失、防超卖用条件更新影响行数、面交码失败 5 次锁定且计数持久化。

## 1. 初始化数据库（双击 `db/init-db.bat`）

输入 MySQL root 密码即可，脚本会自动完成：建库 `brix_campus` → 建应用账号 `brix/brix123` 并授权 → 导入表结构 → 导入种子数据 → 打印行数校验（应显示 users=8、items=36、orders=7）。

## 2. 启动后端（双击 `start-backend.bat`）

自动寻找本机 Maven（`%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.10.0\*\bin\mvn.cmd`），执行 `mvn spring-boot:run`。
看到 `Tomcat started on port 8080` 即成功；首次请求稍慢（建 Druid 连接池）。

- 数据库账号/密码通过环境变量 `DB_USERNAME` / `DB_PASSWORD` 覆盖，缺省 `brix / brix123`（源码中不硬编码）。
- `AI_API_KEY` 留空即走「本地规则估价」，不会报错；填入 Key 则走 LLM。

## 3. 启动前端（双击 `start-frontend.bat`）

首次自动 `npm install`，然后 `npm run dev`。浏览器访问 **http://localhost:5173**（Vite 已代理 `/api`、`/uploads`、`/ws`，无需处理跨域）。

## 测试账号

| 用途 | 用户名 | 密码 |
| --- | --- | --- |
| 普通用户（演示主号，含订单/收藏/私信数据） | `student01` | `user123456` |
| 卖家（阿哲的数码铺） | `seller01` | `user123456` |
| 卖家（悦悦二手书） | `seller02` | `user123456` |
| 卖家（老李杂货） | `seller03` | `user123456` |
| 卖家（阿May） | `seller04` | `user123456` |
| 卖家（篮球少年） | `seller05` | `user123456` |
| 审核员（商品审核 / 举报处理） | `reviewer01` | `reviewer123456` |
| 运营（数据大屏 / 用户 / 订单 / 分类管理） | `operator01` | `operator123456` |

后台入口：`http://localhost:5173/#/admin/login`（普通用户登录会被拒绝）。

> 演示数据里预置了一笔**待面交订单**（确认码 `042317`，用 student01 购买、seller05 核销）和一笔**待付款订单**（可体验收银台倒计时）。
