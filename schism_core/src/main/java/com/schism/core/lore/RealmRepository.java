package com.schism.core.lore;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;

import java.util.List;

public class RealmRepository extends AbstractRepository<RealmDefinition>
{
    private static final RealmRepository INSTANCE = new RealmRepository();

    /**
     * Gets the singleton RealmRepository instance.
     * @return The Realm Repository which loads and stores pantheons.
     */
    public static RealmRepository get()
    {
        return INSTANCE;
    }

    protected RealmRepository()
    {
        super("realm");
    }

    @Override
    public RealmDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new RealmDefinition(subject, dataStore);
    }

    /**
     * Returns a list of all universe name strings from all loaded realms.
     * @return A list of universe names.
     */
    public List<String> universes()
    {
        return this.definitions().stream().map(RealmDefinition::universe).distinct().toList();
    }

    /**
     * Returns a list of all realm type name strings from all loaded realms.
     * @return A list of realm type names.
     */
    public List<String> realmTypes()
    {
        return this.definitions().stream().map(RealmDefinition::realmType).distinct().toList();
    }
}
