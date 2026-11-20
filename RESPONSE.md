# RESPONSE — Principios SOLID y patrones de diseño

Este documento recoge los principios y patrones identificables en la implementación actual de Travel Agency Kata. Los ejemplos hacen referencia al código del repositorio y distinguen los patrones utilizados en la aplicación de los ejercicios de patrones incluidos en los tests.

## 1. Principios SOLID

### S — Single Responsibility Principle (Responsabilidad única)

Cada componente debe tener un motivo principal para cambiar.

- `CustomersController` se ocupa de la interfaz HTTP: recibe las peticiones, construye los casos de uso y traduce sus resultados a respuestas HTTP.
- `CustomerRestMapper` concentra la conversión del modelo de dominio `Customer` al DTO `GetCustomerDTO`. Así, la lógica de transformación no queda mezclada con la gestión de las rutas.
- `CustomersJPAAdapter` se responsabiliza de adaptar el puerto de persistencia del dominio a las operaciones de Spring Data JPA y de convertir entre `Customer` y `CustomerEntity`.

### O — Open/Closed Principle (Abierto/cerrado)

El sistema debe permitir ampliar su comportamiento sin modificar innecesariamente el código ya estable.

- En `BehavioralPatternExercices`, `TravelDocumentationResolver` trabaja con la abstracción `TravelDocumentationStrategy`. Se pueden incorporar nuevas estrategias de documentación implementando esa interfaz, sin introducir un nuevo bloque condicional por cada tipo de viaje en el resolver.
- En `StructuralPatternExercices`, `MenuComponent` permite incorporar nuevos componentes de menú que implementen la misma abstracción y participen en el cálculo agregado.

### L — Liskov Substitution Principle (Sustitución de Liskov)

Las implementaciones de una abstracción deben poder utilizarse allí donde se espera dicha abstracción, respetando su contrato.

- `SameCountryStrategy`, `SchengenTravelStrategy` y `VisaRequiredStrategy` implementan `TravelDocumentationStrategy`; el resolver puede evaluarlas de forma uniforme.
- `Food`, `Dish` y `Menu` implementan `MenuComponent`. Cualquiera de ellos puede formar parte de una composición y responder a `getCalories()`.

### I — Interface Segregation Principle (Segregación de interfaces)

Es preferible definir contratos pequeños y específicos antes que obligar a los consumidores a depender de operaciones que no necesitan.

- `CustomersRepository` define únicamente las operaciones de persistencia de clientes que necesita la capa Application: guardar, consultar por identificador y consultar por pasaporte.
- `EmployeeObserver` expone un único evento de interés, `onEmployeeEnrolled`, y `GreetingSender` representa específicamente el envío de un saludo.

### D — Dependency Inversion Principle (Inversión de dependencias)

Los componentes de alto nivel deben depender de abstracciones, no de detalles concretos de infraestructura.

- Los casos de uso `CreateCustomerCommand` y `GetCustomerQuery` dependen del puerto `CustomersRepository`, no de una implementación concreta de base de datos.
- `CustomersJPAAdapter` implementa ese puerto y encapsula la dependencia de `CustomersJPARepository` y de `CustomerEntity`.
- `CustomersController` recibe sus dependencias mediante inyección de constructor, incluida la abstracción `CustomersRepository`.

## 2. Patrones de diseño observados

### Adapter — Adaptador

**Ubicación:** `infrastructure/persistence/adapter/CustomersJPAAdapter.java`.

Adapta el contrato `CustomersRepository` de Application a la interfaz `CustomersJPARepository` de Spring Data JPA. También transforma los objetos de dominio `Customer` en entidades JPA `CustomerEntity` y viceversa. De este modo, la capa Application no necesita conocer los detalles del ORM.

### Repository — Repositorio

**Ubicación:** `application/port/CustomersRepository.java`, con implementaciones en Infrastructure.

El puerto define las operaciones de acceso a clientes. `CustomersJPAAdapter` ofrece la implementación basada en JPA y `CustomersInMemoryRepository` proporciona una implementación en memoria. La abstracción permite desacoplar los casos de uso del mecanismo concreto de almacenamiento.

### Strategy — Estrategia

