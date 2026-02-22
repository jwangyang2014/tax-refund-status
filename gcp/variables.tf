variable "project_id" { type = string }
variable "region"     { 
  type = string  
  default = "us-central1" 
}

variable "environment" { 
  type = string 
  default = "dev" 
}
variable "service_name" {
  type = string
  default = "taxrefund-backend" 
}

# Artifact Registry repo name (docker)
variable "artifact_repo" { 
  type = string
  default = "taxrefund" 
}

# Cloud Run
variable "container_port" { 
  type = number 
  default = 8080 
}
variable "min_instances"  { 
  type = number 
  default = 0 
}
variable "max_instances"  { 
  type = number
  default = 10 
}
variable "concurrency"    { 
  type = number 
  default = 80 
}

# Cloud SQL
variable "db_name"     { 
  type = string 
  default = "taxrefund" 
}
variable "db_user"     { 
  type = string
  default = "appuser"
}
variable "db_tier"     {
  type = string
  default = "db-f1-micro" 
} # dev/min

variable "db_version"  { 
  type = string 
  default = "POSTGRES_16" 
}

# Redis
variable "redis_memory_gb" { 
  type = number 
  default = 1 
}

# Secret Manager secret IDs
# Each secret should have a "latest" version.
variable "sm_jwt_secret_id" { 
  type = string 
  default = "taxrefund-jwt-secret" 
}
variable "sm_db_password_id" { 
  type = string 
  default = "taxrefund-db-password" 
}
# Optional (only used if AI_PROVIDER=openai)
variable "sm_openai_key_id" { 
  type = string 
  default = "taxrefund-openai-api-key" 
}