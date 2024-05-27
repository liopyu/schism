package com.schism.core.altars.actions;

import com.schism.core.altars.AltarDefinition;
import com.schism.core.database.Action;
import com.schism.core.database.DataStore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractAltarAction extends Action<AltarDefinition>
{
    protected final String ritual;

    public AbstractAltarAction(AltarDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.ritual = dataStore.stringProp("ritual");
    }

    /**
     * Creates a list of altar actions from the provided map.
     * @param definition The definition the list is for.
     * @param dataStoreMap A map of keys and nested data stores for populating actions with.
     * @return The newly created altar action list.
     */
    public static List<AbstractAltarAction> listFromMap(AltarDefinition definition, Map<String, DataStore> dataStoreMap)
    {
        return dataStoreMap.entrySet().stream()
                .map(dataStoreEntry -> AbstractAltarAction.create(definition, dataStoreEntry))
                .filter(Objects::nonNull).toList();
    }

    /**
     * Creates a new altar action instance for the provided data store entry.
     * @param definition The definition the action is for.
     * @param dataStoreEntry The data store entry to get action properties from.
     * @return The newly created altar action.
     */
    public static AbstractAltarAction create(AltarDefinition definition, Map.Entry<String, DataStore> dataStoreEntry)
    {
        DataStore dataStore = dataStoreEntry.getValue();
        return switch (dataStore.stringProp("type")) {
            case "worship" -> new WorshipAltarAction(definition, dataStore);
            default -> throw new RuntimeException("Invalid Altar Action Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Called when the altar with this action is activated.
     * @param livingEntity The entity that activated the altar.
     * @param corePosition The position of the activated altar's core.
     * @param ritual The name of the ritual that was activated.
     * @param tributeEntity An entity that was involved in the activation, could be a sacrifice or the same as the activator entity.
     */
    public void onActivate(LivingEntity livingEntity, BlockPos corePosition, String ritual, Entity tributeEntity)
    {

    }
}
