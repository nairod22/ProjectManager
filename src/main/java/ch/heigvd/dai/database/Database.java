package ch.heigvd.dai.database;

import javax.xml.crypto.Data;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Database {
    private final List<Project> projects;

    public Database() {
        this.projects = new CopyOnWriteArrayList<>();
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
