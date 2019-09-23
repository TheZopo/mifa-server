package fr.mifa.server;

import fr.mifa.server.network.Server;
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
            if (address == null && portStr == null) {
                server.bind();
            }
            else if (address == null) {
                try {
                    int port = Integer.parseInt(portStr);
                    server.bind(port);
                }
                catch (NumberFormatException ex) {
                    logger.error(ex.toString());
                }
            }
            else if (portStr == null) {
                server.bind(address);
            }
        }
        Thread serverThread = new Thread(server::listen);
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
