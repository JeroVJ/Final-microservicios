# Guía de Deployment - Eco-MP Microservices

## Opción Recomendada: Railway + Vercel

### Paso 1: Preparar el Repositorio

1. Sube el proyecto a GitHub:
```bash
cd eco-mp-microservices
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/eco-mp-microservices.git
git push -u origin main
```

### Paso 2: Configurar Railway

1. Ve a [railway.app](https://railway.app) y crea una cuenta
2. Crea un nuevo proyecto: "New Project" → "Empty Project"
3. Añade los servicios en este orden:

#### 2.1 PostgreSQL
- Click "New" → "Database" → "PostgreSQL"
- Anota las credenciales (se usarán después)

#### 2.2 Keycloak
- Click "New" → "Docker Image"
- Image: `quay.io/keycloak/keycloak:23.0`
- Variables de entorno:
  ```
  KC_DB=postgres
  KC_DB_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/${{Postgres.POSTGRES_DB}}
  KC_DB_USERNAME=${{Postgres.POSTGRES_USER}}
  KC_DB_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}
  KEYCLOAK_ADMIN=admin
  KEYCLOAK_ADMIN_PASSWORD=admin123
  KC_HOSTNAME_STRICT=false
  KC_PROXY=edge
  ```
- Start Command: `start --optimized --hostname-strict=false --proxy=edge`

#### 2.3 Eureka Server
- Click "New" → "GitHub Repo" → Selecciona tu repo
- Root Directory: `eureka-server`
- Variables:
  ```
  PORT=8761
  ```

#### 2.4 API Gateway
- Click "New" → "GitHub Repo"
- Root Directory: `api-gateway`
- Variables:
  ```
  PORT=8082
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://${{eureka-server.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
  SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI=https://${{keycloak.RAILWAY_PUBLIC_DOMAIN}}/realms/eco-mp
  SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWKSETURI=http://${{keycloak.RAILWAY_PRIVATE_DOMAIN}}:8080/realms/eco-mp/protocol/openid-connect/certs
  ```

#### 2.5 Resto de Microservicios
Repite para: `auth-service`, `user-service`, `service-catalog`, `cart-service`, `review-service`

Variables comunes para cada uno:
```
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://${{eureka-server.RAILWAY_PRIVATE_DOMAIN}}:8761/eureka/
SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/${{Postgres.POSTGRES_DB}}
SPRING_DATASOURCE_USERNAME=${{Postgres.POSTGRES_USER}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.POSTGRES_PASSWORD}}
```

### Paso 3: Configurar Keycloak en Producción

1. Accede a Keycloak con la URL pública de Railway
2. Login: admin / admin123
3. Crea el realm "eco-mp"
4. Crea el cliente "eco-mp-web":
   - Client type: Public
   - Valid Redirect URIs: https://tu-frontend.vercel.app/*
   - Web Origins: https://tu-frontend.vercel.app
5. Crea usuarios de prueba (client1, provider1)

### Paso 4: Frontend en Vercel

1. Ve a [vercel.com](https://vercel.com)
2. "New Project" → Importa tu repo de GitHub
3. Root Directory: `frontend`
4. Variables de entorno:
   ```
   VITE_KEYCLOAK_URL=https://tu-keycloak.railway.app
   VITE_API_URL=https://tu-api-gateway.railway.app
   ```
5. Deploy

### Paso 5: Actualizar Frontend

Antes de desplegar, actualiza los archivos del frontend para usar variables de entorno:

**src/Keycloack.ts:**
```typescript
import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL || "http://localhost:8080",
    realm: "eco-mp",
    clientId: "eco-mp-web",
});

export const keycloakInitOptions = {
    onLoad: 'check-sso' as const,
    pkceMethod: 'S256' as const,
    checkLoginIframe: false,
};
```

**src/apollo.ts:**
```typescript
import { ApolloClient, InMemoryCache, createHttpLink } from "@apollo/client";
import { setContext } from "@apollo/client/link/context";

export function makeClient(getToken: () => string | undefined) {
    const httpLink = createHttpLink({
        uri: `${import.meta.env.VITE_API_URL || "http://localhost:8082"}/graphql`,
    });

    const authLink = setContext((_, { headers }) => {
        const token = getToken();
        return {
            headers: {
                ...headers,
                authorization: token ? `Bearer ${token}` : "",
            },
        };
    });

    return new ApolloClient({
        link: authLink.concat(httpLink),
        cache: new InMemoryCache(),
    });
}
```

---

## Alternativa Rápida: Render.com

### Paso 1: Crear render.yaml

```yaml
services:
  - type: web
    name: eureka-server
    env: docker
    dockerfilePath: ./eureka-server/Dockerfile
    dockerContext: ./eureka-server

  - type: web
    name: api-gateway
    env: docker
    dockerfilePath: ./api-gateway/Dockerfile
    dockerContext: ./api-gateway
    envVars:
      - key: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
        value: http://eureka-server:8761/eureka/

databases:
  - name: postgres
    databaseName: ecomp
    user: ecomp
```

---

## URLs Finales (Ejemplo)

Después del deployment tendrás URLs como:
- Frontend: https://eco-mp.vercel.app
- API Gateway: https://eco-mp-gateway.railway.app
- Keycloak: https://eco-mp-keycloak.railway.app
- Eureka: https://eco-mp-eureka.railway.app (interno)

---

## Verificación Post-Deployment

1. Accede al frontend y verifica login
2. Prueba crear un servicio como proveedor
3. Prueba buscar y agregar al carrito como cliente
4. Verifica que GraphQL funcione: https://tu-gateway.railway.app/graphiql
