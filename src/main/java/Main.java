import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // initialize server socket and client socket.
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);
            clientSocket = serverSocket.accept();
            System.out.println("accepted new connection");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }

        // initialize input and output stream.
        BufferedReader inputStreamBufferedReader = null;
        PrintWriter textOutputWriter = null;
        String requestLine = null;
        String pathName = null;

        try {
            inputStreamBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            textOutputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            requestLine = inputStreamBufferedReader.readLine();
            // GET /hello HTTP/1.1
            pathName = requestLine.split(" ")[1];

            if (pathName.equals("/")) {
                // clientSocket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                textOutputWriter.println("HTTP/1.1 200 OK\r\n\r\n");
            } else {
                textOutputWriter.println("HTTP/1.1 404 Not Found\r\n\r\n");
            }
            inputStreamBufferedReader.close();
            textOutputWriter.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
