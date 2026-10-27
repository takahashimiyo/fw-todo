package com.example.todoapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private final TodoService todoService;

    public HomeController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "やること管理");
        return "index";
    }

    @GetMapping("/todos")
    public String todos(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "すべて") String category,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "false") boolean showCompleted,
            @RequestParam(defaultValue = "") String trash,
            @RequestParam(defaultValue = "1") int page, Model model) {
        boolean showTrash = "1".equals(trash);
        if (showTrash) {
            showCompleted = true;
        }
        if (!"asc".equals(order) && !"desc".equals(order)) {
            order = "asc";
        }
        page = Math.max(page, 1);
        String searchCategory = "すべて".equals(category) ? "" : category;
        long totalItems = todoService.countSearch(keyword, searchCategory, showCompleted, showTrash);
        int totalPages = (int) Math.max(1, (totalItems + 9) / 10);
        page = Math.min(page, totalPages);
        model.addAttribute("todos", todoService.search(keyword, searchCategory, order, showCompleted, showTrash, page, 10));
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("today", java.time.LocalDate.now());
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("order", order);
        model.addAttribute("showCompleted", showCompleted);
        model.addAttribute("trash", showTrash);
        model.addAttribute("categories", new String[] { "デザイン", "マーケティング", "プログラミング", "資格", "就職活動" });
        return "todos";
    }

    @GetMapping("/todos/new")
    public String createForm(Model model) {
        model.addAttribute("todo", new Todo());
        return "create";
    }

    @PostMapping("/todos/confirm")
    public String confirmCreate(@Validated @ModelAttribute Todo todo, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "create";
        }
        model.addAttribute("todo", todo);
        return "create-confirm";
    }

    @PostMapping("/todos/new")
    public String rewriteCreate(@ModelAttribute Todo todo, Model model) {
        model.addAttribute("todo", todo);
        return "create";
    }

    @PostMapping("/todos")
    public String createTodo(@ModelAttribute Todo todo, RedirectAttributes redirectAttributes) {
        todoService.create(todo);
        redirectAttributes.addFlashAttribute("message", "登録しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "edit";
    }

    @PostMapping("/todos/{id}/confirm")
    public String confirmEdit(@PathVariable Long id, @Validated @ModelAttribute Todo todo,
            BindingResult bindingResult, Model model) {
        todo.setId(id);
        if (bindingResult.hasErrors()) {
            return "edit";
        }
        model.addAttribute("todo", todo);
        return "edit-confirm";
    }

    @PostMapping("/todos/{id}/edit")
    public String rewriteEdit(@PathVariable Long id, @ModelAttribute Todo todo, Model model) {
        todo.setId(id);
        model.addAttribute("todo", todo);
        return "edit";
    }

    @PostMapping("/todos/{id}")
    public String updateTodo(@PathVariable Long id, @ModelAttribute Todo todo,
            RedirectAttributes redirectAttributes) {
        todo.setId(id);
        todoService.update(todo);
        redirectAttributes.addFlashAttribute("message", "保存しました");
        return "redirect:/todos";
    }

    @GetMapping("/todos/{id}/delete")
    public String deleteConfirm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.findById(id);
        if (todo == null) {
            redirectAttributes.addFlashAttribute("message", "見つかりませんでした");
            return "redirect:/todos";
        }
        model.addAttribute("todo", todo);
        return "delete";
    }

    @PostMapping("/todos/{id}/delete")
    public String deleteTodo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.delete(id);
        redirectAttributes.addFlashAttribute("message", "削除しました");
        return "redirect:/todos";
    }

    @PostMapping("/todos/{id}/restore")
    public String restoreTodo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.restore(id);
        redirectAttributes.addFlashAttribute("message", "やることを元に戻しました");
        return "redirect:/todos?trash=1";
    }
}
