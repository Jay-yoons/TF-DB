$ErrorActionPreference = 'Stop'

function Start-Console {
	param(
		[string]$Title,
		[string]$Command
	)
	Start-Process -FilePath powershell -ArgumentList @('-NoExit', '-Command', "`$host.UI.RawUI.WindowTitle='$Title'; $Command")
}

# 1) store-service (8080)
Start-Console -Title 'store-service :8080' -Command "cd 'store-service'; ./gradlew.bat bootRun"

# 2) review-service (8081)
Start-Console -Title 'review-service :8081' -Command "cd 'review-service'; ./gradlew.bat bootRun"

# 3) frontend (5173) - install if needed, then dev
$frontendPath = 'store-service/frontend-sample'
$npmInstallCmd = "if (!(Test-Path node_modules)) { npm install }; npm run dev"
Start-Console -Title 'frontend-sample :5173' -Command "cd '$frontendPath'; $npmInstallCmd"

# 4) Open browser tabs
Start-Process 'http://localhost:5173'
Start-Process 'http://localhost:8080/api/stores'

Write-Host "Launched: store-service(8080), review-service(8081), frontend(5173)."

