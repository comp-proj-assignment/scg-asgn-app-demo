# comp-proj-shared-lib

Reusable GitHub Actions workflows and composite actions for COMP application pipelines.
Single source of truth for CI/CD across all COMP services.

## What This Repo Contains

```
comp-proj-shared-lib/
├── .github/workflows/
│   ├── service-pipeline.yml          # Main reusable pipeline
│   ├── promote-environment.yml       # Env promotion workflow
│   └── release-cut.yml               # Release branch creation
├── actions/
│   ├── setup-language/               # Setup Java/Node/Python
│   ├── run-tests/                    # Lint/test/typecheck
│   ├── build-language/               # mvn package / pnpm build
│   ├── build-image/                  # Docker build & push
│   ├── scan-image/                   # Trivy vulnerability scan
│   ├── sign-image/                   # Cosign keyless signing
│   ├── bump-deploy-repo/             # GitOps update
│   └── notify-slack/                 # Notifications
├── examples/
│   ├── spring-boot-ci.yml            # Reference for consumers
│   └── nextjs-ci.yml
└── docs/
    ├── INPUTS.md                     # All workflow inputs
    └── MIGRATION.md                  # How to adopt
```

## Quick Start (For Consumer Repos)

Add to your service repo's `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [main, 'release/**']
  pull_request:

jobs:
  pipeline:
    uses: comp/comp-proj-shared-lib/.github/workflows/service-pipeline.yml@v1
    with:
      service_name: api
      service_path: '.'
      language: java                  # java | node | python | go
      coverage_threshold: 80
    secrets: inherit
```

That's it — your service has full CI/CD.

## Pipeline Stages

```
Push → Pre-Build → Coverage → Build → Scans → GitOps → Deploy
        (parallel)
        - format
        - lint
        - typecheck
        - unit tests
                        ↓
                   Coverage gate
                        ↓
                  Language build (mvn/pnpm)
                        ↓
                  Docker build & push
                        ↓
              Parallel: Trivy + DAST + Cosign
                        ↓
             Bump deploy repo (kustomize edit)
                        ↓
              Argo CD auto-syncs to DEV
```

Total: ~25-30 min push → deployed.

## Required Inputs

| Input | Type | Description |
|-------|------|-------------|
| `service_name` | string | Service identifier (e.g., `api`) |
| `service_path` | string | Path to service code (e.g., `.` or `apps/api`) |
| `language` | string | One of: `java`, `node`, `python`, `go` |

## Common Optional Inputs

| Input | Default | Description |
|-------|---------|-------------|
| `coverage_threshold` | `80` | Min coverage % |
| `target_env` | `dev` | Auto-deploy target env |
| `enable_image_scan` | `true` | Trivy scan |
| `enable_image_sign` | `true` | Cosign signing |
| `enable_dast_scan` | `true` | OWASP ZAP scan |
| `enable_sbom` | `true` | Generate SBOM |

See `docs/INPUTS.md` for complete list (40+ inputs).

## Required Secrets (Consumer Repo)

Configure in `Settings > Secrets and variables > Actions`:

| Secret | Required | Description |
|--------|----------|-------------|
| `DEPLOY_REPO_PAT` | Yes | PAT for deploy repo (write access) |
| `SLACK_WEBHOOK` | Optional | Slack notifications |
| `NEXUS_USERNAME` | If using Nexus | Nexus credentials |
| `NEXUS_PASSWORD` | If using Nexus | Nexus credentials |

Use `secrets: inherit` to pass all to reusable workflow.

## Versioning

| Tag | Use Case |
|-----|----------|
| `@main` | Bleeding edge (dev/test only) |
| `@v1` | **v1.x.x latest (recommended)** |
| `@v1.2.3` | Pinned (strict environments) |

## Local Development

Test workflow changes locally before pushing:

```bash
# Install act (run GitHub Actions locally)
brew install act

# Test the reusable workflow
act -W .github/workflows/service-pipeline.yml \
    --input service_name=api \
    --input language=java
```

## Adding a New Language

1. Update `actions/setup-language/action.yml` — add language case
2. Update `actions/run-tests/action.yml` — add test commands
3. Update `actions/build-language/action.yml` — add build steps
4. Add example to `examples/`
5. Update `docs/INPUTS.md`
6. Tag new minor version

## Adding a New Pipeline Stage

1. Create composite action in `actions/<stage-name>/action.yml`
2. Reference in `.github/workflows/service-pipeline.yml`
3. Add feature toggle input (default: enabled)
4. Document in `docs/INPUTS.md`
5. Test on a non-critical service first

## Standards Compliance

- **OpenSSF Scorecard**: pinned action SHAs, branch protection
- **SLSA Level 3**: provenance + cosign signing
- **OWASP CI/CD Top 10**: minimum permissions, secret scanning
- **Conventional Commits**: enforced via commitlint

## Repository Conventions

- Branch protection on `main`: 1 reviewer, status checks required
- Tag releases via `gh release create v1.2.3 --generate-notes`
- Breaking changes bump major version (v1 → v2)
- Document breaking changes in `MIGRATION.md`

## Support

- **Issues**: open in this repo
- **Slack**: `#platform-engineering`
- **Owner**: `@comp/platform-team`

## License

Internal use only — COMP.