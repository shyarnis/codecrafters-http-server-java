import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // initialize client socket.
        Socket clientSocket = null;

        try {
            ServerSocket serverSocket = new ServerSocket(4221);

            // client socket for accepting connection and responding 200 in bytes
            clientSocket = serverSocket.accept();
            clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());

            System.out.println("accepted new connection");

            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
