import ReactDOM from "react-dom/client";
import App from "./App";
import { AuthProvider, useAuth } from "./auth";

import { makeClient } from "./apollo";
import {ApolloProvider} from "@apollo/client/react";

import "./index.css";

function AppWithProviders() {
    const { ready, token } = useAuth();
    if (!ready) return <div>Inicializando autenticación…</div>;
    const client = makeClient(() => token);
    return (
        <ApolloProvider client={client}>
            <App />
        </ApolloProvider>
    );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
    <AuthProvider>
        <AppWithProviders />
    </AuthProvider>
);