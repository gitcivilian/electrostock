# Spring Boot foundation — Member 1

Setup owner: **Dilukshan (`gitcivilian`)**.

This is a Java 21 / Spring Boot 3.5.7 application with Spring Web, Actuator, Validation, and Spring Data MongoDB.

## Run

Set `MONGODB_URI` (either as an environment variable or in a root `.env` file copied from `../.env.example`), then run Spring Boot from `backend/`:

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

`ApplicationHealthTest` and the catalog unit/controller tests run without requiring a live MongoDB server. When the app runs normally, MongoDB contributes to `/actuator/health`. The server defaults to `127.0.0.1:8080`; override with `SERVER_ADDRESS` / `SERVER_PORT` environment variables when needed. If changing the port, update the frontend proxy.

## MongoDB configuration

- Default fallback URI: `mongodb://127.0.0.1:27017/electrostock?serverSelectionTimeoutMS=3000`.
- Once your MongoDB database (local or MongoDB Atlas) is created, set `MONGODB_URI` in `../.env` or in your environment/secret store. No code changes are required.
- Never commit `.env` or real database credentials to Git.
