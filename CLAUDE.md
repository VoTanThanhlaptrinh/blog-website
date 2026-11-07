# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

Monorepo with independent apps (no root build file):

- `file-service/` — a stateless Express 4 + TypeScript upload service (CommonJS) shared by three webs (`blog`, `cellphones`, `job-portal`); the blog backend uses it as its only storage (see Backend architecture). All `/api/v1/files/*` routes require an `x-api-key` header; each web has its own key (`API_KEY_BLOG`, `API_KEY_CELLPHONES`, `API_KEY_JOB_PORTAL`) and `apiKeyAuth` derives the calling app from the key into `req.appName`, so request bodies carry no `app` field. Flow: `presign`/`presign-batch` return PUT URLs under `temp/{visibility}/{app}/{context}/...`, `confirm`/`confirm-batch` copy to `{visibility}/{app}/...` (dropping `temp/`; the temp copy is deliberately left in place for a bucket lifecycle rule to clear), plus delete and a presigned `read-url` for private files. Per-app/context rules (MIME, size, batch limit, public/private) live in `src/config/app-policies.ts`. `src/config/env.ts` validates env with zod and exits with a readable list on bad config. Public and private files live in two separate R2 buckets (`STORAGE_BUCKET_NAME` for `public/` + `temp/public/`, `STORAGE_PRIVATE_BUCKET_NAME` for `private/` + `temp/private/`); the bucket is chosen from the `public`/`private` segment of the key, and the private bucket must have no public access because R2's r2.dev/custom domain exposes a whole bucket. Env var names are `STORAGE_*` (including `STORAGE_PUBLIC_URL`) and the three `API_KEY_*` (min 32 chars, must differ), not the old `CLOUDFLARE_R2_*`. `presign` also returns `previewUrl` (public temp copy, for editor previews only). The `S3Client` sets `WHEN_REQUIRED` checksums because R2 rejects the SDK's default CRC32 params on presigned URLs. Copy `.env.example` to `.env` to configure. `file-service/README.md` is the (Vietnamese) API usage guide: endpoints, request/response examples, errors, deployment.
- `backend/` — Spring Boot 4.0.7, Java 17, Maven wrapper, PostgreSQL + Redis, images via `file-service` and email via `mail-service` (HTTP clients; no direct R2 access, no direct SMTP).
- `frontend/` — Angular 19 (standalone components, SSR + prerender), Tailwind 4, Karma/Jasmine. Deployed to Vercel.
- `docs/` — Vietnamese-language docs. `NGHIEP_VU.md` has business rules and per-module implementation status; `URL_ROUTING_GUIDE.md` has the frontend route tree and FE↔BE URL mapping. `backend/API_TEST_CASES.md` lists every endpoint with example requests. `frontend/design/DESIGN.md` is the design system. Read the relevant one before changing a feature.
- `frontend/_bmad/`, `frontend/_bmad-output/`, `frontend/.agent/` are BMAD agent tooling, not app code. `frontend/*.bak` files are leftovers.

## Commands

Backend (run in `backend/`):

- Run: `./mvnw spring-boot:run` (profile `dev`, port 8080)
- Build: `./mvnw clean package -DskipTests`
- Test: `./mvnw test`; single test: `./mvnw test -Dtest=ClassName#method`. The only test is `BackendApplicationTests` (context load), which needs the DB, Redis and env vars below.

Frontend (run in `frontend/`; both `package-lock.json` and `pnpm-lock.yaml` exist):

