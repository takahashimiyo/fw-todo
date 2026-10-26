package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month, Model model) {
        YearMonth current = YearMonth.now();
        YearMonth displayed = YearMonth.of(year == null ? current.getYear() : year,
                month == null ? current.getMonthValue() : month);

        LocalDate firstDay = displayed.atDay(1);
        LocalDate lastDay = displayed.atEndOfMonth();
        List<LocalDate> days = new ArrayList<>();
        int leadingBlanks = firstDay.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < leadingBlanks; i++) {
            days.add(null);
        }
        for (int day = 1; day <= displayed.lengthOfMonth(); day++) {
            days.add(displayed.atDay(day));
        }
        while (days.size() % 7 != 0) {
            days.add(null);
        }

        YearMonth previous = displayed.minusMonths(1);
        YearMonth next = displayed.plusMonths(1);
        model.addAttribute("year", displayed.getYear());
        model.addAttribute("month", displayed.getMonthValue());
        model.addAttribute("days", days);
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        model.addAttribute("previousYear", previous.getYear());
        model.addAttribute("previousMonth", previous.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());
        return "calendar";
    }
}
