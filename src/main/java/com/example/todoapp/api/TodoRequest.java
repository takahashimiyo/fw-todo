package com.example.todoapp.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TodoRequest {

    @NotBlank(message = "やることを入力してください")
    @Size(max = 255, message = "やることは255文字以内で入力してください")
    private String title;

    @Size(max = 255, message = "メモは255文字以内で入力してください")
    private String detail;

    @NotBlank(message = "ジャンルを選んでください")
    private String category;

    @NotNull(message = "優先度を選んでください")
    private Integer priority;

    private java.time.LocalDate dueDate;
    private Boolean completed;

    public com.example.todoapp.Todo toTodo() {
        var todo = new com.example.todoapp.Todo();
        todo.setTitle(title);
        todo.setDetail(detail);
        todo.setCategory(category);
        todo.setPriority(priority);
        todo.setDueDate(dueDate);
        todo.setCompleted(completed);
        return todo;
    }
}
