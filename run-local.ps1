# 本地启动后端：先从 .env 读取数据库密码、JWT 密钥等环境变量，再运行 Spring Boot。
# 首次使用：复制 .env.example 为 .env，并填入本地真实值（.env 已被 .gitignore 忽略，不会提交）。
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line -match "^([^=]+)=(.*)$") {
            [Environment]::SetEnvironmentVariable($Matches[1].Trim(), $Matches[2].Trim())
        }
    }
    Write-Host "已加载 .env 环境变量" -ForegroundColor Green
} else {
    Write-Warning "未找到 .env：请先复制 .env.example 为 .env 并填写，否则数据库密码/JWT 密钥为空会导致启动后无法登录。"
}

.\mvnw.cmd spring-boot:run
