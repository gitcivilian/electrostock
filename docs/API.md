# Initial endpoint notes

## Implemented

`GET http://127.0.0.1:8080/actuator/health`

```json
{"status":"UP"}
```

The React landing page calls `/actuator/health`; Vite proxies it to the Spring Boot backend. This confirms the foundation is connected.

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
```

## Future contracts

Login, users, components, stock movements, suppliers and procurement endpoints will be specified with their feature work. There are no domain endpoints in the basic setup.
