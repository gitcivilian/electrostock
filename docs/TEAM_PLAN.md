# Initial team setup ownership

| Order | Member | GitHub | Current scope |
| --- | --- | --- | --- |
| 1 | Dilukshan | `gitcivilian` | Spring Boot scaffold, Maven launcher, setup docs and CI foundation |
| 2 | Ganamoorthy | `Ganamoorthy` | React/Vite scaffold, landing page, health connection and frontend build |

Names follow the requested member order. The active authenticated GitHub login for Member 1 is `gitcivilian`.

## Member 1 basic setup

- [x] Initialize repository structure and Git rules.
- [x] Configure Java 21 and Spring Boot.
- [x] Provide Maven launchers and backend startup instructions.
- [x] Expose and verify application health.
- [x] Add shared documentation and backend CI build.

Branch: `setup/member-1-backend`.

Later ownership: authentication, users/roles, integration and deployment.

## Member 2 basic setup

- [x] Configure React and Vite.
- [x] Add the landing page and minimal responsive styles.
- [x] Connect to the backend health endpoint through the Vite proxy.
- [x] Provide frontend startup/build documentation and CI build.

Branch: `setup/member-2-frontend`.

Later ownership: component catalog, stock transactions and inventory reporting.

## Future work

Member 3's setup and implementation are unstarted. Supplier/procurement tasks will be assigned later.

Commit authors must reflect who prepared the change. These branches describe setup ownership and do not claim that another member authored commits made by the authenticated account. Each member should use their own Git identity for their future work.
