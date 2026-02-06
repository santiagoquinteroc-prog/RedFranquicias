## Franchises API

### Requisitos
- Java 17
- Docker
- Docker Compose

### Levantar todo con Docker (app + base de datos)
Desde la raíz del proyecto:
```bash
docker-compose up --build
```
Esto va a:
- Construir la imagen de la aplicación usando el `Dockerfile`
- Levantar el contenedor de la API en `http://localhost:8080`
- Levantar MySQL en el puerto `3307` de tu máquina (internamente `3306` en el contenedor)

Para detener y limpiar contenedores/volúmenes:
```bash
docker-compose down
```

### Ejecutar localmente (sin Docker para la app)
1. Levantar solo la base de datos con Docker:
   ```bash
   docker-compose up -d mysql
   ```
2. Ejecutar la aplicación con Gradle:
   ```bash
   ./gradlew bootRun
   ```

La app se expone en `http://localhost:8080` y se conecta al MySQL de Docker mediante R2DBC.

### Swagger UI
- `http://localhost:8080/swagger-ui.html`

### Tests
```bash
./gradlew test
```

### Estructura del Proyecto

El proyecto sigue Clean Architecture con capas principales:

- **Domain Layer**: Entidades de negocio, lógica de validación y excepciones de dominio
- **Application Layer**: Casos de uso (lógica de negocio) e interfaces de puertos
- **Infrastructure Layer**: Repositorios R2DBC, adaptadores y configuración
- **Entrypoint Layer**: Handlers HTTP usando Spring WebFlux Router Functions

### Endpoints

- `POST /franchises` - Crear franquicia
- `PUT /franchises/{id}` - Actualizar nombre de franquicia
- `POST /franchises/{franchiseId}/branches` - Crear sucursal
- `PUT /franchises/{franchiseId}/branches/{branchId}` - Actualizar nombre de sucursal
- `POST /franchises/{franchiseId}/branches/{branchId}/products` - Crear producto
- `PUT /franchises/{franchiseId}/branches/{branchId}/products/{productId}` - Actualizar nombre de producto
- `PATCH /franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` - Actualizar stock de producto
- `DELETE /franchises/{franchiseId}/branches/{branchId}/products/{productId}` - Eliminar producto
- `GET /franchises/{franchiseId}/branches/top-products` - Obtener productos top por sucursal
