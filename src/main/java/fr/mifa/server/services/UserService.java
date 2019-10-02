package fr.mifa.server.services;

import fr.mifa.core.models.User;
import fr.mifa.core.network.PacketManager;
import fr.mifa.core.services.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class UserService extends AbstractService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /** Singleton **/
    private UserService()
    {
        this.users = new ArrayList<>();
    }

    private static class SingletonHolder
    {
        /** Instance unique non préinitialisée */
        private final static UserService instance = new UserService();
    }

    /** Point d'accès pour l'instance unique du singleton */
    public static UserService getInstance()
    {
        return UserService.SingletonHolder.instance;
    }

    /** Sécurité anti-désérialisation */
    protected Object readResolve() {
        return UserService.SingletonHolder.instance;
    }
    /** End Singleton **/

    ArrayList<User> users;

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

    @Override
    public void saveState() {
        ObjectOutputStream os = this.getOutputStream();
        if (os != null) {
            try {
                // disconnect all
                for (User user : this.users) {
                    user.setConnected(false);
                }
                os.writeObject(this.users);
                logger.info("Users saved");
                os.close();
            }
            catch (IOException ex) {
                logger.error(ex.toString());
            }
        }
    }

    @Override
    public void loadState() {
        ObjectInputStream os = this.getInputStream();
        if (os != null) {
            try {
                this.users = (ArrayList<User>) os.readObject();
                logger.info("Users loaded");
                os.close();
            }
            catch (IOException | ClassNotFoundException ex) {
                logger.error(ex.toString());
            }
        }
    }
}
