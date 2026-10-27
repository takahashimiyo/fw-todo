package com.example.todoapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TodoService {

    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to, true, Integer.MAX_VALUE, 0);
    }

    public List<Todo> search(String keyword, String category, String order) {
        return search(keyword, category, order, null, null);
    }

    public List<Todo> search(String keyword, String category, String order, boolean showCompleted) {
        return search(keyword, category, order, showCompleted, 1, 10);
    }

    public List<Todo> search(String keyword, String category, String order, boolean showCompleted,
            int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return todoMapper.search(keyword, category, order, null, null, showCompleted, pageSize, offset);
    }

    public long countSearch(String keyword, String category, boolean showCompleted) {
        return todoMapper.countSearch(keyword, category, null, null, showCompleted);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public int create(Todo todo) {
        int result = todoMapper.insert(todo);
        log.info("Todo registration completed");
        return result;
    }

    public int update(Todo todo) {
        Todo existing = todoMapper.findById(todo.getId());
        if (existing != null) {
            if (!Boolean.TRUE.equals(existing.getCompleted()) && Boolean.TRUE.equals(todo.getCompleted())) {
                todo.setCompletedAt(LocalDateTime.now());
            } else if (Boolean.TRUE.equals(existing.getCompleted()) && !Boolean.TRUE.equals(todo.getCompleted())) {
                todo.setCompletedAt(null);
            } else {
                todo.setCompletedAt(existing.getCompletedAt());
            }
        }
        int result = todoMapper.update(todo);
        log.info("Todo edit completed");
        return result;
    }

    public int delete(Long id) {
        int result = todoMapper.deleteById(id);
        log.info("Todo deletion completed");
        return result;
    }
}
