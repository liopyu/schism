package com.schism.core.config;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;

import java.util.Random;

public class ConfigDefinition extends AbstractDefinition
{
    /**
     * Config definitions hold global config settings.
     * @param subject The name of the config.
     * @param dataStore The data store that holds config data about this definition.
     */
    public ConfigDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return ConfigRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);
    }

    /**
     * Gets a boolean setting value.
     * @param setting The setting to get a value for.
     * @return The setting value.
     */
    public boolean booleanSetting(String setting)
    {
        return this.dataStore.booleanProp(setting);
    }

    /**
     * Gets an integer setting value.
     * @param setting The setting to get a value for.
     * @return The setting value.
     */
    public int intSetting(String setting)
    {
        return this.dataStore.intProp(setting);
    }

    /**
     * Gets a float setting value.
     * @param setting The setting to get a value for.
     * @return The setting value.
     */
    public float floatSetting(String setting)
    {
        return this.dataStore.floatProp(setting);
    }

    /**
     * Gets a random value of a min max setting.
     * @param random An instance of random.
     * @param setting The setting to get a random range for.
     * @return A random value for the provided setting.
     */
    public int randomIntSetting(Random random, String setting)
    {
        int min = this.intSetting(setting + ".min");
        int max = this.intSetting(setting + ".max");
        if (min >= max) {
            return max;
        }
        return random.nextInt(min, max + 1);
    }
}
