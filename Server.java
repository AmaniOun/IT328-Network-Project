package server;

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private String host;
    private int port;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Server{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
    system.out.println("Server is running on " + host + ":" + port);
}