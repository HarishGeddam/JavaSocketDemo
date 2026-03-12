import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int PORT = 6013;

    // Directory: client name -> client handler
    private static ConcurrentHashMap<String, ClientHandler> clients =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server started...");
            System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("PORT: " + PORT);

            // Thread to handle server console commands
            new Thread(ChatServer::handleServerCommands).start();

            while (true) {

                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());

                ClientHandler client = new ClientHandler(socket);
                client.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================
    // SERVER CONSOLE COMMANDS
    // ===========================

    private static void handleServerCommands() {

        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String command;

        try {

            while ((command = console.readLine()) != null) {

                // Show client list
                if (command.equalsIgnoreCase("/list")) {

                    System.out.println("\nConnected Clients:");
                    for (ClientHandler c : clients.values()) {
                        System.out.println("Name: " + c.name + " | IP: " + c.ip + " | Thread: " + c.getId());
                    }
                }

                // Force disconnect client
                else if (command.startsWith("/kick")) {

                    String[] parts = command.split(" ");
                    if (parts.length == 2) {
                        ClientHandler client = clients.get(parts[1]);
                        if (client != null) {
                            client.disconnect("Kicked by server");
                        } else {
                            System.out.println("Client not found.");
                        }
                    }
                }

                // Server private message to a client
                else if (command.startsWith("/msgclient")) {

                    String[] parts = command.split(" ", 3);
                    if (parts.length >= 3) {

                        String target = parts[1];
                        String message = parts[2];

                        ClientHandler client = clients.get(target);

                        if (client != null) {
                            client.send("(Server Private) " + message);
                            System.out.println("Message sent to " + target);
                        } else {
                            System.out.println("Client not found.");
                        }
                    }
                }

                // Server broadcast message
                else {
                    broadcast("SERVER: " + command);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===========================
    // BROADCAST MESSAGE
    // ===========================

    private static void broadcast(String msg) {
        for (ClientHandler client : clients.values()) {
            client.send(msg);
        }
    }

    // ===========================
    // SEND UPDATED CLIENT LIST
    // ===========================

    private static void updateClientList() {
        StringBuilder list = new StringBuilder("CLIENT_LIST ");
        for (String name : clients.keySet()) {
            list.append(name).append(",");
        }
        broadcast(list.toString());
    }

    // ===========================
    // CLIENT HANDLER THREAD
    // ===========================

    static class ClientHandler extends Thread {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        String name;
        String ip;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {
                ip = socket.getInetAddress().getHostAddress();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for client name
                out.println("ENTER_NAME");
                while (true) {
                    name = in.readLine();
                    if (name == null) return;

                    if (!clients.containsKey(name)) {
                        clients.put(name, this);
                        break;
                    } else {
                        out.println("NAME_TAKEN");
                    }
                }

                System.out.println(name + " connected | IP: " + ip);
                send("WELCOME " + name);
                broadcast(name + " joined the chat.");
                updateClientList();

                String msg;
                while ((msg = in.readLine()) != null) {

                    // Log all messages on server
                    System.out.println(name + ": " + msg);

                    // Request client list
                    if (msg.equalsIgnoreCase("/list")) {
                        System.out.println(name + " requested the client list.");
                        updateClientList();
                    }

                    // Exit
                    else if (msg.equalsIgnoreCase("/exit")) {
                        disconnect("Client exited");
                        break;
                    }

                    // Private message
                    else if (msg.startsWith("/msg ")) {
                        String[] parts = msg.split(" ", 3);
                        if (parts.length >= 3) {
                            String target = parts[1];
                            String message = parts[2];
                            privateMessage(target, message);
                        }
                    }

                    // Broadcast request
                    else if (msg.startsWith("/broadcast ")) {
                        String message = msg.substring(11);
                        broadcast(name + ": " + message);
                    }

                    // Any other message will also be broadcasted
                    else {
                        broadcast(name + ": " + msg);
                    }
                }

            } catch (Exception e) {
                System.out.println("Connection lost: " + name);
            } finally {
                disconnect("Disconnected");
            }
        }

        // ===========================
        // SEND MESSAGE
        // ===========================

        void send(String msg) {
            out.println(msg);
        }

        // ===========================
        // PRIVATE MESSAGE
        // ===========================

        void privateMessage(String target, String message) {
            ClientHandler client = clients.get(target);
            if (client != null) {
                client.send("(Private) " + name + ": " + message);
                send("(Private to " + target + "): " + message);
            } else {
                send("User not found.");
            }
        }

        // ===========================
        // DISCONNECT CLIENT
        // ===========================

        void disconnect(String reason) {
            try {
                if (name != null && clients.containsKey(name)) {
                    clients.remove(name);
                    System.out.println(name + " disconnected.");
                    broadcast(name + " left the chat.");
                    updateClientList();
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}