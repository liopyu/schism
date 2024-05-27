package com.schism.core.config;

import com.schism.core.AbstractRepository;
import com.schism.core.Log;
import com.schism.core.database.DataStore;

import java.util.Random;

public class ConfigRepository extends AbstractRepository<ConfigDefinition>
{
    private static final ConfigRepository INSTANCE = new ConfigRepository();

    /**
     * Gets the singleton ConfigRepository instance.
     * @return The Config Repository which loads and stores global configs.
     */
    public static ConfigRepository get()
    {
        return INSTANCE;
    }

    protected ConfigRepository()
    {
        super("config");
    }

    @Override
    public ConfigDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new ConfigDefinition(subject, dataStore);
    }

    /**
     * Gets a boolean setting value.
     * @param subject The config subject ex: 'creatures', 'worldgen', etc.
     * @param setting The setting to get a value for.
     * @param fallback The default fallback value to return if the config setting is missing.
     * @return The setting value.
     */
    public boolean booleanSetting(String subject, String setting, boolean fallback)
    {
        return this.getDefinition(subject).map(config -> config.booleanSetting(setting)).orElse(fallback);
    }

    /**
     * Gets an integer setting value.
     * @param subject The config subject ex: 'creatures', 'worldgen', etc.
     * @param setting The setting to get a value for.
     * @param fallback The default fallback value to return if the config setting is missing.
     * @return The setting value.
     */
    public int intSetting(String subject, String setting, int fallback)
    {
        return this.getDefinition(subject).map(config -> config.intSetting(setting)).orElse(fallback);
    }

    /**
     * Gets a float setting value.
     * @param subject The config subject ex: 'creatures', 'worldgen', etc.
     * @param setting The setting to get a value for.
     * @param fallback The default fallback value to return if the config setting is missing.
     * @return The setting value.
     */
    public float floatSetting(String subject, String setting, float fallback)
    {
        return this.getDefinition(subject).map(config -> config.floatSetting(setting)).orElse(fallback);
    }

    /**
     * Gets a random value of a min max setting.
     * @param subject The config subject ex: 'creatures', 'worldgen', etc.
     * @param random An instance of random.
     * @param setting The setting to get a random range for.
     * @param fallback The default fallback value to return if the config setting is missing.
     * @return A random value for the provided setting.
     */
    public int randomIntSetting(Random random, String subject, String setting, int fallback)
    {
        int min = this.intSetting(subject, setting + ".min", fallback);
        int max = this.intSetting(subject, setting + ".max", fallback);
        if (min >= max) {
            return max;
        }
        return random.nextInt(min, max + 1);
    }
}
