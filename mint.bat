rem @ECHO OFF
if exist jdk (
    set javaDir=jdk\bin\
)

%javaDir%java.exe -cp classes;lib\*;conf rcc.mint.MintWorker