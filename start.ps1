$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "检查端口占用..."
$backendRunning = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
$frontendRunning = Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue

if ($backendRunning) {
    Write-Host "后端已在运行：http://localhost:8080"
} else {
    Write-Host "正在启动后端（首次启动需要下载依赖，请耐心等待）..."
    Start-Process -FilePath "mvn.cmd" -ArgumentList "spring-boot:run" -WorkingDirectory (Join-Path $root "backend")
}

if ($frontendRunning) {
    Write-Host "前端已在运行：http://localhost:5173"
} else {
    if (-not (Test-Path (Join-Path $root "frontend\node_modules"))) {
        Write-Host "正在安装前端依赖..."
        Start-Process -FilePath "npm.cmd" -ArgumentList "install" -WorkingDirectory (Join-Path $root "frontend") -Wait
    }
    Write-Host "正在启动前端..."
    Start-Process -FilePath "npm.cmd" -ArgumentList "run", "dev" -WorkingDirectory (Join-Path $root "frontend")
}

Start-Sleep -Seconds 5
Write-Host ""
Write-Host "启动完成："
Write-Host "  前端：http://localhost:5173"
Write-Host "  后端：http://localhost:8080"
Write-Host "  账号：admin / teacher1 / student1，密码均为 123456"
