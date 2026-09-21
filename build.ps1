param([string]$Sdk='D:\Androidsdk',[string]$Jdk='D:\android\jbr')
$ErrorActionPreference='Stop'
$buildRoot=Join-Path $PSScriptRoot '../../work/suiri-build'
$buildRoot=[IO.Path]::GetFullPath($buildRoot)
$toolsRoot=Join-Path $Sdk 'build-tools/36.0.0'
$platform=Join-Path $Sdk 'platforms/android-36.1/android.jar'
$java=Join-Path $Jdk 'bin/java.exe'
New-Item -ItemType Directory -Force -Path $buildRoot,(Join-Path $buildRoot 'classes'),(Join-Path $buildRoot 'dex'),(Join-Path $buildRoot 'generated') | Out-Null
function Check-Exit { if($LASTEXITCODE -ne 0){throw "Build failed: exit $LASTEXITCODE"} }
& (Join-Path $toolsRoot 'aapt2.exe') compile --dir (Join-Path $PSScriptRoot 'app/src/main/res') -o (Join-Path $buildRoot 'resources.zip')
Check-Exit
& (Join-Path $toolsRoot 'aapt2.exe') link -I $platform --manifest (Join-Path $PSScriptRoot 'app/src/main/AndroidManifest.xml') --java (Join-Path $buildRoot 'generated') -o (Join-Path $buildRoot 'unsigned.apk') (Join-Path $buildRoot 'resources.zip')
Check-Exit
$sources=@(Get-ChildItem (Join-Path $PSScriptRoot 'app/src/main/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
$sources+=@(Get-ChildItem (Join-Path $buildRoot 'generated') -Recurse -Filter '*.java' | ForEach-Object FullName)
& (Join-Path $Jdk 'bin/javac.exe') -encoding UTF-8 -source 8 -target 8 -bootclasspath "$platform;$(Join-Path $toolsRoot 'core-lambda-stubs.jar')" -d (Join-Path $buildRoot 'classes') $sources
Check-Exit
& (Join-Path $Jdk 'bin/jar.exe') cf (Join-Path $buildRoot 'classes.jar') -C (Join-Path $buildRoot 'classes') .
Check-Exit
& $java -cp (Join-Path $toolsRoot 'lib/d8.jar') com.android.tools.r8.D8 --lib $platform --min-api 30 --output (Join-Path $buildRoot 'dex') (Join-Path $buildRoot 'classes.jar')
Check-Exit
& (Join-Path $Jdk 'bin/jar.exe') uf (Join-Path $buildRoot 'unsigned.apk') -C (Join-Path $buildRoot 'dex') classes.dex
Check-Exit
& (Join-Path $toolsRoot 'zipalign.exe') -f 4 (Join-Path $buildRoot 'unsigned.apk') (Join-Path $buildRoot 'aligned.apk')
Check-Exit
$key=Join-Path $buildRoot 'personal-debug.p12'
if(!(Test-Path $key)){
 & (Join-Path $Jdk 'bin/keytool.exe') -genkeypair -keystore $key -storepass android -keypass android -alias suiri -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=SuiRi Personal Development'
 Check-Exit
}
$apk=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../saber喵-1.1.5.apk'))
& $java -jar (Join-Path $toolsRoot 'lib/apksigner.jar') sign --ks $key --ks-key-alias suiri --ks-pass pass:android --out $apk (Join-Path $buildRoot 'aligned.apk')
Check-Exit
& $java -jar (Join-Path $toolsRoot 'lib/apksigner.jar') verify --verbose $apk
Check-Exit
Get-Item $apk | Select-Object FullName,Length
