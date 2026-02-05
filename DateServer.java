import java.net.*;
import java.io.*;

public class DateServer
{
    public static void main(String[] args)
    {
        try
        {
            ServerSocket sock = new ServerSocket(6013);
            System.out.println("DateServer started");

            while (true)
            {
                Socket client = sock.accept();

                System.out.println("Client connected ");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));

                PrintWriter pout =
                        new PrintWriter(client.getOutputStream(), true);

                pout.println(new java.util.Date().toString());

                String message;
                while ((message = in.readLine()) != null)
                {
                    System.out.println("Client says: " + message);
                }


                client.close();
                System.out.println("Client disconnected");
            }
        }
        catch (IOException ioe)
        {
            System.err.println("Server error: " + ioe.getMessage());
        }
    }
}
