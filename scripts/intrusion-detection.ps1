#!/usr/bin/env pwsh

# Real-time Intrusion Detection System
$logFile = "logs/security.log"
$alertThreshold = 10
$timeWindow = 300  # 5 minutes

function Monitor-Attacks {
    while ($true) {
        $recentAttacks = Get-Content $logFile -ErrorAction SilentlyContinue | Select-String "ATTACK" | 
            Where-Object { $_.Line -match (Get-Date).AddMinutes(-5).ToString("yyyy-MM-dd HH:mm") }
        
        if ($recentAttacks.Count -gt $alertThreshold) {
            Send-Alert "ALERT: Potential attack detected: $($recentAttacks.Count) attacks in last 5 minutes"
        }
        
        Start-Sleep 30
    }
}

function Send-Alert($message) {
    Write-Host $message -ForegroundColor Red
    New-Item -ItemType Directory -Force -Path "logs" | Out-Null
    Add-Content -Path "logs/critical-alerts.log" -Value "$(Get-Date): $message"
}

Monitor-Attacks
