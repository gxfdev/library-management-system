$base = 'http://localhost:8080/api'

Write-Host "=== Login ==="
$loginBody = @{username='admin';password='admin123'} | ConvertTo-Json -Compress
$r = Invoke-WebRequest -Uri "$base/auth/login" -Method Post -Body $loginBody -ContentType 'application/json' -UseBasicParsing
$json = $r.Content | ConvertFrom-Json
$token = $json.data.token
$headers = @{ Authorization = "Bearer $token" }
Write-Host "   Login OK"

Write-Host ""
Write-Host "=== Test /statistics/category-distribution ==="
try {
    $r2 = Invoke-WebRequest -Uri "$base/statistics/category-distribution" -Headers $headers -UseBasicParsing
    Write-Host "   Status: $($r2.StatusCode)"
    $data = ($r2.Content | ConvertFrom-Json).data
    Write-Host "   Categories: $($data.Count)"
    foreach ($cat in $data) {
        Write-Host "     - $($cat.name): $($cat.count) ($($cat.percentage)%)"
    }
} catch {
    Write-Host "   Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = [System.IO.StreamReader]::new($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response body: $responseBody"
    }
}

Write-Host ""
Write-Host "=== Test /statistics/borrow-trend ==="
try {
    $r3 = Invoke-WebRequest -Uri "$base/statistics/borrow-trend" -Headers $headers -UseBasicParsing
    Write-Host "   Status: $($r3.StatusCode)"
} catch {
    Write-Host "   Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "=== Test /statistics/hot-books ==="
try {
    $r4 = Invoke-WebRequest -Uri "$base/statistics/hot-books" -Headers $headers -UseBasicParsing
    Write-Host "   Status: $($r4.StatusCode)"
} catch {
    Write-Host "   Error: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "=== Test /dashboard/stats ==="
try {
    $r5 = Invoke-WebRequest -Uri "$base/dashboard/stats" -Headers $headers -UseBasicParsing
    Write-Host "   Status: $($r5.StatusCode)"
} catch {
    Write-Host "   Error: $($_.Exception.Message)"
}
