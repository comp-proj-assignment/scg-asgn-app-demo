# comp-proj-app-demo — Setup

Spring Boot backend (`app/backend`) + Next.js frontend (`app/frontend`).
CI uses the reusable workflow from `comp-proj-shared-lib`.
Deploys land via `comp-proj-app-demo-deployment`.

## Prerequisites

- [ ] Java 21 (`sdk install java 21-tem`)
- [ ] Node 20+ (`nvm install 20`)
- [ ] Maven (or use `./mvnw` once present)
- [ ] Docker, for image builds
- [ ] `comp-proj-shared-lib` has a `v1` tag (see its SETUP.md)

## 1. Run locally

**Backend:**
```bash
cd app/backend
mvn spring-boot:run
# health: http://localhost:8080/actuator/health
# metrics: http://localhost:8080/actuator/prometheus
```

**Frontend:**
```bash
cd app/frontend
npm install
npm run dev
# http://localhost:3000
```

Verify the frontend can reach the backend (set the API base URL via
env or `next.config.js`).

## 2. Run the test suites

```bash
# backend
cd app/backend && mvn test

# frontend
cd app/frontend && npm test
```

Coverage threshold expected by CI is 80% (see `examples/spring-boot-ci.yml`
in shared-lib). Tighten or relax via the `coverage_threshold` input.

## 3. Wire CI

The repo doesn't yet have `.github/workflows/ci-backend.yml` or
`ci-frontend.yml` (only old skeletons exist — review and replace).
Use the examples from shared-lib as the starting point:

- [ ] Copy `comp-proj-shared-lib/examples/spring-boot-ci.yml`
      → `.github/workflows/ci-backend.yml`. Adjust paths.
- [ ] Copy `comp-proj-shared-lib/examples/nextjs-ci.yml`
      → `.github/workflows/ci-frontend.yml`. Adjust paths.
- [ ] In repo Settings → Secrets, add `DEPLOY_REPO_PAT`
      (a PAT with `repo` scope on `comp/comp-proj-app-demo-deployment`)

## 4. First image push

The first push to `main` should:

1. Build the image
2. Push to `ghcr.io/comp/api:<sha>` (and `…/web:<sha>`)
3. Open a PR in the deploy repo updating `apps/api/envs/dev/version.yml`

**Registry visibility (Phase 1: public).** After the first push, go to
GitHub → org `comp` → Packages → `api` → Package settings → change
visibility to **Public**. Same for `web`. This lets EKS nodes pull
anonymously — no `imagePullSecrets`, no ECR, no Nexus credentials.

> If GHCR pushes fail with 403, the workflow's `GITHUB_TOKEN` is
> missing `packages: write` permission. Add it to the job's
> `permissions:` block.

If step 3 fails with 403, the `DEPLOY_REPO_PAT` is missing or scoped wrong.

## 5. Release-cut workflow (once you have a v3.3 candidate)

When dev is stable, cut a release branch — that's the trigger for
the deploy repo's `envs/qa/version.yml` to update.

```bash
git checkout -b release/v3.3.x main
git push -u origin release/v3.3.x
```

The shared-lib's `release-cut.yml` watches for branches matching
`release/**` and bumps the QA version pointer.

## You're done when

- [ ] Both services run locally and respond on their health endpoints
- [ ] CI is green on `main`
- [ ] An image tag corresponding to your last commit exists at
      `ghcr.io/comp/api` and `ghcr.io/comp/web`
- [ ] The deploy repo has an open or merged PR titled
      "bump api/web to <sha>" from the bot
