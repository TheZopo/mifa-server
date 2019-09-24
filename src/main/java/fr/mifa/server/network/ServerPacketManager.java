package fr.mifa.server.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.mifa.core.network.PacketManager;
import fr.mifa.core.network.protocol.AuthPacket;
import fr.mifa.core.network.protocol.Packet;

import java.net.Socket;

public class ServerPacketManager extends PacketManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerPacketManager.class);

    public ServerPacketManager(Socket socket) {
        super(socket);
    }

    @Override
    protected void processPacket(Packet packet) {
        if(packet instanceof AuthPacket) {
            logger.info("AuthPacket");
        }
    }
}
