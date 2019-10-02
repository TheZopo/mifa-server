package fr.mifa.server.services;

import fr.mifa.core.models.Message;
import fr.mifa.core.models.Room;
import fr.mifa.core.models.User;
import fr.mifa.core.network.protocol.*;
import fr.mifa.core.services.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomService extends AbstractService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);


    /** Singleton **/
    private RoomService()
    {
        this.rooms = new HashMap<>();
    }

    private static class SingletonHolder
    {
        /** Instance unique non préinitialisée */
        private final static RoomService instance = new RoomService();
    }

    /** Point d'accès pour l'instance unique du singleton */
    public static RoomService getInstance()
    {
        return SingletonHolder.instance;
    }

    /** Sécurité anti-désérialisation */
    protected Object readResolve() {
        return SingletonHolder.instance;
    }
    /** End Singleton **/

    HashMap<String, Room> rooms;

    public HashMap<String, Room> getRooms() {
        return rooms;
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
        if (UserService.getInstance().getUser(roomName).isPresent() && room.getUsers().size() == 2) {
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
            if (user.getPacketManager() == null) {
                Optional<User> correctUser = UserService.getInstance().getUser(user.getNickname());
                if (correctUser.isPresent()) {
                    user.setPacketManager(correctUser.get().getPacketManager());
                }
            }
            if (user.getPacketManager() != null) {
                user.getPacketManager().send(packet);
            }
        }
    }

    @Override
    public void saveState() {
        ObjectOutputStream os = this.getOutputStream();
        if (os != null) {
            try {
                os.writeObject(this.rooms);
                logger.info("Rooms saved");
                os.close();
            } catch (IOException ex) {
                logger.error(ex.toString());
            }
        }
    }

    @Override
    public void loadState() {
        ObjectInputStream os = this.getInputStream();
        if (os != null) {
            try {
                this.rooms = (HashMap<String, Room>) os.readObject();
                logger.info("Rooms loaded");
                os.close();
            } catch (IOException | ClassNotFoundException ex) {
                logger.error(ex.toString());
            }
        }
    }
}
