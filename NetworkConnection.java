import javax.swing.*;
import java.io.*;
import java.net.*;

public class NetworkConnection implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private GameWindow gameWindow; 
    private boolean isRunning = true;

    public NetworkConnection(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
    }

    public void startServer(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        this.socket = serverSocket.accept(); 
        setupStreams();
    }

    public void connect(String ip, int port) throws IOException {
        this.socket = new Socket(ip, port);
        setupStreams();
    }

    private void setupStreams() throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void sendMove(int index) {
        try {
            out.writeInt(index);
            out.flush(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                int index = in.readInt();
                SwingUtilities.invokeLater(() -> gameWindow.receiveOpponentMove(index));
            } catch (IOException e) {
                System.out.println("З'єднання розірвано");
                isRunning = false;
                break;
            }
        }
    }
}
