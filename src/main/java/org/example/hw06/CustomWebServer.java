package org.example.hw06;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CustomWebServer {
    private final int port;
    private final CustomExecutorService executor;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private final AtomicLong requestCount = new AtomicLong(0);
    private long startTime;

    private static final int KEEP_ALIVE_TIMEOUT = 30000;
    private static final int MAX_REQUESTS_PER_CONNECTION = 100;
    private static final String UPLOAD_DIR = "./uploads/";

    private final Map<String, WebSocketConnection> webSocketConnections = new ConcurrentHashMap<>();

    public CustomWebServer(int port, int threadPoolSize, boolean useVirtualThreads) {
        this.port = port;
        this.executor = new CustomExecutorService(threadPoolSize, useVirtualThreads);
    }

    public void start() throws IOException {
        running = true;
        startTime = System.currentTimeMillis();
        serverSocket = new ServerSocket(port);
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(KEEP_ALIVE_TIMEOUT);
                executor.submit(() -> handleClientWithKeepAlive(socket));
            } catch (SocketException e) {
                if (!running) break;
            }
        }
    }

    public void stop() {
        running = false;

        webSocketConnections.values().forEach(WebSocketConnection::close);
        webSocketConnections.clear();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private void handleClientWithKeepAlive(Socket clientSocket) {
        int requestsHandled = 0;
        boolean upgradedToWebSocket = false;
        boolean keepAlive = true;

        try {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            while (running && requestsHandled < MAX_REQUESTS_PER_CONNECTION) {
                if (keepAlive) {
                    clientSocket.setSoTimeout(KEEP_ALIVE_TIMEOUT);
                }
                String request = readLine(input);
                if (request == null || request.isEmpty()) break;

                requestCount.incrementAndGet();

                String[] tokens = request.split(" ");
                if (tokens.length < 3) break;
                String method = tokens[0];
                String path = tokens[1];

                Map<String, String> headers = new HashMap<>();
                String line;
                while ((line = readLine(input)) != null && !line.isEmpty()) {
                    int index = line.indexOf(':');
                    if(index > 0) {
                        String key = line.substring(0, index).trim();
                        String value = line.substring(index + 1).trim();
                        headers.put(key, value);
                    }
                }

                if (isWebSocketUpgrade(headers)) {
                    handleWebSocketUpgrade(clientSocket, output, headers, path);
                    upgradedToWebSocket = true;
                    return;
                }

                byte[] response = handleRequest(method, path, headers, input, keepAlive);
                output.write(response);
                output.flush();

                requestsHandled++;

                String connection = headers.getOrDefault("Connection", "").toLowerCase();
                if(connection.contains("close") || !connection.contains("keep-alive")) {
                    keepAlive = false;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(!upgradedToWebSocket) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] handleRequest(String method, String path, Map<String, String> headers, InputStream input, boolean keepAlive) throws IOException {
        if (method.equals("GET")) {
            if (path.equals("/"))
                return getFile("index.html", keepAlive);
            if (path.startsWith("/static/"))
                return getFile(path.substring(8), keepAlive);
            if(path.equals("/api/time"))
                return getApiTime(keepAlive);
            if(path.equals("/api/stats"))
                return getApiStats(keepAlive);
            if (path.equals("/upload"))
                return getUploadPage(keepAlive);
            if (path.equals("/websocket-demo"))
                return getWebSocketDemo(keepAlive);
        } else if(method.equals("POST")) {
            if(path.equals("/api/echo"))
                return getApiEcho(headers, input, keepAlive);
            if (path.equals("/api/upload"))
                return handleFileUpload(headers, input, keepAlive);
        }

        return getNotFoundPage(keepAlive);
    }

    private byte[] getFile(String path, boolean keepAlive) throws IOException {
        if (path.contains("..")) return getForbiddenPage(keepAlive);
        File file = new File("./static/" + path);

        if (!file.exists() || file.isDirectory()) return getNotFoundPage(keepAlive);

        byte[] fileContent = Files.readAllBytes(file.toPath());
        String mimeType = getMimeType(path);

        return buildHttpResponse(200, "OK", mimeType, fileContent, keepAlive);
    }

    private byte[] handleFileUpload(Map<String, String> headers, InputStream input, boolean keepAlive) throws IOException {
        String contentType = headers.get("Content-Type");

        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return buildHttpResponse(400, "Bad Request", "application/json",
                    "{\"error\":\"Content-Type must be multipart/form-data\"}"
                            .getBytes(StandardCharsets.UTF_8), keepAlive);
        }

        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            return buildHttpResponse(400, "Bad Request", "application/json",
                    "{\"error\":\"No boundary found\"}".getBytes(StandardCharsets.UTF_8), keepAlive);
        }
        String contentLengthStr = headers.get("Content-Length");
        if (contentLengthStr == null) {
            return buildHttpResponse(400, "Bad Request", "application/json",
                    "{\"error\":\"Content-Length required\"}".getBytes(StandardCharsets.UTF_8), keepAlive);
        }
        int contentLength = Integer.parseInt(contentLengthStr);

        List<UploadedFile> uploadedFiles = parseMultipartData(input, boundary, contentLength);
        StringBuilder json = new StringBuilder("{\"uploaded\":[");
        for (int i = 0; i < uploadedFiles.size(); i++) {
            UploadedFile file = uploadedFiles.get(i);
            if (i > 0) json.append(",");
            json.append(String.format(
                    "{\"filename\":\"%s\",\"size\":%d,\"path\":\"%s\"}",
                    escapeJson(file.filename), file.size, escapeJson(file.savedPath)
            ));
        }
        json.append("]}");

        return buildHttpResponse(200, "OK", "application/json",
                json.toString().getBytes(StandardCharsets.UTF_8), keepAlive);
    }

    private String extractBoundary(String boundary) {
        String[] parts = boundary.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                return "--" + part.substring(9);
            }
        }
        return null;
    }

    private List<UploadedFile> parseMultipartData(InputStream input, String boundary, int contentLength)
            throws IOException {
        List<UploadedFile> files = new ArrayList<>();

        byte[] data = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = input.read(data, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
        }

        byte[] boundaryBytes = boundary.getBytes(StandardCharsets.UTF_8);
        List<Integer> boundaryPositions = findBoundaryPositions(data, boundaryBytes);

        for (int i = 0; i < boundaryPositions.size() - 1; i++) {
            int start = boundaryPositions.get(i) + boundaryBytes.length;
            int end = boundaryPositions.get(i + 1);

            if (end - start < 10) continue;

            byte[] partData = Arrays.copyOfRange(data, start, end);

            int headerEnd = findHeaderEnd(partData);
            if (headerEnd == -1) continue;

            String headers = new String(partData, 0, headerEnd, StandardCharsets.UTF_8);
            String filename = extractFilename(headers);

            if (filename != null) {
                int contentStart = headerEnd + 4;
                int contentEnd = partData.length;

                if (contentEnd >= 2 && partData[contentEnd-2] == '\r' && partData[contentEnd-1] == '\n') {
                    contentEnd -= 2;
                }

                byte[] fileContent = Arrays.copyOfRange(partData, contentStart, contentEnd);

                String savedPath = saveUploadedFile(filename, fileContent);
                files.add(new UploadedFile(filename, fileContent.length, savedPath));
            }
        }

        return files;
    }

    private List<Integer> findBoundaryPositions(byte[] data, byte[] boundary) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i <= data.length - boundary.length; i++) {
            boolean match = true;
            for (int j = 0; j < boundary.length; j++) {
                if (data[i + j] != boundary[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                positions.add(i);
            }
        }
        return positions;
    }

    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == '\r' && data[i+1] == '\n' &&
                    data[i+2] == '\r' && data[i+3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private String saveUploadedFile(String filename, byte[] content) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String timestamp = String.valueOf(System.currentTimeMillis());
        String safeName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String savedFilename = timestamp + "_" + safeName;

        Path filePath = Paths.get(UPLOAD_DIR, savedFilename);
        Files.write(filePath, content);

        return filePath.toString();
    }

    private String extractFilename(String headers) {
        for (String line : headers.split("\r\n")) {
            if (line.startsWith("Content-Disposition:")) {
                String[] parts = line.split(";");
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("filename=")) {
                        return part.substring(10, part.length() - 1);
                    }
                }
            }
        }
        return null;
    }

    private void handleWebSocketUpgrade(Socket socket, OutputStream output,
                                        Map<String, String> headers, String path) throws IOException {
        String key = headers.get("Sec-WebSocket-Key");
        if (key == null) {
            socket.close();
            return;
        }

        String acceptKey = generateWebSocketAcceptKey(key);

        String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + acceptKey + "\r\n" +
                "\r\n";

        output.write(response.getBytes(StandardCharsets.UTF_8));
        output.flush();

        socket.setSoTimeout(0);

        String connectionId = UUID.randomUUID().toString();
        WebSocketConnection wsConnection = new WebSocketConnection(socket, connectionId, this);
        webSocketConnections.put(connectionId, wsConnection);

        executor.submit(() -> wsConnection.handleMessages());
    }

    private boolean isWebSocketUpgrade(Map<String, String> headers) {
        String upgrade = headers.get("Upgrade");
        String connection = headers.get("Connection");
        return upgrade != null && upgrade.equalsIgnoreCase("websocket") &&
                connection != null && connection.toLowerCase().contains("upgrade");
    }

    private String generateWebSocketAcceptKey(String key) {
        try {
            String magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest((key + magic).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getApiTime(boolean keepAlive) {
        LocalDateTime now = LocalDateTime.now();
        long timestamp = System.currentTimeMillis();

        String json = String.format(
                "{\"currentTime\":\"%s\",\"timestamp\":%d}",
                escapeJson(now.toString()),
                timestamp
        );
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        return buildHttpResponse(200, "OK", "application/json", jsonBytes, keepAlive);
    }

    private byte[] getWebSocketDemo(boolean keepAlive) throws IOException {
        return getFile("websocket-demo.html", keepAlive);
    }

    private byte[] getApiStats(boolean keepAlive) {
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        String json = String.format(
                "{\"requestsServed\":%d,\"uptimeSeconds\":%d,\"port\":%d,\"webSocketConnections\":%d}",
                requestCount.get(), uptimeSeconds, port, webSocketConnections.size());
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);

        return buildHttpResponse(200, "OK", "application/json", jsonBytes, keepAlive);
    }

    private byte[] getNotFoundPage(boolean keepAlive) {
        String html = "<html><body><h1>404 Not Found</h1></body></html>";
        return buildHttpResponse(404, "Not Found", "text/html", html.getBytes(StandardCharsets.UTF_8), keepAlive);
    }

    private byte[] getForbiddenPage(boolean keepAlive) {
        String html = "<html><body><h1>403 Forbidden</h1></body></html>";
        return buildHttpResponse(403, "Forbidden", "text/html", html.getBytes(StandardCharsets.UTF_8), keepAlive);
    }

    private byte[] getApiEcho(Map<String, String> headers, InputStream reader, boolean keepAlive) throws IOException {
        String contentLengthStr = headers.get("Content-Length");

        if (contentLengthStr == null) {
            return buildHttpResponse(400, "Bad Request", "text/plain",
                    "Content-Length required".getBytes(StandardCharsets.UTF_8), keepAlive);
        }

        int contentLength = Integer.parseInt(contentLengthStr);
        byte[] bodyChars = new byte[contentLength];

        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
        }

        return buildHttpResponse(200, "OK", "text/plain", bodyChars, keepAlive);
    }

    private byte[] getUploadPage(boolean keepAlive) throws IOException {
        return getFile("upload.html", keepAlive);
    }

    private String readLine(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        int prev = -1;
        int ch;

        while ((ch = input.read()) != -1) {
            if (ch == '\n' && prev == '\r') {
                sb.setLength(sb.length() - 1);
                return sb.toString();
            }
            sb.append((char) ch);
            prev = ch;
        }

        return sb.toString();
    }

    private byte[] buildHttpResponse(int statusCode, String statusMessage, String mimeType, byte[] content, boolean keepAlive) {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        response.append("Content-Type: ").append(mimeType).append("\r\n");
        response.append("Content-Length: ").append(content.length).append("\r\n");
        if (keepAlive) {
            response.append("Connection: keep-alive\r\n");
            response.append("Keep-Alive: timeout=30, max=100\r\n");
        } else {
            response.append("Connection: close\r\n");
        }
        response.append("\r\n");

        byte[] headers = response.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fullResponse = new byte[headers.length + content.length];
        System.arraycopy(headers, 0, fullResponse, 0, headers.length);
        System.arraycopy(content, 0, fullResponse, headers.length, content.length);

        return fullResponse;
    }

    private String getMimeType(String path) {
        if(path.endsWith(".html")) return  "text/html";
        else if(path.endsWith(".css")) return  "text/css";
        else if(path.endsWith(".js")) return  "application/javascript";
        else if(path.endsWith(".png")) return  "image/png";
        else if(path.endsWith(".gif")) return  "image/gif";
        else if(path.endsWith(".jpg")) return  "image/jpeg";
        else if(path.endsWith(".ico")) return  "image/x-icon";
        return "application/octet-stream";
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static class UploadedFile {
        String filename;
        long size;
        String savedPath;

        UploadedFile(String filename, long size, String savedPath) {
            this.filename = filename;
            this.size = size;
            this.savedPath = savedPath;
        }
    }

    private static class WebSocketConnection {
        private final Socket socket;
        private final String id;
        private final CustomWebServer server;
        private volatile boolean active = true;

        WebSocketConnection(Socket socket, String id, CustomWebServer server) {
            this.socket = socket;
            this.id = id;
            this.server = server;
        }

        void handleMessages() {
            try (InputStream input = socket.getInputStream()) {
                while (active && !socket.isClosed()) {
                    WebSocketFrame frame = readFrame(input);
                    if (frame == null) break;

                    if (frame.opcode == 0x8) {
                        break;
                    } else if (frame.opcode == 0x1) {
                        String message = new String(frame.payload, StandardCharsets.UTF_8);

                        sendMessage("Echo: " + message);
                        server.broadcastWebSocketMessage("Broadcast: " + message, id);
                    }
                }
            } catch (IOException e) {
            } finally {
                close();
            }
        }

        WebSocketFrame readFrame(InputStream input) throws IOException {
            int b1 = input.read();
            if (b1 == -1) return null;

            boolean fin = (b1 & 0x80) != 0;
            int opcode = b1 & 0x0F;

            int b2 = input.read();
            if (b2 == -1) return null;

            boolean masked = (b2 & 0x80) != 0;
            long payloadLen = b2 & 0x7F;

            if (payloadLen == 126) {
                payloadLen = (input.read() << 8) | input.read();
            } else if (payloadLen == 127) {
                payloadLen = 0;
                for (int i = 0; i < 8; i++) {
                    payloadLen = (payloadLen << 8) | input.read();
                }
            }

            byte[] mask = null;
            if (masked) {
                mask = new byte[4];
                input.read(mask);
            }

            byte[] payload = new byte[(int) payloadLen];
            input.read(payload);

            if (masked && mask != null) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= mask[i % 4];
                }
            }

            return new WebSocketFrame(fin, opcode, payload);
        }

        void sendMessage(String message) throws IOException {
            byte[] payload = message.getBytes(StandardCharsets.UTF_8);

            ByteArrayOutputStream frame = new ByteArrayOutputStream();
            frame.write(0x81);

            if (payload.length < 126) {
                frame.write(payload.length);
            } else if (payload.length < 65536) {
                frame.write(126);
                frame.write((payload.length >> 8) & 0xFF);
                frame.write(payload.length & 0xFF);
            } else {
                frame.write(127);
                for (int i = 7; i >= 0; i--) {
                    frame.write((payload.length >> (i * 8)) & 0xFF);
                }
            }

            frame.write(payload);

            synchronized (socket) {
                socket.getOutputStream().write(frame.toByteArray());
                socket.getOutputStream().flush();
            }
        }

        void close() {
            active = false;
            server.removeWebSocketConnection(id);
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeWebSocketConnection(String id) {
        webSocketConnections.remove(id);
    }

    public void broadcastWebSocketMessage(String message, String excludeId) {
        webSocketConnections.forEach((id, conn) -> {
            if (!id.equals(excludeId)) {
                try {
                    conn.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static class WebSocketFrame {
        boolean fin;
        int opcode;
        byte[] payload;

        WebSocketFrame(boolean fin, int opcode, byte[] payload) {
            this.fin = fin;
            this.opcode = opcode;
            this.payload = payload;
        }
    }

    public static void main(String[] args) {
        CustomWebServer virtualServer = new CustomWebServer(8080, 100, true);
        CustomWebServer platformServer = new CustomWebServer(8081, 50, false);

        Thread thread1 = new Thread(() -> {
            try {
                virtualServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                platformServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Servers started:");
        System.out.println("Virtual thread server: http://localhost:8080");
        System.out.println("Platform thread server: http://localhost:8081");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            virtualServer.stop();
            platformServer.stop();
        }));

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}