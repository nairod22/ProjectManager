package ch.heigvd.dai.commands;

import ch.heigvd.dai.database.Database;
import ch.heigvd.dai.database.Metadata;
import ch.heigvd.dai.database.Project;
import ch.heigvd.dai.database.Task;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.xml.crypto.Data;
import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Start the client part of the network game.")
public class Client implements Callable<Integer> {
	public enum Message {
		HELLO,
		PROJL,
		PROJS,
		ADDPR,
		DELPR,
		ADDTS,
		DELTS,
		GETTS,
		MODTS,
		HELP,
		CLOSE,
	}

	// End of line character
	public static String END_OF_LINE = "\n";

	private enum Menu {
		TASK,
		PROJECT
	}

	// private Map<String, Project> projects;
	private Database db;
	private Database tempDb;
	private Project currentProject;

	Menu currentMenu;

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

			// projects = new HashMap<>();
			db = new Database();
			tempDb = new Database();
			currentProject = null;
			boolean firstLoop = true;
			currentMenu = Menu.PROJECT;

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

				try {
					String[] userInputParts = userInput.split(" ", 2);
					Message message = Client.Message.valueOf(userInputParts[0]);
					// Message message = Message.valueOf(userInputParts[0].toUpperCase());

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
							request = Message.PROJS + " " + arg + END_OF_LINE;
							currentProject = db.getProject(userInputParts[1]);
						}

						case ADDPR -> {
							request = Message.ADDPR + " " + arg + END_OF_LINE;
						}

						case DELPR -> {
							request = Message.DELPR + " " + arg + END_OF_LINE;
						}

						case ADDTS -> {
							request = Message.ADDTS + " " + arg + END_OF_LINE;
						}

						case DELTS -> {
							request = Message.DELTS + " " + arg + END_OF_LINE;
						}

						case GETTS -> {
							request = Message.GETTS + " " + arg + END_OF_LINE;
						}

						case MODTS -> {
							request = Message.MODTS + " " + arg + END_OF_LINE;
						}

						case HELP -> {
						}

