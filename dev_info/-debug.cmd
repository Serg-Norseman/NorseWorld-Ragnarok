@echo off
cls
rem copy .\libs\win-x64\*.* .\build\classes\
xcopy ".\libs" ".\build\classes" /s /v /f /y