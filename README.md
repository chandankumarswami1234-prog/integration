# Social Media Platform — Monorepo

This repo holds the entire platform: backend, and later the mobile frontend, in one place.

```
integration/
├── backend/        Spring Boot 3 / Java 21 API (Phase 1: done - foundation + auth)
├── frontend/        React Native / Expo mobile app (coming in a later phase)
├── render.yaml       Render Blueprint - deploys backend/ automatically
└── .github/workflows/ci-cd.yml   Tests + deploy pipeline
```

## Where to look

- **Backend setup, local run, and deploy instructions:** see [`backend/README.md`](./backend/README.md)
- **Deployment:** this repo's root `render.yaml` tells Render to build from `backend/` — connect this repo (not a subfolder) as a Render Blueprint.
- **CI/CD:** `.github/workflows/ci-cd.yml` runs backend tests on every push/PR and triggers a Render deploy on `main` once tests pass.

## Why one repo

Keeping backend and frontend together makes sense here since they version and deploy in lockstep for now, and it's simpler to manage as a solo/small-team project. If this grows into separate teams later, splitting into multiple repos is a clean, well-understood refactor — nothing here locks that in.
