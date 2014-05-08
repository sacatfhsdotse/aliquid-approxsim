REM Batch file for starting the Approxsim client.

@echo off

REM Start client
java -Xmx300m -Djava.library.path=ApproxsimClient\lib\Windows-x86 -cp ApproxsimClient ApproxsimClient.Client -noJoglResolve %1
