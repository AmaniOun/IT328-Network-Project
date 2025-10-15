package server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();
    public static ServerDatabase database = new ServerDatabase();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("Server started...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");
            ClientHandler handler = new ClientHandler(clientSocket, database);
            clients.add(handler);
            new Thread(handler).start();
        }
    }
}