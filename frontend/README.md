# React foundation — Member 2

Setup owner: **Ganamoorthy (`Ganamoorthy`)**.

React 19 + Vite 7 landing page with a Spring Boot health connection check. Requires Node.js 22.12+.

```powershell
npm ci
npm run dev
```

Open http://127.0.0.1:5173. Start MongoDB and the backend on port 8080 to see **Spring Boot backend connected.** Use **Check connection** to retry the health check.

## Files

- `src/App.jsx`: initial landing page and connection state.
- `src/api.js`: shared fetch helper.
- `src/styles.css`: basic responsive styles.
- `vite.config.js`: local `/actuator` and future `/api` proxy.

## Build

```powershell
npm run build
```

Output: `dist/`. The production host will need to forward backend requests to Spring Boot. Feature screens are future work.
