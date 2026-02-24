import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    public ChatClient() {
    }

    public static void main(String[] var0) {
        try {
            Socket var1 = new Socket("172.20.10.9", 6013);

            BufferedReader var2 =
                    new BufferedReader(new InputStreamReader(var1.getInputStream()));

            PrintWriter var3 =
                    new PrintWriter(var1.getOutputStream(), true);

            BufferedReader var4 =
                    new BufferedReader(new InputStreamReader(System.in));

            // Read first message from server (date or name prompt)
            String var5 = var2.readLine();
            System.out.println(var5);

            // Thread to read all server messages
            Thread var6 = new Thread(() -> {
                while (true) {
                    try {
                        String var7;
                        if ((var7 = var2.readLine()) != null) {
                            System.out.println(var7);
                            continue;
                        }
                    } catch (IOException var8) {
                        System.out.println("Disconnected from server.");
                    }
                    return;
                }
            });
            var6.start();

            // Send user input to server
            String var9;
            while ((var9 = var4.readLine()) != null) {
                var3.println(var9);
            }

            var1.close();

        } catch (IOException var10) {
            System.err.println("Error Connecting to server: " + var10);
        }
    }
}
