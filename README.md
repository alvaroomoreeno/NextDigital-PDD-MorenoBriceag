# Travel Agency Kata

Aplicación Spring Boot para gestionar clientes de una agencia de viajes. El proyecto utiliza Java 21, Maven, Spring Web y Spring Data JPA, organizado en capas siguiendo una arquitectura hexagonal.

## Requisitos previos

- JDK 21.
- Apache Maven 3.9.8 o superior.
- Docker (opcional, únicamente para construir y ejecutar la imagen).

Comprueba las versiones instaladas:

```bash
java -version
mvn -version
```

## Instalación y ejecución local

Clona el repositorio y accede al directorio del proyecto. Después, compila y ejecuta las pruebas:

```bash
mvn clean test
```

Para generar el artefacto ejecutable:

```bash
mvn clean package
```

Para iniciar la aplicación:

```bash
mvn spring-boot:run
```

La aplicación queda disponible en el puerto `8080`.

## API REST

La API permite crear clientes y consultarlos por identificador o número de pasaporte.

### Crear un cliente

`PUT /customers`

Ejemplo:

```bash
curl -i -X PUT http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ada",
    "surnames": "Lovelace",
    "birthDate": "1990-05-20",
    "passportNumber": "P123456"
  }'
```

La respuesta satisfactoria es `201 Created` e incluye la cabecera `Location` con la URI del cliente creado.

### Consultar un cliente por identificador

`GET /customers/{customer-id}`

```bash
curl -i http://localhost:8080/customers/{customer-id}
```

Devuelve `200 OK` si encuentra el cliente o `204 No Content` si no existe.

### Consultar un cliente por pasaporte

`GET /customers?passport-number={passport-number}`

```bash
curl -i "http://localhost:8080/customers?passport-number=P123456"
```

Devuelve el cliente encontrado o `204 No Content` si no hay coincidencias.

## Swagger / OpenAPI

Con la aplicación en ejecución:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Especificación OpenAPI: http://localhost:8080/v3/api-docs

## Persistencia y consola H2

La configuración de desarrollo utiliza una base de datos H2 en memoria con `MODE=PostgreSQL`. Flyway gestiona el esquema mediante las migraciones de `src/main/resources/db/migration`.

Consola H2: http://localhost:8080/h2-console

Parámetros de conexión:

| Parámetro | Valor |
|---|---|
| JDBC URL | `jdbc:h2:mem:testdb;MODE=PostgreSQL` |
| Usuario | `sa` |
| Contraseña | Vacía |

**Nota:** H2 en modo PostgreSQL facilita pruebas de compatibilidad, pero no es un servidor PostgreSQL. Al ser una base de datos en memoria, los datos no se conservan al detener la aplicación.

## Docker

Primero genera el JAR:

```bash
mvn clean package
```

Construye la imagen desde la raíz del repositorio:

```bash
docker build -t travel-agency-kata .
```

Ejecuta el contenedor:

```bash
docker run --rm -p 8080:8080 travel-agency-kata
```

La aplicación estará disponible en http://localhost:8080. Swagger UI también estará accesible en http://localhost:8080/swagger-ui/index.html.

## Estructura del proyecto

- `domain`: modelo y conceptos centrales del dominio.
- `application`: casos de uso y puertos que definen las operaciones requeridas.
- `infrastructure`: adaptadores de persistencia, entidades JPA, repositorios y API REST.
- `src/test`: pruebas de integración y ejercicios de patrones de diseño.

El adaptador `CustomersJPAAdapter` implementa el puerto `CustomersRepository` y traduce entre el modelo de dominio y las entidades JPA.

## Pruebas y CI

Ejecuta las pruebas localmente:

```bash
mvn test
```

El workflow de GitHub Actions utiliza Java 21 y verifica cada cambio con estas etapas:

1. `mvn clean compile`
2. `mvn test`
3. `mvn package`

## Ejercicios y documentación

- [KATA_INSTRUCTIONS.md](KATA_INSTRUCTIONS.md): contexto, requisitos y enunciado original de la kata.
- [RESPONSE.md](RESPONSE.md): análisis de principios SOLID y patrones de diseño identificados en el proyecto.
