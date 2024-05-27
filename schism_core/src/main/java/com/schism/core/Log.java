package com.schism.core;

import com.schism.core.database.Database;
import com.schism.core.database.IDatabaseEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

/**
 * This class is used for quickly logging things, this covers simple text logging but can also handle other common debug output, etc.
 */
public class Log implements IDatabaseEventListener
{
    private static Log INSTANCE;

    /**
     * The logger to use.
     */
    protected final Logger logger = LogManager.getLogger();

    /**
     * Gets the singleton Log instance, this is lazy loaded so can be called by other static initializers.
     * @return The Log singleton instance.
     */
    protected static Log get()
    {
        if (INSTANCE == null) {
            INSTANCE = new Log();
            Database.get().addLoadListener(INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public void onDatabaseLoaded(Database database)
    {
        // TODO Load log configs. Should move to a Config Repo.
    }

    /**
     * Logs a message at info level.
     * @param category The category of the message, should be lowercase and subject to config control, ex: core, ai, equipment, dungeon.
     * @param message The message to display.
     * @param objects Optional additional objects to include in the message.
     */
    public static void info(String category, String message, Object... objects)
    {
        // TODO Check if category info logging is enabled in the config.
        String prefix = "[" + Schism.MOD_NAME + "|" + category.toLowerCase(Locale.ENGLISH) + "] ";
        if (objects.length > 0) {
            get().logger.info(prefix + message, objects);
        } else {
            get().logger.info(prefix + message);
        }
    }

    /**
     * Logs a message at info level and in the debug category.
     * @param message The message to display.
     * @param objects Optional additional objects to include in the message.
     */
    public static void info(String message, Object... objects)
    {
        info("debug", message, objects);
    }

    /**
     * Logs a message at info level with some fancy title ascii styling.
     * @param category The category of the message, should be lowercase and subject to config control, ex: core, ai, equipment, dungeon.
     * @param message The message to display as a title.
     */
    public static void infoTitle(String category, String message)
    {
        info(category, "~o==========[::] " + message + " [::]==========o~");
    }

    /**
     * Logs a message at error level.
     * @param category The category of the message, should be lowercase and subject to config control, ex: core, ai, equipment, dungeon.
     * @param message The message to display.
     * @param objects Optional additional objects to include in the message.
     */
    public static void error(String category, String message, Object... objects)
    {
        // TODO Check if category info logging is enabled in the config.
        String prefix = "[" + Schism.MOD_NAME + "]" + "[" + category.toLowerCase(Locale.ENGLISH) + "] ";
        if (objects.length > 0) {
            get().logger.error(prefix + message, objects);
        } else {
            get().logger.error(prefix + message);
        }
    }
}
