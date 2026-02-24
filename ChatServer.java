import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    // Store client information
    private static ConcurrentHashMap<String, ClientHandler> clients =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(6013)) {

            System.out.println("Server started... Waiting for clients.");

            // Start a thread to read messages from server console
            new Thread(() -> {
                BufferedReader consoleReader = new BufferedReader(
                        new InputStreamReader(System.in));
                String serverMessage;
                try {
                    while ((serverMessage = consoleReader.readLine()) != null) {
                        broadcast("Server: " + serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Accept clients continuously
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                handler.start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Broadcast message to all clients
    private static void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.out.println(message);
        }
    }

    static class ClientHandler extends Thread {

        private Socket socket;
        private String clientName;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))
            ) {

                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for username
                out.println("Enter your name:");

                while (true) {
                    clientName = in.readLine();

                    if (clientName == null) return;

                    if (clients.putIfAbsent(clientName, this) != null) {
                        out.println("Name already taken. Try another:");
                    } else {
                        break;
                    }
                }

                System.out.println(clientName + " joined the chat.");
                System.out.println("Total connected clients: " + clients.size());

                out.println("Welcome " + clientName + "!");
                broadcast(clientName + " has joined the chat.");

                String message;

                // Handle client messages
                while ((message = in.readLine()) != null) {

                    String fullMessage = clientName + ": " + message;

                    // Display on server console
                    System.out.println(fullMessage);

                    // Broadcast to all clients
                    broadcast(fullMessage);
                }

            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
            } finally {

                if (clientName != null) {
                    clients.remove(clientName);
                    System.out.println(clientName + " left the chat.");
                    System.out.println("Total connected clients: " + clients.size());

                    broadcast(clientName + " has left the chat.");
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}