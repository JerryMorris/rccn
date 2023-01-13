if exist jdk (
    set javaDir=jdk\bin\
)

%javaDir%java.exe -Xmx1024m -cp "classes;lib/*;conf" -Drcc.runtime.mode=desktop rcc.tools.SignTransactionJSON %*
