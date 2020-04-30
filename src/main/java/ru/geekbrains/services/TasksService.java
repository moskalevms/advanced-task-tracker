package ru.geekbrains.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.geekbrains.entities.Project;
import ru.geekbrains.entities.Task;
import ru.geekbrains.entities.TaskHistory;
import ru.geekbrains.entities.User;
import ru.geekbrains.errors_handlers.ResourceNotFoundException;
import ru.geekbrains.events.TaskCreatedEvent;
import ru.geekbrains.repositories.TasksRepository;

import java.security.Principal;
import java.util.List;


@Service
public class TasksService {

    private TasksRepository tasksRepository;
    private UserService userService;
    private ProjectService projectService;
    private TaskHistoryService taskHistoryService;

    private ApplicationEventPublisher eventPub;

    @Autowired
    public void setEventPub(ApplicationEventPublisher eventPub) {
        this.eventPub = eventPub;
    }

    @Autowired
    public void setTasksRepository(TasksRepository tasksRepository) {
        this.tasksRepository = tasksRepository;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Autowired
    public void setTaskHistoryService(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    public Page<Task> findAllSpec(Specification<Task> spec, Pageable pageable) {
        return tasksRepository.findAll(spec, pageable);
    }

    public List<Task> findAll() {
        return tasksRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    public Task findById(Long id) {
        return tasksRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    @Transactional
    public void save(Task task) {
        tasksRepository.save(task);
        TaskCreatedEvent event = new TaskCreatedEvent(task);
        eventPub.publishEvent(event);
    }

    @Transactional
    public Task save(Task task, Principal principal) {

        if (task.getId() != null) {
            TaskHistory taskHistory = new TaskHistory();
            taskHistory.setTask_id(task.getId());

            // что изменилось в задаче
            Task currentTask = this.findById(task.getId());
            StringBuilder description = new StringBuilder();

            if (!task.getTitle().equals(currentTask.getTitle())) {
                description.append("Название изменено с '" + currentTask.getTitle() + "' на " + "'" + task.getTitle() + "'. ");
            }

            if (!task.getDescription().equals(currentTask.getDescription())) {
                description.append("Описание изменено с '" + currentTask.getDescription() + "' на " + "'" + task.getDescription() + "'. ");
            }

            if (!task.getEmployer_id().equals(currentTask.getEmployer_id())) {
                User currentEmployer = userService.findById(currentTask.getEmployer_id());
                User newEmployer = userService.findById(task.getEmployer_id());
                description.append("Исполнитель изменен с '" + currentEmployer.getLastname() + " " + currentEmployer.getFirstname()
                            + "' на " + "'" + newEmployer.getLastname() + " " + newEmployer.getFirstname() + "'. ");
            }

            if (!task.getDue_time().equals(currentTask.getDue_time())) {
                description.append("Дата завершения изменена с '" + currentTask.getDue_time() + "' на " + "'" + task.getDue_time() + "'. ");
            }

            if (!task.getPlan_time().equals(currentTask.getPlan_time())) {
                description.append("Время выполнения изменилось с '" + currentTask.getPlan_time() + "ч' на " + "'" + task.getPlan_time() + "ч'. ");
            }

            if (Float.compare(task.getProgress(), currentTask.getProgress()) != 0) {
                description.append("Прогресс изменился с '" + currentTask.getProgress() + "%' на " + "'" + task.getProgress() + "%'.");
            }

            if (!task.getProject_id().equals(currentTask.getProject_id())) {
                Project currentProject = projectService.findById(currentTask.getProject_id());
                Project newProject = projectService.findById(task.getProject_id());
                description.append("Проект изменен с '" + currentProject.getTitle()
                        + "' на " + "'" + newProject.getTitle() + "'. ");
            }

            if (!task.getUrgency().equals(currentTask.getUrgency())) {
                description.append("Срочность изменилась с '" + currentTask.getUrgency() + "' на " + "'" + task.getUrgency() + "'. ");
            }

            if (!task.getStatus().equals(currentTask.getStatus())) {
                description.append("Статус изменился с '" + currentTask.getStatus() + "' на " + "'" + task.getStatus() + "'. ");
            }

            if (description.length() > 0) {
                User curUser = userService.getUser(principal.getName());
                description.insert(0, "Пользователь " + curUser.getFirstname()
                        + " " + curUser.getLastname()
                        + " внес изменения: ");

                taskHistory.setDescription(description.toString());
                taskHistory.setUser_id(curUser.getId());
                taskHistoryService.save(taskHistory);
            }
        }
        return tasksRepository.save(task);
    }


}
