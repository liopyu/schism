package com.schism.core.altars;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;

public class AltarRepository extends AbstractRepository<AltarDefinition>
{
    private static final AltarRepository INSTANCE = new AltarRepository();

    /**
     * Gets the singleton AltarRepository instance.
     * @return The Altar Repository which loads and stores altars.
     */
    public static AltarRepository get()
    {
        return INSTANCE;
    }

    protected AltarRepository()
    {
        super("altar");
    }

    @Override
    public AltarDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new AltarDefinition(subject, dataStore);
    }
}
