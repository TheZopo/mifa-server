package fr.mifa.server.services;

import fr.mifa.core.models.User;
import fr.mifa.core.network.PacketManager;

import java.util.ArrayList;

public enum UserService {
    INSTANCE;

    ArrayList<User> users;

    UserService() {
        users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
    }
}
