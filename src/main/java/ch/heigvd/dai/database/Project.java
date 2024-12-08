package ch.heigvd.dai.database;

import java.util.LinkedList;
import java.util.List;

public class Project {
	private String name;
	private List<Task> tasks;

	// must have a name, but the task list could be null
	public Project(String name) {
		this.name = name;
		this.tasks = new LinkedList<Task>();
	}

	public Project(String name, List<Task> tasks) {
		this.name = name;
		this.tasks = tasks;
	}

	public String getName() {
		return name;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void addTask(Task task) {
		tasks.add(task);
	}

	public void removeTask(String task_name) {
		for (Task task : tasks) {
			if (task.getName().equals(task_name)) {
				tasks.remove(task);
			}
		}
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
