-- Crear schemas separados para cada microservicio
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS catalog;
CREATE SCHEMA IF NOT EXISTS cart;
CREATE SCHEMA IF NOT EXISTS reviews;

-- Tablas para user-service
CREATE TABLE IF NOT EXISTS users.user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    age INTEGER,
    photo_base64 TEXT,
    description TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',
    phone VARCHAR(50),
    website VARCHAR(255),
    social_media VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tablas para service-catalog
CREATE TABLE IF NOT EXISTS catalog.services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provider_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    category VARCHAR(100),
    city VARCHAR(100),
    country_code VARCHAR(10),
    rating DECIMAL(2,1) DEFAULT 0,
    rating_count INTEGER DEFAULT 0,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    transport_type VARCHAR(50),
    departure_time TIMESTAMP,
    arrival_time TIMESTAMP,
    route_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS catalog.service_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID REFERENCES catalog.services(id) ON DELETE CASCADE,
    image_url TEXT,
    image_base64 TEXT,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS catalog.service_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID REFERENCES catalog.services(id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL,
    question TEXT NOT NULL,
    answer TEXT,
    answered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tablas para cart-service
CREATE TABLE IF NOT EXISTS cart.cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    service_id UUID NOT NULL,
    service_name VARCHAR(255),
    service_category VARCHAR(100),
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, service_id)
);

CREATE TABLE IF NOT EXISTS cart.orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10,2),
    status VARCHAR(50) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cart.order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES cart.orders(id) ON DELETE CASCADE,
    service_id UUID NOT NULL,
    service_name VARCHAR(255),
    quantity INTEGER,
    unit_price DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tablas para review-service
CREATE TABLE IF NOT EXISTS reviews.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(100),
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(service_id, user_id)
);

-- Índices para optimización
CREATE INDEX IF NOT EXISTS idx_services_category ON catalog.services(category);
CREATE INDEX IF NOT EXISTS idx_services_city ON catalog.services(city);
CREATE INDEX IF NOT EXISTS idx_services_provider ON catalog.services(provider_id);
CREATE INDEX IF NOT EXISTS idx_cart_user ON cart.cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_service ON reviews.reviews(service_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews.reviews(user_id);

-- Datos de ejemplo
INSERT INTO catalog.services (id, provider_id, name, description, price, category, city, country_code, rating, rating_count) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'provider-1', 'Eco Lodge Amazónico', 'Hospedaje ecológico en medio de la selva amazónica con tours guiados', 150.00, 'Alojamiento', 'Leticia', 'CO', 4.5, 23),
('550e8400-e29b-41d4-a716-446655440002', 'provider-1', 'Tour Avistamiento de Aves', 'Recorrido por senderos ecológicos para observación de aves tropicales', 75.00, 'Paseos Ecológicos', 'Minca', 'CO', 4.8, 45),
('550e8400-e29b-41d4-a716-446655440003', 'provider-2', 'Transporte Ecoturístico Sierra Nevada', 'Transporte en vehículos eléctricos hacia la Sierra Nevada de Santa Marta', 45.00, 'Transporte', 'Santa Marta', 'CO', 4.2, 18),
('550e8400-e29b-41d4-a716-446655440004', 'provider-2', 'Restaurante Orgánico del Valle', 'Gastronomía local con ingredientes 100% orgánicos de la región', 35.00, 'Alimentación', 'Villa de Leyva', 'CO', 4.6, 67),
('550e8400-e29b-41d4-a716-446655440005', 'provider-3', 'Senderismo Cocora', 'Caminata ecológica por el Valle del Cocora y sus palmas de cera', 60.00, 'Paseos Ecológicos', 'Salento', 'CO', 4.9, 89);
