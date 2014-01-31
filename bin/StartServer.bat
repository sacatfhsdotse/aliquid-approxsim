REM Batch file for starting the Stratmas server.

@echo off

REM Set path to libraries.
path %PATH%;dependencies\xerces\2.7.0\win\bin

REM Start server. Arguments to the server goes here.
REM For example: stratmas_win -p 29000
stratmas_win
