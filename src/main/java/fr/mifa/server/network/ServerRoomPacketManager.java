package fr.mifa.server.network;

import fr.mifa.core.models.Room;
import fr.mifa.core.network.RoomPacketManager;
import fr.mifa.core.network.protocol.MessagePacket;
import fr.mifa.core.network.protocol.Packet;

public class ServerRoomPacketManager extends RoomPacketManager {

    public ServerRoomPacketManager(Room room) {
        super(room.getAddress(), room.getPort());
    }

    @Override
    protected void processPacket(Packet packet) {
        if(packet instanceof MessagePacket) {
            //TODO: history
        }
    }
}
