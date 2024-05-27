package com.schism.core.gods;

import com.schism.core.database.DataStore;
import com.schism.core.gods.perks.AbstractWorshipPerk;

import java.util.List;

public class WorshipRank
{
    protected final String name;
    protected final int devotion;
    protected final List<AbstractWorshipPerk> perks;

    public WorshipRank(DataStore dataStore, String name)
    {
        this.name = name;
        this.devotion = dataStore.intProp("devotion");
        this.perks = AbstractWorshipPerk.listFromMap(dataStore.mapProp("perks"));
    }

    /**
     * The name of this rank.
     * @return The rank name.
     */
    public String name()
    {
        return this.name;
    }

    /**
     * The minimum amount of devotion required ot unlock this rank.
     * @return The minimum devotion required to unlock this rank.
     */
    public int devotion()
    {
        return this.devotion;
    }

    /**
     * Returns all perks in this rank.
     * @return A list of perks in this rank.
     */
    public List<AbstractWorshipPerk> perks()
    {
        return this.perks;
    }
}
