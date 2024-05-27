package com.schism.core.creatures;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;

public class CreatureRoleRepository extends AbstractRepository<CreatureRoleDefinition>
{
    private static final CreatureRoleRepository INSTANCE = new CreatureRoleRepository();

    /**
     * Gets the singleton CreatureRoleRepository instance.
     * @return The Creature Role Repository which loads and stores creature roles.
     */
    public static CreatureRoleRepository get()
    {
        return INSTANCE;
    }

    protected CreatureRoleRepository()
    {
        super("creature_role");
    }

    @Override
    public CreatureRoleDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new CreatureRoleDefinition(subject, dataStore);
    }
}
