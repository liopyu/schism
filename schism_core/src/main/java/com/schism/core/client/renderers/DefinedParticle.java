package com.schism.core.client.renderers;

import com.schism.core.particles.ParticleDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.NotNull;

public class DefinedParticle extends TextureSheetParticle
{
    protected final ParticleDefinition particleDefinition;
    protected final SpriteSet spriteSet;
    protected final int animationTicks;
    protected final int animationTicksOffset;
    protected final float speedMultiplier;
    protected final float sizeMultiplier;
    protected final float alphaMultiplier;

    protected DefinedParticle(ParticleDefinition particleDefinition, SpriteSet spriteSet, ClientLevel clientLevel, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        super(clientLevel, x, y, z);
        this.particleDefinition = particleDefinition;
        this.spriteSet = spriteSet;
        this.setParticleSpeed(motionX, motionY, motionZ);

        this.quadSize = particleDefinition.size() * 0.25F;
        this.setSize(particleDefinition.size() * 0.25F, particleDefinition.size() * 0.25F);
        this.lifetime = particleDefinition.lifetime(this.random);
        this.hasPhysics = particleDefinition.physics();
        this.gravity = particleDefinition.gravity();
        this.alpha = particleDefinition.alpha();
        this.animationTicks = Math.max(particleDefinition.animationTicks(), 1);
        this.animationTicksOffset = particleDefinition.randomAnimationTicks() ? this.random.nextInt(this.animationTicks) : 0;
        this.speedMultiplier = particleDefinition.speedMultiplier();
        this.sizeMultiplier = particleDefinition.sizeMultiplier();
        this.alphaMultiplier = particleDefinition.alphaMultiplier();

        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick()
    {
        if (this.speedMultiplier != 0) {
            this.setParticleSpeed(this.xd * this.speedMultiplier, this.yd * this.speedMultiplier, this.zd * this.speedMultiplier);
        }
        if (this.sizeMultiplier != 0) {
            this.setSize(this.bbWidth * this.sizeMultiplier, this.bbHeight * this.sizeMultiplier);
            this.quadSize *= this.sizeMultiplier;
        }
        if (this.alphaMultiplier != 0) {
            this.alpha *= this.alphaMultiplier;
        }
        super.tick();
        this.setSpriteFromAge(this.spriteSet);
    }

    @Override
    public void setSpriteFromAge(SpriteSet spriteSet)
    {
        if (this.removed) {
            return;
        }
        this.setSprite(spriteSet.get((this.age + this.animationTicksOffset) % this.animationTicks, this.animationTicks));
    }

    @Override
    public @NotNull ParticleRenderType getRenderType()
    {
        return this.particleDefinition.glow() ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    /**
     * Gets the light color that this particle should use.
     * @param age The age of the particle.
     * @return The light color to use.
     */
    public int getLightColor(float age)
    {
        return 15728880;
    }
}
