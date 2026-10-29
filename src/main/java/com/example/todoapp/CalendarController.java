package com.example.todoapp;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalendarController {

    private final TodoService todoService;

    public CalendarController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) LocalDate date, Model model) {
        String selectedView = List.of("day", "week", "month").contains(view) ? view : "month";
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate focusDate = date != null ? date : LocalDate.of(
                year == null ? currentMonth.getYear() : year,
                month == null ? currentMonth.getMonthValue() : month, 1);
        YearMonth displayedMonth = YearMonth.from(focusDate);

        LocalDate firstDay;
        LocalDate lastDay;
        List<LocalDate> days = new ArrayList<>();
        if ("day".equals(selectedView)) {
            firstDay = focusDate;
            lastDay = focusDate;
        } else if ("week".equals(selectedView)) {
            firstDay = focusDate.minusDays(focusDate.getDayOfWeek().getValue() % 7);
            lastDay = firstDay.plusDays(6);
            for (int i = 0; i < 7; i++) days.add(firstDay.plusDays(i));
        } else {
            firstDay = displayedMonth.atDay(1);
            lastDay = displayedMonth.atEndOfMonth();
            int leadingBlanks = firstDay.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < leadingBlanks; i++) days.add(null);
            for (int day = 1; day <= displayedMonth.lengthOfMonth(); day++) days.add(displayedMonth.atDay(day));
            while (days.size() % 7 != 0) days.add(null);
        }

        List<Todo> datedTodos = todoService.search("", "", "asc", firstDay, lastDay);
        Map<String, List<Todo>> todosByDate = new LinkedHashMap<>();
        for (Todo todo : datedTodos) {
            todosByDate.computeIfAbsent(todo.getDueDate().toString(), ignored -> new ArrayList<>()).add(todo);
        }
        List<Todo> dayTodos = "day".equals(selectedView)
                ? todosByDate.getOrDefault(focusDate.toString(), List.of()) : List.of();
        List<Todo> undatedTodos = "day".equals(selectedView) && focusDate.equals(today)
                ? todoService.findUndatedActiveTodos() : List.of();
        long completedCount = dayTodos.stream().filter(todo -> Boolean.TRUE.equals(todo.getCompleted())).count()
                + undatedTodos.stream().filter(todo -> Boolean.TRUE.equals(todo.getCompleted())).count();
        int totalCount = dayTodos.size() + undatedTodos.size();

        LocalDate previousDate = "day".equals(selectedView) ? focusDate.minusDays(1)
                : "week".equals(selectedView) ? firstDay.minusDays(7) : displayedMonth.minusMonths(1).atDay(1);
        LocalDate nextDate = "day".equals(selectedView) ? focusDate.plusDays(1)
                : "week".equals(selectedView) ? firstDay.plusDays(7) : displayedMonth.plusMonths(1).atDay(1);
        String title = "day".equals(selectedView)
                ? focusDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日（E）"))
                : "week".equals(selectedView)
                        ? firstDay.format(DateTimeFormatter.ofPattern("yyyy年M月d日")) + "〜"
                                + lastDay.format(DateTimeFormatter.ofPattern("M月d日"))
                        : displayedMonth.format(DateTimeFormatter.ofPattern("yyyy年M月"));

        model.addAttribute("selectedView", selectedView);
        model.addAttribute("activeMenu", "day".equals(selectedView) ? "day" : "month");
        model.addAttribute("title", title);
        model.addAttribute("today", today);
        model.addAttribute("focusDate", focusDate);
        model.addAttribute("firstDay", firstDay);
        model.addAttribute("lastDay", lastDay);
        model.addAttribute("days", days);
        model.addAttribute("todosByDate", todosByDate);
        model.addAttribute("dayTodos", dayTodos);
        model.addAttribute("undatedTodos", undatedTodos);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("remainingCount", totalCount - completedCount);
        model.addAttribute("progress", totalCount == 0 ? 0 : completedCount * 100 / totalCount);
        model.addAttribute("previousDate", previousDate);
        model.addAttribute("nextDate", nextDate);
        return "calendar";
    }
}
