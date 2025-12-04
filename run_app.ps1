$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.2\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.2\plugins\maven\lib\maven3\bin\mvn.cmd" clean javafx:run
