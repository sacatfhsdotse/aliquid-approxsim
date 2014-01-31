@echo off

REM Go to the Stratmas server source code directory.
cd StratmasServer

set DEPDIR=..\..\dependencies
set PLATFORM=win

REM Setup dependencies
REM Xerces
REM Create with cabarc -p -r N boost.cab win\*
set XERCESVERSION=2.7.0
cabarc -p X "%DEPDIR%\xerces\%XERCESVERSION%\xerces-%XERCESVERSION%-%PLATFORM%.cab" "%DEPDIR%\xerces\%XERCESVERSION%\\"
set XERCESCROOT=%DEPDIR%\xerces\%XERCESVERSION%\%PLATFORM%
REM Boost
REM Create with cabarc -p -r N boost.cab win\*
set BOOSTVERSION=1.33.1
cabarc -p X "%DEPDIR%\boost\%BOOSTVERSION%\boost-%BOOSTVERSION%-%PLATFORM%.cab" "%DEPDIR%\boost\%BOOSTVERSION%\\"
set BOOSTROOT=%DEPDIR%\boost\%BOOSTVERSION%\%PLATFORM%

REM Run make
nmake /NOLOGO /F Makefile.nmake "BOOSTROOT=%BOOSTROOT%" "XERCESCROOT=%XERCESCROOT%" DEBUG=0 clean
nmake /NOLOGO /F Makefile.nmake "BOOSTROOT=%BOOSTROOT%" "XERCESCROOT=%XERCESCROOT%" DEBUG=0 depend
nmake /NOLOGO /F Makefile.nmake "BOOSTROOT=%BOOSTROOT%" "XERCESCROOT=%XERCESCROOT%" DEBUG=0 all

REM Try to copy the executable to the correct directory.
echo =============================================================
echo Build completed. Copying executable...
copy stratmas.exe ..\..\stratmas_%PLATFORM%.exe
echo Done!
echo =============================================================

cd ..
