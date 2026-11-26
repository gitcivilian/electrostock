# ElectroStock API notes

## Health

`GET http://127.0.0.1:8080/actuator/health`

```json
{"status":"UP"}
```

When Spring Boot runs normally, Actuator checks MongoDB connectivity and returns HTTP `503` with `{"status":"DOWN"}` if the database is unreachable.

## Categories (`/api/categories`)

- `GET /api/categories?search=&status=ALL|ACTIVE|INACTIVE` — List categories sorted by name, filtered by search text (`code`, `name`, `description`) and active status.
- `GET /api/categories/{id}` — Retrieve one category with its total and active component counts.
- `POST /api/categories` — Create a category (`code`, `name`, optional `description`).
- `PUT /api/categories/{id}` — Update category details.
- `PATCH /api/categories/{id}/deactivate` — Soft-deactivate a category (blocked with `409 Conflict` if active components are still assigned).
- `PATCH /api/categories/{id}/activate` — Reactivate a category.

## Components (`/api/components`)

- `GET /api/components?search=&categoryId=&status=ALL|ACTIVE|INACTIVE` — List components sorted by part number, filtered by search text (`partNumber`, `name`, `manufacturer`, `packageType`, `unit`, `storageLocation`, technical `specifications`, `description`), category, and active status.
- `GET /api/components/{id}` — Retrieve one component with category details.
- `POST /api/components` — Create a component (`partNumber`, `name`, `categoryId`, `unit`, `storageLocation`, `reorderThreshold`, optional `manufacturer`, `packageType`, `specifications` key-value map, `description`). Requires an active category.
- `PUT /api/components/{id}` — Update component details, specifications, unit, storage location, and reorder threshold.
- `PATCH /api/components/{id}/deactivate` — Soft-deactivate a component.
- `PATCH /api/components/{id}/activate` — Reactivate a component (requires its assigned category to be active).
