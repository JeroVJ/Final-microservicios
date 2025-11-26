import { useState } from "react";
import { GET_SERVICES } from "./queries";
import ServiceDetail from "./ServiceDetail";
import UserProfile from "./UserProfile";
import ShoppingCart from "./ShoppingCart";
import { useAuth } from "./auth";
import { useQuery } from "@apollo/client/react";

type Service = {
    id: string;
    name: string;
    description?: string | null;
    price?: number | null;
    city?: string | null;
    rating?: number | null;
    category?: string | null;
};

type ServicesData = { services: Service[] };
type ServicesVars = { filter?: string | null };

export default function App() {
    const { ready, authenticated, user, login, logout } = useAuth();
    const [term, setTerm] = useState("");
    const [openId, setOpenId] = useState<string | null>(null);
    const [showProfile, setShowProfile] = useState(false);
    const [showCart, setShowCart] = useState(false);

    const { data, loading, error, refetch } = useQuery<ServicesData, ServicesVars>(
        GET_SERVICES,
        { variables: { filter: "" }, skip: !authenticated }
    );

    const list = data?.services ?? [];

    // Pantalla de carga
    if (!ready) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
                <div className="text-center">
                    <div className="w-16 h-16 mx-auto mb-4 border-4 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin"></div>
                    <p className="text-slate-400">Inicializando...</p>
                </div>
            </div>
        );
    }

    // Pantalla de login
    if (!authenticated) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center">
                <div className="fixed inset-0 overflow-hidden pointer-events-none">
                    <div className="absolute top-0 right-0 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl animate-pulse"></div>
                    <div className="absolute bottom-0 left-0 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }}></div>
                </div>
                <div className="relative bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-3xl p-12 text-center max-w-md mx-4">
                    <div className="w-20 h-20 mx-auto mb-6 rounded-2xl bg-gradient-to-br from-cyan-400 to-purple-600 flex items-center justify-center">
                        <span className="text-white font-bold text-3xl">E</span>
                    </div>
                    <h1 className="text-3xl font-bold bg-gradient-to-r from-cyan-400 to-purple-500 bg-clip-text text-transparent mb-3">
                        Eco-MP
                    </h1>
                    <p className="text-slate-400 mb-8">
                        Marketplace de Turismo Ecológico
                    </p>
                    <button
                        onClick={login}
                        className="w-full px-8 py-4 rounded-2xl bg-gradient-to-r from-cyan-500 to-purple-600 text-white font-medium hover:shadow-lg hover:shadow-cyan-500/25 transition-all duration-300 hover:scale-105 active:scale-95"
                    >
                        Iniciar Sesión
                    </button>
                    <p className="text-slate-500 text-sm mt-6">
                        Usuarios de prueba: client1 / provider1 (password123)
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950">
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <div className="absolute top-0 right-0 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl animate-pulse"></div>
                <div className="absolute bottom-0 left-0 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl animate-pulse" style={{ animationDelay: '1s' }}></div>
            </div>

            <header className="relative border-b border-white/5 backdrop-blur-xl bg-slate-900/50">
                <div className="max-w-7xl mx-auto flex justify-between items-center px-6 py-5">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-cyan-400 to-purple-600 flex items-center justify-center">
                            <span className="text-white font-bold text-lg">E</span>
                        </div>
                        <h1 className="text-2xl font-bold bg-gradient-to-r from-cyan-400 to-purple-500 bg-clip-text text-transparent">
                            Eco-MP
                        </h1>
                    </div>
                    <div className="flex items-center gap-4">
                        <button
                            onClick={() => setShowProfile(true)}
                            className="px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-medium hover:bg-white/10 hover:border-white/20 transition-all duration-300 flex items-center gap-2"
                        >
                            <span>👤</span>
                            {user?.username && <span>{user.username}</span>}
                        </button>
                        <button
                            onClick={() => setShowCart(true)}
                            className="px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-medium hover:bg-white/10 hover:border-white/20 transition-all duration-300"
                        >
                            🛒 Carrito
                        </button>
                        <button
                            onClick={logout}
                            className="px-4 py-2 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-medium hover:bg-white/10 hover:border-white/20 transition-all duration-300"
                        >
                            Salir
                        </button>
                    </div>
                </div>
            </header>

            <section className="relative max-w-7xl mx-auto px-6 pt-12 pb-8">
                <div className="max-w-2xl mx-auto">
                    <h2 className="text-4xl font-bold text-white mb-2 text-center">
                        Descubre Servicios
                    </h2>
                    <p className="text-slate-400 text-center mb-8">
                        Encuentra los mejores servicios ecológicos cerca de ti
                    </p>
                    <div className="relative group">
                        <div className="absolute inset-0 bg-gradient-to-r from-cyan-500 to-purple-600 rounded-2xl blur opacity-20 group-hover:opacity-30 transition-opacity duration-300"></div>
                        <div className="relative flex gap-2">
                            <input
                                type="text"
                                placeholder="Buscar servicios..."
                                value={term}
                                onChange={(e) => setTerm(e.target.value)}
                                onKeyDown={(e) => e.key === "Enter" && refetch({ filter: term })}
                                className="flex-1 bg-slate-900/90 backdrop-blur-xl border border-white/10 rounded-2xl px-6 py-4 text-white placeholder:text-slate-500 focus:outline-none focus:border-cyan-500/50 focus:ring-2 focus:ring-cyan-500/20 transition-all duration-300"
                            />
                            <button
                                onClick={() => refetch({ filter: term })}
                                className="px-8 py-4 rounded-2xl bg-gradient-to-r from-cyan-500 to-purple-600 text-white font-medium hover:shadow-lg hover:shadow-cyan-500/50 transition-all duration-300 hover:scale-105 active:scale-95"
                            >
                                Buscar
                            </button>
                        </div>
                    </div>
                </div>
            </section>

            <main className="relative max-w-7xl mx-auto px-6 pb-16">
                {loading && (
                    <div className="flex justify-center py-12">
                        <div className="w-8 h-8 border-2 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin"></div>
                    </div>
                )}
                {error && (
                    <div className="max-w-2xl mx-auto bg-red-500/10 border border-red-500/20 rounded-2xl p-4 text-red-400 text-center">
                        Error: {error.message}
                    </div>
                )}

                <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
                    {list.map((srv) => (
                        <div
                            key={srv.id}
                            onClick={() => setOpenId(srv.id)}
                            className="group relative bg-slate-900/50 backdrop-blur-xl border border-white/10 rounded-2xl p-6 cursor-pointer hover:border-cyan-500/50 transition-all duration-300 hover:shadow-xl hover:shadow-cyan-500/10 hover:scale-[1.02] active:scale-[0.98]"
                        >
                            <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/5 to-purple-500/5 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>

                            <div className="relative">
                                <div className="flex items-start justify-between mb-3">
                                    <h3 className="text-lg font-semibold text-white group-hover:text-cyan-400 transition-colors duration-300">
                                        {srv.name}
                                    </h3>
                                    {srv.rating && (
                                        <div className="flex items-center gap-1 bg-yellow-500/10 border border-yellow-500/20 rounded-lg px-2 py-1">
                                            <span className="text-yellow-500 text-xs">★</span>
                                            <span className="text-yellow-500 text-xs font-medium">{srv.rating}</span>
                                        </div>
                                    )}
                                </div>

                                <p className="text-sm text-slate-400 mb-4 line-clamp-2">
                                    {srv.description ?? "Sin descripción"}
                                </p>

                                <div className="flex items-center justify-between pt-4 border-t border-white/5">
                                    <div className="flex flex-col">
                                        <span className="text-xs text-slate-500 mb-1">Precio</span>
                                        <span className="text-xl font-bold text-white">
                                            ${srv.price ?? 0}
                                        </span>
                                    </div>
                                    <div className="flex flex-col items-end">
                                        <span className="text-xs text-slate-500 mb-1">Ubicación</span>
                                        <span className="text-sm text-slate-300">
                                            {srv.city ?? "—"}
                                        </span>
                                    </div>
                                </div>

                                {srv.category && (
                                    <div className="mt-3">
                                        <span className="inline-block bg-white/5 border border-white/10 rounded-lg px-3 py-1 text-xs text-slate-400">
                                            {srv.category}
                                        </span>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>

                {!loading && list.length === 0 && (
                    <div className="text-center py-12">
                        <div className="w-20 h-20 mx-auto mb-4 rounded-2xl bg-slate-800/50 border border-white/10 flex items-center justify-center">
                            <span className="text-4xl">🔍</span>
                        </div>
                        <p className="text-slate-400">No se encontraron servicios</p>
                    </div>
                )}
            </main>

            {openId && <ServiceDetail id={openId} onClose={() => setOpenId(null)} />}
            {showProfile && <UserProfile onClose={() => setShowProfile(false)} />}
            {showCart && <ShoppingCart onClose={() => setShowCart(false)} />}
        </div>
    );
}