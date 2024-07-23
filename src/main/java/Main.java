import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;

/**
 * ClientHandler
 */

class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    // method overridding `run()`
    public void run() {
        // initialize input and output stream.
        BufferedReader inputStreamBufferedReader = null;
        PrintWriter textOutputWriter = null;

        // GET request variables.
        String requestLine = null;
        String pathName = null;
        String subString = null;
        String userAgent = null;
        String headerLine = null;

        try {
            inputStreamBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            textOutputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            requestLine = inputStreamBufferedReader.readLine();
            pathName = requestLine.split(" ")[1];

            // find the user-agent
            while ((headerLine = inputStreamBufferedReader.readLine()) != null && !headerLine.isEmpty()) {
                if (headerLine.startsWith("User-Agent: ")) {
                    userAgent = headerLine.substring("User-Agent: ".length());
                }
            }

            if (pathName.equals("/")) {

                textOutputWriter.println("HTTP/1.1 200 OK\r\n\r\n");

            } else if (pathName.startsWith("/echo")) {

                subString = pathName.substring(6);
                textOutputWriter.println("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + subString.length() + "\r\n\r\n" + subString);

            } else if (pathName.startsWith("/user-agent")) {

                textOutputWriter.println("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + userAgent.length() + "\r\n\r\n" + userAgent);

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

public class Main {
    public static void main(String[] args) {
        // initialize server socket and client socket.
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(4221);

            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("accepted new connection");

                // Handle each connection in a new thread.
                ClientHandler clientThread = new ClientHandler(clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}