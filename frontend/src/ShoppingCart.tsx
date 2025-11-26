import { useState } from "react";

import {
    GET_MY_CART,
    GET_CART_TOTAL,
    UPDATE_CART_ITEM_QUANTITY,
    REMOVE_FROM_CART,
    CHECKOUT_CART
} from "./queries";
import {useMutation, useQuery} from "@apollo/client/react";

type CartItem = {
    id: string;
    serviceId: string;
    quantity: number;
    unitPrice: number;
    serviceName: string;
    serviceCategory: string;
};

export default function ShoppingCart({ onClose }: { onClose: () => void }) {
    const { data: cartData, loading: cartLoading } = useQuery<{ myCart: CartItem[] }>(GET_MY_CART);
    const { data: totalData } = useQuery<{ cartTotal: number }>(GET_CART_TOTAL);

    const [updateQuantity] = useMutation(UPDATE_CART_ITEM_QUANTITY, {
        refetchQueries: [{ query: GET_MY_CART }, { query: GET_CART_TOTAL }],
    });

    const [removeItem] = useMutation(REMOVE_FROM_CART, {
        refetchQueries: [{ query: GET_MY_CART }, { query: GET_CART_TOTAL }],
    });

    const [checkout] = useMutation(CHECKOUT_CART, {
        refetchQueries: [{ query: GET_MY_CART }, { query: GET_CART_TOTAL }],
    });

    const [showCheckout, setShowCheckout] = useState(false);

    const cartItems = cartData?.myCart || [];
    const total = totalData?.cartTotal || 0;

    const handleUpdateQuantity = async (itemId: string, newQuantity: number) => {
        await updateQuantity({
            variables: { cartItemId: itemId, quantity: newQuantity },
        });
    };

    const handleRemove = async (itemId: string) => {
        await removeItem({
            variables: { cartItemId: itemId },
        });
    };

    const handleCheckout = async () => {
        await checkout();
        setShowCheckout(true);
        setTimeout(() => {
            setShowCheckout(false);
            onClose();
        }, 2000);
    };

    if (showCheckout) {
        return (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-md">
                <div className="bg-slate-900/95 border border-white/10 rounded-3xl p-8 text-center">
                    <div className="w-20 h-20 mx-auto mb-4 bg-gradient-to-br from-green-400 to-green-600 rounded-full flex items-center justify-center text-4xl">
                        ✓
                    </div>
                    <h3 className="text-2xl font-bold text-white mb-2">¡Pago Exitoso!</h3>
                    <p className="text-slate-400">Gracias por tu compra</p>
                </div>
            </div>
        );
    }

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-md"
            onClick={onClose}
        >
            <div
                className="w-full max-w-4xl bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] flex flex-col"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="relative overflow-hidden bg-gradient-to-br from-cyan-500/20 to-purple-500/20 px-8 py-6 flex items-center justify-between">
                    <h2 className="text-3xl font-bold text-white">🛒 Carrito de Compras</h2>
                    <button
                        onClick={onClose}
                        className="w-10 h-10 rounded-xl bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-all"
                    >
                        ✕
                    </button>
                </div>

                {cartLoading ? (
                    <div className="flex-1 flex items-center justify-center py-12">
                        <div className="w-12 h-12 border-2 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin"></div>
                    </div>
                ) : cartItems.length === 0 ? (
                    <div className="flex-1 flex flex-col items-center justify-center py-12">
                        <div className="w-20 h-20 mb-4 rounded-2xl bg-slate-800/50 border border-white/10 flex items-center justify-center text-4xl">
                            🛒
                        </div>
                        <p className="text-slate-400 text-lg">Tu carrito está vacío</p>
                    </div>
                ) : (
                    <>
                        <div className="flex-1 overflow-y-auto p-8 space-y-4">
                            {cartItems.map((item) => (
                                <div
                                    key={item.id}
                                    className="bg-slate-800/50 border border-white/10 rounded-2xl p-6 flex items-center gap-4"
                                >
                                    <div className="flex-1">
                                        <h3 className="text-lg font-semibold text-white mb-1">
                                            {item.serviceName}
                                        </h3>
                                        <div className="inline-block bg-white/5 border border-white/10 rounded-lg px-3 py-1 text-xs text-slate-400">
                                            {item.serviceCategory}
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-4">
                                        <div className="flex items-center gap-2 bg-slate-900/50 border border-white/10 rounded-xl">
                                            <button
                                                onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}
                                                className="w-10 h-10 flex items-center justify-center text-white hover:bg-white/5 rounded-l-xl transition-all"
                                            >
                                                −
                                            </button>
                                            <span className="w-12 text-center text-white font-medium">
                                                {item.quantity}
                                            </span>
                                            <button
                                                onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)}
                                                className="w-10 h-10 flex items-center justify-center text-white hover:bg-white/5 rounded-r-xl transition-all"
                                            >
                                                +
                                            </button>
                                        </div>

                                        <div className="text-right min-w-[100px]">
                                            <div className="text-2xl font-bold text-white">
                                                ${(item.unitPrice * item.quantity).toFixed(2)}
                                            </div>
                                            <div className="text-xs text-slate-500">
                                                ${item.unitPrice} c/u
                                            </div>
                                        </div>

                                        <button
                                            onClick={() => handleRemove(item.id)}
                                            className="w-10 h-10 rounded-xl bg-red-500/10 hover:bg-red-500/20 border border-red-500/20 text-red-400 flex items-center justify-center transition-all"
                                        >
                                            🗑️
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div className="px-8 py-6 border-t border-white/5 bg-slate-800/30">
                            <div className="flex items-center justify-between mb-6">
                                <span className="text-xl text-slate-400">Total:</span>
                                <span className="text-4xl font-bold text-white">
                                    ${total.toFixed(2)}
                                </span>
                            </div>

                            <div className="flex gap-4">
                                <button
                                    onClick={onClose}
                                    className="flex-1 px-6 py-4 rounded-xl bg-white/5 border border-white/10 text-white font-medium hover:bg-white/10 transition-all"
                                >
                                    Seguir Comprando
                                </button>
                                <button
                                    onClick={handleCheckout}
                                    className="flex-1 px-6 py-4 rounded-xl bg-gradient-to-r from-cyan-500 to-purple-600 text-white font-medium hover:shadow-lg hover:shadow-cyan-500/50 transition-all"
                                >
                                    Proceder al Pago
                                </button>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}