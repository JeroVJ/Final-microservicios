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