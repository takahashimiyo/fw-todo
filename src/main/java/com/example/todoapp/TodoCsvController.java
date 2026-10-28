package com.example.todoapp;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoCsvController {

    private static final DateTimeFormatter COMPLETED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TodoService todoService;

    public TodoCsvController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos.csv")
    public ResponseEntity<byte[]> exportTodos(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "すべて") String category,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "false") boolean showCompleted,
            @RequestParam(defaultValue = "") String trash) {
        if (!"asc".equals(order) && !"desc".equals(order)) {
            order = "asc";
        }
        boolean showTrash = "1".equals(trash);
        if (showTrash) {
            showCompleted = true;
        }
        String searchCategory = "すべて".equals(category) ? "" : category;
        List<Todo> todos = todoService.search(keyword, searchCategory, order, showCompleted, showTrash, 1,
                Integer.MAX_VALUE);

        StringBuilder csv = new StringBuilder();
        appendRow(csv, List.of("やること", "ジャンル", "優先度", "期限", "状態", "メモ"));
        for (Todo todo : todos) {
            String priority = switch (todo.getPriority() == null ? 0 : todo.getPriority()) {
                case 1 -> "高";
                case 2 -> "中";
                default -> "低";
            };
            String state = Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了";
            if (Boolean.TRUE.equals(todo.getCompleted()) && todo.getCompletedAt() != null) {
                state += "（" + todo.getCompletedAt().format(COMPLETED_AT_FORMAT) + "）";
            }
            appendRow(csv, List.of(
                    safeCell(todo.getTitle()),
                    safeCell(todo.getCategory()),
                    priority,
                    todo.getDueDate() == null ? "" : todo.getDueDate().toString(),
                    state,
                    safeCell(todo.getDetail())));
        }

        byte[] content = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[content.length + 3];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(content, 0, withBom, 3, content.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"todos.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(withBom);
    }

    private static String safeCell(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        int firstVisible = 0;
        while (firstVisible < value.length() && Character.isWhitespace(value.charAt(firstVisible))) {
            firstVisible++;
        }
        if (firstVisible < value.length() && "=+-@".indexOf(value.charAt(firstVisible)) >= 0) {
            return "'" + value;
        }
        return value;
    }

    private static void appendRow(StringBuilder csv, List<String> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append('"').append(cells.get(i).replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }
}
