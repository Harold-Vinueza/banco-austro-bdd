# Práctica: Banco del Austro — Base de Datos Distribuida

**Universidad Técnica Estatal de Quevedo (UTEQ)**
Facultad de Ciencias de la Computación y Diseño Digital
Carrera de Ingeniería de Software
Aplicaciones Distribuidas (ISR-701) — Unidad 2
Período académico 2026-2027 SPA

**Autor:** Harold Vinueza Sánchez

## Descripción

Prototipo didáctico de una base de datos distribuida para el caso Banco del Austro.
Implementa dos sedes (Cuenca y Quito), cada una con su propio motor PostgreSQL 16
corriendo en Docker, coordinadas desde una aplicación Spring Boot que decide en qué
nodo consultar según el número de cuenta.

Se evidencian tres propiedades centrales de una BDD:

- **Fragmentación horizontal**: la tabla `cuentas` y `transacciones` se dividen por
  oficina (`CUENCA` / `QUITO`), garantizado a nivel de motor con restricciones `CHECK`.
- **Replicación**: la tabla `clientes` existe completa en ambos nodos.
- **Transparencia de ubicación**: el cliente HTTP consulta un único endpoint sin saber
  en qué motor físico vive el dato.
- **Autonomía local**: cada nodo sigue operando aunque el otro nodo caiga.

## Arquitectura

```
                     ┌─────────────────────────┐
                     │   Spring Boot (8080)    │
                     │  Enrutador por prefijo   │
                     │   22 → Cuenca / 17 → Quito│
                     └───────────┬─────────────┘
                    ┌────────────┴────────────┐
              ┌─────▼─────┐              ┌─────▼─────┐
              │  Cuenca   │              │   Quito   │
              │ PostgreSQL│              │ PostgreSQL│
              │  :5442    │              │  :5443    │
              └───────────┘              └───────────┘
```

## Estructura del repositorio

```
.
├── banco-austro/          # Proyecto Spring Boot (Java 21, Maven)
│   └── src/main/java/ec/edu/uteq/banco_austro/
│       ├── config/         # DataSourceConfig: dos DataSource (Cuenca, Quito)
│       ├── service/        # ConsultaDistribuidaService: enrutamiento + unión
│       └── controller/     # BancoController: endpoints REST
├── sql-cuenca/              # DDL + datos del nodo Cuenca
├── sql-quito/                # DDL + datos del nodo Quito
└── docker-compose.yml        # Definición de los dos contenedores PostgreSQL
```

## Requisitos

- Docker Desktop
- JDK 21+ (probado con JDK 26 en modo compatible `release 21`)
- Maven (incluido vía `mvnw`/`mvnw.cmd`)

## Cómo levantar el ambiente

### 1. Levantar los motores PostgreSQL

```bash
docker compose up -d
docker compose ps
```

Debe mostrar `banco-cuenca` en el puerto **5442** y `banco-quito` en el puerto **5443**
(remapeados desde 5432/5433 originales para evitar conflicto con instalaciones locales
de PostgreSQL).

### 2. Ejecutar la aplicación Spring Boot

```bash
cd banco-austro
mvnw.cmd clean compile
mvnw.cmd spring-boot:run
```

La aplicación queda escuchando en `http://localhost:8080`.

## Endpoints

| Método | Endpoint                        | Descripción                                  |
|--------|----------------------------------|-----------------------------------------------|
| GET    | `/api/banco/saldo/{numero}`     | Consulta el saldo de una cuenta (enruta según prefijo `22`=Cuenca, `17`=Quito) |
| GET    | `/api/banco/clientes`           | Lista todos los clientes (unión Cuenca + Quito) |

### Ejemplos

```bash
curl http://localhost:8080/api/banco/saldo/2201000001
# {"numero":"2201000001","saldo":15000.00,"oficina":"CUENCA"}

curl http://localhost:8080/api/banco/saldo/1701000001
# {"numero":"1701000001","saldo":8000.00,"oficina":"QUITO"}

curl http://localhost:8080/api/banco/clientes
# [ ... 6 clientes, 3 de Cuenca + 3 de Quito ... ]
```

## Prueba de tolerancia a fallos

```bash
docker stop banco-quito
curl http://localhost:8080/api/banco/saldo/2201000001   # sigue respondiendo (Cuenca vivo)
curl http://localhost:8080/api/banco/saldo/1701000001   # falla (Quito caído)

docker start banco-quito                                  # restaura el nodo
```

Se verificó que Cuenca continúa operando con normalidad mientras Quito está caído,
evidenciando la propiedad de **autonomía local**.

## Convención de enrutamiento

| Prefijo de cuenta | Sede   | Puerto Docker |
|--------------------|--------|----------------|
| `22`               | Cuenca | 5442           |
| `17`               | Quito  | 5443           |

## Detener el ambiente

```bash
docker compose down       # detiene contenedores, conserva los datos
docker compose down -v    # detiene y elimina también los volúmenes (reset total)
```

## Referencias

- Özsu, M. T. & Valduriez, P. (2020). *Principles of Distributed Database Systems* (4th ed.). Springer.
- Kleppmann, M. (2017). *Designing Data-Intensive Applications*. O'Reilly Media.
