package com.schism.core.database.conditions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.effects.actions.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AbstractCondition
{
    protected final String type;

    public AbstractCondition(DataStore dataStore)
    {
        this.type = dataStore.stringProp("type");
    }

    public static List<AbstractCondition> listFromMap(Map<String, DataStore> dataStoreMap)
    {
        return dataStoreMap.entrySet().stream()
                .map(AbstractCondition::create)
                .filter(Objects::nonNull).toList();
    }

    /**
     * Creates a new condition instance for the provided data store entry.
     * @param dataStoreEntry The data store entry to get properties from.
     * @return The newly created condition.
     */
    public static AbstractCondition create(Map.Entry<String, DataStore> dataStoreEntry)
    {
        DataStore dataStore = dataStoreEntry.getValue();
        return switch (dataStore.stringProp("type")) {
            case "light" -> new LightCondition(dataStore);
            default -> throw new RuntimeException("Invalid Condition Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Tests if the provided level passes this condition. Defaults to true.
     * @param level The level to test.
     * @return True if the conditions are met for the provided level.
     */
    public boolean test(Level level)
    {
        return true;
    }

    /**
     * Tests if the provided level and position pass this condition. Defaults to testing the level only.
     * @param level The level to test.
     * @param blockPos The position to test.
     * @return True if the conditions are met for the provided level and position.
     */
    public boolean test(Level level, BlockPos blockPos)
    {
        return this.test(level);
    }

    /**
     * Tests if the provided entity passes this condition. Defaults to testing the level and position of the entity.
     * @param entity The entity to test.
     * @return True if the conditions are met for the provided entity.
     */
    public boolean test(Entity entity)
    {
        return this.test(entity.getLevel(), entity.blockPosition());
    }
}
