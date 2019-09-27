package fr.mifa.server.services;

import fr.mifa.core.models.User;
import fr.mifa.core.network.PacketManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;

public enum UserService {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    ArrayList<User> users;

    UserService() {
        users = new ArrayList<>();
    }

    public void addUser(User user) {
        logger.info("User " + user.getNickname() + " connected");
        users.add(user);
    }

    public void removeUser(User user) {
        logger.info("User " + user.getNickname() + " disconnected");
        users.remove(user);
    }

    public Optional<User> getUser(String nickname) {
        return users.stream()
                    .filter(u -> nickname.equals(u.getNickname()))
                    .findFirst();
    }
}
