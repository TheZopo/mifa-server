package fr.mifa.server.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

public enum ServerProperties {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ServerProperties.class);

    private Dotenv dotenv;

    ServerProperties() {
        dotenv = Dotenv.configure().ignoreIfMissing().load();
    }

    public String get(String key, String defaultValue) {
        return dotenv.get(key, defaultValue);
    }
}
