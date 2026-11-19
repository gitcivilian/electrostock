# Spring Boot foundation — Member 1

Setup owner: **Dilukshan (`gitcivilian`)**.

This is a Java 21 / Spring Boot 3.5.7 scaffold with Spring Web, Actuator, and Spring Data MongoDB. Business modules are future work.

## Run

Start MongoDB from the repository root using the [README instructions](../README.md#run-on-windows), then run Spring Boot from `backend/`:

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

`ApplicationHealthTest` verifies application startup and the health endpoint without connecting to MongoDB. With the app running normally, MongoDB contributes to the live health status. The server defaults to `127.0.0.1:8080`; override with `SERVER_ADDRESS` / `SERVER_PORT` environment variables when needed. If changing the port, update the frontend proxy.

## MongoDB configuration

- Local address: `127.0.0.1:27017`; database and authentication database: `electrostock`.
- Application user: `electrostock_app` (read/write on `electrostock` only).
- Start from `backend/` so Spring Boot can read the application password from `../.env`. The application does not use the MongoDB administrator account.
- To use an existing hosted MongoDB instance instead, set `MONGODB_URI` in your environment or deployment secret store. Use an application user scoped to the target database. Never put a real URI in Git or frontend code.
- The initialization script runs only for a new Docker volume. Editing `.env` does not change an existing database user's password. Keep the `.env` that belongs to the volume; rotate passwords in MongoDB before updating local settings.
