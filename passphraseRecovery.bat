@REM Attempt to recover mis-typed passphrase
@echo *********************************************************************
@echo * Use this batch file to search for a lost passphrase.              *
@echo *********************************************************************

if exist jdk (
    set javaDir=jdk\bin\
)

%javaDir%java.exe -Xmx1024m -cp "classes;lib/*;conf" -Drcc.runtime.mode=desktop rcc.tools.PassphraseRecovery
