# API Integration Playground

Three runnable integration labs focused on the failure modes that make OAuth, webhooks, and server-side conversion tracking difficult in real projects.

> Backend/API integration developer with one year of hands-on full-stack experience, focused on OAuth, webhooks, and third-party APIs.

## Projects

| Project | What it demonstrates | Status |
| --- | --- | --- |
| [OAuth Flow Demo](oauth-flow-demo/) | Authorization code flow, CSRF state validation, provider errors, durable sessions, and automatic refresh | In development |
| [Webhook HMAC Guard](webhook-hmac-guard/) | Constant-time HMAC verification, replay windows, idempotency, and rejection audit trails | In development |
| [Meta CAPI Harness](meta-capi-harness/) | PII normalization/hashing, event construction, deduplication IDs, and Meta Test Events delivery | In development |

## Technology

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3-42B883?logo=vuedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)

Each subproject is an independent monolith with its own setup, database, tests, troubleshooting guide, and evidence checklist. Run all backend tests with:

```bash
mvn test
```

No real credentials belong in this repository. Copy the relevant `.env.example` to `.env` locally before testing a real provider.
