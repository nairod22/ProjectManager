package ch.heigvd.dai.commands;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
  // End of line character
  public static String END_OF_LINE = "\n";

  @CommandLine.Option(
          names = {"-p", "--port"},
          description = "Port to use (default: ${DEFAULT-VALUE}).",
          defaultValue = "6433")
  protected int port;

  //Database
  private Database database;

  @Override
  public Integer call() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("[SERVER] Listening on port " + port);

      while (!serverSocket.isClosed()) {
        try (Socket socket = serverSocket.accept();
             Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(reader);
             Writer writer =
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(writer)) {

          System.out.println(
                  "[Server] New client connected from "
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

            String response;
            Gson gson = new Gson();
            String projectSelected = "";

            switch (message) {
              case HELLO -> {
                String projectList = gson.toJson(database.getProjects());
                if (projectList != null)
                  response = Message.PROJL + " " + projectList + END_OF_LINE;
                else
                  response = Message.ERROR + END_OF_LINE; //+ numéro d'erreur !
              }

              case PROJS -> {
                String projectData = gson.toJson(database.getProject(arg));
                if (projectData != null){
                  response = Message.PROJD + " " + projectData + END_OF_LINE;
                  projectSelected = arg;
                }

                else
                  response = Message.ERROR + END_OF_LINE; //+ numéro d'erreur !
              }

              case ADDPRJ -> {
                //faire gestion d'erreur !
                Project project = gson.fromJson(arg, Project.class);
                this.database.addProject(project);
                response = Message.OKAYY + END_OF_LINE;
              }

              case DELPR -> {
                //faire gestion d'erreur !
                this.database.deleteProject(arg);
                response = Message.OKAYY + END_OF_LINE;
              }

              case ADDTS -> {
                Task task = gson.fromJson(arg, Task.class);
                database.getProject(projectSelected).addTask(task); //faire à partir du constructeur ?
                response = Message.OKAYY + END_OF_LINE; //+ numéro d'erreur !
              }

              case DELTS -> {
                database.getProject(projectSelected).removeTask(arg); //faire méthode pour selectionné le bon projet et la bonne tâche !
                response = Message.OKAYY + END_OF_LINE;
              }

              case GETTS -> {
                String taskData = gson.toJson(database.getProject(projectSelected).getTask(arg));
                if (taskData != null)
                  response = Message.TASKD + " " + taskData + END_OF_LINE;
                else
                  response = Message.ERROR + END_OF_LINE; //+ numéro d'erreur !
              }

              case MODTS -> {
                //todo : dépends de comment sera implémenté la modification
                Task task_to_modify = gson.fromJson(arg, Task.class);
                String name = task_to_modify.getName();
                database.getProject(projectSelected).removeTask(name);
                database.getProject(projectSelected).addTask(task_to_modify);
                response = Message.OKAYY + END_OF_LINE; //gestion erreur !
              }

              case null, default -> {
                System.out.println("[Server] Testing default message");
                response = "default";
              }
            }

            out.write(response);
            out.flush();
          }

          System.out.println("[Server] Closing connection");
        } catch (IOException e) {
          System.out.println("[Server] IO exception: " + e);
          return 1;
        }
      }
    } catch (IOException e) {
      System.out.println("[Server] IO exception: " + e);
      return 1;
    }

    return 0;
  }

}

