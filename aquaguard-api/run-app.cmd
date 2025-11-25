@echo off
set MAVEN_USER_HOME=C:\maven-home
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
set SPRING_DATASOURCE_PASSWORD=
set SPRING_FLYWAY_ENABLED=false
.\mvnw.cmd %*

