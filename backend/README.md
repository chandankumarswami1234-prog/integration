# Social Media Backend — Phase 1+2+3: Foundation, Auth, Posts/Feed, Social Features

Spring Boot 3 / Java 21 backend, with JWT auth, Posts/Feed/Comments/Likes, and Follow/Block/Mute/Search, Dockerized for local dev, deployed on Render.

## What's included

**Phase 1 — Foundation + Auth**
- `POST /api/auth/register`, `/login`, `/refresh`, `/logout`
- `GET /api/health`

**Phase 2 — Posts + Feed**
- `POST /api/posts` — create (drafts, scheduled posts, hashtags, media URLs)
- `PUT /api/posts/{id}` / `DELETE /api/posts/{id}` — edit/delete (owner or admin only)
- `GET /api/posts/{id}` — single post
- `GET /api/posts/feed?sort=latest|trending&page=0&size=20` — public feed
- `GET /api/posts/user/{username}` — a user's published posts
- `GET /api/posts/drafts` — your own drafts
- `GET /api/posts/search?q=keyword` — content search
- `GET /api/posts/hashtag/{tag}` — posts by hashtag
- `POST /api/posts/{id}/like` / `DELETE /api/posts/{id}/like` — like/unlike
- `POST /api/posts/{postId}/comments` — add comment or reply (`parentCommentId`)
- `GET /api/posts/{postId}/comments` — top-level comments (paginated)
- `GET /api/comments/{commentId}/replies` — replies to a comment (paginated)
- `DELETE /api/comments/{commentId}` — delete (owner or admin only)

**Phase 3 — Social Features**
- `GET /api/posts/feed/following` — feed of only users you follow
- `POST /api/users/{username}/follow` / `DELETE /api/users/{username}/follow` — follow/unfollow
- `GET /api/users/{username}/followers` / `/following` — paginated lists
- `POST /api/users/{username}/block` / `DELETE /api/users/{username}/block` — bidirectional: hides content both ways, severs any existing follow
- `POST /api/users/{username}/mute` / `DELETE /api/users/{username}/mute` — one-directional, only affects your own feed
- `GET /api/users/{username}` — profile view (follower/following counts, whether you follow them)
- `GET /api/users/search?q=keyword` — search by username or full name

Blocked/muted authors are automatically excluded from feed, search, and hashtag results. A block (either direction) also makes that user's posts/profile return 404 rather than 403, to avoid confirming block status to the blocked party.

All endpoints require a valid `Authorization: Bearer <accessToken>` header except `/api/auth/**` and `/api/health`.

Not yet built (next phases): messaging, notifications, admin panel, mobile app.

## Run locally

Requires Docker + Docker Compose.

```bash
cd backend
cp .env.example .env
docker compose up --build
```

Backend will be available at `http://localhost:8080`. Check it's alive:

```bash
curl http://localhost:8080/api/health
```

Try registering a user:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","email":"john@example.com","password":"Password123","fullName":"John Doe"}'
```

## Push to GitHub

This backend lives inside a monorepo. Run these commands from the **repo root** (one level up from `backend/`), not from inside `backend/`:

```bash
cd ..    # to the integration/ repo root, if you're inside backend/
git init
git add .
git commit -m "Phase 1: backend foundation + auth"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

## Deploy to Render (free tier)

**Option A — Blueprint (recommended, one click):**

1. Go to [dashboard.render.com](https://dashboard.render.com) → **New** → **Blueprint**
2. Connect your GitHub repo — the whole monorepo, not a subfolder (Render reads `render.yaml` from the repo root, which points it at `backend/` automatically via `rootDir`)
3. Render provisions both the free Postgres database and the web service, wires the env vars together, and deploys
4. First build takes a few minutes; after that, every push to `main` auto-deploys

**Option B — Manual:**

1. **New** → **PostgreSQL** → note the internal connection details
2. **New** → **Web Service** → connect your repo → Runtime: **Docker**
3. Add environment variables from `.env.example`, pointing `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USERNAME`/`DB_PASSWORD` at the database you just created
4. Set health check path to `/api/health`

### Enable the GitHub Actions test gate before deploy

By default Render auto-deploys on every push, without waiting for tests. To only deploy after CI passes:

1. In Render, open your web service → **Settings** → turn **Auto-Deploy** to **"No"**
2. Get a Deploy Hook URL: **Settings** → **Deploy Hook** → copy the URL
3. In your GitHub repo: **Settings** → **Secrets and variables** → **Actions** → **New repository secret**
   - Name: `RENDER_DEPLOY_HOOK_URL`
   - Value: the URL you copied
4. Now every push to `main` runs tests first via `.github/workflows/ci-cd.yml`, and only calls the deploy hook if they pass

### Note on Render's free tier

The free web service spins down after 15 minutes of inactivity and takes ~30-50 seconds to wake on the next request. This is fine for development/demo; if you need always-on later, upgrade that one service to the Starter plan.

## Environment variables reference

See `.env.example` for the full list. In production (Render), never reuse the example `JWT_SECRET` — Render's Blueprint auto-generates a secure one via `generateValue: true`.

## Next steps

Once this is deployed and verified, the next phase adds Post/Comment/Like entities, the feed endpoints, and pagination/filtering/sorting — say the word and we'll keep building on top of this same repo.
