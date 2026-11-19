$ErrorActionPreference = 'Stop'
$envFile = Join-Path (Split-Path $PSScriptRoot -Parent) '.env'

if (Test-Path -LiteralPath $envFile) {
    Write-Output 'Using the existing .env; no credentials were changed.'
    exit 0
}

function New-LocalPassword {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
        return [System.BitConverter]::ToString($bytes).Replace('-', '').ToLowerInvariant()
    } finally {
        $rng.Dispose()
    }
}

$settings = @(
    "MONGO_ROOT_PASSWORD=$(New-LocalPassword)"
    "MONGO_APP_PASSWORD=$(New-LocalPassword)"
)
$settings | Set-Content -LiteralPath $envFile -Encoding Ascii
Write-Output 'Created .env with separate local MongoDB credentials.'
