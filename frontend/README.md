# React catalog & foundation — Member 2

Setup & catalog owner: **Ganamoorthy (`Ganamoorthy`)**.

React 19 + Vite 7 application with Spring Boot + MongoDB health checks and the Component & Category Catalog workflows. Requires Node.js 22.12+.

```powershell
npm ci
npm run dev
```

Open http://127.0.0.1:5173. Start the backend on port 8080 (with `MONGODB_URI` configured in root `.env`) to see **Backend & MongoDB connected.** Use **Check connection** to retry the health check.

## Files

- `src/App.jsx`: sticky top bar, backend/MongoDB health pill, KPI summary cards, and tab navigation.
- `src/components/ComponentCatalog.jsx`: component create/edit modal, search, category/status filters, and soft-deactivate/activate UI.
- `src/components/CategoryCatalog.jsx`: category create/edit modal, search, status filters, and soft-deactivate/activate UI.
- `src/components/Icons.jsx`: inline SVG UI icons.
- `src/api.js`: shared fetch helper and `/api/categories` + `/api/components` client methods.
- `src/styles.css`: responsive styles.
- `vite.config.js`: local `/actuator` and `/api` proxy to Spring Boot (`127.0.0.1:8080`).

## Build

```powershell
npm run build
```

Output: `dist/`. The production host will need to forward `/api` and `/actuator` requests to Spring Boot.
