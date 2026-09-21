param([string]$Sdk='D:\Androidsdk',[string]$Jdk='D:\android\jbr',[string]$Ics='')
$ErrorActionPreference='Stop'
$testRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../work/suiri-tests'))
$jsonRoot=Join-Path $testRoot 'json-runtime'
$classes=Join-Path $testRoot 'classes'
New-Item -ItemType Directory -Force -Path $jsonRoot,$classes | Out-Null
$androidJar=Join-Path $Sdk 'platforms/android-36.1/android.jar'
$jsonSource=Join-Path $Sdk 'sources/android-36.1/org/json'
if(!(Test-Path -LiteralPath $jsonSource)){throw '本地单元测试需要 SDK Sources for Android 36.1'}
# Use the SDK's JSON implementation on the JVM; remove only Android-specific annotations.
Get-ChildItem -LiteralPath $jsonSource -Filter '*.java' | ForEach-Object {
 $s=Get-Content -LiteralPath $_.FullName -Raw
 $s=$s -replace '(?m)^import (?:static )?(?:android\.|libcore\.).*;\r?\n',''
 $s=$s -replace '@SystemApi\(client = MODULE_LIBRARIES\)',''
 $s=$s -replace '@(?:UnsupportedAppUsage|NonNull|Nullable)\b',''
 Set-Content -LiteralPath (Join-Path $jsonRoot $_.Name) -Value $s -Encoding utf8
}
$sources=@(Get-ChildItem -LiteralPath $jsonRoot -Filter '*.java' | ForEach-Object FullName)
$sources+=@(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'app/src/main/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
$generated=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../work/suiri-build/generated'))
if(!(Test-Path -LiteralPath $generated)){throw '请先执行 build.ps1 生成资源类'}
$sources+=@(Get-ChildItem -LiteralPath $generated -Recurse -Filter '*.java' | ForEach-Object FullName)
$sources+=@(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'tests') -Filter '*.java' | ForEach-Object FullName)
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -d $classes -cp $androidJar $sources
if($LASTEXITCODE -ne 0){throw '测试编译失败'}
& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.FeatureTest
if($LASTEXITCODE -ne 0){throw '功能检查失败'}
& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.UpgradeTest
if($LASTEXITCODE -ne 0){throw '升级功能检查失败'}
& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.ChatTest
if($LASTEXITCODE -ne 0){throw '聊天与自动操作检查失败'}
if($Ics){& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.ScheduleTest $Ics; if($LASTEXITCODE -ne 0){throw '真实课表检查失败'}}

& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.WeatherTest
if($LASTEXITCODE -ne 0){throw '天气与Saber检查失败'}

& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.Version11Test
if($LASTEXITCODE -ne 0){throw '1.1 checks failed'}

& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.Version113Test (Join-Path $testRoot 'pptx-smoke.pptx')
if($LASTEXITCODE -ne 0){throw '1.1.3 checks failed'}

& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.Version114Test
if($LASTEXITCODE -ne 0){throw '1.1.4 checks failed'}

& (Join-Path $Jdk 'bin/java.exe') -cp "$classes;$androidJar" com.example.suiri.Version115Test
if($LASTEXITCODE -ne 0){throw '1.1.5 checks failed'}
