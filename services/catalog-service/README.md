# Catalog Service

`catalog-service` is the canonical product catalog service. Product documents are stored in MongoDB, and the HTTP API is protected by the configured OIDC resource server.

## Local development

From the repository root, build the project with `mvn install`. Start the local dependencies with `docker compose --profile documents up -d`, then start the service:

```powershell
$env:MONGO_ROOT_USERNAME = "local-root"
$env:MONGO_ROOT_PASSWORD = "local-mongodb-change-me"
mvn -pl services/catalog-service spring-boot:run -Dspring-boot.run.profiles=local
```

The HTTP API listens on port `8081`; health is available at `/actuator/health`. With the `local` profile, the internal gRPC server listens on port `9091` without TLS for local development only. Outside that profile, configure its server certificate, private key, and client CA certificate.

See the root README and `docs/ARCHITECTURE.md` for the overall service boundaries and local infrastructure.
