import { gql } from "@apollo/client";

// Servicios
export const GET_SERVICES = gql`
    query GetServices($filter: String) {
        services(filter: $filter) {
            id
            name
            description
            price
            city
            rating
            category
        }
    }
`;

export const GET_SERVICE = gql`
    query GetService($id: ID!) {
        serviceById(id: $id) {
            id
            name
            description
            price
            city
            rating
            category
            countryCode
            providerId
            latitude
            longitude
            transportType
            departureTime
            arrivalTime
            routeDescription
            images {
                id
                imageUrl
                imageBase64
                isPrimary
            }
            questions {
                id
                userId
                question
                answer
                answeredAt
                createdAt
            }
            reviews {
                id
                userId
                username
                rating
                comment
                createdAt
            }
            countryInfo {
                name
                capital
                region
                population
                currency
                flag
                languages
            }
            weatherInfo {
                description
                temperature
                humidity
                windSpeed
                icon
            }
        }
    }
`;

export const CREATE_SERVICE = gql`
    mutation CreateService($input: ServiceInput!) {
        createService(input: $input) {
            id
            name
            description
            price
            category
            city
            countryCode
        }
    }
`;

export const UPDATE_SERVICE = gql`
    mutation UpdateService($id: ID!, $input: ServiceInput!) {
        updateService(id: $id, input: $input) {
            id
            name
            description
            price
            category
            city
            countryCode
        }
    }
`;

export const DELETE_SERVICE = gql`
    mutation DeleteService($id: ID!) {
        deleteService(id: $id)
    }
`;

// Perfil de Usuario
export const GET_CURRENT_USER_PROFILE = gql`
    query GetCurrentUserProfile {
        currentUserProfile {
            keycloakId
            username
            email
            age
            photoBase64
            description
            role
            phone
            website
            socialMedia
        }
    }
`;

export const CREATE_OR_UPDATE_USER_PROFILE = gql`
    mutation CreateOrUpdateUserProfile($input: UserProfileInput!) {
        createOrUpdateUserProfile(input: $input) {
            keycloakId
            username
            email
            age
            photoBase64
            description
            role
            phone
            website
            socialMedia
        }
    }
`;

// Carrito de Compras
export const GET_MY_CART = gql`
    query GetMyCart {
        myCart {
            id
            serviceId
            quantity
            unitPrice
            serviceName
            serviceCategory
        }
    }
`;

export const GET_CART_TOTAL = gql`
    query GetCartTotal {
        cartTotal
    }
`;

export const ADD_TO_CART = gql`
    mutation AddToCart($serviceId: ID!, $quantity: Int) {
        addToCart(serviceId: $serviceId, quantity: $quantity) {
            id
            serviceId
            quantity
            unitPrice
        }
    }
`;

export const UPDATE_CART_ITEM_QUANTITY = gql`
    mutation UpdateCartItemQuantity($cartItemId: ID!, $quantity: Int!) {
        updateCartItemQuantity(cartItemId: $cartItemId, quantity: $quantity) {
            id
            quantity
        }
    }
`;

export const REMOVE_FROM_CART = gql`
    mutation RemoveFromCart($cartItemId: ID!) {
        removeFromCart(cartItemId: $cartItemId)
    }
`;

export const CLEAR_CART = gql`
    mutation ClearCart {
        clearCart
    }
`;

export const CHECKOUT_CART = gql`
    mutation CheckoutCart {
        checkoutCart
    }
`;

// Reviews
export const GET_REVIEWS_BY_SERVICE = gql`
    query GetReviewsByService($serviceId: String!) {
        reviewsByService(serviceId: $serviceId) {
            id
            serviceId
            userId
            username
            rating
            comment
            createdAt
        }
    }
`;

export const CREATE_REVIEW = gql`
    mutation CreateReview($input: ReviewInput!) {
        createReview(input: $input) {
            id
            serviceId
            userId
            username
            rating
            comment
            createdAt
        }
    }
`;

// Preguntas
export const ASK_QUESTION = gql`
    mutation AskQuestion($input: QuestionInput!) {
        askQuestion(input: $input) {
            id
            userId
            question
            answer
            createdAt
        }
    }
`;

export const ANSWER_QUESTION = gql`
    mutation AnswerQuestion($questionId: ID!, $answer: String!) {
        answerQuestion(questionId: $questionId, answer: $answer) {
            id
            answer
            answeredAt
        }
    }
`;

// Registro
export const REGISTER = gql`
    mutation Register($input: RegisterInput!) {
        register(input: $input)
    }
`;
