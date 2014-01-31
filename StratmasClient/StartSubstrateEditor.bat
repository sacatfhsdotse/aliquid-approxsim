REM Batch file for starting the Stratmas client.

@echo off

REM Start client
java -Xmx300m -Djava.library.path=StratmasClient\lib\Windows-x86 -cp StratmasClient StratmasClient.Client -noJoglResolve -substrate=? %1
