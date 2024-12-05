package ch.heigvd.dai.commands;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

import java.util.concurrent.Callable;

import ch.heigvd.dai.database.Database;
import ch.heigvd.dai.database.Project;
import ch.heigvd.dai.database.Task;
import com.google.gson.Gson;
import picocli.CommandLine;

@CommandLine.Command(name = "server", description = "Start the server part of the network game.")
public class Server implements Callable<Integer> {

  public enum Message {
    PROJL,
    PROJD,
    OKAYY,
    ERROR,
    TASKD
  }

  public enum Error {
    LOST_CONNECTION,
    INVALID_COMMAND,
    INVALID_PROJECT,
    INVALID_TASK,
    DATABASE_ERROR,
    }

  // End of line character
  public static String END_OF_LINE = "\n";

  @CommandLine.Option(
          names = {"-p", "--port"},
          description = "Port to use (default: ${DEFAULT-VALUE}).",
          defaultValue = "6433")
  protected int port;

  Gson gson = new Gson();
  //Database
  Database database;

  private void loadDatabase() {
    try (FileReader file = new FileReader("database.json")) {
      this.database = gson.fromJson(file, Database.class);
    } catch (IOException e) {
      System.out.println("[SERVER e] failed to load the database");
    }
  }

  private void saveDatabase(){
    String database = gson.toJson(this.database);
    try (FileWriter file = new FileWriter("database.json")) {
      file.write(database);
      file.flush();
    } catch (IOException e) {
      System.out.println("[SERVER e] failed to save the database");
    }
  }

  @Override
  public Integer call() {
    try (ServerSocket serverSocket = new ServerSocket(port);
         ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      System.out.println("[SERVER] Listening on port " + port);

      loadDatabase();

      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        executor.submit(new ClientHandler(clientSocket, database));
      }

      saveDatabase();
      return 0;

    } catch (IOException e) {
      System.out.println("[SERVER e] IO exception: " + e);
      return 1;
    } finally {
      System.out.println("[SERVER] Closing server");
    }
  }

  static class ClientHandler implements Runnable {
    final private Socket socket;
    final private Database database;

    Gson gson = new Gson();

    public ClientHandler(Socket socket, Database database) {
      this.socket = socket;
      this.database = database;
    }

    @Override
    public void run() {
      try (socket;
           Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
           BufferedReader in = new BufferedReader(reader);
           Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
           BufferedWriter out = new BufferedWriter(writer)) {

        System.out.println("[SERVER] New client connected from "
                + socket.getInetAddress().getHostAddress()
                + ":"
                + socket.getPort());

        while (!socket.isClosed()) {
          String clientRequest = in.readLine();

          if (clientRequest == null) {
            socket.close();
            continue;
          }

          String[] clientRequestParts = clientRequest.split(" ", 2);

          Client.Message message = null;
          String arg = "";
          try {
            message = Client.Message.valueOf(clientRequestParts[0]);

            if (clientRequestParts.length > 1) {
              arg = clientRequestParts[1];
            }
          } catch (Exception e) {
            // Do nothing
          }

          String response = "";
          String projectSelected = "";

          switch (message) {
            case HELLO -> {
              String projectList = gson.toJson(database.getProjectsList());

              if (projectList != null) //project list could be empty and still not being an error !
                response = Message.PROJL + " " + projectList + END_OF_LINE;
              else
                response = Message.ERROR + " " + Error.DATABASE_ERROR + END_OF_LINE;
            }

            case PROJS -> {
              //on essaie de récupérer les data du projet demandé
              String projectData = gson.toJson(database.getProject(arg));
              if (projectData != null) { // c'est à dire que database a retourné le projet demandé
                response = Message.PROJD + " " + projectData + END_OF_LINE;
              } else // c'est à dire que database n'a pas trouvé le projet demandé
                response = Message.ERROR + " " + Error.INVALID_PROJECT + END_OF_LINE;
            }

            case ADDPRJ -> {
              //todo : does ADDPRJ just add an empty project ?

              Project project;
              try {
                project = gson.fromJson(arg, Project.class);
                this.database.addProject(project);
                response = Message.OKAYY + END_OF_LINE;
                // valider que certains champs comme name ou id ne soient pas nuls après désérialisation ?
                // c'est implicitement fait au travers de addProject() ?
              } catch (Exception e) {
               response = Message.ERROR + " " + Error.DATABASE_ERROR + END_OF_LINE;
              }
            }

            case DELPR -> {
              boolean deleted = this.database.deleteProject(arg);
              if (deleted){
                response = Message.OKAYY + END_OF_LINE;
              } else {
                response = Message.ERROR + " " + Error.INVALID_PROJECT + END_OF_LINE;
              }
            }

            case ADDTS -> {
              Task task;
              try {
                task = gson.fromJson(arg, Task.class);
                database.getProject(projectSelected).addTask(task);
                response = Message.OKAYY + END_OF_LINE;
              } catch (Exception e) {
                response = Message.ERROR + " " + Error.INVALID_TASK + END_OF_LINE;
              }
            }

            case DELTS -> {
              //une autre façon simple de faire... ?
              if (database.getProject(projectSelected).getTask(arg) == null) {
                response = Message.ERROR + " " + Error.INVALID_TASK + END_OF_LINE;
              } else
              database.getProject(projectSelected).removeTask(arg);
              response = Message.OKAYY + END_OF_LINE;
            }

            case GETTS -> {
              String taskData;
              try {
                taskData = gson.toJson(database.getProject(projectSelected).getTask(arg));
                response = Message.TASKD + " " + taskData + END_OF_LINE;
              } catch (Exception e) {
                response = Message.ERROR + END_OF_LINE; //+ numéro d'erreur !
              }
            }

            case MODTS -> {
              //todo : dépends de comment sera implémenté la modification
              try {
                Task task_to_modify = gson.fromJson(arg, Task.class);
                String name = task_to_modify.getName();
                database.getProject(projectSelected).removeTask(name);
                database.getProject(projectSelected).addTask(task_to_modify);
                response = Message.OKAYY + END_OF_LINE; //gestion erreur !
              } catch (Exception e) {
                response = Message.ERROR + " " + Error.INVALID_TASK + END_OF_LINE;
              }
            }

            case CLOSE -> {
              //todo : message d'erreur en plus ?
              socket.close();
            }

            case null, default -> {
              System.out.println("[SERVER e] Invalid message received: " + clientRequest);
              response = Message.ERROR + " " + Error.INVALID_COMMAND + END_OF_LINE;
            }
          }

          out.write(response);
          out.flush();
        }

        System.out.println("[SERVER] Closing connection");
      } catch (IOException e) {
        System.out.println("[SERVER e] IO exception: " + e);
      }
    }
  }
}






