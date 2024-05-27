package com.schism.core.lore;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;

public class RealmDefinition extends AbstractDefinition
{
    protected String universe;
    protected String realmType;

    /**
     * Realms definitions hold information about realms.
     * @param subject The name of the realm.
     * @param dataStore The data store that holds config data about this definition.
     */
    public RealmDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return RealmRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.universe = dataStore.stringProp("universe");
        this.realmType = dataStore.stringProp("type");
    }

    /**
     * Returns the name of the universe this realm is from.
     * @return The universe this realm is from.
     */
    public String universe()
    {
        return this.universe;
    }

    /**
     * Returns the name of the type that this realm is.
     * @return This realm's type name.
     */
    public String realmType()
    {
        return this.realmType;
    }
}
