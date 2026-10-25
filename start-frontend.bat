@echo off
chcp 65001 >nul
setlocal

rem ============================================================
rem Brix Campus Market 前端启动（本地开发态，非部署产物）
rem 首次运行自动 npm install，之后直接 npm run dev（5173）
rem ============================================================

cd /d "%~dp0frontend"

if not exist node_modules (
  echo 首次运行，安装依赖（约 1-2 分钟）...
  call npm install
  if errorlevel 1 (
    echo [失败] npm install 出错，请检查网络或 Node 版本（需 ^>=20.19）。
    pause
    exit /b 1
  )
)

echo 启动开发服务器：http://localhost:5173
echo.
call npm run dev
