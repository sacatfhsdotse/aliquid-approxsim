@echo off
REM Builds the Stratmas Client and places the installation in ..\..\..\StratmasClient

set CLIENTDIR=..\..\StratmasClient

@ant "-DinstallPrefix=..\%CLIENTDIR%" -buildfile "%CLIENTDIR%/build/StratmasClient/build.xml" install
