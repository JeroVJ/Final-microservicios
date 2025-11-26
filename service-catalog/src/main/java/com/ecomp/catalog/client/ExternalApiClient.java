package com.ecomp.catalog.client;

import com.ecomp.catalog.dto.CatalogDtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ExternalApiClient {

    private final WebClient webClient;

    @Value("${external.restcountries.url:https://restcountries.com/v3.1}")
    private String restCountriesUrl;

    @Value("${external.openweather.url:https://api.openweathermap.org/data/2.5}")
    private String openWeatherUrl;

    @Value("${external.openweather.apikey:}")
    private String openWeatherApiKey;

    public ExternalApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public Mono<CountryInfoDto> getCountryInfo(String countryCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return Mono.empty();
        }

        return webClient.get()
                .uri(restCountriesUrl + "/alpha/" + countryCode)
                .retrieve()
                .bodyToMono(List.class)
                .map(list -> {
                    if (list.isEmpty()) return null;
                    Map<String, Object> country = (Map<String, Object>) list.get(0);
                    
                    Map<String, Object> name = (Map<String, Object>) country.get("name");
                    List<String> capital = (List<String>) country.get("capital");
                    Map<String, Object> currencies = (Map<String, Object>) country.get("currencies");
                    Map<String, Object> flags = (Map<String, Object>) country.get("flags");
                    Map<String, String> languages = (Map<String, String>) country.get("languages");

                    String currencyName = "";
                    if (currencies != null && !currencies.isEmpty()) {
                        Map<String, Object> firstCurrency = (Map<String, Object>) currencies.values().iterator().next();
                        currencyName = (String) firstCurrency.get("name");
                    }

                    return CountryInfoDto.builder()
                            .name(name != null ? (String) name.get("common") : null)
                            .capital(capital != null && !capital.isEmpty() ? capital.get(0) : null)
                            .region((String) country.get("region"))
                            .population(country.get("population") != null ? ((Number) country.get("population")).longValue() : null)
                            .currency(currencyName)
                            .flag(flags != null ? (String) flags.get("svg") : null)
                            .languages(languages != null ? List.copyOf(languages.values()) : List.of())
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Error fetching country info for {}: {}", countryCode, e.getMessage());
                    return Mono.empty();
                });
    }

    @SuppressWarnings("unchecked")
    public Mono<WeatherInfoDto> getWeatherInfo(String city, String countryCode) {
        if (city == null || city.isEmpty() || openWeatherApiKey.isEmpty()) {
            return Mono.empty();
        }

        String query = city + (countryCode != null ? "," + countryCode : "");

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.openweathermap.org")
                        .path("/data/2.5/weather")
                        .queryParam("q", query)
                        .queryParam("appid", openWeatherApiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "es")
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
                    Map<String, Object> main = (Map<String, Object>) response.get("main");
                    Map<String, Object> wind = (Map<String, Object>) response.get("wind");

                    return WeatherInfoDto.builder()
                            .description(weather != null && !weather.isEmpty() ? 
                                    (String) weather.get(0).get("description") : null)
                            .temperature(main != null ? ((Number) main.get("temp")).doubleValue() : null)
                            .humidity(main != null ? ((Number) main.get("humidity")).doubleValue() : null)
                            .windSpeed(wind != null ? ((Number) wind.get("speed")).doubleValue() : null)
                            .icon(weather != null && !weather.isEmpty() ? 
                                    (String) weather.get(0).get("icon") : null)
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("Error fetching weather for {}: {}", city, e.getMessage());
                    return Mono.empty();
                });
    }
}
