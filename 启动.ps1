# 卷积核微课 - 一键启动脚本
Write-Host "🚀 正在启动卷积核微课全栈服务..." -ForegroundColor Cyan

# 1. 清理端口 8000 占用
Write-Host "`n[1/3] 检查并清理端口 8000..." -ForegroundColor Yellow
$port8000 = netstat -ano | Select-String ":8000\s" | ForEach-Object { $_.Line -split '\s+' | Where-Object { $_ -match '^\d+$' } | Select-Object -Last 1 }
if ($port8000) {
    Write-Host "    发现占用进程 PID: $port8000，正在终止..." -ForegroundColor Red
    taskkill /PID $port8000 /F 2>$null
    Start-Sleep -Milliseconds 500
}

# 2. 启动后端 FastAPI（在新窗口运行）
Write-Host "`n[2/3] 启动后端服务 (端口 8000)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; python main.py" -WindowStyle Normal

# 3. 等待后端启动，然后启动前端
Write-Host "`n[3/3] 启动前端服务 (端口 5173)..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

Write-Host "`n✅ 服务启动中！" -ForegroundColor Green
Write-Host "   前端: http://localhost:5173" -ForegroundColor Cyan
Write-Host "   后端: http://localhost:8000" -ForegroundColor Cyan
Write-Host "`n📌 关闭两个 PowerShell 窗口即可停止所有服务" -ForegroundColor Gray
Write-Host "=" * 60 -ForegroundColor Gray

# 启动前端（前台运行）
Set-Location frontend
npm run dev
