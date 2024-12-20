package ch.heigvd.dai.database;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database {
	private final List<Project> projects;

	public Database() {
		this.projects = new CopyOnWriteArrayList<>();
	}

	public Database(Database other) {
		this.projects = new ArrayList<>(other.projects.size());
		for (Project project : other.projects) {
			this.projects.add(new Project(project));
		}
	}

	public boolean addProject(Project project) {
		if (getProject(project.getName()) != null) {
			// Le projet existe déjà
			return false;
		}
		projects.add(project);
		return true;
	}

	public Project getProject(String name) {
		for (Project project : projects) {
			if (project.getName().equals(name)) {
				return project;
			}
		}
		return null;
	}

	public List<Project> getProjects() {
		return projects;
	}

	// faire une gestion d'erreur ?
	public boolean deleteProject(String name) {
		for (Project project : projects) {
			if (project.getName().equals(name)) {
				projects.remove(project);
				return true;
			}
		}

		return false;
	}

	public Database getProjectsList() {
		// return the empty database with just the title
		Database db = new Database();
		for (Project project : projects) {
			db.addProject(new Project(project.getName()));
		}

		return db;
	}
}
