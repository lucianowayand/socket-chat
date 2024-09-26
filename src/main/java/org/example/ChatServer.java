package org.example;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.example.ChatServer.log;

public class ChatServer {
    private static final Map<String, ClientHandler> clients = new HashMap<>();
    private static final String LOG_FILE = "server-log.txt";
    public static int PORT = 12345;
    public static boolean PRIVATE_MODE = false;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado na porta " + PORT);
            log("Servidor iniciado");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    public static synchronized void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        broadcast("/b users", null);  // Notifica todos sobre os clientes conectados
    }

    public static synchronized void removeClient(String username) {
        clients.remove(username);
        broadcast("/b users", null);  // Notifica todos sobre os clientes conectados
    }

    public static synchronized Set<String> getClients() {
        return clients.keySet();
    }

    public static synchronized void sendMessage(String sender, String recipient, String message) {
        ClientHandler clientHandler = clients.get(recipient);
        if (clientHandler != null) {
            clientHandler.sendMessage(sender + ": " + message);
        }
    }

    public static synchronized void sendFile(String sender, String recipient, byte[] fileBytes, String fileName) {
        ClientHandler clientHandler = clients.get(recipient);
        if (clientHandler != null) {
            clientHandler.sendFile(sender, fileBytes, fileName);
        }
    }

    private static void broadcast(String command, ClientHandler excludeHandler) {
        for (ClientHandler client : clients.values()) {
            if (client != excludeHandler) {
                client.sendCommand(command);
            }
        }
    }

    public static void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println("[" + timeStamp + "] " + message);
        } catch (IOException e) {
            System.err.println("Erro ao gravar log: " + e.getMessage());
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private String username;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            handleAddUser();

            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("/send message")) {
                    handleMessage(input);
                } else if (input.startsWith("/send file")) {
                    handleFile(input);
                } else if (input.equals("/users")) {
                    listUsers();
                } else if (input.equals("/sair")) {
                    break;
                } else {
                    log("Usuário " + username + " tentou enviar um comando ilegal: " + input );
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no cliente " + username + ": " + e.getMessage());
        } finally {
            ChatServer.removeClient(username);
            closeConnection();
        }
    }

    private void handleAddUser() throws IOException {
        out.println("Digite seu nome de usuário:");
        username = in.readLine();

        if (ChatServer.getClients().contains(username)) {
            out.println("Já existe usuário com este nome, tente novamente");
            handleAddUser();

        } else if (username.split(" ").length > 1) {
            out.println("Nome de usuário nao pode conter espaços, tente novamente");
            handleAddUser();

        } else {
            out.println("Conectado ao servidor com sucesso!");

            ChatServer.addClient(username, this);
            log("Cliente conectado: " + username + " - " + this.socket.getInetAddress());
        }
    }


    private void handleMessage(String input) {
        String[] tokens = input.split(" ", 4);
        if (tokens.length == 4) {
            String recipient = tokens[2];
            String message = tokens[3];

            if (ChatServer.getClients().contains(recipient)) {
                if (!ChatServer.PRIVATE_MODE) {
                    System.out.println(username + " -> " + recipient + ": " + message);
                }

                ChatServer.sendMessage(username, recipient, message);
            } else {
                out.println("Envio falhou: usuario "+recipient+ " não encontrado.");
            }


        } else {
            out.println("Envio falhou: para enviar uma mensagem siga o padrão /send message <username> <mensagem>");
        }
    }

    private void handleFile(String input) throws IOException {
        String[] tokens = input.split(" ", 4);
        if (tokens.length == 4) {
            String recipient = tokens[2];
            String filePath = tokens[3];
            File file = new File(filePath);

            if (file.exists()) {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                ChatServer.sendFile(username, recipient, fileBytes, file.getName());
            } else {
                out.println("Arquivo não encontrado.");
            }
        }
    }

    private void listUsers() {
        out.println("Usuários conectados: " + ChatServer.getClients());
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void sendFile(String sender, byte[] fileBytes, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(fileBytes);
            fos.close();
            out.println("Arquivo recebido de " + sender + ": " + fileName);
            sendCommand("/b send message "+sender+" Recebi o arquivo: "+ fileName);
        } catch (IOException e) {
            out.println("Erro ao receber o arquivo.");
        }
    }

    public void sendCommand(String command) {
        out.println(command);
    }

    private void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}
