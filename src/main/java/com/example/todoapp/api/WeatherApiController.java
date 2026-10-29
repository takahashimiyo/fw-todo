package com.example.todoapp.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherApiController {

    private final WeatherClient weatherClient;

    public WeatherApiController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    @GetMapping("/api/weather")
    public ResponseEntity<Map<String, Object>> getWeather() {
        WeatherClient.WeatherResult result = weatherClient.getWeather();
        Map<String, Object> response = new LinkedHashMap<>();
        if (!result.unavailable()) {
            response.put("forecasts", result.forecasts());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok().header("X-Weather-Unavailable", "true").body(response);
    }
}
