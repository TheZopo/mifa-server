package fr.mifa.server;

import fr.mifa.server.network.Server;
import fr.mifa.server.services.RoomService;
import fr.mifa.server.services.UserService;
import fr.mifa.server.utils.ServerProperties;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static Thread mainThread;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Hello World mifa-server !");

        mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                if (ServerProperties.INSTANCE.get("STORAGE", "true").equals("true")) {
                    UserService.getInstance().saveState();
                    RoomService.getInstance().saveState();
                }
                System.out.println("Shutting down...");
                Main.mainThread.interrupt();
            }
        });

        Options options = new Options();
        options.addOption("p", true, "Listening port");
        options.addOption("a", true, "Listening address");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
        }
        catch (ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java mifa-server.jar", options );
        }
        Server server = Server.INSTANCE;
        if (cmd == null) {
            server.bind();
        }
        else {
            String address = cmd.getOptionValue("a");
            String portStr = cmd.getOptionValue("p");
            // fallback to settings file
            if (address == null) {
                address = ServerProperties.INSTANCE.get("HOST", "");
            }
            if (portStr == null) {
                portStr = ServerProperties.INSTANCE.get("PORT", "");
            }
            if ("".equals(address) && "".equals(portStr)) {
                server.bind();
            }
            else if ("".equals(address)) {
                try {
                    int port = Integer.parseInt(portStr);
                    server.bind(port);
                }
                catch (NumberFormatException ex) {
                    logger.error(ex.toString());
                }
            }
            else if ("".equals(portStr)) {
                server.bind(address);
            }
        }
        Thread serverThread = new Thread(server::listen);
        if (ServerProperties.INSTANCE.get("STORAGE", "true").equals("true")) {
            UserService.getInstance().loadState();
            RoomService.getInstance().loadState();
        }
        serverThread.start();
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            if ("exit".equals(input)) {
                break;
            }
        }
        server.close();
    }
}
