package ch.heigvd.dai.commands;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "server", description = "Start the server part of the network game.")
public class Server implements Callable<Integer> {
  public enum Message {
    HELLO,
    TESTB
  }

  // End of line character
  //todo : make something for sharing the end of line char ?
  public static String END_OF_LINE = "\n";

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "6433")
  protected int port;

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
            try {
              message = Client.Message.valueOf(clientRequestParts[0]);
            } catch (Exception e) {
              // Do nothing
            }

            String response;

            switch (message) {
              //todo : comment passer de MESSAGE à une string ?
              case TEST -> {
                System.out.println("[Server] Testing test");
                response = "test"; //TESTBACK !
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

}

