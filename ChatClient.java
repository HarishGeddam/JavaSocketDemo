import java.io.*;
import java.net.*;

public class ChatClient {

    private static final String SERVER_IP = "172.20.10.9";
    private static final int PORT = 6013;

    public static void main(String[] args) {

        try {

            Socket socket = new Socket(SERVER_IP, PORT);

            System.out.println("Connected to server.");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in));

            // Thread to receive messages from server
            Thread receiveThread = new Thread(() -> {

                String serverMsg;

                try {

                    while ((serverMsg = in.readLine()) != null) {

                        System.out.println(serverMsg);
                    }

                } catch (IOException e) {

                    System.out.println("Disconnected from server.");
                }
            });

            receiveThread.start();

            // Send messages to server
            String userInput;

            while ((userInput = console.readLine()) != null) {

                out.println(userInput);

                if (userInput.equalsIgnoreCase("/exit")) {
                    break;
                }
            }

            socket.close();

        } catch (Exception e) {

            System.out.println("Unable to connect to server.");
        }
    }
}