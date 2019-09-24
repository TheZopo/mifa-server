package fr.mifa.server.network;

import fr.mifa.core.models.Message;
import fr.mifa.core.models.User;
import fr.mifa.core.network.protocol.*;
import fr.mifa.server.services.RoomService;
import fr.mifa.server.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.mifa.core.network.PacketManager;

import java.net.Socket;

public class ServerPacketManager extends PacketManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerPacketManager.class);

    public ServerPacketManager(Socket socket) {
        super(socket);
    }

    @Override
    protected void processPacket(Packet packet) {
        if(packet instanceof AuthPacket) {
            logger.debug("Received AuthPacket");
            AuthPacket authPacket = (AuthPacket)packet;
            User user = new User(authPacket.getNickname(), this);
            this.setUser(user);
            UserService.INSTANCE.addUser(user);
        }
        else if (packet instanceof JoinRoomPacket) {
            logger.debug("Received JoinRoomPacket");
            JoinRoomPacket joinRoomPacket = (JoinRoomPacket)packet;
            if (this.getUser() != null) {
                RoomService.INSTANCE.joinRoom(this.getUser(), joinRoomPacket.getRoomId());
            }
            else {
                logger.warn("User is not authenticated yet - can't join room");
            }
        }
        else if (packet instanceof MessagePacket) {
            logger.debug("Received MessagePacket");
            MessagePacket messagePacket = (MessagePacket) packet;
            if (this.getUser() != null) {
                Message message = messagePacket.getMessage();
                User user = this.getUser();
                // fill more detail about message origin for other clients
                message.setAuthorName(user.getNickname());
                message.setAuthorId(user.getId());
                RoomService.INSTANCE.broadcastMessage(message);
            }
            else {
                logger.warn("User is not authenticated yet - can't send message");
            }
        }
        else if (packet instanceof LeaveRoomPacket) {
            logger.debug("Received LeaveRoomPacket");
            LeaveRoomPacket leaveRoomPacket = (LeaveRoomPacket) packet;
            if (this.getUser() != null) {
                RoomService.INSTANCE.leaveRoom(this.getUser(), leaveRoomPacket.getRoomId());
            }
            else {
                logger.warn("User is not authenticated yet - can't send message");
            }
        }
        else {
            logger.warn("Received unknown packet :" + packet.getClass().getName());
        }
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();

        UserService.INSTANCE.removeUser(this.getUser());
    }
}
