package fr.mifa.server.services;

import fr.mifa.core.models.Message;
import fr.mifa.core.models.Room;
import fr.mifa.core.models.User;
import fr.mifa.core.network.protocol.*;
import fr.mifa.core.services.AbstractService;
import fr.mifa.server.network.ServerRoomPacketManager;
import fr.mifa.server.utils.ServerProperties;

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

    private HashMap<String, Room> rooms;

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
            room = new Room(roomName, "224.0.0.1");
            room.setPacketManager(new ServerRoomPacketManager(room));

            rooms.put(roomName, room);
        }
        if (UserService.getInstance().getUser(roomName).isPresent() && room.getUsers().size() == 2) {
            return;
        }
        room.getUsers().add(user);
        logger.info(user.getNickname() + " joined room " + room.getName());

        JoinedRoomPacket packet = new JoinedRoomPacket(user.getNickname(), room);
        user.getPacketManager().send(packet);
    }

    public void leaveRoom(User user, String roomName) {
        Room room = this.rooms.get(roomName);
        if (room != null) {
            room.getUsers().remove(user);
            logger.info(user.getNickname() + " left room " + room.getName());
            Packet packet = new LeftRoomPacket(user.getNickname(), room.getId());
            user.getPacketManager().send(packet);
        } else {
            logger.warn("Room " + roomName + " does not exist");
        }
    }

    public void receivedMessage(Message message) {
        Room room = this.rooms.get(message.getRoomName());
        if (room != null) {
            room.getHistory().add(message);
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
                for(Room room : rooms.values()) {
                    room.setPacketManager(new ServerRoomPacketManager(room));
                }
                logger.info("Rooms loaded");
                os.close();
            } catch (IOException | ClassNotFoundException ex) {
                logger.error(ex.toString());
            }
        }
    }
}
