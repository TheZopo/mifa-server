package fr.mifa.server.network;

import fr.mifa.core.network.RoomPacketManager;
import fr.mifa.core.network.protocol.MessagePacket;
import fr.mifa.core.network.protocol.Packet;

public class ServerRoomPacketManager extends RoomPacketManager {

    public ServerRoomPacketManager(String address, int port) {
        super(address, port);
    }

    @Override
    protected void processPacket(Packet packet) {
        if(packet instanceof MessagePacket) {
            //TODO: history
        }
    }
}
