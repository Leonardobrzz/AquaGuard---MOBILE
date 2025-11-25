@echo off
REM cd /d "C:\Users\Leonardo\Downloads\AquaGuard-main (1)\AquaGuard-main\aquaguard-api"
cd /d "C:\Users\Maria Vitória\AquaGuard-main (1)\AquaGuard-main\aquaguard-api"
"C:\Program Files\Java\jdk-21\bin\java" -jar target/aquaguard-api-0.0.1-SNAPSHOT.jar ^
  --spring.profiles.active=dev ^
  --spring.flyway.enabled=false ^
  --spring.jpa.hibernate.ddl-auto=create-drop ^
  --spring.datasource.url=jdbc:h2:mem:aquaguard_dev ^
  --spring.datasource.username=sa ^
  --spring.datasource.password= ^
  --spring.datasource.driver-class-name=org.h2.Driver ^
  --api.security.token.secret=AquaGuardSecretKeyForDevelopment2024!@# ^
  --api.security.token.expiration-hours=24
