package com.schism.core.creatures;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;

public class CreatureTypeDefinition extends AbstractDefinition
{
    /**
     * Beastiary definitions hold beastiary information creature types.
     * @param subject The name of the creature type.
     * @param dataStore The data store that holds config data about this definition.
     */
    public CreatureTypeDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return CreatureTypeRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);
    }
}
