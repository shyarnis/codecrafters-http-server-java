import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.zip.GZIPOutputStream;


class ClientRunnable implements Runnable {
    private final Socket clientSocket;
    private final String directory;

    public ClientRunnable(Socket socket, String directory) {
        this.clientSocket = socket;
        this.directory = directory;
    }

    // method overridding `run()`
    @Override
    public void run() {
        // initialize input and output stream.
        BufferedReader inputStreamBufferedReader = null;
        PrintWriter textOutputWriter = null;

        // GET request variables.
        String requestLine = null;
        String pathName = null;
        String userAgent = null;
        String headerLine = null;
        String requestMethod = null;
        int contentLength = 0;
        String compressionScheme = "xyz";

        try {
            inputStreamBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            textOutputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            requestLine = inputStreamBufferedReader.readLine();
            pathName = requestLine.split(" ")[1];
            requestMethod = requestLine.split(" ")[0];

            // find the user-agent and content-length header
            while ((headerLine = inputStreamBufferedReader.readLine()) != null && !headerLine.isEmpty()) {
                if (headerLine.startsWith("User-Agent: ")) {
                    userAgent = headerLine.substring("User-Agent: ".length());
                } else if (headerLine.startsWith("Content-Length: ")) {
                    contentLength = Integer.parseInt(headerLine.substring("Content-Length: ".length()));
                } else if (headerLine.startsWith("Accept-Encoding: ")) {
                    if (headerLine.contains("gzip")){
                        compressionScheme = "gzip";
                    }
                }
            }

            // read the body
            StringBuilder bodyBuilder = new StringBuilder();
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                inputStreamBufferedReader.read(bodyChars, 0, contentLength);
                bodyBuilder.append(bodyChars);
            }
            String body = bodyBuilder.toString();
            
            // handle request method
            switch (requestMethod) {
                case "GET" ->
                    handleGetRequest(textOutputWriter, pathName, userAgent, compressionScheme, directory);
                case "POST" -> 
                    handlePostRequest(textOutputWriter, pathName, body, directory);
                default -> 
                    textOutputWriter.println("HTTP/1.1 404 Not Found\r\n\r\n");
            }

            inputStreamBufferedReader.close();
            textOutputWriter.close();

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    // handle get request
    private void handleGetRequest(PrintWriter textOutputWriter, String pathName, String userAgent, String compressionScheme, String directory) {

        if (pathName.equals("/")) {

            textOutputWriter.println("HTTP/1.1 200 OK\r\n\r\n");

        } else if (pathName.startsWith("/echo")) {
            // GET /echo/{substring}
            String subString = pathName.substring(6);
                        
            try {
                byte[] responseContent = subString.getBytes();
                
                if (compressionScheme.equals("gzip")) {
                    responseContent = compressGzip(responseContent);
                    textOutputWriter.print("HTTP/1.1 200 OK\r\n");
                    textOutputWriter.print("Content-Type: text/plain\r\n");
                    textOutputWriter.print("Content-Encoding: gzip\r\n");
                    textOutputWriter.print("Content-Length: " + responseContent.length + "\r\n\r\n");
                } else {
                    textOutputWriter.print("HTTP/1.1 200 OK\r\n");
                    textOutputWriter.print("Content-Type: text/plain\r\n");
                    textOutputWriter.print("Content-Length: " + responseContent.length + "\r\n\r\n");
                }

                textOutputWriter.flush();
                clientSocket.getOutputStream().write(responseContent);
                clientSocket.getOutputStream().flush();
            } catch (IOException e) {
                textOutputWriter.println("HTTP/1.1 500 Internal Server Error\r\n\r\n");
            }

        } else if (pathName.startsWith("/user-agent")) {

            textOutputWriter.println("""
                                     HTTP/1.1 200 OK\r
                                     Content-Type: text/plain\r
                                     Content-Length: """ + userAgent.length() + "\r\n\r\n" + userAgent);

        } else if (pathName.startsWith("/files")) {
            // GET /files/{filename}
            String fileName = pathName.substring(6);
            File file = new File(directory, fileName);

            if (file.exists()) {
                try {

                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    textOutputWriter.println("""
                                             HTTP/1.1 200 OK\r
                                             Content-Type: application/octet-stream\r
                                             Content-Length: """ + fileContent.length + "\r\n\r\n" + new String(fileContent));

                } catch (IOException e) {
                    textOutputWriter.println("HTTP/1.1 500 Internal Server Error\r\n\r\n");
                }

            } else {
                textOutputWriter.println("HTTP/1.1 404 Not Found\r\n\r\n");
            }

        } else {
            textOutputWriter.println("HTTP/1.1 404 Not Found\r\n\r\n");
        }
    }

    // handle POST Request
    private void handlePostRequest(PrintWriter textOutputWriter, String pathName, String body, String directory) {
        // POST /files/{filename}
        String fileName = pathName.substring(7);
        File file = new File(directory, fileName);

        try {
            if (file.createNewFile() || file.exists()) {
                try (FileWriter fileWriter = new FileWriter(file, false)) {
                    fileWriter.write(body);
                }
                textOutputWriter.println("HTTP/1.1 201 Created\r\n\r\n");
            } else {
                textOutputWriter.println("HTTP/1.1 404 Not Found\r\n\r\n");
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private byte[] compressGzip(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        }
        return byteArrayOutputStream.toByteArray();
    }

}

public class Main {
    public static void main(String[] args) {
        // initialize server socket and client socket.
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        // parse command line arguments.
        String directory = "";
        if (args.length == 2 && args[0].equals("--directory")) {
            directory = args[1];
        }

        try {
            serverSocket = new ServerSocket(4221);

            while (true) {
                clientSocket = serverSocket.accept();
                System.out.println("accepted new connection");

                // Handle each connection in a new thread.
                ClientRunnable clientRun = new ClientRunnable(clientSocket, directory);
                new Thread(clientRun).start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}