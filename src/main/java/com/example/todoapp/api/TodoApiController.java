package com.example.todoapp.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.example.todoapp.TodoService;

@RestController
public class TodoApiController {

    private final TodoService todoService;

    public TodoApiController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/api/todos")
    public List<TodoDto> listTodos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "asc") String order) {
        return todoService.search(keyword, category, order)
                .stream()
                .map(TodoDto::from)
                .toList();
    }

    @GetMapping("/api/todos/{id}")
    public ResponseEntity<?> getTodo(@PathVariable Long id) {
        var todo = todoService.findById(id);
        if (todo == null) {
            return notFound(id);
        }
        return ResponseEntity.ok(TodoDto.from(todo));
    }

    @PostMapping("/api/todos")
    public ResponseEntity<TodoDto> createTodo(@Valid @RequestBody TodoRequest request) {
        var todo = request.toTodo();
        if (todo.getCompleted() == null) {
            todo.setCompleted(false);
        }
        todoService.create(todo);
        var created = todoService.findById(todo.getId());
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/todos/{id}")
                .buildAndExpand(todo.getId())
                .toUri();
        return ResponseEntity.created(location).body(TodoDto.from(created));
    }

    @PutMapping("/api/todos/{id}")
    public ResponseEntity<?> replaceTodo(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        if (todoService.findById(id) == null) {
            return notFound(id);
        }
        var todo = request.toTodo();
        todo.setId(id);
        todoService.update(todo);
        return ResponseEntity.ok(TodoDto.from(todoService.findById(id)));
    }

    @DeleteMapping("/api/todos/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        if (todoService.findById(id) == null) {
            return notFound(id);
        }
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<java.util.Map<String, Object>> notFound(Long id) {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("type", "about:blank");
        body.put("title", "Not Found");
        body.put("status", 404);
        body.put("detail", "Todo with id " + id + " was not found.");
        body.put("instance", "/api/todos/" + id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<java.util.Map<String, Object>> invalidInput(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        var errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    var item = new java.util.LinkedHashMap<String, String>();
                    item.put("field", error.getField());
                    item.put("message", error.getDefaultMessage());
                    return item;
                })
                .toList();
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("type", "about:blank");
        body.put("title", "Bad Request");
        body.put("status", 400);
        body.put("detail", "入力に誤りがあります");
        body.put("instance", request.getRequestURI());
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }
}
