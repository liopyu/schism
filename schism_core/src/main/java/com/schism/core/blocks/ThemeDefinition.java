package com.schism.core.blocks;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;

public class ThemeDefinition extends AbstractDefinition
{
    /**
     * Theme definitions hold information about a collection of decorative blocks.
     * @param subject The name of the block theme, block names will be based on this.
     * @param dataStore The data store that holds config data about this definition.
     */
    public ThemeDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return ThemeRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);
    }
}
