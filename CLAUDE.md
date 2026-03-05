# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

dgit-server is a Spring Boot (3.4.1) + Kotlin backend service that tracks GitHub activity for users authenticated via DAuth (Dodam authentication system). It scrapes GitHub commit data, computes stats (streaks, rankings, levels), and provides APIs for leaderboards and a hall of fame.

## Build & Run Commands

```bash
./gradlew build          # Build the project (produces build/libs/dgit-server-0.0.1-SNAPSHOT.jar)
./gradlew bootRun        # Run the application locally
./gradlew clean build    # Clean and rebuild
```

No test suite exists yet — `src/test/` is empty.

## Required Environment Variables

`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (MySQL), `DAUTH_CLIENT_ID`, `DAUTH_CLIENT_SECRET`, `JWT_SECRET_KEY`, `GITHUB_TOKEN`, `SWAGGER_USERNAME`, `SWAGGER_PASSWORD`

## Architecture

**Layered architecture** under `dodam.b1nd.dgit`:

- **`presentation/`** — REST controllers, DTOs (request/response/external), Swagger docs interfaces. Controllers delegate to use-case interfaces.
- **`application/`** — Service implementations and use-case interfaces. Each domain area (auth, github, token, user) has a `usecase/` sub-package with interfaces and a corresponding `*Service.kt` implementation.
- **`domain/`** — JPA entities and Spring Data repositories. Key entities: `User`, `GithubAccount`, `GithubStats`, `Repository`, `WeeklyRecord`.
- **`infrastructure/`** — Cross-cutting concerns: security (JWT filter, Spring Security config), external API clients (`GithubClient`, `DAuthClient`), config properties, exception handling.

**Key data flow:**
- Users authenticate via DAuth OAuth → JWT issued → subsequent requests use JWT
- `GithubClient` calls GitHub REST API and GraphQL API to fetch commit data
- `GithubStatsService` aggregates commit dates into stats (total commits, streaks, weekly counts)
- `WeeklyRecordService` snapshots weekly commit counts (scheduled via `@EnableScheduling`)
- `RankingService` and `HallOfFameService` compute leaderboard data from stored stats

**External integrations:**
- GitHub API (REST + GraphQL) via `GithubClient` using WebClient
- DAuth API (`https://dauthapi.b1nd.com`) via `DAuthClient` for OAuth token exchange and user info

**Database:** MySQL with JPA `ddl-auto: validate` — schema must exist before running. Entities map to tables: `users`, `github_accounts`, `github_stats`, `repositories`, `weekly_records`.

## Key Conventions

- Gradle build (Groovy DSL), Java 17 toolchain, Kotlin 2.x
- Use-case pattern: controllers depend on `*UseCase` interfaces, not service classes directly
- Controller docs are defined as separate interfaces in `controller/docs/` packages
- WebClient (from spring-boot-starter-webflux) is used for all external HTTP calls, called synchronously with `.block()`
- Security: stateless JWT auth, Swagger endpoints protected by HTTP Basic auth
