@echo off
echo Iniciando AquaGuard API com banco H2 persistente...
echo.
echo Configuracoes:
echo - Banco: H2 Persistente
echo - Arquivo: C:\temp\aquaguard_dev.mv.db
echo - Console H2: http://localhost:8080/h2-console
echo - API: http://localhost:8080
echo.
cd /d "%~dp0"
java -jar "target\aquaguard-api-0.0.1-SNAPSHOT.jar" ^
  --spring.profiles.active=dev ^
  --spring.datasource.url="jdbc:h2:file:C:/temp/aquaguard_dev" ^
  --spring.jpa.hibernate.ddl-auto=update
pause
