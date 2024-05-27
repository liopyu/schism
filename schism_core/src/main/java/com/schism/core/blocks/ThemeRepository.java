package com.schism.core.blocks;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;

public class ThemeRepository extends AbstractRepository<ThemeDefinition>
{
    private static final ThemeRepository INSTANCE = new ThemeRepository();

    /**
     * Gets the singleton ThemeRepository instance.
     * @return The Theme Repository which loads and stores block themes.
     */
    public static ThemeRepository get()
    {
        return INSTANCE;
    }

    protected ThemeRepository()
    {
        super("theme");
    }

    @Override
    public ThemeDefinition createDefinition(String subject, DataStore dataStore)
    {
        ThemeDefinition definition = new ThemeDefinition(subject, dataStore);
        BlockRepository.get().loadThemeDefinition(definition);
        return definition;
    }
}
