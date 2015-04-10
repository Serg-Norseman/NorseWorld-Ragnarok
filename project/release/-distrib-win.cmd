@echo off
cls
set VER=v0.11.0
set PLAT=win
set TEMP_PATH=D:\TEMP
set DIST_PATH="%TEMP_PATH%\nwr-dist-%VER%-%PLAT%"

if not exist "%TEMP_PATH%" mkdir "%TEMP_PATH%"
if not exist "%DIST_PATH%" mkdir "%DIST_PATH%"
if not exist "%DIST_PATH%\languages" mkdir "%DIST_PATH%\languages"

xcopy "..\dist" "%DIST_PATH%" /s /v /f /y
xcopy "..\libs" "%DIST_PATH%" /s /v /f /y
xcopy "..\..\languages" "%DIST_PATH%\languages" /s /v /f /y

xcopy ".\history.txt" "%DIST_PATH%" /s /v /f /y
xcopy ".\copyrights.txt" "%DIST_PATH%" /s /v /f /y
xcopy ".\gpl.txt" "%DIST_PATH%" /s /v /f /y

del "%DIST_PATH%\readme.txt"

"c:\Program Files\7-zip\7z.exe" a -tzip -mx5 -scsWIN -r %DIST_PATH%\Ragnarok.rfa ..\..\resources > %TEMP_PATH%\resources.log
"c:\Program Files\7-zip\7z.exe" a -tzip -mx5 -scsWIN -r %TEMP_PATH%\nwr-dist-%VER%-%PLAT%.zip %DIST_PATH% > %TEMP_PATH%\dist-%PLAT%.log
