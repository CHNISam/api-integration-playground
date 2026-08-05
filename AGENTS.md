# AGENTS.md

This file applies to the entire repository unless a closer instruction file overrides it.

## Repository purpose and boundaries

This is a truthful portfolio repository demonstrating practical OAuth, webhook-security, and Meta Conversions API integration work for a junior full-stack developer. Favor clear, testable monoliths over microservices, queues, Kubernetes, CQRS, event sourcing, or speculative abstractions.

The three applications are independent:

- `oauth-flow-demo/`: GitHub OAuth authorization-code flow plus a local refresh-token lab.
- `webhook-hmac-guard/`: generic signed webhook receiver with replay protection and idempotency.
- `meta-capi-harness/`: purchase-event builder and real Meta CAPI test-event sender.

Do not introduce runtime dependencies between these applications. Do not claim production experience, clients, traffic volume, or external verification that is not evidenced by this repository.

## Stack and commands

- Java 17 and Spring Boot 3.5.x; Maven builds each backend.
- Vue 3 and TypeScript with Vite build each frontend.
- MySQL is the documented runtime database; H2 may be used only for automated tests.
- Run all backend tests from the root with `mvn test`.
- Run a frontend with `npm.cmd install`, then `npm.cmd run build` from its `frontend/` directory.
- Run an application with its local `docker compose up --build` instructions.

Use commands verified from `pom.xml`, `package.json`, CI, or project documentation. Do not silently switch package managers or framework versions.

## Implementation workflow

1. Read the relevant specification, source, tests, configuration, and README.
2. Confirm the expected behavior from the specification before encoding it.
3. Add focused tests for security-sensitive and business-critical logic before or with implementation.
4. Implement the smallest coherent solution and preserve each application's independence.
5. Run focused tests, related module tests, frontend builds, then the root suite when feasible.
6. Review the diff and report exact checks, omissions, credential-dependent validation, and remaining risk.

Never weaken, skip, or delete a test just to make a run pass. Obtain confirmation before changing acceptance criteria, PII handling, authentication boundaries, event semantics, or destructive migrations.

## Security and data rules

- Never commit real OAuth secrets, access tokens, Meta tokens, test event codes, customer data, email addresses, or phone numbers.
- Document credentials in `.env.example`; keep `.env` and local overrides ignored.
- Keep OAuth client secrets and provider tokens on the backend.
- Use constant-time comparison for webhook signatures and bind the signature to the timestamp and raw request body.
- Normalize and SHA-256 hash Meta customer information before transmission; never include plaintext PII in logs or persisted payload previews.
- Treat real-provider verification as credential-dependent. A mock or local lab may support automated tests but must not be described as proof of a real external call.

## Git workflow

- Before any branch, commit, push, pull, merge, rebase, PR, tag, release, CI/CD, or Git-hook operation, completely read and follow the installed `git-workflow` skill.
- Use controlled GitFlow:
  - `main` contains only releasable, traceable versions. Do not develop, commit, or push directly on `main`.
  - Create `feature/*` from `develop`; after relevant and full checks pass, merge it into `develop`. A PR is optional for solo work and required when review or collaboration is requested.
  - Create `release/*` from `develop`; merge to `main` only after release validation and explicit user approval, then merge the release result back to `develop`.
  - Create `hotfix/*` from `main`; after validation, merge it into both `main` and `develop` with explicit release approval.
- Use small, natural Conventional Commits: `<type>[scope]: <description>`.
- Preserve unrelated and uncommitted changes.
- Inspect status, branch, remote, and exact targets before Git mutations.
- Do not rewrite shared history. Do not discard work, delete refs, force-push, tag, release, or deploy without explicit authorization.
- Verify branch, tracking relationship, working tree, and remote results with fresh output before claiming a Git operation completed.
- Keep commits organized by meaningful delivery stages: scaffold, core flows, tests/hardening, and documentation/evidence.

## UI and documentation

Use an “Integration Forensics Lab” visual language: dark technical workbench, warm diagnostic accent, crisp status colors, dense but readable trace panels, and restrained motion. Avoid generic SaaS cards and purple-gradient AI styling.

Each project README must include purpose, architecture or sequence Mermaid diagram, setup, verified curl or UI walkthrough, common failure modes in “symptom → cause → fix” form, test commands, credential boundaries, and honest evidence status. Screenshots belong in `docs/screenshots/` and must come from a running build; do not fabricate provider dashboards or successful external events.

Reuse an installed browser or the Codex Chrome connector before downloading browser binaries or drivers; any download requires explicit user authorization. Before saving localhost screenshots, verify the exact URL, port owner, page title, and a distinctive DOM marker. An HTTP 200 response alone is insufficient because development servers may move to another port when the requested port is occupied.

## Completion criteria

- All three applications build independently and their core tests pass.
- Frontends type-check and build.
- Root and project READMEs are complete and truthful.
- `.gitignore` excludes secrets and generated outputs.
- Secret scanning covers the working tree and Git history before delivery.
- Final reporting distinguishes locally verified behavior from GitHub/Meta credential-dependent checks.
