# Franchises API

## Requisitos
- Java 17
- Docker

## MySQL local
```bash
docker compose up -d
```

## Ejecutar
```bash
./gradlew bootRun
```

## Swagger UI
- http://localhost:8080/swagger-ui.html

## Tests
```bash
./gradlew test
```

## Estructura del Proyecto

El proyecto sigue Clean Architecture con tres capas principales:

- **Domain Layer**: Entidades de negocio, lógica de validación y excepciones de dominio
- **Application Layer**: Casos de uso (lógica de negocio) e interfaces de puertos
- **Infrastructure Layer**: Repositorios R2DBC, adaptadores y configuración
- **Entrypoint Layer**: Handlers HTTP usando Spring WebFlux Router Functions

## Endpoints

- `POST /franchises` - Crear franquicia
- `PUT /franchises/{id}` - Actualizar nombre de franquicia
- `POST /franchises/{franchiseId}/branches` - Crear sucursal
- `PUT /franchises/{franchiseId}/branches/{branchId}` - Actualizar nombre de sucursal
- `POST /franchises/{franchiseId}/branches/{branchId}/products` - Crear producto
- `PUT /franchises/{franchiseId}/branches/{branchId}/products/{productId}` - Actualizar nombre de producto
- `PATCH /franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` - Actualizar stock de producto
- `DELETE /franchises/{franchiseId}/branches/{branchId}/products/{productId}` - Eliminar producto
- `GET /franchises/{franchiseId}/branches/top-products` - Obtener productos top por sucursal
