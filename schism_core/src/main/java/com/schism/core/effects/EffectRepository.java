package com.schism.core.effects;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

public class EffectRepository extends AbstractRepository<EffectDefinition>
{
    private static final EffectRepository INSTANCE = new EffectRepository();

    /**
     * Gets the singleton EffectRepository instance.
     * @return The Theme Repository which loads and stores block themes.
     */
    public static EffectRepository get()
    {
        return INSTANCE;
    }

    protected EffectRepository()
    {
        super("effect");
    }

    @Override
    public EffectDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new EffectDefinition(subject, dataStore);
    }

    /**
     * Registers effects into Forge.
     * @param event The effect registry event.
     */
    @SubscribeEvent
    public void onEffectsRegistry(final NewRegistryEvent event)
    {
        RegistryBuilder<MobEffect> builder = new RegistryBuilder<>();
        var registry = event.create(builder).get();

        this.definitions.values().stream().sorted().forEach(effectDefinition ->
                effectDefinition.registerEffect(registry));
    }
}
