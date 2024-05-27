package com.schism.core.gods;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;

public class GodRepository extends AbstractRepository<GodDefinition>
{
    private static final GodRepository INSTANCE = new GodRepository();

    /**
     * Gets the singleton GodRepository instance.
     * @return The God Repository which loads and stores gods.
     */
    public static GodRepository get()
    {
        return INSTANCE;
    }

    protected GodRepository()
    {
        super("god");
    }

    @Override
    public GodDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new GodDefinition(subject, dataStore);
    }
}
