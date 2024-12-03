package ch.heigvd.dai.commands;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

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

  @Override
  public Integer call() {
    try (ServerSocket serverSocket = new ServerSocket(port);
         ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      System.out.println("[SERVER] Listening on port " + port);

      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        executor.submit(new ClientHandler(clientSocket));
      }

      return 0;

    } catch (IOException e) {
      System.out.println("[Server] IO exception: " + e);
      return 1;
    } finally {
      System.out.println("[Server] Closing server");
    }
  }

  static class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try (socket;
           Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
           BufferedReader in = new BufferedReader(reader);
           Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
           BufferedWriter out = new BufferedWriter(writer)) {

        System.out.println("[Server] New client connected from "
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

          switch (message) {
            case HELLO -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case PROJL -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case PROJS -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case ADDPRJ -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case DELPR -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case ADDTS -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case DELTS -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case GETTS -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
            }

            case MODTS -> {
              System.out.println("[SERVER] receive " + message);
              response = Server.Message.OKAYY + END_OF_LINE;
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
      }
    }
  }
}



