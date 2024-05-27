package com.schism.core.creatures;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class CreatureRepository extends AbstractRepository<CreatureDefinition>
{
    private static final CreatureRepository INSTANCE = new CreatureRepository();

    /**
     * Gets the singleton CreatureRepository instance.
     * @return The Creature Repository which loads and stores creatures.
     */
    public static CreatureRepository get()
    {
        return INSTANCE;
    }

    protected CreatureRepository()
    {
        super("creature");
    }

    @Override
    public CreatureDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new CreatureDefinition(subject, dataStore);
    }

    /**
     * Gets a Creature Definition for the provided living entity.
     * @param livingEntity The living entity to get a definition for.
     * @return A Creature Definition or null if none are available for the provided entity.
     */
    public Optional<CreatureDefinition> getDefinition(LivingEntity livingEntity)
    {
        ResourceLocation entityId = livingEntity.getType().getRegistryName();
        if (entityId == null) {
            return Optional.empty();
        }
        String entityIdString = entityId.toString();
        return this.definitions().stream().filter(definition -> definition.entityId().equals(entityIdString)).findFirst();
    }

    /**
     * Registers capabilities into forge.
     * @param registerCapabilitiesEvent The capabilities registry event.
     */
    @SubscribeEvent
    public void onCapabilitiesRegistry(RegisterCapabilitiesEvent registerCapabilitiesEvent)
    {
        registerCapabilitiesEvent.register(ICreature.class);
    }
}
