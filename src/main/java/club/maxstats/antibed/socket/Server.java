package club.maxstats.antibed.socket;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start() {
        try {
            this.serverSocket = new ServerSocket(8080);
            this.clientSocket = this.serverSocket.accept();

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Accepted connection with Bot"));

            this.out = new PrintWriter(this.clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            String text;

            while ((text = this.in.readLine()) != null) {
                if (text.contains("Lobby")) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(text.replace("Lobby ", "")));
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/lobby");
                }
            }

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Socket Closed"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String sendMessage(String message) {
        try {
            this.out.println(message);
            return in.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return "";
    }

    public void teardown() throws IOException {
        this.in.close();
        this.out.close();
        this.clientSocket.close();
        this.serverSocket.close();
    }

}
