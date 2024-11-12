package ch.heigvd.dai.commands;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "client", description = "Start the client part of the network game.")
public class Client implements Callable<Integer> {

  public enum Message {
    TEST,
    STOP
  }

  // End of line character
  public static String END_OF_LINE = "\n";

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

          String request = null;

          switch (message) {
            case TEST -> {
              request = Message.TEST + END_OF_LINE;
            }
            case STOP -> {
              socket.close();
              continue;
            }
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
        try {
          message = Server.Message.valueOf(serverResponseParts[0]);
        } catch (IllegalArgumentException e) {
          // Do nothing
        }

        switch (message) {
          case HELLO -> System.out.println("Server says HELLO...");
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

  private static void help() {
    System.out.println("Usage:");
    System.out.println("  " + Message.TEST + " this is a test message. No message to display yet !");
    System.out.println("  " + Message.STOP + " this is a stop message.");
  }
}
