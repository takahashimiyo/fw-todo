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
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) LocalDate date, Model model) {
        boolean weekView = "week".equals(view);
        YearMonth current = YearMonth.now();
        LocalDate focusDate = date != null ? date : LocalDate.of(
                year == null ? current.getYear() : year,
                month == null ? current.getMonthValue() : month, 1);
        YearMonth displayed = YearMonth.from(focusDate);

        LocalDate firstDay;
        LocalDate lastDay;
        List<LocalDate> days = new ArrayList<>();
        if (weekView) {
            firstDay = focusDate.minusDays(focusDate.getDayOfWeek().getValue() % 7);
            lastDay = firstDay.plusDays(6);
            for (int i = 0; i < 7; i++) {
                days.add(firstDay.plusDays(i));
            }
        } else {
            firstDay = displayed.atDay(1);
            lastDay = displayed.atEndOfMonth();
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
        }

        YearMonth previous = displayed.minusMonths(1);
        YearMonth next = displayed.plusMonths(1);
        LocalDate previousWeek = firstDay.minusDays(7);
        LocalDate nextWeek = firstDay.plusDays(7);
        LocalDate switchDate = weekView ? focusDate
                : (displayed.equals(current) ? LocalDate.now() : displayed.atDay(1));
        model.addAttribute("title", weekView
                ? firstDay.getYear() + "年" + firstDay.getMonthValue() + "月" + firstDay.getDayOfMonth()
                        + "日〜" + lastDay.getYear() + "年" + lastDay.getMonthValue() + "月"
                        + lastDay.getDayOfMonth() + "日"
                : displayed.getYear() + "年" + displayed.getMonthValue() + "月");
        model.addAttribute("year", displayed.getYear());
        model.addAttribute("month", displayed.getMonthValue());
        model.addAttribute("weekView", weekView);
        model.addAttribute("days", days);
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        model.addAttribute("previousYear", previous.getYear());
        model.addAttribute("previousMonth", previous.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());
        model.addAttribute("previousWeekDate", previousWeek);
        model.addAttribute("nextWeekDate", nextWeek);
        model.addAttribute("switchDate", switchDate);
        return "calendar";
    }
}
