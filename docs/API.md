# Initial endpoint notes

## Implemented

`GET http://127.0.0.1:8080/actuator/health`

```json
{"status":"UP"}
```

The React landing page calls `/actuator/health`; Vite proxies it to Spring Boot. In normal operation, Spring Boot checks MongoDB and returns HTTP 503 with `{"status":"DOWN"}` if the database is unavailable.

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
```

## Future contracts

Login, users, components, stock movements, suppliers and procurement endpoints will be specified with their feature work. There are no domain endpoints in the basic setup.
