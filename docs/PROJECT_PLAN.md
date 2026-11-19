# ElectroStock project plan

## Initial milestone

Prepare a Spring Boot backend and React frontend that start locally and communicate through a health check. This milestone contains basic setup for Dilukshan (Member 1) and Ganamoorthy (Member 2).

## Current architecture

```text
React landing page -> Vite development proxy -> Spring Boot /actuator/health -> MongoDB
```

Java 21, Spring Boot 3.5.7, React 19, Vite 7, Maven and npm. The local `electrostock` database is initialized with a restricted application user; it does not contain domain records yet.

## Planned later

- Member 1: authentication, roles, user management and integration.
- Member 2: component catalog, categories, stock transactions and inventory reports.
- Future assigned work: suppliers, procurement, goods receipts and procurement reports.
- Shared: MongoDB document models and indexes, API contracts, integration testing and deployment.

Planned domain architecture: React -> Spring Boot controllers -> services -> Spring Data MongoDB -> MongoDB. If stock balances and ledger entries require multi-document transactions, configure a replica set before implementing that workflow; the local standalone container is only a development scaffold.

Stock changes must eventually update the balance and an immutable movement ledger atomically. Purchase orders must not increase stock; verified goods receipts must increase it exactly once.

## Collaboration evidence

Keep genuine meeting records and use individual Git accounts for ongoing work. Branches and PRs identify task ownership, while commit authors identify who prepared the code. The generic assessment guideline specifies four-member groups, while the supplied allocation includes three-member groups; use the demonstrator's registered allocation for the final team details.
