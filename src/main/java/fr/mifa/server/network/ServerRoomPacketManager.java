package fr.mifa.server.network;

import fr.mifa.core.network.protocol.ReactionPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.mifa.core.models.Message;
import fr.mifa.core.models.Room;
import fr.mifa.core.models.User;
import fr.mifa.core.network.RoomPacketManager;
import fr.mifa.core.network.protocol.MessagePacket;
import fr.mifa.core.network.protocol.Packet;
import fr.mifa.server.services.RoomService;

public class ServerRoomPacketManager extends RoomPacketManager {
    private static final Logger logger = LoggerFactory.getLogger(ServerPacketManager.class);

    public ServerRoomPacketManager(Room room) {
        super(room.getAddress(), room.getPort());
    }

    @Override
    protected void processPacket(Packet packet) {
        if (packet instanceof MessagePacket) {
            logger.debug("Received MessagePacket");

            MessagePacket messagePacket = (MessagePacket) packet;
            RoomService.getInstance().receivedMessage(messagePacket.getMessage());
        }
        else if (packet instanceof ReactionPacket) {
            logger.debug("Received ReactionPacket");

            ReactionPacket reactionPacket = (ReactionPacket)packet;
            RoomService.getInstance().receivedReaction(reactionPacket);
        }
    }
}
