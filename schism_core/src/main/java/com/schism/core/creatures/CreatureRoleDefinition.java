package com.schism.core.creatures;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;

public class CreatureRoleDefinition extends AbstractDefinition
{
    /**
     * Beastiary definitions hold beastiary information creature roles.
     * @param subject The name of the creature role.
     * @param dataStore The data store that holds config data about this definition.
     */
    public CreatureRoleDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return CreatureRoleRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);
    }
}
