# Brix Campus Market 校园二手交易平台

Spring Boot 3 + Vue3 的校园二手交易平台（本地开发态双进程：后端 8080 + 前端 5173）。

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
