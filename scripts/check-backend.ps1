param(
    [ValidateSet("local", "prod")]
    [string]$Target = "prod"
)

$urls = @{
    local = "http://localhost:8080/api/v1/optical-stores"
    prod  = "https://logix-optiflow-back-end.onrender.com/api/v1/optical-stores"
}

$url = $urls[$Target]
Write-Host "Comprobando backend ($Target): $url"

try {
    $response = Invoke-RestMethod -Uri $url -TimeoutSec 120
    $count = 0
    if ($null -ne $response.opticalStores) {
        $count = $response.opticalStores.Count
    } elseif ($null -ne $response.value) {
        $count = $response.value.Count
    }
    Write-Host "OK - backend respondio. Opticas: $count"
    exit 0
} catch {
    Write-Host "FALLO - $($_.Exception.Message)"
    if ($Target -eq "local") {
        Write-Host "Levanta el backend: mvnw spring-boot:run en el repo back-end (puerto 8080)."
    } else {
        Write-Host "Abre Swagger en el navegador o usa prodDebug y espera un minuto."
    }
    exit 1
}
