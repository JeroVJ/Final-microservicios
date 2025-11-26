import { useState } from "react";
import { useMutation, gql } from "@apollo/client";

const REGISTER_USER = gql`
    mutation RegisterUser($input: RegisterInput!) {
        register(input: $input)
    }
`;

type RegisterProps = {
    onClose: () => void;
    onSuccess: () => void;
};

export default function Register({ onClose, onSuccess }: RegisterProps) {
    const [formData, setFormData] = useState({
        username: "",
        email: "",
        password: "",
        confirmPassword: "",
        firstName: "",
        lastName: "",
    });
    const [error, setError] = useState("");

    const [register, { loading }] = useMutation(REGISTER_USER, {
        onCompleted: (data) => {
            if (data.register) {
                onSuccess();
            } else {
                setError("Error al registrar usuario");
            }
        },
        onError: (err) => {
            setError(err.message || "Error al registrar usuario");
        },
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");

        if (formData.password !== formData.confirmPassword) {
            setError("Las contraseñas no coinciden");
            return;
        }

        if (formData.password.length < 6) {
            setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        await register({
            variables: {
                input: {
                    username: formData.username,
                    email: formData.email,
                    password: formData.password,
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                },
            },
        });
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-md"
            onClick={onClose}
        >
            <div
                className="w-full max-w-md bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-3xl shadow-2xl overflow-hidden"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="relative overflow-hidden bg-gradient-to-br from-cyan-500/20 to-purple-500/20 px-8 py-6">
                    <h2 className="text-2xl font-bold text-white">Crear Cuenta</h2>
                    <p className="text-slate-400 text-sm mt-1">Únete a Eco-MP</p>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-4">
                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 rounded-xl p-3 text-red-400 text-sm">
                            {error}
                        </div>
                    )}

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Nombre</label>
                            <input
                                type="text"
                                value={formData.firstName}
                                onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                                placeholder="Juan"
                            />
                        </div>
                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Apellido</label>
                            <input
                                type="text"
                                value={formData.lastName}
                                onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                                placeholder="Pérez"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm text-slate-400 mb-2">Usuario *</label>
                        <input
                            type="text"
                            value={formData.username}
                            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                            className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                            placeholder="juanperez"
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-slate-400 mb-2">Email *</label>
                        <input
                            type="email"
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                            placeholder="juan@example.com"
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-slate-400 mb-2">Contraseña *</label>
                        <input
                            type="password"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                            className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-slate-400 mb-2">Confirmar Contraseña *</label>
                        <input
                            type="password"
                            value={formData.confirmPassword}
                            onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                            className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                            placeholder="••••••••"
                            required
                        />
                    </div>

                    <div className="flex gap-4 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-6 py-3 rounded-xl bg-white/5 border border-white/10 text-white font-medium hover:bg-white/10 transition-all"
                        >
                            Cancelar
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 px-6 py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-purple-600 text-white font-medium hover:shadow-lg hover:shadow-cyan-500/50 transition-all disabled:opacity-50"
                        >
                            {loading ? "Registrando..." : "Registrarse"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
