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
import java.util.Optional;

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
            Optional<User> user = UserService.getInstance().getUser(authPacket.getNickname());
            if (!user.isPresent()) {
                User nuser = new User(authPacket.getNickname(), this);
                this.setUser(nuser);
                UserService.getInstance().addUser(nuser);
                RoomService.getInstance().sendRoomsList(nuser);
            }
            else {
                if (user.get().isConnected()) {
                    this.send(new DisconnectPacket("A user with the same nickname is already connected"));
                }
                else {
                    this.setUser(user.get());
                    user.get().setPacketManager(this);
                    RoomService.getInstance().sendRoomsList(user.get());
                }
            }
        }
        else if (packet instanceof JoinRoomPacket) {
            logger.debug("Received JoinRoomPacket");
            JoinRoomPacket joinRoomPacket = (JoinRoomPacket)packet;
            if (this.getUser() != null) {
                RoomService.getInstance().joinRoom(this.getUser(), joinRoomPacket.getRoomName());
            }
            else {
                logger.warn("User is not authenticated yet - can't join room");
            }
        }
        else if (packet instanceof LeaveRoomPacket) {
            logger.debug("Received LeaveRoomPacket");
            LeaveRoomPacket leaveRoomPacket = (LeaveRoomPacket) packet;
            if (this.getUser() != null) {
                RoomService.getInstance().leaveRoom(this.getUser(), leaveRoomPacket.getRoomName());
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
        if (this.getUser() != null) {
            logger.debug("User " + this.getUser().getNickname() + " is now deconnected");
            this.getUser().setConnected(false);
        }
    }
}
