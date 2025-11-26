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
    routeDescription: string;
};

const CATEGORIES = [
    "Ecoturismo",
    "Transporte Ecológico",
    "Alojamiento Sostenible",
    "Alimentación Orgánica",
    "Actividades al Aire Libre",
    "Tours Guiados",
    "Experiencias Culturales",
    "Conservación",
];

const TRANSPORT_TYPES = [
    "Bicicleta",
    "Caminata",
    "Kayak",
    "Caballo",
    "Vehículo Eléctrico",
    "Bote Solar",
    "Ninguno",
];

export default function CreateService({ onClose, onCreated }: { onClose: () => void; onCreated?: () => void }) {
    const [createService, { loading }] = useMutation(CREATE_SERVICE, {
        refetchQueries: [{ query: GET_SERVICES, variables: { filter: "" } }],
        onCompleted: (data) => {
            console.log("Service created successfully:", data);
            onCreated?.();
            onClose();
        },
        onError: (error) => {
            console.error("Error creating service:", error);
            console.error("Error details:", error.graphQLErrors);
            alert("Error al crear el servicio. Por favor verifica los datos e intenta de nuevo.");
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
        routeDescription: "",
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.name || !formData.price || !formData.category) {
            alert("Por favor completa los campos obligatorios: Nombre, Precio y Categoría");
            return;
        }

        // Validar que el precio sea un número válido
        const priceValue = parseFloat(formData.price);
        if (isNaN(priceValue) || priceValue < 0) {
            alert("Por favor ingresa un precio válido");
            return;
        }

        // Construir el input limpiando campos vacíos
        const input: any = {
            name: formData.name.trim(),
            description: formData.description.trim() || null,
            price: priceValue,
            category: formData.category,
            city: formData.city.trim() || null,
            countryCode: formData.countryCode.trim() || null,
        };

        // Solo agregar coordenadas si ambas están presentes y son válidas
        if (formData.latitude && formData.longitude) {
            const lat = parseFloat(formData.latitude);
            const lon = parseFloat(formData.longitude);
            if (!isNaN(lat) && !isNaN(lon)) {
                input.latitude = lat;
                input.longitude = lon;
            }
        }

        // Agregar campos opcionales solo si tienen valor
        if (formData.transportType) {
            input.transportType = formData.transportType;
        }

        if (formData.routeDescription.trim()) {
            input.routeDescription = formData.routeDescription.trim();
        }

        console.log("Sending input:", input);

        try {
            await createService({
                variables: { input },
            });
        } catch (err) {
            console.error("Mutation error:", err);
        }
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
                    <p className="text-slate-300 mt-1">Completa la información de tu servicio ecológico</p>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-6">
                    {/* Información Básica */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-white border-b border-white/10 pb-2">
                            Información Básica
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
                                    min="0"
                                    value={formData.price}
                                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="0.00"
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Categoría *</label>
                                <select
                                    value={formData.category}
                                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    required
                                >
                                    <option value="">Selecciona una categoría</option>
                                    {CATEGORIES.map((cat) => (
                                        <option key={cat} value={cat}>{cat}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Descripción</label>
                            <textarea
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50 h-24"
                                placeholder="Describe tu servicio en detalle..."
                            />
                        </div>
                    </div>

                    {/* Ubicación */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-white border-b border-white/10 pb-2">
                            Ubicación
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Ciudad</label>
                                <input
                                    type="text"
                                    value={formData.city}
                                    onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                                    className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50"
                                    placeholder="Ej: Bogotá"
                                />
                            </div>

                            <div>
                                <label className="block text-sm text-slate-400 mb-2">Código de País</label>
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

                    {/* Descripción de Ruta */}
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold text-white border-b border-white/10 pb-2">
                            Información Adicional (Opcional)
                        </h3>
                        <div>
                            <label className="block text-sm text-slate-400 mb-2">Descripción de la Ruta</label>
                            <textarea
                                value={formData.routeDescription}
                                onChange={(e) => setFormData({ ...formData, routeDescription: e.target.value })}
                                className="w-full bg-slate-800/50 border border-white/10 rounded-xl px-4 py-3 text-white focus:outline-none focus:border-emerald-500/50 h-20"
                                placeholder="Describe el itinerario o ruta del servicio..."
                            />
                        </div>
                    </div>

                    {/* Botones */}
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
                            className="px-6 py-3 rounded-xl bg-gradient-to-r from-emerald-500 to-cyan-600 text-white font-medium hover:shadow-lg hover:shadow-emerald-500/50 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? "Creando..." : "Crear Servicio"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}