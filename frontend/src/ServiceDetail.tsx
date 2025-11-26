import { useState } from "react";

import { GET_SERVICE, ADD_TO_CART, GET_MY_CART, GET_CART_TOTAL } from "./queries";
import {useMutation, useQuery} from "@apollo/client/react";

type Service = {
    id: string;
    name: string;
    description?: string | null;
    price?: number | null;
    city?: string | null;
    rating?: number | null;
    category?: string | null;
    countryCode?: string | null;
    providerId?: string | null;
};

type GetServiceData = { serviceById: Service | null };
type GetServiceVars = { id: string };

export default function ServiceDetail({
                                          id,
                                          onClose,
                                      }: { id: string; onClose: () => void }) {
    const { data, loading, error } = useQuery<GetServiceData, GetServiceVars>(
        GET_SERVICE,
        { variables: { id } }
    );

    const [addToCart] = useMutation(ADD_TO_CART, {
        refetchQueries: [{ query: GET_MY_CART }, { query: GET_CART_TOTAL }],
    });

    const [quantity, setQuantity] = useState(1);
    const [showSuccess, setShowSuccess] = useState(false);

    const handleAddToCart = async () => {
        try {
            await addToCart({
                variables: { serviceId: id, quantity },
            });
            setShowSuccess(true);
            setTimeout(() => {
                setShowSuccess(false);
            }, 2000);
        } catch (err) {
            console.error("Error adding to cart:", err);
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in duration-200"
            onClick={onClose}
        >
            <div
                className="w-full max-w-2xl bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-3xl shadow-2xl shadow-cyan-500/10 overflow-hidden animate-in zoom-in-95 duration-300"
                onClick={(e) => e.stopPropagation()}
            >
                {loading && (
                    <div className="flex flex-col items-center justify-center py-16">
                        <div className="w-12 h-12 border-2 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin mb-4"></div>
                        <p className="text-slate-400">Cargando...</p>
                    </div>
                )}

                {error && (
                    <div className="p-8">
                        <div className="bg-red-500/10 border border-red-500/20 rounded-2xl p-4 text-red-400">
                            Error: {error.message}
                        </div>
                    </div>
                )}

                {data?.serviceById && (
                    <>
                        <div className="relative overflow-hidden">
                            <div className="absolute inset-0 bg-gradient-to-br from-cyan-500/20 to-purple-500/20"></div>
                            <div className="relative px-8 pt-8 pb-6">
                                <div className="flex items-start justify-between mb-2">
                                    <h2 className="text-3xl font-bold text-white">
                                        {data.serviceById.name}
                                    </h2>
                                    {data.serviceById.rating && (
                                        <div className="flex items-center gap-2 bg-yellow-500/10 border border-yellow-500/20 rounded-xl px-4 py-2">
                                            <span className="text-yellow-500 text-lg">★</span>
                                            <span className="text-yellow-500 text-lg font-bold">
                                                {data.serviceById.rating}
                                            </span>
                                        </div>
                                    )}
                                </div>
                                {data.serviceById.category && (
                                    <div className="inline-block bg-white/10 border border-white/20 rounded-lg px-4 py-1.5 text-sm text-cyan-400 font-medium">
                                        {data.serviceById.category}
                                    </div>
                                )}
                            </div>
                        </div>

                        <div className="px-8 py-6">
                            <div className="mb-6">
                                <h3 className="text-sm text-slate-500 mb-2 uppercase tracking-wider">
                                    Descripción
                                </h3>
                                <p className="text-slate-300 leading-relaxed">
                                    {data.serviceById.description || "Sin descripción disponible"}
                                </p>
                            </div>

                            <div className="grid grid-cols-2 gap-4 mb-6">
                                <div className="bg-slate-800/50 border border-white/5 rounded-2xl p-4">
                                    <div className="text-xs text-slate-500 mb-1 uppercase tracking-wider">
                                        Precio
                                    </div>
                                    <div className="text-2xl font-bold text-white">
                                        ${data.serviceById.price ?? 0}
                                    </div>
                                </div>

                                <div className="bg-slate-800/50 border border-white/5 rounded-2xl p-4">
                                    <div className="text-xs text-slate-500 mb-1 uppercase tracking-wider">
                                        Ciudad
                                    </div>
                                    <div className="text-lg font-semibold text-white">
                                        {data.serviceById.city ?? "—"}
                                    </div>
                                </div>

                                <div className="bg-slate-800/50 border border-white/5 rounded-2xl p-4">
                                    <div className="text-xs text-slate-500 mb-1 uppercase tracking-wider">
                                        País
                                    </div>
                                    <div className="text-lg font-semibold text-white">
                                        {data.serviceById.countryCode ?? "—"}
                                    </div>
                                </div>

                                <div className="bg-slate-800/50 border border-white/5 rounded-2xl p-4">
                                    <div className="text-xs text-slate-500 mb-1 uppercase tracking-wider">
                                        Proveedor
                                    </div>
                                    <div className="text-lg font-semibold text-white">
                                        {data.serviceById.providerId ?? "—"}
                                    </div>
                                </div>
                            </div>

                            {/* Selector de cantidad */}
                            <div className="bg-slate-800/30 border border-white/5 rounded-2xl p-4 mb-6">
                                <label className="block text-sm text-slate-400 mb-3">Cantidad</label>
                                <div className="flex items-center gap-4">
                                    <div className="flex items-center gap-2 bg-slate-900/50 border border-white/10 rounded-xl">
                                        <button
                                            onClick={() => setQuantity(Math.max(1, quantity - 1))}
                                            className="w-12 h-12 flex items-center justify-center text-white hover:bg-white/5 rounded-l-xl transition-all text-xl"
                                        >
                                            −
                                        </button>
                                        <span className="w-16 text-center text-white font-bold text-xl">
                                            {quantity}
                                        </span>
                                        <button
                                            onClick={() => setQuantity(quantity + 1)}
                                            className="w-12 h-12 flex items-center justify-center text-white hover:bg-white/5 rounded-r-xl transition-all text-xl"
                                        >
                                            +
                                        </button>
                                    </div>
                                    <div className="flex-1 text-right">
                                        <div className="text-xs text-slate-500 mb-1">Subtotal</div>
                                        <div className="text-3xl font-bold text-white">
                                            ${((data.serviceById.price ?? 0) * quantity).toFixed(2)}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="px-8 py-6 border-t border-white/5 bg-slate-800/30 flex justify-between items-center gap-4">
                            <button
                                onClick={onClose}
                                className="px-6 py-3 rounded-xl bg-white/5 border border-white/10 text-white font-medium hover:bg-white/10 hover:border-white/20 transition-all duration-300"
                            >
                                Cerrar
                            </button>
                            <div className="flex gap-4">
                                <button
                                    className="px-6 py-3 rounded-xl bg-white/5 border border-white/10 text-white font-medium hover:bg-white/10 hover:border-white/20 transition-all duration-300"
                                >
                                    Contactar
                                </button>
                                <button
                                    onClick={handleAddToCart}
                                    disabled={showSuccess}
                                    className="px-8 py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-purple-600 text-white font-medium hover:shadow-lg hover:shadow-cyan-500/50 transition-all duration-300 hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                                >
                                    {showSuccess ? (
                                        <>
                                            <span>✓</span>
                                            <span>Agregado</span>
                                        </>
                                    ) : (
                                        <>
                                            <span>🛒</span>
                                            <span>Agregar al Carrito</span>
                                        </>
                                    )}
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}