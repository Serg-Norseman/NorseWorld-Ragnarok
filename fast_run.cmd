@echo off

@if exist ".\NWR.exe" goto start

call clean.cmd
dotnet build .\project\NWR.sln /p:Configuration="Debug" /t:Rebuild /verbosity:quiet

:start
start .\NWR.exe
