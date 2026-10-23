package com.example.todoapp.api;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

@RestController
public class HolidayApiController {

    private final HolidayClient holidayClient = new HolidayClient();

    @GetMapping("/api/holidays")
    public ResponseEntity<Map<String, String>> getHolidays(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        HolidayClient.HolidayResult result = holidayClient.getHolidays();
        Map<String, String> holidays = result.holidays();
        Map<String, String> response;
        if (from == null && to == null) {
            response = holidays;
        } else {
            response = holidays.entrySet().stream()
                    .filter(entry -> {
                        LocalDate date = LocalDate.parse(entry.getKey());
                        return (from == null || !date.isBefore(from))
                                && (to == null || !date.isAfter(to));
                    })
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (first, second) -> first,
                            java.util.LinkedHashMap::new));
        }
        if (result.unavailable()) {
            return ResponseEntity.ok().header("X-Holidays-Unavailable", "true").body(response);
        }
        return ResponseEntity.ok().body(response);
    }
}
