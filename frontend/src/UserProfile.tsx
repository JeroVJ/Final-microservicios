import { useState } from "react";

import { GET_CURRENT_USER_PROFILE, CREATE_OR_UPDATE_USER_PROFILE } from "./queries";
import {useMutation, useQuery} from "@apollo/client/react";

type UserProfile = {
    keycloakId: string;
    username: string;
    email: string;
    age?: number;
    photoBase64?: string;
    description?: string;
    role: "CLIENT" | "PROVIDER";
    phone?: string;
    website?: string;
    socialMedia?: string;
};

export default function UserProfile({ onClose }: { onClose: () => void }) {
    const { data, loading } = useQuery<{ currentUserProfile: UserProfile | null }>(
        GET_CURRENT_USER_PROFILE
    );

    const [updateProfile] = useMutation(CREATE_OR_UPDATE_USER_PROFILE, {
        refetchQueries: [{ query: GET_CURRENT_USER_PROFILE }],
    });

    const profile = data?.currentUserProfile;
    const [formData, setFormData] = useState({
        username: profile?.username || "",
        email: profile?.email || "",
        age: profile?.age || "",
        description: profile?.description || "",
        role: profile?.role || "CLIENT",
        phone: profile?.phone || "",
        website: profile?.website || "",
        socialMedia: profile?.socialMedia || "",
    });
    const [photo, setPhoto] = useState(profile?.photoBase64 || "");

    const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                setPhoto(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async () => {
        await updateProfile({
            variables: {
                input: {
                    ...formData,
                    age: formData.age ? parseInt(formData.age.toString()) : null,
                    photoBase64: photo || null,
                },
            },
        });
        onClose();
    };

    if (loading) {
        return (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-md">
                <div className="w-12 h-12 border-2 border-cyan-500/20 border-t-cyan-500 rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-md"
            onClick={onClose}
        >
            <div
                className="w-full max-w-3xl bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="relative overflow-hidden bg-gradient-to-br from-cyan-500/20 to-purple-500/20 px-8 py-6">
                    <h2 className="text-3xl font-bold text-white">
                        {profile ? "Editar Perfil" : "Crear Perfil"}
                    </h2>
                </div>

                <div className="p-8 space-y-6">
                    <div className="flex flex-col items-center gap-4">
                        <div className="w-32 h-32 rounded-full border-4 border-cyan-500/20 overflow-hidden bg-slate-800">
                            {photo ? (
                                <img src={photo} alt="Profile" className="w-full h-full object-cover" />
                            ) : (
                                <div className="w-full h-full flex items-center justify-center text-slate-600 text-4xl">
                                    👤
                                </div>
                            )}
                        </div>
                        <label className="px-6 py-2 rounded-xl bg-gradient-to-r from-cyan-500 to-purple-600 text-white font-medium cursor-pointer hover:shadow-lg transition-all">
                            <input type="file" accept="image/*" onChange={handlePhotoUpload} className="hidden" />
                            Subir Foto
                        </label>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Nombre de Usuario</label>
                            <input
                                type="text"
                                value={formData.username}
                                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Email</label>
                            <input
                                type="email"
                                value={formData.email}
                                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Edad</label>
                            <input
                                type="number"
                                value={formData.age}
                                onChange={(e) => setFormData({ ...formData, age: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                            />
                        </div>

                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Rol</label>
                            <select
                                value={formData.role}
                                onChange={(e) => setFormData({ ...formData, role: e.target.value as "CLIENT" | "PROVIDER" })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                            >
                                <option value="CLIENT">Cliente</option>
                                <option value="PROVIDER">Proveedor</option>
                            </select>
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm text-slate-400 mb-2">Descripción</label>
                        <textarea
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50 h-24"
                            placeholder="Cuéntanos sobre ti..."
                        />
                    </div>

                    {formData.role === "PROVIDER" && (
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Teléfono</label>
                                <input
                                    type="tel"
                                    value={formData.phone}
                                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Sitio Web</label>
                                <input
                                    type="url"
                                    value={formData.website}
                                    onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Redes Sociales</label>
                                <input
                                    type="text"
                                    value={formData.socialMedia}
                                    onChange={(e) => setFormData({ ...formData, socialMedia: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-cyan-500/50"
                                />
                            </div>
                        </div>
                    )}

                    <div className="flex gap-4 justify-end pt-4 border-t border-white/5">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-6 py-3 rounded-xl bg-white/5 border border-white/10 text-white font-medium hover:bg-white/10 transition-all"
                        >
                            Cancelar
                        </button>
                        <button
                            type="button"
                            onClick={handleSubmit}
                            className="px-6 py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-purple-600 text-white font-medium hover:shadow-lg hover:shadow-cyan-500/50 transition-all"
                        >
                            Guardar
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}