						case CLOSE -> {
							request = Message.CLOSE + END_OF_LINE;
							return 0;
						}
							// pas de default -> erreur gérée dans le catch !
					}

					if (request != null) {
						out.write(request);
						out.flush();
					}
				} catch (Exception e) {
					System.out.println("Invalid command. Please try again.");
					System.out.println(e);
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

				switch (message) {
					case PROJL -> {
						Gson gson = new Gson();
						db = gson.fromJson(arg, Database.class);
						tempDb = new Database(db);
					}
					case ERROR -> {
						System.out.println("[Client] Receive ERROR");
						tempDb = new Database(db);
						// Give error to user so he can redo
					}

					case OKAYY -> {
						// System.out.println("[Client]  Receive OKAYY");
						db = new Database(tempDb);
					}

					case PROJD -> {
						currentMenu = Menu.TASK;
						Gson gson = new Gson();
						JsonObject projectDetails = gson.fromJson(serverResponseParts[1], JsonObject.class);
						String projectName = projectDetails.get("name").getAsString();
						JsonArray taskArray = projectDetails.getAsJsonArray("tasks");

						List<Task> tasks = new ArrayList<>();
						if (taskArray != null) {
							for (int i = 0; i < taskArray.size(); i++) {
								JsonObject taskJson = taskArray.get(i).getAsJsonObject();
								String taskName = taskJson.get("name").getAsString();
								JsonObject metadataJson = taskJson.get("metadata").getAsJsonObject();
								Metadata metadata = null;

								if (metadataJson != null) {
									JsonElement metaPriority = metadataJson.get("priority");
									String priority = metaPriority == null ? "" : metaPriority.getAsString();
									JsonElement metaDue = metadataJson.get("due");
									String dueDate = metaDue == null ? "" : metaDue.getAsString();
									metadata = new Metadata(priority, dueDate);
								}

								Task task = new Task(taskName, metadata);
								tasks.add(task);
								db.getProject(currentProject.getName()).addTask(task);
							}
						}

						Project project = new Project(projectName, tasks);
						tempDb.addProject(project);
					}

					case TASKD -> {
						Gson gson = new Gson();
						JsonObject taskDetails = gson.fromJson(serverResponseParts[1], JsonObject.class);
						String taskName = taskDetails.get("name").getAsString();
						JsonObject metadataJson = taskDetails.get("metadata").getAsJsonObject();
						Metadata metadata = null;

						if (metadataJson != null) {
							JsonElement metaPriority = metadataJson.get("priority");
							String priority = metaPriority == null ? "" : metaPriority.getAsString();
							JsonElement metaDue = metadataJson.get("due");
							String dueDate = metaDue == null ? "" : metaDue.getAsString();
							metadata = new Metadata(priority, dueDate);
						}

						Task task = new Task(taskName, metadata);
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

		// List<String> projectNames = new ArrayList<>(projects.keySet());
		List<String> projectNames = new ArrayList<>();
		List<Project> projects = db.getProjects();
		for (int i = 0; i < projects.size(); i++) {
			String projectTitle = projects.get(i).getName();
			projectNames.add(projectTitle);
		}

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
			System.out.println("[close]: Closes the connection and quits the program");
			System.out.print("> ");

			try {
				String userInput = reader.readLine().trim();

				// Option: Add a new project
				if (userInput.equalsIgnoreCase("add")) {
					System.out.print("Enter the name of the new project: ");
					String newProjectName = reader.readLine().trim();
					projectNames.add(newProjectName);
					Project newProject = new Project(newProjectName);
					db.addProject(newProject);
					String payload = gson.toJson(Map.of("name", newProjectName));
					return "ADDPR " + payload;
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
							tempDb.deleteProject(removedProject);
							return "DELPR " + removedProject;
						} else {
							System.out.println("Invalid project number.");
						}
					}
					// Handle delete by name
					else if (db.getProject(toDelete) != null) {
						projectNames.remove(toDelete);
						tempDb.deleteProject(toDelete);
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
						currentProject = db.getProject(selectedProject);
						return "PROJS " + selectedProject;
					}
				}

				// Option: Choose a project by name
				if (db.getProject(userInput) != null) {
					currentProject = db.getProject(userInput);
					return "PROJS " + userInput;
				}

				if (userInput.equalsIgnoreCase("close")) {
					System.out.println("Closing the application. Goodbye!");
					return "CLOSE";
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
				taskMap.forEach((key, task) -> System.out.println("- " + task));
			}

			System.out.println("\nOptions:");
			System.out.println("[add]: Add a new task");
			System.out.println("[delete]: Delete a task");
			System.out.println("[modify]: Modify a task");
			System.out.println("[back]: Go back to project menu");
			System.out.println("[close]: Closes the connection and quits the program");
			System.out.print("> ");

			try {
				String userInput = reader.readLine().trim();

				// Add a task
				if (userInput.equalsIgnoreCase("add")) {
					System.out.print("Enter the task name: ");
					String taskName = reader.readLine().trim();

					// Validate priority
					String priority = null;
					while (true) {
						System.out.print("Enter the priority (low/medium/high) or leave empty: ");
						String priorityInput = reader.readLine().trim().toUpperCase();
						if (priorityInput.isEmpty() || priorityInput.matches("(?i)LOW|MEDIUM|HIGH")) {
							priority = priorityInput.isEmpty() ? null : priorityInput;
							break;
						} else {
							System.out.println("Invalid priority. Please enter 'low', 'medium', or 'high'.");
						}
					}

					// Validate date
					String dueDate = null;
					while (true) {
						System.out.print("Enter the due date (YYYY-MM-DD) or leave empty: ");
						String dueDateInput = reader.readLine().trim();
						if (dueDateInput.isEmpty() || dueDateInput.matches("\\d{4}-\\d{2}-\\d{2}")) {
							dueDate = dueDateInput.isEmpty() ? null : dueDateInput;
							break;
						} else {
							System.out.println("Invalid date format. Please use YYYY-MM-DD.");
						}
					}

					Metadata metadata = new Metadata(priority, dueDate);
					Task newTask = new Task(taskName, metadata);

					taskMap.put(taskName, newTask);
					currentProject.getTasks().add(newTask);
					String payload = gson.toJson(Map.of("name", taskName, "metadata", metadata));
					tempDb.getProject(currentProject.getName()).addTask(newTask);
					return "ADDTS " + payload;
				}

				// Delete a task
				if (userInput.equalsIgnoreCase("delete")) {
					System.out.print("Enter the task name to delete: ");
					String taskName = reader.readLine().trim();
					if (taskMap.containsKey(taskName)) {
						taskMap.remove(taskName);
						currentProject.getTasks().removeIf(task -> task.getName().equals(taskName));
						tempDb.getProject(currentProject.getName()).removeTask(taskName);
						return "DELTS " + taskName;
					} else {
						System.out.println("Task not found.");
					}
					continue;
				}

				// Modify a task
				if (userInput.equalsIgnoreCase("modify")) {
					System.out.print("Enter the task name to modify: ");
					String taskName = reader.readLine().trim();
					String oldName = taskName.substring(0);
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

						// Validate new priority
						while (true) {
							System.out.print("New priority (low/medium/high) or leave empty: ");
							String newPriorityInput = reader.readLine().trim().toUpperCase();
							if (newPriorityInput.isEmpty() || newPriorityInput.matches("LOW|MEDIUM|HIGH")) {
								if (!newPriorityInput.isEmpty()) {
									task.getMetadata().setPriority(newPriorityInput);
								}
								break;
							} else {
								System.out.println("Invalid priority. Please enter 'low', 'medium', or 'high'.");
							}
						}

						// Validate new date
						while (true) {
							System.out.print("New due date (YYYY-MM-DD) or leave empty: ");
							String newDueDateInput = reader.readLine().trim();
							if (newDueDateInput.isEmpty() || newDueDateInput.matches("\\d{4}-\\d{2}-\\d{2}")) {
								if (!newDueDateInput.isEmpty()) {
									task.getMetadata().setDue(newDueDateInput);
								}
								break;
							} else {
								System.out.println("Invalid date format. Please use YYYY-MM-DD.");
							}
						}

						String payload = gson.toJson(Map.of("tasks", task));
						tempDb.getProject(currentProject.getName()).removeTask(oldName);
						tempDb.getProject(currentProject.getName()).addTask(task);
						return "MODTS " + taskName + " " + payload;
					} else {
						System.out.println("Task not found.");
					}
					continue;
				}

				// Go back
				if (userInput.equalsIgnoreCase("back")) {
					currentMenu = Menu.PROJECT;
					return null;
				}

				// Close
				if (userInput.equalsIgnoreCase("close")) {
					System.out.println("Closing the application. Goodbye!");
					return "CLOSE";
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
