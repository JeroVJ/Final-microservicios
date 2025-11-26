import { useState } from "react";
import { useMutation } from "@apollo/client/react";
import { CREATE_SERVICE, GET_SERVICES } from "./queries";

type ServiceInput = {
    name: string;
    description: string;
    price: string;
    category: string;
    city: string;
    countryCode: string;
    latitude: string;
    longitude: string;
    transportType: string;
    departureTime: string;
    arrivalTime: string;
    routeDescription: string;
};

const CATEGORIES = [
    "Ecoturismo",
    "Transporte Ecologico",
    "Alojamiento Sostenible",
    "Alimentacion Organica",
    "Actividades al Aire Libre",
    "Tours Guiados",
    "Experiencias Culturales",
    "Conservacion",
];

const TRANSPORT_TYPES = [
    "Bicicleta",
    "Caminata",
    "Kayak",
    "Caballo",
    "Vehiculo Electrico",
    "Bote Solar",
    "Ninguno",
];

export default function CreateService({ onClose, onCreated }: { onClose: () => void; onCreated?: () => void }) {
    const [createService, { loading }] = useMutation(CREATE_SERVICE, {
        refetchQueries: [{ query: GET_SERVICES, variables: { filter: "" } }],
        onCompleted: () => {
            onCreated?.();
            onClose();
        },
        onError: (error) => {
            console.error("Error creating service:", error);
            alert("Error al crear el servicio: " + error.message);
        },
    });

    const [formData, setFormData] = useState<ServiceInput>({
        name: "",
        description: "",
        price: "",
        category: "",
        city: "",
        countryCode: "CO",
        latitude: "",
        longitude: "",
        transportType: "",
        departureTime: "",
        arrivalTime: "",
        routeDescription: "",
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.name || !formData.price || !formData.category) {
            alert("Por favor completa los campos obligatorios: Nombre, Precio y Categoria");
            return;
        }

        await createService({
            variables: {
                input: {
                    name: formData.name,
                    description: formData.description || null,
                    price: parseFloat(formData.price) || 0,
                    category: formData.category,
                    city: formData.city || null,
                    countryCode: formData.countryCode || null,
                    latitude: formData.latitude ? parseFloat(formData.latitude) : null,
                    longitude: formData.longitude ? parseFloat(formData.longitude) : null,
                    transportType: formData.transportType || null,
                    departureTime: formData.departureTime || null,
                    arrivalTime: formData.arrivalTime || null,
                    routeDescription: formData.routeDescription || null,
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
                className="w-full max-w-4xl bg-slate-900/95 backdrop-blur-xl border border-white/10 rounded-3xl shadow-2xl overflow-hidden max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="relative overflow-hidden bg-gradient-to-br from-emerald-500/20 to-cyan-500/20 px-8 py-6">
                    <h2 className="text-3xl font-bold text-white">Crear Nuevo Servicio</h2>
                    <p className="text-slate-300 mt-1">Completa la informacion de tu servicio ecologico</p>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-6">
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-white border-b border-white/10 pb-2">
                            Informacion Basica
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div className="md:col-span-2">
                                <label className="block text-sm text-slate-400 mb-2">
                                    Nombre del Servicio *
                                </label>
                                <input
                                    type="text"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="Ej: Tour de Avistamiento de Aves"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Precio (USD) *</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    value={formData.price}
                                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="0.00"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Categoria *</label>
                                <select
                                    value={formData.category}
                                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    required
                                >
                                    <option value="">Selecciona una categoria</option>
                                    {CATEGORIES.map((cat) => (
                                        <option key={cat} value={cat}>{cat}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Descripcion</label>
                            <textarea
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50 h-24"
                                placeholder="Describe tu servicio en detalle..."
                            />
                        </div>
                    </div>

                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-white border-b border-white/10 pb-2">
                            Ubicacion
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Ciudad</label>
                                <input
                                    type="text"
                                    value={formData.city}
                                    onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="Ej: Bogota"
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Codigo de Pais</label>
                                <input
                                    type="text"
                                    value={formData.countryCode}
                                    onChange={(e) => setFormData({ ...formData, countryCode: e.target.value.toUpperCase() })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="CO"
                                    maxLength={2}
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Tipo de Transporte</label>
                                <select
                                    value={formData.transportType}
                                    onChange={(e) => setFormData({ ...formData, transportType: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                >
                                    <option value="">Selecciona tipo</option>
                                    {TRANSPORT_TYPES.map((type) => (
                                        <option key={type} value={type}>{type}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Latitud</label>
                                <input
                                    type="number"
                                    step="any"
                                    value={formData.latitude}
                                    onChange={(e) => setFormData({ ...formData, latitude: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="4.6097"
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Longitud</label>
                                <input
                                    type="number"
                                    step="any"
                                    value={formData.longitude}
                                    onChange={(e) => setFormData({ ...formData, longitude: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="-74.0817"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="flex gap-4 justify-end pt-4 border-t border-white/5">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-6 py-3 rounded-xl bg-white/5 border border-white/10 text-white font-medium hover:bg-white/10 transition-all"
                        >
                            Cancelar
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="px-6 py-3 rounded-xl bg-gradient-to-r from-emerald-500 to-cyan-600 text-white font-medium hover:shadow-lg hover:shadow-emerald-500/50 transition-all disabled:opacity-50"
                        >
                            {loading ? "Creando..." : "Crear Servicio"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}