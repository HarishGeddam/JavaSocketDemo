import java.io.*;
import java.net.*;

public class DateClient {

    public static void main(String[] args) {
        try {
            Socket sock = new Socket("172.20.10.9", 6013);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(sock.getInputStream())
            );

    
            PrintWriter out = new PrintWriter(
                    sock.getOutputStream(), true
            );

    
            BufferedReader keyboard = new BufferedReader(
                    new InputStreamReader(System.in)
            );

        
            String serverDate = in.readLine();
            System.out.println("Server date: " + serverDate);


            Thread receiveThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println("Server: " + msg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });

            receiveThread.start();

        
            String userInput;
            while ((userInput = keyboard.readLine()) != null) {
                out.println(userInput);
            }

            sock.close();

        } catch (IOException ioe) {
            System.err.println("Error Connecting to server: " + ioe);
        }
    }
}
