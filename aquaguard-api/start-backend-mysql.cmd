@echo off
REM cd /d "C:\Users\Leonardo\Downloads\AquaGuard-main (1)\AquaGuard-main\aquaguard-api"
cd /d "C:\Users\Maria Vitória\AquaGuard-main (1)\AquaGuard-main\aquaguard-api"
"C:\Program Files\Java\jdk-21\bin\java" -jar target/aquaguard-api-0.0.1-SNAPSHOT.jar ^
  --spring.profiles.active=prod ^
  --spring.flyway.enabled=true ^
  --spring.jpa.hibernate.ddl-auto=validate ^
  --spring.datasource.url=jdbc:mysql://localhost:3306/aquaguard_api?useSSL=false&serverTimezone=UTC ^
  --spring.datasource.username=root ^
  --spring.datasource.password=Claudio534@534 ^
  --spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver ^
  --api.security.token.secret=AquaGuardLocalSecretKey2024!@#Development ^
  --api.security.token.expiration-hours=2
pause
