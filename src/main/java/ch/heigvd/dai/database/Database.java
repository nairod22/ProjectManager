package ch.heigvd.dai.database;

import javax.xml.crypto.Data;
import java.util.LinkedList;
import java.util.List;

public class Database {
    private List<Project> projects;

    public Database() {
        this.projects = new LinkedList<Project>();
    }

    public int addProject(Project project) {
        boolean result = projects.add(project);
        return result ? 0 : -1;
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
        //return the empty database with just the title
        Database db = new Database();
        for (Project project : projects) {
            db.addProject(new Project(project.getName()));
        }

        return db;
    }
}
