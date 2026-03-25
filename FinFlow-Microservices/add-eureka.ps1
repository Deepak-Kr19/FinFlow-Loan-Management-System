$rootDir = "c:\Capgemini-Training\All-Capg-Workspaces\Capg-Sprint1-WorkSpace\FinFlow-Microservices"
$services = @("api-gateway", "auth-service", "application-service", "document-service", "admin-service")

foreach ($service in $services) {
    # 1. Update POM
    $pomPath = Join-Path $rootDir $service "pom.xml"
    $pom = Get-Content $pomPath -Raw
    if ($pom -notmatch "spring-cloud-starter-netflix-eureka-client") {
        $dependency = "`n`t`t<dependency>`n`t`t`t<groupId>org.springframework.cloud</groupId>`n`t`t`t<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>`n`t`t</dependency>`n"
        $pom = $pom -replace "</dependencies>", "$dependency`t</dependencies>"
        Set-Content -Path $pomPath -Value $pom -NoNewline
        Write-Host "Injected Eureka Client into $service pom.xml"
    }

    # 2. Update Application YML
    $ymlPath = Join-Path $rootDir $service "src\main\resources\application.yml"
    $yml = Get-Content $ymlPath -Raw
    if ($yml -notmatch "eureka:") {
        $eurekaConfig = "`n`neureka:`n  client:`n    service-url:`n      defaultZone: http://eureka-server:8761/eureka/"
        $yml = $yml + $eurekaConfig
        Set-Content -Path $ymlPath -Value $yml -NoNewline
        Write-Host "Injected Eureka Config into $service application.yml"
    }
}

# 3. Update API Gateway Routing
$gatewayYmlPath = Join-Path $rootDir "api-gateway\src\main\resources\application.yml"
$gatewayYml = Get-Content $gatewayYmlPath -Raw
$gatewayYml = $gatewayYml -replace 'uri: \$\{AUTH_SERVICE_URL:http://localhost:8081\}', 'uri: lb://auth-service'
$gatewayYml = $gatewayYml -replace 'uri: \$\{APP_SERVICE_URL:http://localhost:8082\}', 'uri: lb://application-service'
$gatewayYml = $gatewayYml -replace 'uri: \$\{DOC_SERVICE_URL:http://localhost:8083\}', 'uri: lb://document-service'
$gatewayYml = $gatewayYml -replace 'uri: \$\{ADMIN_SERVICE_URL:http://localhost:8084\}', 'uri: lb://admin-service'
Set-Content -Path $gatewayYmlPath -Value $gatewayYml -NoNewline
Write-Host "Updated API Gateway to use load balancer (lb://) routing."

# 4. Compile Eureka Server just to test
Set-Location -Path (Join-Path $rootDir "eureka-server")
Write-Host "Compiling Eureka Server..."
mvn clean compile
