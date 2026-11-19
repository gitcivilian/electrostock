# Small project-local Maven launcher. Downloads are verified against Maven Central.
$ErrorActionPreference = 'Stop'
$version = '3.9.11'
$root = Split-Path $PSScriptRoot -Parent
$tools = Join-Path $root '.tools'
$mavenHome = Join-Path $tools "apache-maven-$version"
if (!(Test-Path (Join-Path $mavenHome 'bin/mvn.cmd'))) {
    New-Item -ItemType Directory -Force -Path $tools | Out-Null
    $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip"
    $zip = Join-Path $tools 'maven.zip'
    Invoke-WebRequest -UseBasicParsing $url -OutFile $zip
    $expected = (Invoke-WebRequest -UseBasicParsing "$url.sha512").Content.Trim().Split(' ')[0]
    if ((Get-FileHash $zip -Algorithm SHA512).Hash -ine $expected) {
        Remove-Item $zip
        throw 'Maven download checksum mismatch.'
    }
    Expand-Archive -Path $zip -DestinationPath $tools -Force
    Remove-Item $zip
}
& (Join-Path $mavenHome 'bin/mvn.cmd') @args
exit $LASTEXITCODE
