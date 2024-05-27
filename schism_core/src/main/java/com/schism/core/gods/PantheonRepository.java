package com.schism.core.gods;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;

public class PantheonRepository extends AbstractRepository<PantheonDefinition>
{
    private static final PantheonRepository INSTANCE = new PantheonRepository();

    /**
     * Gets the singleton PantheonRepository instance.
     * @return The Pantheon Repository which loads and stores pantheons.
     */
    public static PantheonRepository get()
    {
        return INSTANCE;
    }

    protected PantheonRepository()
    {
        super("pantheon");
    }

    @Override
    public PantheonDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new PantheonDefinition(subject, dataStore);
    }
}
