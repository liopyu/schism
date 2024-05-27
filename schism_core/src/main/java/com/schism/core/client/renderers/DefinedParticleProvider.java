package com.schism.core.client.renderers;

import com.schism.core.client.renderers.DefinedParticle;
import com.schism.core.particles.DefinedParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class DefinedParticleProvider implements ParticleProvider<DefinedParticleType>
{
    protected final SpriteSet spriteSet;

    public DefinedParticleProvider(SpriteSet spriteSet)
    {
        this.spriteSet = spriteSet;
    }

    @Nullable
    @Override
    public Particle createParticle(DefinedParticleType particleType, ClientLevel clientLevel, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        return new DefinedParticle(particleType.definition(), this.spriteSet, clientLevel, x, y, z, motionX, motionY, motionZ);
    }
}
