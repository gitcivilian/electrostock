# ElectroStock

Electronics inventory and procurement management university project.

**Current milestone: basic setup for Members 1 and 2.**

| Member | Name / GitHub | Setup ownership |
| --- | --- | --- |
| 1 | Dilukshan / `gitcivilian` | Spring Boot foundation, build tooling, repository documentation |
| 2 | Ganamoorthy / `Ganamoorthy` | React/Vite foundation, landing page, backend connection check |
| 3 | Name / GitHub to be added | Supplier and procurement module; work planned |

Member 3's implementation has not started. Authentication, inventory, databases, stock movements, and procurement are future milestones.

## Separate tasks for the three members

Each member owns the Spring Boot APIs, React screens, tests, and documentation for their assigned features. Checked items below are completed setup; unchecked items are planned development.

### Member 1 — Dilukshan (`gitcivilian`)

**Ownership: authentication, users/roles, and application integration.**

- [x] Set up the Spring Boot backend, Maven launchers, and health endpoint test.
- [x] Initialize Git rules, shared setup documentation, and backend CI.
- [ ] Implement user persistence and secure password hashing.
- [ ] Implement login/logout and server-side authorization with Spring Security.
- [ ] Define administrator, storekeeper, requester, and procurement-officer permissions.
- [ ] Build login, profile, and administrator user/role management screens.
- [ ] Integrate protected navigation and shared access/error states.
- [ ] Test authentication and restricted endpoints.
- [ ] Coordinate CI, deployment configuration, and final setup documentation.

### Member 2 — Ganamoorthy (`Ganamoorthy`)

**Ownership: component catalog, stock management, and inventory reporting.**

- [x] Set up React/Vite, the landing page, and basic responsive styles.
- [x] Add the backend connection check, frontend build instructions, and frontend CI.
- [x] Implement component/category records and create, edit, search, filter, and deactivate workflows.
- [x] Add component specifications, units, storage locations, and reorder thresholds.
- [ ] Implement stock receipts, issues, returns, and authorized adjustments.
- [ ] Update stock balances and the transaction ledger atomically; prevent insufficient-stock issues.
- [ ] Build stock movement forms and auditable history screens.
- [ ] Implement low-stock indicators, inventory dashboard data, and filtered CSV reports.
- [ ] Test validation, stock rollback, and concurrent stock updates.
- [ ] Document the inventory schema and stock-accounting rules.

### Member 3 — name and GitHub account to be added

**Ownership: suppliers, procurement, and goods receipts. All tasks are planned.**

- [ ] Set up the supplier/procurement module structure and agree its API contracts.
- [ ] Implement supplier records and create, edit, search, and deactivate screens.
- [ ] Implement purchase requests with requested items and justification.
- [ ] Add authorized approval/rejection and request-status tracking.
- [ ] Create purchase orders from approved requests with supplier and order-line details.
- [ ] Implement verified goods receipts and partial-delivery tracking.
- [ ] Reuse Member 2's stock service so verified deliveries update inventory exactly once.
- [ ] Build procurement status views, dashboard data, and filtered reports.
- [ ] Test invalid status transitions, over-deliveries, and duplicate receipt prevention.
- [ ] Document procurement workflows and prepare the request-to-delivery demonstration.

### Shared responsibilities and handoffs

- All three members agree on the ER diagram, API conventions, and PostgreSQL migration plan.
- Member 1 provides user identity and permission rules to Members 2 and 3.
- Members 2 and 3 agree on the stock receipt service and delivery references before goods-receipt implementation.
- Review rotation: Member 1 reviews Member 2, Member 2 reviews Member 3, and Member 3 reviews Member 1.
- Everyone maintains meeting logs, tests the integrated application, and contributes to the final report and viva demonstration.

## Included now

- Java 21 + Spring Boot 3.5.7 application with a health endpoint.
- React 19 + Vite 7 landing page showing backend connectivity.
- Project-local Maven launchers and npm lockfile.
- Git ignore/formatting rules, setup documentation, and GitHub Actions builds.
- Team ownership and meeting-log template.

## Run on Windows

Install JDK 21 and Node.js 22.12+ with npm. Git is required for collaboration. Maven downloads automatically on first use; internet access is needed for dependencies.

Open two PowerShell terminals from the repository root.

**Backend**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Frontend**

```powershell
cd frontend
npm ci
npm run dev
```

Open **http://127.0.0.1:5173**. Backend health: **http://127.0.0.1:8080/actuator/health**. Stop servers using `Ctrl+C`.

Vite forwards `/actuator` and future `/api` requests to Spring Boot on port 8080. Both servers bind to loopback for local development.

On Linux/macOS use `sh ./mvnw spring-boot:run` inside `backend`. The launcher requires `curl`, `tar`, and `sha512sum` or `shasum`. Maven 3.9.11 is downloaded to ignored `backend/.tools/` and checksum-verified.

## Verify

```powershell
# In backend
.\mvnw.cmd clean verify

# In frontend
npm run build
```

The backend test checks application startup and the health endpoint. The frontend build generates `frontend/dist/`. Static deployment will need a reverse proxy for backend requests; Vite's development proxy is not included in the build.

## Structure

```text
backend/       Spring Boot application, health test, Maven launchers
frontend/      React landing page, shared fetch helper, Vite configuration
docs/          Scope, member ownership, endpoint notes, meeting-log template
.github/       CI build workflow
```

## Team workflow

Use separate feature branches and pull requests for each setup task, then merge reviewed work into `main`. Branch labels document task ownership; commit authors reflect the account that actually prepared the changes.

- [Team plan](docs/TEAM_PLAN.md)
- [Project plan](docs/PROJECT_PLAN.md)
- [Endpoint notes](docs/API.md)
- [Meeting log](docs/MEETING_LOG.md)
- [Contribution guide](CONTRIBUTING.md)
- [Backend README](backend/README.md)
- [Frontend README](frontend/README.md)
