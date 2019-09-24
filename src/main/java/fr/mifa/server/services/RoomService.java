package fr.mifa.server.services;

import fr.mifa.core.models.Message;
import fr.mifa.core.models.Room;
import fr.mifa.core.models.User;
import fr.mifa.core.network.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public enum RoomService {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    HashMap<Integer, Room> rooms;

    public HashMap<Integer, Room> getRooms() {
        return rooms;
    }

    RoomService() {
        this.rooms = new HashMap<>();
    }

    public void sendRoomsList(User user) {
        user.getPacketManager().send(new RoomListPacket(new ArrayList<Room>(rooms.values())));
    }

    public void joinRoom(User user, int roomId) {
        Room room = this.rooms.get(roomId);
        if (room == null) {
            //room does not exist yet, create it
            room = new Room();
            rooms.put(room.getId(), room);
        }
        room.getUsers().add(user);
        this.broadcastPacket(roomId, new JoinedRoomPacket(user.getNickname(), roomId));
    }

    public void leaveRoom(User user, int roomId) {
        Room room = this.rooms.get(roomId);
        if (room != null) {
            room.getUsers().remove(user);
            this.broadcastPacket(roomId, new LeftRoomPacket(user.getNickname(), roomId));
        } else {
            logger.warn("Room " + roomId + " does not exist");
        }
    }

    public void broadcastMessage(Message message) {
        this.broadcastPacket(message.getRoomId(), new MessageSentPacket(message));
    }

    public void broadcastPacket(int roomId, Packet packet) {
        Room room = this.rooms.get(roomId);
        if (room != null) {
            for (User user: room.getUsers()) {
                user.getPacketManager().send(packet);
            }
        }
        else {
            logger.warn("Room " + roomId + " does not exist");
        }

    }
}
