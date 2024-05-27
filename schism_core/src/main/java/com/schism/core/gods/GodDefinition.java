package com.schism.core.gods;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.CachedDefinition;
import com.schism.core.database.DataStore;
import com.schism.core.lore.RealmDefinition;
import com.schism.core.lore.RealmRepository;

import java.util.List;

public class GodDefinition extends AbstractDefinition
{
    protected CachedDefinition<PantheonDefinition> cachedPantheon;
    protected CachedDefinition<RealmDefinition> cachedRealm;
    protected List<WorshipRank> ranks;

    // Instances:
    protected God god;

    /**
     * God definitions hold information about gods.
     * @param subject The name of the god.
     * @param dataStore The data store that holds config data about this definition.
     */
    public GodDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return GodRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.cachedPantheon = new CachedDefinition<>(dataStore.stringProp("pantheon"), PantheonRepository.get());
        this.cachedRealm = new CachedDefinition<>(dataStore.stringProp("realm"), RealmRepository.get());
        this.ranks = dataStore.mapProp("ranks").entrySet().stream()
                .map(entrySet -> new WorshipRank(entrySet.getValue(), entrySet.getKey())).toList();
    }

    /**
     * Gets the pantheon definition this god is in.
     * @return The pantheon definition for this god definition.
     */
    public PantheonDefinition pantheon()
    {
        return this.cachedPantheon.get();
    }

    /**
     * Gets the realm that this god originates from.
     * @return The god's home realm.
     */
    public RealmDefinition realm()
    {
        return this.cachedRealm.get();
    }

    /**
     * Returns a list of ranks available for worshipping this god.
     * @return A list of available worship ranks.
     */
    public List<WorshipRank> ranks()
    {
        return this.ranks;
    }

    /**
     * Gets the god instance using this definition.
     * @return The god instance.
     */
    public God god()
    {
        if (this.god == null) {
            this.god = new God(this);
        }
        return this.god;
    }
}
