package com.schism.core.gods;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;

public class PantheonDefinition extends AbstractDefinition
{
    /**
     * Pantheon definitions hold information about pantheons.
     * @param subject The name of the pantheon.
     * @param dataStore The data store that holds config data about this definition.
     */
    public PantheonDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return PantheonRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);
    }
}
