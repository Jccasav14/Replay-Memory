param (
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " REPLAY Engine - QA Smoke Test Suite" -ForegroundColor Cyan
Write-Host " Target URL: $BaseUrl" -ForegroundColor Cyan
Write-Host "=========================================="

# 1. Health Check
Write-Host "`n[1/3] Testing Actuator Health Endpoint..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Method Get -TimeoutSec 5
    if ($healthResponse.status -eq "UP") {
        Write-Host "  [PASS] Actuator Health is UP" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] Actuator Health status: $($healthResponse.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  [FAIL] Could not reach $BaseUrl/actuator/health: $_" -ForegroundColor Red
}

# 2. Login Authentication
Write-Host "`n[2/3] Testing QA User Authentication..." -ForegroundColor Yellow
$loginPayload = @{
    email = "demo@replay.app"
    password = "Password123!"
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $loginPayload -ContentType "application/json" -TimeoutSec 5
    $token = $authResponse.data.token
    if ($token) {
        Write-Host "  [PASS] User Login Successful! Token acquired." -ForegroundColor Green
        
        # 3. Protected Memories Endpoint
        Write-Host "`n[3/3] Testing Protected Memories Query..." -ForegroundColor Yellow
        $headers = @{ "Authorization" = "Bearer $token" }
        $memoriesResponse = Invoke-RestMethod -Uri "$BaseUrl/api/memories" -Method Get -Headers $headers -TimeoutSec 5
        Write-Host "  [PASS] Retrieved $($memoriesResponse.data.content.Count) memory records successfully." -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Login succeeded but token was missing." -ForegroundColor Red
    }
} catch {
    Write-Host "  [SKIP/FAIL] Authentication failed or server offline: $_" -ForegroundColor Yellow
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host " QA Smoke Tests Finished" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
