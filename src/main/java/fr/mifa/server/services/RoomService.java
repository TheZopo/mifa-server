package fr.mifa.server.services;

import fr.mifa.core.models.Message;
import fr.mifa.core.models.Room;
import fr.mifa.core.models.User;
import fr.mifa.core.network.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public enum RoomService {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    HashMap<String, Room> rooms;

    public HashMap<String, Room> getRooms() {
        return rooms;
    }

    RoomService() {
        this.rooms = new HashMap<>();
    }

    public void sendRoomsList(User user) {
        Collection<Room> userRooms = rooms.values()
                .stream()
                .filter(p -> p.getUsers().contains(user))
                .collect(Collectors.toList());
        user.getPacketManager().send(new RoomListPacket(new ArrayList<Room>(userRooms)));
    }

    public void joinRoom(User user, String roomName) {
        Room room = this.rooms.get(roomName);
        if (room == null) {
            //room does not exist yet, create it
            room = new Room(roomName);
            rooms.put(roomName, room);
        }
        if (UserService.INSTANCE.getUser(roomName).isPresent() && room.getUsers().size() == 2) {
            return;
        }
        room.getUsers().add(user);
        logger.info(user.getNickname() + " joined room " + room.getName());
        this.broadcastPacket(room, new JoinedRoomPacket(user.getNickname(), room));
    }

    public void leaveRoom(User user, String roomName) {
        Room room = this.rooms.get(roomName);
        if (room != null) {
            room.getUsers().remove(user);
            logger.info(user.getNickname() + " left room " + room.getName());
            Packet packet = new LeftRoomPacket(user.getNickname(), room.getId());
            user.getPacketManager().send(packet);
            this.broadcastPacket(room, packet);
        } else {
            logger.warn("Room " + roomName + " does not exist");
        }
    }

    public void broadcastMessage(Message message) {
        Room room = this.rooms.get(message.getRoomName());
        if (room != null) {
            this.broadcastPacket(room, new MessageSentPacket(message));
            room.getHistory().add(message);
        }
    }

    public void broadcastPacket(Room room, Packet packet) {
        logger.info("Broadcasting  " + packet.getClass().getName() + " to room " + room.getId());
        for (User user: room.getUsers()) {
            user.getPacketManager().send(packet);
        }
    }
}
