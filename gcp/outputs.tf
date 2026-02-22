output "cloud_run_url" {
  value = google_cloud_run_v2_service.svc.uri
}

output "artifact_repo" {
  value = google_artifact_registry_repository.repo.repository_id
}

output "redis_host" {
  value = google_redis_instance.redis.host
}

output "cloudsql_instance" {
  value = google_sql_database_instance.pg.connection_name
}