# Eco-MP: Marketplace de Turismo Ecológico

Sistema de marketplace basado en microservicios para la comercialización de servicios de turismo ecológico.

## Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND                                 │
│                    React + Vite + GraphQL                        │
│                      (Vercel / Local)                            │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                      API GATEWAY                                 │
│              Spring Cloud Gateway + GraphQL                      │
│                       (Puerto 8082)                              │
└─────────────────────────┬───────────────────────────────────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
┌─────────┐        ┌─────────────┐       ┌─────────────┐
│  AUTH   │        │   SERVICE   │       │    CART     │
│ SERVICE │        │   CATALOG   │       │   SERVICE   │
│ (8083)  │        │   (8085)    │       │   (8086)    │
└─────────┘        └─────────────┘       └─────────────┘
    │                     │                     │
    │              ┌──────┴──────┐              │
    │              │             │              │
    │              ▼             ▼              │
    │        ┌─────────┐   ┌─────────┐         │
    │        │ REVIEW  │   │ USER    │         │
    │        │ SERVICE │   │ SERVICE │         │
    │        │ (8087)  │   │ (8084)  │         │
    │        └────┬────┘   └────┬────┘         │
    │             │             │              │
    └─────────────┼─────────────┼──────────────┘
                  │             │
    ┌─────────────▼─────────────▼──────────────┐
    │              EUREKA SERVER                │
    │           Service Discovery               │
    │              (Puerto 8761)                │
    └─────────────────────┬────────────────────┘
                          │
    ┌─────────────────────▼────────────────────┐
    │               KEYCLOAK                    │
    │         Identity Provider                 │
    │              (Puerto 8080)                │
    └─────────────────────┬────────────────────┘
                          │
    ┌─────────────────────▼────────────────────┐
    │              POSTGRESQL                   │
    │               Database                    │
    │              (Puerto 5432)                │
    └──────────────────────────────────────────┘
```

## Microservicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| eureka-server | 8761 | Service Discovery |
| api-gateway | 8082 | Gateway + GraphQL Federation |
| auth-service | 8083 | Autenticación via Keycloak |
| user-service | 8084 | Gestión de perfiles |
| service-catalog | 8085 | Catálogo de servicios turísticos |
| cart-service | 8086 | Carrito de compras |
| review-service | 8087 | Calificaciones y comentarios |

## Requisitos

- Docker y Docker Compose
- Java 17+ (para desarrollo local)
- Maven 3.8+
- Node.js 18+ (para frontend)

## Ejecución Local con Docker

### 1. Clonar y navegar al proyecto

```bash
cd eco-mp-microservices
```

### 2. Iniciar todos los servicios

```bash
docker-compose up -d --build
```

### 3. Verificar el estado

```bash
docker-compose ps
```

### 4. URLs de acceso

- **Eureka Dashboard**: http://localhost:8761
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **API Gateway**: http://localhost:8082
- **GraphQL Playground**: http://localhost:8082/graphiql
- **Frontend**: http://localhost:5173

## Ejecución Local sin Docker (Desarrollo)

### 1. Iniciar solo infraestructura

```bash
docker-compose up -d postgres keycloak
```

### 2. Iniciar Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
```

### 3. Iniciar cada microservicio (en terminales separadas)

```bash
# Terminal 1 - API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 2 - Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 3 - User Service
cd user-service
mvn spring-boot:run

# Terminal 4 - Service Catalog
cd service-catalog
mvn spring-boot:run

# Terminal 5 - Cart Service
cd cart-service
mvn spring-boot:run

# Terminal 6 - Review Service
cd review-service
mvn spring-boot:run
```

### 4. Iniciar Frontend

```bash
cd frontend
npm install
npm run dev
```

## Usuarios de Prueba

| Usuario | Password | Rol |
|---------|----------|-----|
| admin | admin123 | ADMIN, CLIENT |
| provider1 | provider123 | PROVIDER |
| client1 | client123 | CLIENT |

## API GraphQL

### Queries principales

```graphql
# Obtener todos los servicios
query {
  services(filter: "eco") {
    id
    name
    description
    price
    category
    city
    rating
  }
}

# Obtener servicio por ID
query {
  serviceById(id: "uuid-here") {
    id
    name
    description
    countryInfo {
      name
      capital
      flag
    }
    weatherInfo {
      temperature
      description
    }
  }
}

# Obtener carrito
query {
  myCart {
    id
    serviceName
    quantity
    unitPrice
  }
  cartTotal
}
```

### Mutations principales

```graphql
# Agregar al carrito
mutation {
  addToCart(serviceId: "uuid-here", quantity: 2) {
    id
    quantity
  }
}

# Crear review
mutation {
  createReview(input: {
    serviceId: "uuid-here"
    rating: 5
    comment: "Excelente servicio"
  }) {
    id
    rating
  }
}

# Checkout
mutation {
  checkoutCart
}
```

## Despliegue en Producción

### Railway (Backend)

1. Crear cuenta en [Railway](https://railway.app)
2. Crear nuevo proyecto
3. Añadir servicios:
   - PostgreSQL
   - Keycloak (desde template o Docker)
   - Cada microservicio desde GitHub

4. Configurar variables de entorno en cada servicio:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=https://eureka-xxx.railway.app/eureka/
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI=https://keycloak-xxx.railway.app/realms/eco-mp
```

### Vercel (Frontend)

1. Crear cuenta en [Vercel](https://vercel.com)
2. Importar repositorio de GitHub
3. Configurar variables de entorno:

```
VITE_KEYCLOAK_URL=https://keycloak-xxx.railway.app
VITE_API_URL=https://gateway-xxx.railway.app
```

4. Desplegar

## Pruebas

### Ejecutar pruebas unitarias

```bash
# En cada microservicio
cd service-catalog
mvn test

# O todos los microservicios
for dir in auth-service user-service service-catalog cart-service review-service; do
    cd $dir && mvn test && cd ..
done
```

## Estructura del Proyecto

```
eco-mp-microservices/
├── docker-compose.yml
├── init-db.sql
├── keycloak/
│   └── realm-export.json
├── eureka-server/
├── api-gateway/
├── auth-service/
├── user-service/
├── service-catalog/
├── cart-service/
├── review-service/
└── frontend/
```

## Tecnologías Utilizadas

### Backend
- Java 17
- Spring Boot 3.2
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Security + OAuth2
- Spring Data JPA
- GraphQL (Spring GraphQL)
- PostgreSQL / H2

### Frontend
- React 18
- TypeScript
- Vite
- Apollo Client (GraphQL)
- TailwindCSS
- Keycloak JS

### Infraestructura
- Docker & Docker Compose
- Keycloak (Identity Provider)
- PostgreSQL

## Licencia

MIT License - Universidad Javeriana 2025
