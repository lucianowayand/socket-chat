package org.example;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;

    public static void main(String[] args) {
        new ChatClient().start();
    }

    public void start() {
        try {
            socket = new Socket("localhost", ChatServer.PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            Thread listenerThread = new Thread(new Listener());
            listenerThread.start();

            String input;
            while (true) {
                input = scanner.nextLine();
                out.println(input);

                if (input.equals("/sair")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar a conexão: " + e.getMessage());
        }
    }

    private class Listener implements Runnable {
        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    if (response.startsWith("/b")){
                        var broadcastCommand = response.replace("/b ","");
                        out.println("/"+broadcastCommand);
                    } else {
                        System.out.println(response);
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro ao ler do servidor: " + e.getMessage());
            }
        }
    }
}