- Dev server: `npm start` (http://localhost:4200)
- Build: `npm run build`; serve the SSR build with `npm run serve:ssr:frontend`
- Test: `npm test` (Karma, needs Chrome); single spec: `npx ng test --include='**/auth.service.spec.ts'`

File service (run in `file-service/`): `npm run dev` (tsx watch, port from `PORT`, default 4000), `npm run build` then `npm start`, and `npx tsc --noEmit` to typecheck. There are no unit tests; `npm run case` (see `scripts/api-cases.mts`) lists step-by-step API cases that hit the running server and the real R2 buckets, e.g. `npm run case -- pub-presign pub-put`, `npm run case -- all`, `npm run case -- cleanup`. It targets `API_BASE` or `http://localhost:$PORT`, prints each request/response, and keeps keys between steps in the gitignored `scripts/.api-state.json`. The same cases exist as a Postman collection in `file-service/postman/` (API keys left blank in it).

No lint tooling is configured in any app.

## Backend configuration

- `application.yaml` activates the `dev` profile. The Dockerfile sets `SPRING_PROFILES_ACTIVE=prod`.
- Secrets come from env vars, and `spring.config.import: optional:file:.env[.properties]` loads a gitignored `backend/.env`. Startup fails if any placeholder is unset: `DEV_DB_URL/USERNAME/PASSWORD`, `SECRET_KEY`, `GOOGLE_CLIENT_ID/SECRET`, `FACEBOOK_CLIENT_ID/SECRET`, `MAIL_SERVICE_API_KEY` (an API key with scope `mail:send` from mail-service; `MAIL_SERVICE_URL` defaults to `http://localhost:4000/api`, which clashes with file-service's default port, so run one of them on another `PORT`), `FILE_SERVICE_API_KEY` (the blog's `API_KEY_BLOG` in file-service; `FILE_SERVICE_URL` defaults to `http://localhost:4000`). `backend/.env.example` lists the file-service variables. Prod uses `PROD_DB_*` and `REDIS_URL` instead of the dev DB and localhost Redis. Dev expects Redis on `localhost:6379`.
- `ddl-auto: update` in both profiles and there are no migrations, so entity changes alter the schema directly.
- `spring-boot-starter-elasticsearch` is in the pom but not used in code. Blog search goes through JPA (`GET /api/v1/blogs?keyword=`).

## Backend architecture

Package root `com.blog.backend`, modular by domain: `identity`, `content`, `interaction`, `storage`, `notification`, `admin`, `statistics`, `health`, plus cross-cutting `infrastructure/` (Redis cache config, seeders). `recommendation` is planned but has no code.

- Modules follow `api` (controllers, `dto`) → `application` (services; `interaction` and `admin` split into `itf`/`impl`) → `domain` (entity, enums, repository, exception, event) → `infrastructure` (identity: JWT/OAuth2/security config; storage: `fileservice/` HTTP client).
- **Modules talk to each other through Spring application events, not direct service calls.** The publisher defines the event in its own `domain/event` package and the consumer has a listener. Examples: `BlogModerationEvent` → `notification/application/listener` (in-app notification + email); `BlogImagesActivatedEvent` and `ProfileImageConfirmEvent` → `storage/application/listener`. These two are synchronous request/response events: the listener writes the result (permanent URLs) back onto the event. Follow this pattern for cross-module features.
- **Storage / uploads:** `POST /api/v1/file` maps the frontend `folder` (`blog/temp`, `blog/thumbnails` → `post-image`; `profile/avatar` → `avatar`) to a file-service `presign` and returns the old `UploadPostResponse` shape (`publicUrl` = `previewUrl`), so the frontend PUTs straight to R2 unchanged. Uploaded files sit in file-service `temp/` (cleared by a bucket lifecycle rule), so `BlogServiceImpl.createBlog/updateBlog` (drafts too) and `AuthServiceImpl.handleAvatarUpdate` confirm temp URLs before saving and store the permanent URLs; a failed confirm aborts the save. The client is a `@HttpExchange` interface (`storage/infrastructure/fileservice/FileServiceApi`) built in `FileServiceClientConfig` with the `x-api-key` header from `FileServiceProperties` (`file-service.*` in `application.yaml`); file-service errors become `FileServiceException` (400 / 500 bad key / 503 unreachable).
- **Auth:** `SecurityConfig` (`identity/infrastructure/config`) adds `CookieJwtAuthenticationFilter` and also accepts Bearer JWTs as a resource server. Login sets the tokens as cookies. CORS origins come from `app.cors.allowed-origins`, falling back to `app.frontend.url`. RBAC is `User` ↔ `UserRole` ↔ `Role`, and only `ACTIVE` role assignments count. `/api/v1/admin/**` requires ADMIN.
- **Content moderation:** new blogs are `PENDING` unless saved as `DRAFT`. Admins move them to `PUBLISHED` or `REJECTED` (with a reason). Deletes of blogs and comments are soft.
- All responses use `ApiResponse` (`{data, message, code}`) and `GlobalExceptionHandler` handles errors. Both live in `notification/api` but are shared by every module.
- Caching: Redis through `@EnableCaching` (`infrastructure/config/RedisCacheConfig`); `HomeStatsService` is the only `@Cacheable` user.
- Seeders: `DataSeeder.run` is fully commented out, so nothing seeds sample data today. `DatabaseSeeder` (`@Profile("dev")`, uses datafaker) is an in-progress replacement; check which one is active before relying on sample data.
- Lombok runs as an annotation processor configured per-execution in `pom.xml`; keep those `maven-compiler-plugin` executions intact.

## Frontend architecture

- Routes in `src/app/app.routes.ts` lazy-load with `loadComponent` / `loadChildren` into per-area `*.routes.ts` files (`admin`, `profile`, `auth`, `blog`, `search`, `settings`). `core/guards` has `authGuard` and `adminGuard`.
- `core/interceptors/api.interceptor.ts` prefixes any relative URL with `environment.apiUrl` and sets `withCredentials: true` (cookie auth), so services call relative paths like `api/v1/blogs`. Dev API is `http://localhost:8080`; prod is `https://api.thanhblog.online`. `error.interceptor.ts` handles errors globally.
- `core/services` has one service per backend area; `core/models` has the DTO types.
- Backend and frontend integration is partial: `docs/NGHIEP_VU.md` records which pages still use mock data. Check it before assuming an API call exists.
- SSR/prerender is on in `angular.json`, so browser-only APIs (`window`, `localStorage`) must be guarded with `isPlatformBrowser`, as the guards and `app.config.ts` do.
- Markdown editing and rendering use `ngx-markdown` + `marked` + `prismjs` (see `frontend/docs/markdown-editor.md`).
