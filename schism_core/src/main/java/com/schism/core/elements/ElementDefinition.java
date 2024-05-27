package com.schism.core.elements;

import com.schism.core.database.*;
import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.registryobjects.SoundRegistryObject;
import com.schism.core.resolvers.SoundEventResolver;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ElementDefinition extends AbstractDefinition
{
    protected Color color;
    protected List<String> visPolarisations;
    protected CachedList<ElementDefinition> fusionComponents;

    protected BlockRegistryObject<ParticleType<?>> particleType;
    protected BlockRegistryObject<Block> particleBlock;
    protected float particleForce;
    protected float particleOffset;

    protected SoundRegistryObject<SoundEvent> cachedSoundEvent;

    /**
     * Element definitions hold information about elements.
     * @param subject The name of the element.
     * @param dataStore The data store that holds config data about this definition.
     */
    public ElementDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return ElementRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.color = new Color(Integer.decode(dataStore.stringProp("color")));
        this.visPolarisations = dataStore.listProp("vis_polarisations").stream().map(DataStore::stringValue).toList();
        this.fusionComponents = new CachedList<>(dataStore.listProp("fusion_components").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());

        this.particleType = new BlockRegistryObject<>(dataStore.stringProp("particle_type_id"), () -> ForgeRegistries.PARTICLE_TYPES);
        this.particleBlock = new BlockRegistryObject<>(dataStore.stringProp("particle_block_id"), () -> ForgeRegistries.BLOCKS);
        this.particleForce = dataStore.floatProp("particle_force");
        this.particleOffset = dataStore.floatProp("particle_offset");

        this.cachedSoundEvent = SoundEventResolver.get().fromId(dataStore.stringProp("sound"));

        if (dataStore.booleanProp("fallback")) {
            ElementRepository.get().setFallback(() -> this);
        }
    }

    /**
     * The color of this element.
     * @return The element color.
     */
    public Color color()
    {
        return this.color;
    }

    /**
     * Gets the particle type to use for damage effects, etc.
     * @return The particle type for this element. Can be null if the id was invalid.
     */
    public Optional<ParticleType<?>> particleType()
    {
        return this.particleType.optional();
    }

    /**
     * Gets the block to use for particle types that take a block state.
     * @return The block to use for certain particle types. Can be null.
     */
    public Optional<Block> particleBlock()
    {
        return this.particleBlock.optional();
    }

    /**
     * The maximum amount of random force to apply to particles.
     * @return The maximum amount of random force to apply to particles.
     */
    public float particleForce()
    {
        return this.particleForce;
    }

    /**
     * The maximum amount of a random offset to apply to particles.
     * @return The maximum amount of a random offset to apply to particles.
     */
    public float particleOffset()
    {
        return this.particleOffset;
    }

    /**
     * The sound event for this element, used for splash effects, etc.
     * @return The base sound event for this element.
     */
    public Optional<SoundEvent> soundEvent()
    {
        return this.cachedSoundEvent.optional();
    }
}
