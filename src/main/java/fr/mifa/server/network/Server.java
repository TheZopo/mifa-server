package fr.mifa.server.network;

import fr.mifa.core.network.Client;
import fr.mifa.core.network.IClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public enum Server {
    INSTANCE;

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 2021;

    ServerSocket serverSocket;
    ArrayList<IClient> clients;

    Server() {
        try {
            this.serverSocket = new ServerSocket();
        }
        catch (IOException ex) {
            //TODO
        }
        this.clients = new ArrayList<>();
    }

    public void bind(String address, int port) {
        try {
            serverSocket.bind(new InetSocketAddress(address, port));
        }
        catch (IOException ex) {
            //TODO
        }
    }

    public void bind(String address) {
        bind(address, DEFAULT_PORT);
    }

    public void bind(int port) {
        bind(DEFAULT_ADDRESS, port);
    }

    public void bind() {
        bind(DEFAULT_ADDRESS, DEFAULT_PORT);
    }

    public void listen() {
        if (serverSocket == null) {
            //TODO log
            return;
        }
        System.out.println("Listening");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                Client client = new Client(clientSocket);
                clients.add(client);
                client.start();
            }
            catch (SocketException ex) {
                return;
            }
            catch (IOException ex) {
                //TODO
            }
        }
    }

    public void close() {
        if (serverSocket == null) {
            //TODO log
            return;
        }
        try {
            serverSocket.close();
        }
        catch (IOException ex) {
            //TODO
        }
    }
}
