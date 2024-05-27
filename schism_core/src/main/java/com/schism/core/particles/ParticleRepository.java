package com.schism.core.particles;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ParticleRepository extends AbstractRepository<ParticleDefinition>
{
    private static final ParticleRepository INSTANCE = new ParticleRepository();

    /**
     * Gets the singleton ParticleRepository instance.
     * @return The Particle Repository which loads and stores particle types.
     */
    public static ParticleRepository get()
    {
        return INSTANCE;
    }

    protected ParticleRepository()
    {
        super("particle");
    }

    @Override
    public ParticleDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new ParticleDefinition(subject, dataStore);
    }

    /**
     * Registers particle types into Forge.
     * @param event The particle registry event.
     */
    @SubscribeEvent
    public void onParticleTypesRegistry(final RegistryEvent.Register<ParticleType<?>> event)
    {
        this.definitions.values().stream().sorted().forEach(definition -> definition.registerParticleType(event.getRegistry()));
    }
}
