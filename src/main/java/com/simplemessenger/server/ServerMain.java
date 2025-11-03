package com.simplemessenger.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerMain {
    private static final int PORT = 5000;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("☕ Simple Messenger Server started on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private String username;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // first line must be username
                username = reader.readLine();
                if (username == null || username.trim().isEmpty()) {
                    socket.close();
                    return;
                }
                clients.put(username, this);
                broadcastUsers();

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("/msg ")) {
                        String payload = line.substring(5);
                        String[] parts = payload.split(":", 2);
                        if (parts.length == 2) {
                            String recipient = parts[0];
                            String message = parts[1];
                            sendToUser(recipient, username + ":" + message);
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println("Client disconnected: " + username);
            } finally {
                clients.remove(username);
                broadcastUsers();
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void sendToUser(String recipient, String message) {
            ClientHandler target = clients.get(recipient);
            if (target != null) {
                try {
                    target.writer.write("/msg " + message);
                    target.writer.newLine();
                    target.writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcastUsers() {
            String csv = String.join(",", clients.keySet());
            for (ClientHandler c : clients.values()) {
                try {
                    c.writer.write("/users " + csv);
                    c.writer.newLine();
                    c.writer.flush();
                } catch (IOException ignored) {}
            }
        }
    }
}
