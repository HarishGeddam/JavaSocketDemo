import java.io.*;
import java.net.*;

public class DateClient {
    public static void main(String[] args) {
        try {
            // Replace with SERVER laptop IP
            Socket sock = new Socket("172.20.10.9", 6013);

            BufferedReader in = new BufferedReader(
                new InputStreamReader(sock.getInputStream())
            );
            String serverDate = in.readLine();
            System.out.println("Server date: " + serverDate);

            sock.close();
        } catch (IOException ioe) {
            System.err.println("Error Connecting to server: " + ioe);
        }
    }
}
