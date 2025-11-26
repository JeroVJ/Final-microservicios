import React, { createContext, useEffect, useState, useContext } from "react";
import { keycloak, keycloakInitOptions } from "./Keycloack.ts";
import type { KeycloakProfile } from "keycloak-js";

type AuthCtx = {
    ready: boolean;
    authenticated: boolean;
    token?: string;
    user?: KeycloakProfile;
    login: () => void;
    logout: () => void;
};

const Ctx = createContext<AuthCtx>({
    ready: false,
    authenticated: false,
    login: () => {},
    logout: () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [ready, setReady] = useState(false);
    const [authenticated, setAuthenticated] = useState(false);
    const [token, setToken] = useState<string | undefined>();
    const [user, setUser] = useState<KeycloakProfile | undefined>();

    useEffect(() => {
        let interval: number | undefined;

        keycloak
            .init(keycloakInitOptions)
            .then(async (auth) => {
                setAuthenticated(auth);
                if (auth) {
                    setToken(keycloak.token);
                    // loadUserProfile es opcional, puede fallar por CORS
                    try {
                        const profile = await keycloak.loadUserProfile();
                        setUser(profile);
                    } catch (e) {
                        // Si falla, usamos la info del token
                        console.warn("Could not load user profile, using token info");
                        setUser({ 
                            username: keycloak.tokenParsed?.preferred_username,
                            email: keycloak.tokenParsed?.email,
                        } as any);
                    }

                    interval = window.setInterval(async () => {
                        try {
                            const refreshed = await keycloak.updateToken(30);
                            if (refreshed) setToken(keycloak.token);
                        } catch {
                            keycloak.login();
                        }
                    }, 20_000);
                }
                setReady(true);
            })
            .catch((err) => {
                console.error("Keycloak init error:", err);
                setReady(true);
            });

        return () => {
            if (interval) window.clearInterval(interval);
        };
    }, []);

    const value: AuthCtx = {
        ready,
        authenticated,
        token,
        user,
        login: () => keycloak.login(),
        logout: () => keycloak.logout({ redirectUri: window.location.origin }),
    };

    return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export const useAuth = () => useContext(Ctx);
