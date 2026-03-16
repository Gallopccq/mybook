param(
	[string]$BaseUrl = 'http://localhost:8081',
	[string]$Username = 'admin',
	[string]$Password = 'admin123',
	[string]$Code = '',
	[string]$Uuid = ''
)

$ErrorActionPreference = 'Stop'

$loginBody = @{
	username = $Username
	password = $Password
	code = $Code
	uuid = $Uuid
} | ConvertTo-Json -Compress

try {
	Write-Host "[1/3] login -> $BaseUrl/login"
	$token = (Invoke-RestMethod -Method Post -Uri "$BaseUrl/login" -ContentType 'application/json' -Body $loginBody).token
	Write-Host "token: $token"

	Write-Host "[2/3] getInfo -> $BaseUrl/getInfo"
	$info = Invoke-RestMethod -Method Get -Uri "$BaseUrl/getInfo" -Headers @{ Authorization = "Bearer $token" }
	$info | ConvertTo-Json -Depth 6

	Write-Host "[3/3] getRouters -> $BaseUrl/getRouters"
	$routers = Invoke-RestMethod -Method Get -Uri "$BaseUrl/getRouters" -Headers @{ Authorization = "Bearer $token" }
	$routers | ConvertTo-Json -Depth 6
}
catch {
	Write-Error "测试失败: $($_.Exception.Message)"
	throw
}

