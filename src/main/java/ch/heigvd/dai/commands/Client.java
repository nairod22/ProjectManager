package ch.heigvd.dai.commands;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import ch.heigvd.dai.database.Database;
import com.google.gson.Gson;
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
    CLOSE
  }

  // End of line character
  public static String END_OF_LINE = "\n";

  Gson gson = new Gson();

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

      help();

      while (!socket.isClosed()) {
        System.out.println("> ");

        //Ecriture de message à envoyer au server
        Reader inputReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        BufferedReader bir = new BufferedReader(inputReader);
        String userInput = bir.readLine();

        try {
          String[] userInputParts = userInput.split(" ", 2);
          Message message = Message.valueOf(userInputParts[0].toUpperCase());

          //todo : est-ce que c'est ok ?
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
            }

            case ADDPRJ -> {
              request = Message.ADDPRJ + " " + arg + END_OF_LINE;
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
              help();
            }
            //pas de default -> erreur gérée dans le catch !
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

        //todo : a finir d'implémenter une fois les réponses du servers sont implémentée correctement

        switch (message) {
          case PROJL -> {

            //test for json usage
            System.out.println("[test] receive projetcs list with json format.");
            System.out.println("[test] " + arg);
            Database db = gson.fromJson(arg, Database.class);
            System.out.println("[test] " + db.getProject("test1").getName() + db.getProject("test2").getName());
          }
          case ERROR -> {
            System.out.println("[Client] Receive ERROR");
          }

          case OKAYY -> {
            System.out.println("[Client]  Receive OKAYY");
          }

          case PROJD -> {
            System.out.println("[Client] Receive Données du projet : ");
            //System.out.println(serverResponseParts[1]);
          }

          case TASKD -> {
            System.out.println("[Client] ReceiveDonnées de la tâche");
            //System.out.println(serverResponseParts[1]);
          }
          case null, default ->
                  System.out.println("Invalid/unknown command sent by server, ignore.");
        }
      }
      System.out.println("[Client] Closing connection and quitting...");
    } catch (IOException e) {
      System.out.println("[Client] Exception : " + e.getMessage());
      return -1;
    }
    return 0;
  }

  //todo : corriger les arg dans le schéma ?
  private static void help() {
    System.out.println("Usage:");
    System.out.println("  " + Message.HELLO + " - Display the list of projects.");
    System.out.println("  " + Message.PROJS + " <project_name> - Get data linked with selected.");
    System.out.println("  " + Message.ADDPRJ + " <project_info> - Add a new project.");
    System.out.println("  " + Message.DELPR + " <project_name> - Delete a project.");
    System.out.println("  " + Message.ADDTS + " <task_info> - Add a new task.");
    System.out.println("  " + Message.DELTS + " <task_name> - Delete a task.");
    System.out.println("  " + Message.GETTS + " <task_name> - Add a task to the project .");
    System.out.println("  " + Message.MODTS + " <task_name> - Modify a task.");
    System.out.println("  " + Message.HELP + " - Display help.");
  }
}
