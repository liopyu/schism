package com.schism.core.creatures;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;

public class CreatureTypeRepository extends AbstractRepository<CreatureTypeDefinition>
{
    private static final CreatureTypeRepository INSTANCE = new CreatureTypeRepository();

    /**
     * Gets the singleton CreatureTypeRepository instance.
     * @return The Creature Type Repository which loads and stores creature types.
     */
    public static CreatureTypeRepository get()
    {
        return INSTANCE;
    }

    protected CreatureTypeRepository()
    {
        super("creature_type");
    }

    @Override
    public CreatureTypeDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new CreatureTypeDefinition(subject, dataStore);
    }
}
