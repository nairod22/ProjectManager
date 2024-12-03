package ch.heigvd.dai.database;

import java.util.LinkedList;
import java.util.List;

public class Database {
    private List<Project> projects;

    public Database() {
        this.projects = new LinkedList<Project>();
    }

    public void addProject(Project project) {
        projects.add(project);
    }

    public void removeProject(Project project) {
        projects.remove(project);
    }

    public List<Project> getProjects() {
        return projects;
    }

    public Project getProject(String name) {
        for (Project project : projects) {
            if (project.getName().equals(name)) {
                return project;
            }
        }
        return null;
    }

    //faire une gestion d'erreur ?
    public void deleteProject(String name) {
        for (Project project : projects) {
            if (project.getName().equals(name)) {
                projects.remove(project);
            }
        }
    }
}
