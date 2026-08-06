# Docker Deployment Guide for Page Search API

This guide covers building, publishing, and deploying the Page Search API using Docker containers.

## Overview

The Page Search API is containerized and published to Docker Hub. This allows for:
- Consistent deployments across all environments
- Easy versioning and rollback
- Simplified dependency management (no shared Apache Tomcat)
- Configuration via environment variables

## Dependencies

**Solr**: This API requires a Solr instance to connect to. Solr configuration and deployment is managed in a separate repository:
- [arquivo-solr-tools](https://github.com/arquivo/arquivo-solr-tools) - Contains Solr image definitions, configuration, and deployment scripts for both page and image search indexes.

## Prerequisites

- Docker and Docker Compose installed
- For local builds: Maven 3.6+ and JDK 8+
- Docker Hub account with push access (for publishing images)
- Access to a Solr instance (dev, staging, or production)

## Automatic CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/docker-build.yml`) automatically:

1. **Builds** the Maven project on push to `master`, `main`, or `development` branches
2. **Builds** Docker image using the compiled WAR file
3. **Pushes** to `arquivo/page-search-api` on Docker Hub with tags:
   - `latest` (for master/main)
   - Branch name (e.g., `development`)
   - Semantic version (for git tags like `v1.0.0`)
   - Short commit SHA

### Docker Hub Secrets Setup

Add these secrets to your GitHub repository:
- `DOCKERHUB_USERNAME`: Your Docker Hub username
- `DOCKERHUB_TOKEN`: Your Docker Hub personal access token

## Local Development & Validation

### Build Locally

The Dockerfile uses a multi-stage build: Maven compiles the WAR in the first stage, then it's
run standalone on its embedded (provided-scope) Tomcat in a plain JRE image. The build context
must be the repository root, since `page-search-api` depends on the sibling `utils` reactor
module.

```bash
cd page-search
docker build -f page-search-api/Dockerfile -t arquivo/page-search-api:local .
```

### Run with Docker Compose

The API requires a Solr instance to connect to. Configure the `SOLR_URL` environment variable to point to your Solr instance.

#### Option 1: Export environment variable (quick testing)

```bash
export SOLR_URL=http://solr-dev-host:8983/solr
docker-compose up -d
```

Or as a one-liner:
```bash
SOLR_URL=http://solr-dev-host:8983/solr docker-compose up -d
```

#### Option 2: Use `docker-compose.override.yml` (recommended for local development)

Create `docker-compose.override.yml` in the repo root:

```yaml
services:
  page-search-api:
    environment:
      SOLR_URL: http://solr-dev-host:8983/solr
      JAVA_OPTS: -Xmx4g -Xms2g
```

Add to `.gitignore`:
```
docker-compose.override.yml
```

Then simply run:
```bash
docker-compose up -d
```

Docker Compose automatically merges this file with `docker-compose.yml`.

#### Option 3: Use environment file

Create `.env.local`:
```
SOLR_URL=http://solr-dev-host:8983/solr
JAVA_OPTS=-Xmx4g -Xms2g
```

Add to `.gitignore`:
```
.env.local
```

Then run:
```bash
docker-compose up -d
```

### Managing Services

```bash
# View logs
docker-compose logs -f page-search-api

# Stop services
docker-compose down

# Restart services
docker-compose restart page-search-api
```

API will be available at `http://localhost:8080`

## Environment Deployment

The `docker-compose.yml` is a generic template for local validation. For environment-specific deployments:

1. Manage environment-specific `docker-compose.yml` files in your Ansible repository
2. Configure `SOLR_URL` to point to your environment's Solr instance
3. Override environment variables as needed for each environment
4. Use the same Docker image across all environments

**Note:** Solr is managed separately in the [arquivo-solr-tools](https://github.com/arquivo/arquivo-solr-tools) repository and should be deployed independently.

### Example: Configuring for Preprod

Create a `docker-compose.yml` in your Ansible repository:

```yaml
version: '3.8'

services:
  page-search-api:
    image: arquivo/page-search-api:v1.2.3
    environment:
      SOLR_URL: http://solr-preprod:8983/solr
      JAVA_OPTS: -Xmx4g -Xms2g
    # ... rest of config
```

### Example: Configuring for Production

```yaml
version: '3.8'

services:
  page-search-api:
    image: arquivo/page-search-api:v1.2.3
    environment:
      SOLR_URL: http://solr-prod:8983/solr
      JAVA_OPTS: -Xmx8g -Xms4g
    # ... rest of config
```

## Container Configuration

### Health Checks

Health checking is configured in `docker-compose.yml`, not baked into the Docker image. The
default check is a shallow TCP liveness probe against port 8080 (via bash's `/dev/tcp`, no extra
tools required):

```yaml
healthcheck:
  test: ["CMD-SHELL", "cat < /dev/null > /dev/tcp/127.0.0.1/8080 || exit 1"]
  interval: 30s
  timeout: 3s
  retries: 3
  start_period: 5s
```

This intentionally only checks that Tomcat is accepting connections — it does not exercise
business logic or the Solr backend. A "deep" check (e.g. hitting `/textsearch`) could return
errors, or simply be slow, whenever Solr itself is overloaded rather than broken, which risks
Docker restarting an otherwise-healthy API container in a cascading fashion. Since the check
lives in `docker-compose.yml` rather than the image, each environment's Ansible-managed compose
file can override it with different semantics if that environment needs something stricter.

### Environment Variables

All configuration is done via environment variables passed to the container at runtime.

| Variable | Default | Description |
|----------|---------|-------------|
| `NUTCHWAX_SEARCH_FILE` | `/app/` | Path to search servers configuration |
| `SOLR_URL` | `http://solr:8983/solr` | Solr server URL |
| `SERVER_PORT` | `8080` | API server port |
| `JAVA_OPTS` | `-Xmx2g -Xms512m` | JVM memory and options |

### Port Mapping

| Service | Default Port | Description |
|---------|--------------|-------------|
| API | 8080 | Page Search API (Swagger UI at `/textsearch/api-docs`, OpenAPI spec at `/textsearch/api-docs/v3`) |
| Solr | 8983 | Solr search server |

## Management Commands

### View Logs

```bash
docker-compose logs -f page-search-api
```

### Check Status

```bash
# View running containers (includes health status)
docker-compose ps

# Manual port check
docker-compose exec page-search-api bash -c 'cat < /dev/null > /dev/tcp/127.0.0.1/8080 && echo OPEN || echo CLOSED'
```

### Update Image

```bash
# Pull latest version
docker-compose pull

# Restart with new image
docker-compose up -d --force-recreate
```

### Cleanup

```bash
# Stop services
docker-compose down

# Remove volumes (careful - deletes data)
docker-compose down -v

# Remove all unused Docker images
docker image prune -a
```

## Publishing New Versions

### Automatic (Recommended)

1. Push to `master` branch or create a git tag:
   ```bash
   git tag v1.2.3
   git push origin v1.2.3
   ```
2. GitHub Actions automatically builds and pushes to Docker Hub

### Manual

```bash
docker build -f page-search-api/Dockerfile -t arquivo/page-search-api:v1.2.3 .
docker push arquivo/page-search-api:v1.2.3
```

## Troubleshooting

### Container fails to start

```bash
# Check logs
docker-compose logs page-search-api

# Check health status
docker inspect <container_id> | grep -A 20 Health
```

### Connection issues to Solr

```bash
# Verify connectivity from inside the container
docker-compose exec page-search-api curl $SOLR_URL/admin/cores
```

### Out of memory errors

Adjust `JAVA_OPTS` environment variable in your deployment configuration:

```yaml
environment:
  JAVA_OPTS: -Xmx8g -Xms4g
```

## Advanced Configuration

### Custom Java Options

Override at runtime:

```bash
export JAVA_OPTS="-Xmx4g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
docker-compose up -d
```

### Using External Solr Instance

```bash
export SOLR_URL=http://external-solr-host:8983/solr
docker-compose up -d
```
