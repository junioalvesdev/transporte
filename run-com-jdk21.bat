@echo off
set JAVA_HOME=C:\Users\junio.barbosa\tools\jdk-21.0.12.1+1
set PATH=%JAVA_HOME%\bin;%PATH%
"C:\Users\junio.barbosa\tools\apache-maven-3.9.9\bin\mvn.cmd" -f C:\dev\transporte-novo\pom.xml spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.WindowsSelectorProvider -Dspring.datasource.username=transportes -Dspring.datasource.password=transportes -Dspring.datasource.url=jdbc:mysql://localhost:3306/transportes"
