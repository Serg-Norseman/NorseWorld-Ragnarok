@echo off
cls

set TEMP_PATH=D:\TEMP
if not exist "%TEMP_PATH%" mkdir "%TEMP_PATH%"

"c:\Program Files\7-zip\7z.exe" a -tzip -mx5 -scsWIN -r %TEMP_PATH%\soundpak.zip ..\..\sfx > %TEMP_PATH%\soundpak.log
