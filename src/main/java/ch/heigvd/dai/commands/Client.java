package ch.heigvd.dai.commands;

import ch.heigvd.dai.database.Metadata;
import ch.heigvd.dai.database.Project;
import ch.heigvd.dai.database.Task;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.heigvd.dai.database.Database;
import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Start the client part of the network game.")
public class Client implements Callable<Integer> {
    public enum Message {
        HELLO,
        PROJL,
        PROJS,
        ADDPRJ,
        DELPR,
        ADDTS,
        DELTS,
        GETTS,
        MODTS,
        HELP,
    }

    // End of line character
    public static String END_OF_LINE = "\n";

    private enum Menu {
        TASK,
        PROJECT
    }

    private Map<String, Project> projects;
    private Project currentProject;

    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Host to connect to.",
            required = true)
    protected String host;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "6433")
    protected int port;

    @Override
    public Integer call() {
        try (Socket socket = new Socket(host, port);
             BufferedReader in =
                     new BufferedReader(
                             new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out =
                     new BufferedWriter(
                             new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            System.out.println("[Client] Connected to " + host + ":" + port);

            // help();

            projects = new HashMap<>();
            currentProject = new Project(null);
            boolean firstLoop = true;
            Menu currentMenu = Menu.PROJECT;

            while (!socket.isClosed()) {
                String userInput = "";

                if (!firstLoop) {
                    userInput = switch (currentMenu) {
                        case PROJECT -> projectPrompt();
                        case TASK -> taskPrompt();
                    };
                } else {
                    userInput = "HELLO";
                    firstLoop = false;
                }

                System.out.println(userInput);

                try {
                    String[] userInputParts = userInput.split(" ", 2);
                    Message message = Message.valueOf(userInputParts[0].toUpperCase());

                    // todo : est-ce que c'est ok ?
                    String arg = "";
                    if (userInputParts.length > 1) {
                        arg = userInputParts[1];
                    }

                    String request = null;

                    switch (message) {
                        case HELLO -> {
                            request = Message.HELLO + END_OF_LINE;
                        }

                        case PROJL -> {
                            request = Message.PROJL + END_OF_LINE;
                        }
                        case PROJS -> {
                            request = Message.PROJS + userInputParts[1] + arg + END_OF_LINE;
                            currentProject = projects.get(userInputParts[1]);
                        }

                        case ADDPRJ -> {
                            request = Message.ADDPRJ + userInputParts[1] + arg + END_OF_LINE;
                        }

                        case DELPR -> {
                            request = Message.DELPR + userInputParts[1] + arg + END_OF_LINE;
                        }

                        case ADDTS -> {
                            request = Message.ADDTS + userInputParts[1] + arg + END_OF_LINE;
                        }

                        case DELTS -> {
                            request = Message.DELTS + userInputParts[1] + arg + END_OF_LINE;
                        }

                        case GETTS -> {
                            request = Message.GETTS + userInputParts[1] + arg + END_OF_LINE;
                        }

                        case MODTS -> {
                            request = Message.MODTS + userInputParts[1] + arg + END_OF_LINE;
                        }

                        case HELP -> {
                        }
                        // pas de default -> erreur gérée dans le catch !
                    }

                    if (request != null) {
                        out.write(request);
                        out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command. Please try again.");
                    continue;
                }

                String serverResponse = in.readLine();

                if (serverResponse == null) {
                    System.out.println("[Client] Server returned a null response.");
                    socket.close();
                    continue;
                }

                String[] serverResponseParts = serverResponse.split(" ", 2);

                Server.Message message = null;
                String arg = "";

                try {
                    message = Server.Message.valueOf(serverResponseParts[0]);
                    if (serverResponseParts.length > 1) {
                        arg = serverResponseParts[1];
                    }

                } catch (IllegalArgumentException e) {
                    // Do nothing
                }

                // todo : a finir d'implémenter une fois les réponses du servers sont implémentée correctement
                switch (message) {
                    case PROJL -> {
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(serverResponseParts[1], JsonObject.class);
                        JsonArray projectNamesArray = jsonObject.getAsJsonArray("projectNames");

                        projects = new HashMap<>();
                        for (int i = 0; i < projectNamesArray.size(); i++) {
                            String projectName = projectNamesArray.get(i).getAsString();
                            projects.put(projectName, new Project(projectName));
                        }
                    }
                    case ERROR -> {
                        System.out.println("[Client] Receive ERROR");
                        // Give error to user so he can redo
                    }

                    case OKAYY -> {
                        System.out.println("[Client]  Receive OKAYY");
                    }

                    case PROJD -> {
                        Gson gson = new Gson();
                        JsonObject projectDetails = gson.fromJson(serverResponseParts[1], JsonObject.class);
                        String projectName = projectDetails.get("name").getAsString();
                        JsonArray taskArray = projectDetails.getAsJsonArray("tasks");

                        List<Task> tasks = new ArrayList<>();
                        for (int i = 0; i < taskArray.size(); i++) {
                            JsonObject taskJson = taskArray.get(i).getAsJsonObject();
                            String taskName = taskJson.get("name").getAsString();
                            JsonObject metadataJson = taskJson.get("metadata").getAsJsonObject();
                            Metadata metadata = null;

                            if (metadataJson != null) {
                                String priority = metadataJson.get("priority").getAsString();
                                String dueDate = metadataJson.get("due").getAsString();
                                metadata = new Metadata(Metadata.Priority.valueOf(priority.toUpperCase()), new Date(dueDate));
                            }

                            Task task = new Task(taskName, metadata);
                            tasks.add(task);
                        }

                        Project project = new Project(projectName, tasks);
                        projects.put(projectName, project);
                        System.out.println("[Client] Received project details: " + projectName);
                    }

                    case TASKD -> {
                        Gson gson = new Gson();
                        JsonObject taskDetails = gson.fromJson(serverResponseParts[1], JsonObject.class);
                        String taskName = taskDetails.get("name").getAsString();
                        JsonObject metadataJson = taskDetails.get("metadata").getAsJsonObject();
                        Metadata metadata = null;

                        if (metadataJson != null) {
                            String priority = metadataJson.get("priority").getAsString();
                            String dueDate = metadataJson.get("due").getAsString();
                            metadata = new Metadata(Metadata.Priority.valueOf(priority.toUpperCase()), new Date(dueDate));
                        }

                        Task task = new Task(taskName, metadata);
                        System.out.println("[Client] Received task details: " + taskName);
                    }
                    case null, default -> System.out.println("Invalid/unknown command sent by server, ignore.");
                }
            }
            System.out.println("[Client] Closing connection and quitting...");
        } catch (IOException e) {
            System.out.println("[Client] Exception : " + e.getMessage());
            return -1;
        }
        return 0;
    }

    // Written with help from chatGPT
    private String projectPrompt() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Gson gson = new Gson();

        List<String> projectNames = new ArrayList<>(projects.keySet());

        while (true) {
            System.out.println("\nList of all projects:");
            for (int i = 0; i < projectNames.size(); i++) {
                System.out.println((i + 1) + ". " + projectNames.get(i));
            }
            System.out.println("\nOptions:");
            System.out.println("[1-N]: Choose a project by number");
            System.out.println("[Name]: Choose a project by name");
            System.out.println("[add]: Add a new project");
            System.out.println("[delete]: Delete a project by number or name");
            System.out.print("> ");

            try {
                String userInput = reader.readLine().trim();

                // Option: Add a new project
                if (userInput.equalsIgnoreCase("add")) {
                    System.out.print("Enter the name of the new project: ");
                    String newProjectName = reader.readLine().trim();
                    if (!projects.containsKey(newProjectName)) {
                        projectNames.add(newProjectName);
                        String payload = gson.toJson(Map.of("name", newProjectName));
                        return "ADDPR " + payload;
                    } else {
                        System.out.println("Project already exists!");
                    }
                    continue; // Show prompt again
                }

                // Option: Delete a project
                if (userInput.equalsIgnoreCase("delete")) {
                    System.out.print("Enter the project number or name to delete: ");
                    String toDelete = reader.readLine().trim();

                    // Handle delete by number
                    if (toDelete.matches("\\d+")) {
                        int index = Integer.parseInt(toDelete) - 1;
                        if (index >= 0 && index < projectNames.size()) {
                            String removedProject = projectNames.remove(index);
                            return "DELPR " + removedProject;
                        } else {
                            System.out.println("Invalid project number.");
                        }
                    }
                    // Handle delete by name
                    else if (projects.containsKey(toDelete)) {
                        projectNames.remove(toDelete);
                        return "DELPR " + toDelete;
                    } else {
                        System.out.println("Invalid project name.");
                    }
                    continue; // Show prompt again
                }

                // Option: Choose a project by number
                if (userInput.matches("\\d+")) {
                    int index = Integer.parseInt(userInput) - 1;
                    if (index >= 0 && index < projectNames.size()) {
                        String selectedProject = projectNames.get(index);
                        return "PROJS " + selectedProject;
                    }
                }

                // Option: Choose a project by name
                if (projects.containsKey(userInput)) {
                    return "PROJS" + userInput;
                }

                // Invalid input
                System.out.println("Invalid choice. Please try again.");
            } catch (IOException e) {
                System.out.println("Error reading input. Try again.");
            }
        }
    }

    private String taskPrompt() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Gson gson = new Gson();
        Map<String, Task> taskMap = new HashMap<>();
        for (Task task : currentProject.getTasks()) {
            taskMap.put(task.getName(), task);
        }

        while (true) {
            // Display task list
            System.out.println("\nTasks in project: " + currentProject.getName());
            if (taskMap.isEmpty()) {
                System.out.println("No tasks available.");
            } else {
                taskMap.forEach((key, task) -> System.out.println("- " + key + ": " + task));
            }

            System.out.println("\nOptions:");
            System.out.println("[add]: Add a new task");
            System.out.println("[delete]: Delete a task");
            System.out.println("[get]: Get task details");
            System.out.println("[modify]: Modify a task");
            System.out.println("[back]: Go back to project menu");
            System.out.print("> ");

            try {
                String userInput = reader.readLine().trim();

                // Add a task
                if (userInput.equalsIgnoreCase("add")) {
                    System.out.print("Enter the task name: ");
                    String taskName = reader.readLine().trim();
                    System.out.print("Enter the priority (low/medium/high) or leave empty: ");
                    String priorityInput = reader.readLine().trim().toUpperCase();
                    Metadata.Priority priority = null;
                    if (!priorityInput.isEmpty()) {
                        priority = Metadata.Priority.valueOf(priorityInput);
                    }
                    System.out.print("Enter the due date (YYYY-MM-DD) or leave empty: ");
                    String dueDateInput = reader.readLine().trim();
                    Date dueDate = null;
                    Metadata tmp = new Metadata(null, null);
                    if (!dueDateInput.isEmpty()) {
                        dueDate = tmp.convertStringToDate(dueDateInput);
                    }
                    tmp = null;
                    Metadata metadata = new Metadata(priority, dueDate);
                    Task newTask = new Task(taskName, metadata);

                    taskMap.put(taskName, newTask);
                    currentProject.getTasks().add(newTask);
                    String payload = gson.toJson(Map.of("tasks", Map.of("name", taskName, "metadata", metadata)));
                    return "ADDTS " + payload;
                }

                // Delete a task
                if (userInput.equalsIgnoreCase("delete")) {
                    System.out.print("Enter the task name to delete: ");
                    String taskName = reader.readLine().trim();
                    if (taskMap.containsKey(taskName)) {
                        taskMap.remove(taskName);
                        currentProject.getTasks().removeIf(task -> task.getName().equals(taskName));
                        return "DELTS " + taskName;
                    } else {
                        System.out.println("Task not found.");
                    }
                    continue;
                }

                // Get task details
                if (userInput.equalsIgnoreCase("get")) {
                    System.out.print("Enter the task name to get details: ");
                    String taskName = reader.readLine().trim();
                    if (taskMap.containsKey(taskName)) {
                        Task task = taskMap.get(taskName);
                        String payload = gson.toJson(Map.of("tasks", task));
                        return "GETTS " + payload;
                    } else {
                        System.out.println("Task not found.");
                    }
                    continue;
                }

                // Modify a task
                if (userInput.equalsIgnoreCase("modify")) {
                    System.out.print("Enter the task name to modify: ");
                    String taskName = reader.readLine().trim();
                    if (taskMap.containsKey(taskName)) {
                        Task task = taskMap.get(taskName);

                        System.out.println("Enter new values (leave empty to keep current value):");
                        System.out.print("New name: ");
                        String newName = reader.readLine().trim();
                        if (!newName.isEmpty()) {
                            task.setName(newName);
                            taskMap.remove(taskName);
                            taskMap.put(newName, task);
                            taskName = newName;
                        }

                        System.out.print("New priority (low/medium/high) or leave empty: ");
                        String newPriorityInput = reader.readLine().trim().toUpperCase();
                        if (!newPriorityInput.isEmpty()) {
                            Metadata.Priority newPriority = Metadata.Priority.valueOf(newPriorityInput);
                            task.getMetadata().setPriority(newPriority);
                        }

                        System.out.print("New due date (YYYY-MM-DD) or leave empty: ");
                        String newDueDateInput = reader.readLine().trim();
                        if (!newDueDateInput.isEmpty()) {
                            Date newDueDate = task.getMetadata().convertStringToDate(newDueDateInput);
                            task.getMetadata().setDueDate(newDueDate);
                        }

                        String payload = gson.toJson(Map.of("tasks", task));
                        return "MODTS " + taskName + " " + payload;
                    } else {
                        System.out.println("Task not found.");
                    }
                    continue;
                }

                // Go back
                if (userInput.equalsIgnoreCase("back")) {
                    return null;
                }

                // Invalid option
                System.out.println("Invalid choice. Please try again.");
            } catch (IOException e) {
                System.out.println("Error reading input. Try again.");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input format. Try again.");
            }
        }
    }
}
