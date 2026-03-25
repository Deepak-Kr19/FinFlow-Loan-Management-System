$rootDir = "c:\Capgemini-Training\All-Capg-Workspaces\Capg-Sprint1-WorkSpace\FinFlow-Microservices"

Write-Host "Replacing text in files..."
$extensions = @("*.java", "pom.xml", "*.yml", "*.md", "Dockerfile", "*.json")
foreach ($ext in $extensions) {
    $files = Get-ChildItem -Path $rootDir -Filter $ext -Recurse -File
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw
        if ($content -match 'com\.finflow') {
            $content = $content -replace 'com\.finflow', 'com.capg'
            Set-Content -Path $file.FullName -Value $content -NoNewline
            Write-Host "Updated $($file.FullName)"
        }
    }
}

Write-Host "Moving directories..."
$dirsToMove = Get-ChildItem -Path $rootDir -Recurse -Directory -Filter "finflow" | Where-Object { $_.FullName -match "\\src\\main\\java\\com\\finflow$" -or $_.FullName -match "\\src\\test\\java\\com\\finflow$" }

foreach ($dir in $dirsToMove) {
    $parentCom = Split-Path $dir.FullName -Parent
    $capgDir = Join-Path $parentCom "capg"
    if (-not (Test-Path $capgDir)) {
        New-Item -ItemType Directory -Path $capgDir | Out-Null
    }
    
    $children = Get-ChildItem -Path $dir.FullName
    foreach ($child in $children) {
        Move-Item -Path $child.FullName -Destination $capgDir -Force
    }
    
    Remove-Item -Path $dir.FullName -Force
    Write-Host "Moved $($dir.FullName) to $capgDir"
}

Write-Host "Refactoring complete."
