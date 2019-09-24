package fr.mifa.server.network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import fr.mifa.core.network.PacketManager;


public enum Server {
    INSTANCE;

    final Logger logger = LoggerFactory.getLogger(Server.class);

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final int DEFAULT_PORT = 2021;

    ServerSocket serverSocket;
    ArrayList<PacketManager> clients;

    Server() {
        try {
            this.serverSocket = new ServerSocket();
        }
        catch (IOException ex) {
            logger.error(ex.toString());
        }
        this.clients = new ArrayList<>();
    }

    public void bind(String address, int port) {
        try {
            serverSocket.bind(new InetSocketAddress(address, port));
        }
        catch (IOException ex) {
            logger.error(ex.toString());
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
            logger.warn("Attempted to listen on a null server socket");
            return;
        }
        System.out.println("Listening");
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                PacketManager client = new ServerPacketManager();
                clients.add(client);
                client.start();
            }
            catch (SocketException ex) {
                logger.error(ex.toString());
                return;
            }
            catch (IOException ex) {
                logger.error(ex.toString());
            }
        }
    }

    public void close() {
        if (serverSocket == null) {
            logger.warn("Attempted to close a null server socket");
            return;
        }
        try {
            serverSocket.close();
        }
        catch (IOException ex) {
            logger.error(ex.toString());
        }
    }
}
