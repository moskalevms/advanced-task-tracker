package ru.geekbrains.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.geekbrains.entities.Task;
import ru.geekbrains.services.TasksService;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TasksController {

    private TasksService tasksService;

    @Autowired
    public void setTasksService(TasksService tasksService) {
        this.tasksService = tasksService;
    }

    @GetMapping("/")
    public String showTasks(Model model) {
        List<Task> tasksList = tasksService.findAll();
        model.addAttribute("tasks", tasksList);
        return "tasks";
    }

    @GetMapping("/create")
    public String createTask(@ModelAttribute(name = "task") Task task) {
//        List<Task> tasksList = tasksService.findAll();
//        model.addAttribute("tasks", tasksList);
        //tasksService.save(task);
        return "create-task";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute(name = "task") Task task) {
        tasksService.save(task);
        return "redirect:/tasks/";
    }

}
