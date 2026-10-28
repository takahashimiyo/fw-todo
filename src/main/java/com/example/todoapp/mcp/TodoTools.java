package com.example.todoapp.mcp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import com.example.todoapp.Todo;
import com.example.todoapp.TodoService;
import com.example.todoapp.api.HolidayClient;
import com.example.todoapp.api.TodoDto;

@Component
public class TodoTools {

    private final TodoService todoService;
    private final HolidayClient holidayClient;

    public TodoTools(TodoService todoService, HolidayClient holidayClient) {
        this.todoService = todoService;
        this.holidayClient = holidayClient;
    }

    @McpTool(name = "list_todos", description = "やることの一覧を返す（期間・ジャンルで絞れる）")
    public List<TodoDto> listTodos(
            @McpToolParam(required = false) String keyword,
            @McpToolParam(required = false) String category,
            @McpToolParam(required = false) String from,
            @McpToolParam(required = false) String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);

        return todoService.search(keyword, category, "asc", fromDate, toDate)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @McpTool(name = "summarize_week", description = "指定期間のやることを集計し、合計件数・ジャンル別件数・期限を過ぎた未完了件数を返す")
    public Map<String, Object> summarizeWeek(
            @McpToolParam(required = false, description = "期間の始まり。yyyy-MM-dd形式（例: 2026-04-06）") String from,
            @McpToolParam(required = false, description = "期間の終わり。yyyy-MM-dd形式（例: 2026-04-06）") String to) {
        LocalDate fromDate = from == null ? null : LocalDate.parse(from);
        LocalDate toDate = to == null ? null : LocalDate.parse(to);
        List<Todo> todos = todoService.search(null, null, "asc", fromDate, toDate);

        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        for (String category : List.of("デザイン", "マーケティング", "プログラミング", "資格", "就職活動")) {
            categoryCounts.put(category, 0);
        }

        LocalDate today = LocalDate.now();
        long overdueCount = 0;
        for (Todo todo : todos) {
            categoryCounts.merge(todo.getCategory(), 1, Integer::sum);
            if (!Boolean.TRUE.equals(todo.getCompleted())
                    && todo.getDueDate() != null
                    && todo.getDueDate().isBefore(today)) {
                overdueCount++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", todos.size());
        summary.put("categoryCounts", categoryCounts);
        summary.put("overdueCount", overdueCount);
        return summary;
    }

    @McpTool(name = "get_todo", description = "やることを1件返す")
    public TodoDto getTodo(@McpToolParam(required = true) Long id) {
        Todo todo = todoService.findById(id);
        return todo == null ? null : TodoDto.from(todo);
    }

    @McpTool(name = "add_todo", description = "やることを1件足す")
    public TodoDto addTodo(
            @McpToolParam(required = true) String title,
            @McpToolParam(required = false) String detail,
            @McpToolParam(required = true) String category,
            @McpToolParam(required = true) Integer priority,
            @McpToolParam(required = false) String dueDate) {
        validateCategory(category);
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(dueDate == null ? null : LocalDate.parse(dueDate));
        todo.setCompleted(false);
        todoService.create(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "update_todo", description = "やることを1件直す（期限を変えるのもこれ）")
    public TodoDto updateTodo(
            @McpToolParam(required = true) Long id,
            @McpToolParam(required = false) String title,
            @McpToolParam(required = false) String detail,
            @McpToolParam(required = false) String category,
            @McpToolParam(required = false) Integer priority,
            @McpToolParam(required = false) String dueDate,
            @McpToolParam(required = false) Boolean completed) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return null;
        }
        if (category != null) {
            validateCategory(category);
            todo.setCategory(category);
        }
        if (title != null)
            todo.setTitle(title);
        if (detail != null)
            todo.setDetail(detail);
        if (priority != null)
            todo.setPriority(priority);
        if (dueDate != null)
            todo.setDueDate(LocalDate.parse(dueDate));
        if (completed != null)
            todo.setCompleted(completed);
        todoService.update(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "complete_todo", description = "やることを完了にする")
    public TodoDto completeTodo(@McpToolParam(required = true) Long id) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            return null;
        }
        todo.setCompleted(true);
        todoService.update(todo);
        return TodoDto.from(todo);
    }

    @McpTool(name = "delete_todo", description = "やることを1件消す")
    public boolean deleteTodo(@McpToolParam(required = true) Long id) {
        if (todoService.findById(id) == null) {
            return false;
        }
        return todoService.delete(id) > 0;
    }

    @McpTool(name = "find_free_days", description = "期間の中で、期限のやることが無く、土日でも祝日でもない「空いている日」を返す。やることの期限を動かす先を決めるのに使う")
    public List<LocalDate> findFreeDays(
            @McpToolParam(required = true) String from,
            @McpToolParam(required = true) String to) {
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        if (toDate.isBefore(fromDate)) {
            return List.of();
        }

        Set<LocalDate> busyDays = todoService.search(null, null, "asc", fromDate, toDate).stream()
                .map(Todo::getDueDate)
                .filter(date -> date != null)
                .collect(java.util.stream.Collectors.toSet());
        Map<String, String> holidays = holidayClient.getHolidays().holidays();
        Set<LocalDate> holidayDays = holidays.keySet().stream()
                .map(LocalDate::parse)
                .collect(java.util.stream.Collectors.toSet());

        List<LocalDate> freeDays = new ArrayList<>();
        for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
            if (!busyDays.contains(date)
                    && !holidayDays.contains(date)
                    && date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                freeDays.add(date);
            }
        }
        return freeDays;
    }

    private void validateCategory(String category) {
        if (!Set.of("デザイン", "マーケティング", "プログラミング", "資格", "就職活動").contains(category)) {
            throw new IllegalArgumentException("category は指定された5つのジャンルから選んでください");
        }
    }
}
