package ch.heigvd.dai.database;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Project {
    private String name;
    private List<Task> tasks;

    //must have a name, but the task list could be null
    public Project(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be null");
        }
        this.name = name;
        this.tasks = new CopyOnWriteArrayList<>();
    }

    public String getName() {
        return name;
    }

    public boolean addTask(Task task) {
        if (getTask(task.getName()) != null) {
            // task already exists
            return false;
        }
        tasks.add(task);
        return true;
    }

    public boolean removeTask(String task_name) {
        for (Task task : tasks) {
            if (task.getName().equals(task_name)) {
                tasks.remove(task);
                return true;
            }
        }
        return false;
    }

    public Task getTask(String task_name) {
        for (Task task : tasks) {
            if (task.getName().equals(task_name)) {
                return task;
            }
        }
        return null;
    }
}
