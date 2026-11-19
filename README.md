# ElectroStock

Electronics inventory and procurement management university project.

**Current milestone: basic setup for Members 1 and 2.**

| Member | Name / GitHub | Setup ownership |
| --- | --- | --- |
| 1 | Dilukshan / `gitcivilian` | Spring Boot foundation, build tooling, repository documentation |
| 2 | Ganamoorthy / `Ganamoorthy` | React/Vite foundation, landing page, backend connection check |

Member 3's implementation has not started. Authentication, inventory, databases, stock movements, and procurement are future milestones.

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
