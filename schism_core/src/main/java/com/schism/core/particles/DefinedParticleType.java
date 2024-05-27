package com.schism.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.schism.core.database.IHasDefinition;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class DefinedParticleType extends ParticleType<DefinedParticleType> implements IHasDefinition<ParticleDefinition>, ParticleOptions
{
    protected static final ParticleOptions.Deserializer<DefinedParticleType> DESERIALIZER = new ParticleOptions.Deserializer<>()
    {
        public @NotNull DefinedParticleType fromCommand(ParticleType<DefinedParticleType> particleType, StringReader stringReader)
        {
            return (DefinedParticleType) particleType;
        }

        public @NotNull DefinedParticleType fromNetwork(ParticleType<DefinedParticleType> particleType, FriendlyByteBuf buffer)
        {
            return (DefinedParticleType) particleType;
        }
    };

    protected final ParticleDefinition definition;
    protected final Codec<DefinedParticleType> codec = Codec.unit(this::getType);

    public DefinedParticleType(ParticleDefinition particleDefinition, boolean overrideLimiter)
    {
        super(overrideLimiter, DESERIALIZER);
        this.setRegistryName(particleDefinition.resourceLocation());
        this.definition = particleDefinition;
    }

    @Override
    public ParticleDefinition definition()
    {
        return this.definition;
    }

    @Override
    public @NotNull Codec<DefinedParticleType> codec()
    {
        return this.codec;
    }

    @Override
    public @NotNull DefinedParticleType getType()
    {
        return this;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer)
    {

    }

    @Override
    public @NotNull String writeToString()
    {
        return Registry.PARTICLE_TYPE.getKey(this).toString();
    }
}
