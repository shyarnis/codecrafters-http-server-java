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

        // GET request variables.
        String requestLine = null;
        String pathName = null;
        String subString = null;

        try {
            inputStreamBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            textOutputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            requestLine = inputStreamBufferedReader.readLine();
            pathName = requestLine.split(" ")[1];

            if (pathName.equals("/")) {

                textOutputWriter.println("HTTP/1.1 200 OK\r\n\r\n");

            } else if (pathName.startsWith("/echo/")) {

                subString = pathName.substring(6);
                textOutputWriter.println("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + subString.length() + "\r\n\r\n" + subString);

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
