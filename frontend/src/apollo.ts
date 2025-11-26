import { ApolloClient, InMemoryCache, createHttpLink } from "@apollo/client";
import { setContext } from "@apollo/client/link/context";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8082";

export function makeClient(getToken: () => string | undefined) {
    const httpLink = createHttpLink({
        uri: `${API_URL}/graphql`,
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
