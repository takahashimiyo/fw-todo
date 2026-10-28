package com.example.todoapp.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WeatherClient {

    private static final Logger logger = LoggerFactory.getLogger(WeatherClient.class);

    private final RestClient restClient;

    public WeatherClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3_000);
        requestFactory.setReadTimeout(5_000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public WeatherResult getWeather() {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.open-meteo.com")
                            .path("/v1/forecast")
                            .queryParam("latitude", 35.68)
                            .queryParam("longitude", 139.76)
                            .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min")
                            .queryParam("forecast_days", 7)
                            .queryParam("timezone", "Asia/Tokyo")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (response == null || !(response.get("daily") instanceof Map<?, ?> daily)
                    || !(daily.get("time") instanceof List<?> dates)
                    || !(daily.get("weather_code") instanceof List<?> weatherCodes)
                    || !(daily.get("temperature_2m_max") instanceof List<?> temperaturesMax)
                    || !(daily.get("temperature_2m_min") instanceof List<?> temperaturesMin)) {
                logger.warn("Weather API response did not contain the expected daily forecast fields");
                return new WeatherResult(null, true);
            }

            int forecastCount = Math.min(7, Math.min(dates.size(), Math.min(weatherCodes.size(),
                    Math.min(temperaturesMax.size(), temperaturesMin.size()))));
            List<DailyForecast> forecasts = new ArrayList<>(forecastCount);
            for (int i = 0; i < forecastCount; i++) {
                if (!(dates.get(i) instanceof String date)
                        || !(weatherCodes.get(i) instanceof Number weatherCode)
                        || !(temperaturesMax.get(i) instanceof Number temperatureMax)
                        || !(temperaturesMin.get(i) instanceof Number temperatureMin)) {
                    logger.warn("Weather API response contained an invalid daily forecast value");
                    return new WeatherResult(null, true);
                }
                forecasts.add(new DailyForecast(date, weatherCode.intValue(), temperatureMax.doubleValue(),
                        temperatureMin.doubleValue()));
            }
            if (forecasts.size() != 7) {
                logger.warn("Weather API returned fewer than seven forecast days");
                return new WeatherResult(null, true);
            }
            return new WeatherResult(forecasts, false);
        } catch (RestClientException exception) {
            logger.warn("Could not retrieve weather from Open-Meteo", exception);
            return new WeatherResult(null, true);
        }
    }

    public record DailyForecast(String date, Integer weatherCode, Double temperatureMax, Double temperatureMin) {
    }

    public record WeatherResult(List<DailyForecast> forecasts, boolean unavailable) {
    }
}
