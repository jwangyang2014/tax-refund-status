terraform {
  required_version = ">= 1.5.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

locals {
  prefix = "${var.environment}-${var.service_name}"
}

# --- Enable required APIs ---
resource "google_project_service" "services" {
  for_each = toset([
    "run.googleapis.com",
    "artifactregistry.googleapis.com",
    "cloudsql.googleapis.com",
    "secretmanager.googleapis.com",
    "redis.googleapis.com",
    "vpcaccess.googleapis.com",
    "compute.googleapis.com",
    "iam.googleapis.com",
  ])
  project = var.project_id
  service = each.value
  disable_on_destroy = false
}

# --- Artifact Registry (Docker) ---
resource "google_artifact_registry_repository" "repo" {
  depends_on = [google_project_service.services]
  location      = var.region
  repository_id = var.artifact_repo
  format        = "DOCKER"
}

# --- Network (only needed for Redis + Serverless VPC Connector) ---
resource "google_compute_network" "vpc" {
  depends_on = [google_project_service.services]
  name                    = "${local.prefix}-vpc"
  auto_create_subnetworks = false
}

resource "google_compute_subnetwork" "subnet" {
  name          = "${local.prefix}-subnet"
  region        = var.region
  network       = google_compute_network.vpc.id
  ip_cidr_range = "10.40.0.0/24"
}

# Serverless VPC connector for Cloud Run -> Redis (private IP)
resource "google_vpc_access_connector" "connector" {
  name          = "${local.prefix}-vpc-conn"
  region        = var.region
  network       = google_compute_network.vpc.name
  ip_cidr_range = "10.8.0.0/28"

  depends_on = [google_project_service.services]
}

# --- Redis (Memorystore) ---
resource "google_redis_instance" "redis" {
  name           = "${local.prefix}-redis"
  region         = var.region
  tier           = "BASIC"
  memory_size_gb = var.redis_memory_gb

  authorized_network = google_compute_network.vpc.id
  redis_version      = "REDIS_7_0"

  depends_on = [google_project_service.services]
}

# --- Cloud SQL (Postgres) ---
resource "google_sql_database_instance" "pg" {
  name             = "${local.prefix}-pg"
  region           = var.region
  database_version = var.db_version

  settings {
    tier = var.db_tier

    # Minimal, public IP enabled (simple). Lock down later if desired.
    ip_configuration {
      ipv4_enabled = true
    }
  }

  depends_on = [google_project_service.services]
}

resource "google_sql_database" "db" {
  name     = var.db_name
  instance = google_sql_database_instance.pg.name
}

# DB user (password comes from Secret Manager)
data "google_secret_manager_secret_version" "db_password" {
  secret  = var.sm_db_password_id
  version = "latest"
}

resource "google_sql_user" "user" {
  name     = var.db_user
  instance = google_sql_database_instance.pg.name
  password = data.google_secret_manager_secret_version.db_password.secret_data
}

# --- Secrets ---
data "google_secret_manager_secret_version" "jwt_secret" {
  secret  = var.sm_jwt_secret_id
  version = "latest"
}

data "google_secret_manager_secret_version" "openai_key" {
  secret  = var.sm_openai_key_id
  version = "latest"
}

# --- Service Account for Cloud Run ---
resource "google_service_account" "run_sa" {
  account_id   = "${local.prefix}-sa"
  display_name = "Taxrefund Cloud Run runtime SA"
}

# Allow Cloud Run to read secrets
resource "google_project_iam_member" "run_sa_secret_access" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:${google_service_account.run_sa.email}"
}

# Allow Cloud Run to connect to Cloud SQL
resource "google_project_iam_member" "run_sa_cloudsql" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.run_sa.email}"
}

# --- Cloud Run (v2) ---
# NOTE: image tag is set to "latest" here. In CI/CD you will deploy immutable tags (sha).
resource "google_cloud_run_v2_service" "svc" {
  name     = local.prefix
  location = var.region

  template {
    service_account = google_service_account.run_sa.email

    scaling {
      min_instance_count = var.min_instances
      max_instance_count = var.max_instances
    }

    vpc_access {
      connector = google_vpc_access_connector.connector.id
      egress    = "ALL_TRAFFIC"
    }

    containers {
      image = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.repo.repository_id}/${var.service_name}:latest"

      ports {
        container_port = var.container_port
      }

      # --- Health checks (Spring Actuator probes) ---
      liveness_probe {
        http_get {
          path = "/actuator/health/liveness"
          port = var.container_port
        }
        initial_delay_seconds = 20
        period_seconds        = 10
      }

      startup_probe {
        http_get {
          path = "/actuator/health/readiness"
          port = var.container_port
        }
        initial_delay_seconds = 5
        period_seconds        = 5
        failure_threshold     = 30
      }

      # --- ENV (matches application.yml placeholders) ---
      env { 
        name = "APP_SECURITY_JWT_ISSUER" 
        value = "refund-status" 
      }

      # Secrets in env (pulled at deploy time by Terraform)
      env { 
        name = "APP_SECURITY_JWT_SECRET"
        value = data.google_secret_manager_secret_version.jwt_secret.secret_data 
      }

      # Redis host/port for your "data.redis.*" usage
      env { 
        name = "REDIS_HOST" 
        value = google_redis_instance.redis.host 
      }
      env { 
        name = "REDIS_PORT" 
        value = "6379" 
      }

      # DB: simplest is public IP here
      env { 
        name = "POSTGRES_DB_USERNAME"
        value = var.db_user
      }
      env { 
        name = "POSTGRES_DB_PASSWORD" 
        value = data.google_secret_manager_secret_version.db_password.secret_data 
      }
      env {
        name  = "POSTGRES_DB_URL"
        value = "jdbc:postgresql://${google_sql_database_instance.pg.ip_address[0].ip_address}:5432/${var.db_name}"
      }

      # AI
      env { 
        name = "AI_PROVIDER"
        value = "mock" 
      }
      # If switching to openai later:
      # env { name = "OPENAI_API_KEY" value = data.google_secret_manager_secret_version.openai_key.secret_data }
      # env { name = "OPENAI_MODEL" value = "gpt-4o-mini" }

      # Rate limit feature flag (optional)
      env { 
        name = "APP_RATELIMIT_ENABLED"
        value = "true" 
      }
    }

    # Concurrency (how many requests per instance)
    max_instance_request_concurrency = var.concurrency
  }

  depends_on = [google_project_service.services]
}

# Public access (for demo). Remove for only authenticated access.
resource "google_cloud_run_v2_service_iam_member" "public_invoker" {
  name     = google_cloud_run_v2_service.svc.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "allUsers"
}