# Spring Boot foundation — Member 1

Setup owner: **Dilukshan (`gitcivilian`)**.

This is a Java 21 / Spring Boot 3.5.7 scaffold with Spring Web and Actuator. Business modules and database integration are future work.

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS: `sh ./mvnw spring-boot:run`.

Maven 3.9.11 downloads automatically into `.tools/` with SHA-512 verification. JDK 21 is required; check `java -version` and `javac -version` and set `JAVA_HOME` if necessary.

Health: `GET http://127.0.0.1:8080/actuator/health` returns `{"status":"UP"}`.

## Verify and package

```powershell
.\mvnw.cmd clean verify
java -jar target/electrostock-0.0.1-SNAPSHOT.jar
```

`ApplicationHealthTest` verifies application startup and the health endpoint. The server defaults to `127.0.0.1:8080`; override with `SERVER_ADDRESS` / `SERVER_PORT` environment variables when needed. If changing the port, update the frontend proxy.

Add domain controllers/services and PostgreSQL persistence in later tasks. Keep server credentials in environment variables.
