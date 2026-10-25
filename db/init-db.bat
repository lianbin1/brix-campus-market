@echo off
chcp 65001 >nul
setlocal

rem ============================================================
rem Brix Campus Market 数据库初始化（仅本地开发用，不是部署产物）
rem 流程：建库 -> 建应用账号并授权 -> 导入表结构 -> 导入种子数据 -> 行数校验
rem root 密码只在本进程内使用，不写入任何文件。
rem ============================================================

set "MYSQL=C:\Program Files\MySQL\MySQL Server 9.6\bin\mysql.exe"

if not exist "%MYSQL%" (
  echo [错误] 未找到 mysql.exe：%MYSQL%
  echo 请确认已安装 MySQL 9.x，或修改本脚本中的 MYSQL 变量。
  pause
  exit /b 1
)

echo ============================================================
echo  Brix Campus Market 数据库初始化
echo  目标库：brix_campus（utf8mb4）  应用账号：brix / brix123
echo ============================================================
set /p ROOT_PWD=请输入 MySQL root 密码:

echo.
echo [1/5] 建库与应用账号 ...
"%MYSQL%" -u root -p%ROOT_PWD% --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS brix_campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci; CREATE USER IF NOT EXISTS 'brix'@'localhost' IDENTIFIED BY 'brix123'; GRANT ALL PRIVILEGES ON brix_campus.* TO 'brix'@'localhost'; FLUSH PRIVILEGES;"
if errorlevel 1 goto :fail

echo [2/5] 导入表结构 db\schema.sql ...
"%MYSQL%" -u root -p%ROOT_PWD% --default-character-set=utf8mb4 brix_campus < "%~dp0db\schema.sql"
if errorlevel 1 goto :fail

echo [3/5] 导入种子数据 db\data.sql ...
"%MYSQL%" -u root -p%ROOT_PWD% --default-character-set=utf8mb4 brix_campus < "%~dp0db\data.sql"
if errorlevel 1 goto :fail

echo [4/5] 校验行数 ...
"%MYSQL%" -u root -p%ROOT_PWD% --default-character-set=utf8mb4 brix_campus -e "SELECT (SELECT COUNT(*) FROM t_user) AS users, (SELECT COUNT(*) FROM t_category) AS categories, (SELECT COUNT(*) FROM t_item) AS items, (SELECT COUNT(*) FROM t_order) AS orders, (SELECT COUNT(*) FROM t_review) AS reviews;"

echo [5/5] 完成。应用账号 brix / brix123 已就绪。
echo 后端默认读取 DB_USERNAME / DB_PASSWORD（缺省 brix / brix123），无需额外配置。
echo.
echo 测试账号（明文密码）：
echo   student01  / user123456     普通用户（演示主号）
echo   seller01~05 / user123456    5 个卖家（普通用户）
echo   reviewer01 / reviewer123456 审核员
echo   operator01 / operator123456 运营
echo.
pause
exit /b 0

:fail
echo.
echo [失败] 上一步执行出错，请检查 root 密码或 MySQL 服务是否启动。
pause
exit /b 1