**Ubicación:** `src/test/java/com/breadhardit/travelagencykata/BehavioralPatternExercices.java`.

`TravelDocumentationStrategy` define el contrato para determinar la documentación necesaria. Las estrategias `SameCountryStrategy`, `SchengenTravelStrategy` y `VisaRequiredStrategy` encapsulan reglas alternativas; `TravelDocumentationResolver` selecciona la primera que admite el viaje. La estrategia de visado actúa como alternativa final.

### Observer — Observador

**Ubicación:** `src/test/java/com/breadhardit/travelagencykata/BehavioralPatternExercices.java`.

`EmployeesRepository` publica la incorporación de un empleado notificando a los suscriptores registrados mediante `EmployeeObserver`. `GreetingsNotificator` reacciona al evento y delega el envío en `GreetingSender`. La notificación se produce al registrar el empleado, sin un hilo que consulte periódicamente el repositorio.

### Factory — Fábrica

**Ubicación:** `src/test/java/com/breadhardit/travelagencykata/CreationalPatternExercices.java`.

`MovementFactory` centraliza la creación de movimientos bancarios: depósitos, retiradas, transferencias y anulaciones. Cada movimiento encapsula cómo aplicar y revertir su efecto, evitando que `Account` tenga que conocer cada tipo concreto mediante condicionales.

### Composite — Compuesto

**Ubicación:** `src/test/java/com/breadhardit/travelagencykata/StructuralPatternExercices.java`.

`MenuComponent` proporciona una abstracción común. `Food` es la hoja y `Dish` y `Menu` son compuestos que contienen otros componentes. El cálculo de calorías se realiza recursivamente mediante la misma operación `getCalories()`, tanto para un alimento individual como para estructuras anidadas.

### Builder — Constructor

**Ubicación:** modelos como `Customer`, `GetCustomerDTO` y `PutCustomerDTO`.

Las anotaciones Lombok `@Builder` generan una API de construcción que permite crear objetos indicando sus propiedades de forma legible, sin tener que mantener manualmente múltiples constructores.

### Data Transfer Object (DTO) y Mapper

**Ubicación:** `infrastructure/rest/dto/` y `infrastructure/rest/mapper/CustomerRestMapper.java`.

Los DTO separan el formato de intercambio HTTP del modelo de dominio. `CustomerRestMapper` centraliza la transformación de `Customer` a `GetCustomerDTO`, evitando exponer directamente el objeto de dominio como contrato de la API.

### Dependency Injection — Inyección de dependencias

**Ubicación:** componentes Spring, por ejemplo `CustomersController` y `CustomersJPAAdapter`.

Spring proporciona las dependencias a través del constructor. Esto reduce el acoplamiento entre consumidores e implementaciones y facilita sustituir dependencias en pruebas o en distintas configuraciones.

## 3. Decisiones y limitaciones conocidas

- La interfaz `CustomersJPARepository` se ha mantenido sin modificaciones. Por esa restricción, la búsqueda por número de pasaporte en `CustomersJPAAdapter` filtra el resultado de `findAll()` en memoria. Es funcional para el alcance actual, pero no es eficiente para grandes volúmenes de clientes; una consulta derivada de Spring Data sería preferible si se autorizara ampliar esa interfaz.
- La configuración actual utiliza H2 en memoria con `MODE=PostgreSQL`. Es útil para desarrollo y pruebas, pero no constituye una conexión a una instancia PostgreSQL ni ofrece persistencia entre reinicios.
- Las estrategias de viaje del ejercicio contienen una lista reducida de países Schengen a modo de ejemplo. No deben interpretarse como una fuente normativa o exhaustiva de requisitos de entrada.
- Los ejercicios Strategy, Observer, Factory y Composite están implementados como ejercicios de pruebas, no como funcionalidades expuestas por los endpoints REST de la aplicación.

## 4. Verificación

La compilación, la ejecución de pruebas y el empaquetado se verifican mediante el workflow de GitHub Actions, que utiliza Java 21 y ejecuta:

```bash
mvn --batch-mode --no-transfer-progress clean compile
mvn --batch-mode --no-transfer-progress test
mvn --batch-mode --no-transfer-progress package
```

La documentación describe el estado del código inspeccionado y no sustituye la validación automatizada del workflow.
