package com.schism.core.particles;

import com.schism.core.database.AbstractDefinition;
import com.schism.core.database.DataStore;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Random;

public class ParticleDefinition extends AbstractDefinition
{
    protected boolean overrideLimiter;
    protected float size;
    protected int lifetimeMin;
    protected int lifetimeMax;
    protected int animationTicks;
    protected boolean randomAnimationTicks;
    protected boolean physics;
    protected float gravity;
    protected float alpha;
    protected boolean glow;
    protected float speedMultiplier;
    protected float sizeMultiplier;
    protected float alphaMultiplier;

    DefinedParticleType particleType;

    /**
     * Particle definitions hold information about particle effects.
     * @param subject The name of the particle.
     * @param dataStore The data store that holds config data about this definition.
     */
    public ParticleDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return ParticleRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.overrideLimiter = dataStore.booleanProp("override_limiter");
        this.size = dataStore.floatProp("size");
        this.lifetimeMin = dataStore.intProp("lifetime_min");
        this.lifetimeMax = dataStore.intProp("lifetime_max");
        this.animationTicks = dataStore.intProp("animation_ticks");
        this.randomAnimationTicks = dataStore.booleanProp("random_animation_offset");
        this.physics = dataStore.booleanProp("physics");
        this.gravity = dataStore.intProp("gravity");
        this.alpha = dataStore.floatProp("alpha");
        this.glow = dataStore.booleanProp("glow");
        this.speedMultiplier = dataStore.floatProp("speed_multiplier");
        this.sizeMultiplier = dataStore.floatProp("size_multiplier");
        this.alphaMultiplier = dataStore.floatProp("alpha_multiplier");
    }

    public DefinedParticleType particleType()
    {
        if (this.particleType == null) {
            this.particleType = new DefinedParticleType(this, this.overrideLimiter);
        }
        return this.particleType;
    }

    /**
     * The render size of the particle.
     * @return The particle size.
     */
    public float size()
    {
        return this.size;
    }

    /**
     * Gets a potentially random lifetime that a particle should last for in ticks.
     * @param random An instance of random for random lifetime ranges if defined.
     * @return The particle lifetime in ticks.
     */
    public int lifetime(Random random)
    {
        if (this.lifetimeMax <= this.lifetimeMin) {
            return this.lifetimeMin;
        }
        return random.nextInt(this.lifetimeMin, this.lifetimeMax + 1);
    }

    /**
     * How long it takes in ticks to cycle through all particle sprite animation frames.
     * @return The sprite animation length in ticks.
     */
    public int animationTicks()
    {
        return this.animationTicks;
    }

    /**
     * If true, the sprite animation of the particle will start at a random tick offset.
     * @return Whether the particle animation should start with a random tick offset.
     */
    public boolean randomAnimationTicks()
    {
        return this.randomAnimationTicks;
    }

    /**
     * Whether the particle should be affected by physics.
     * @return True if the particle should be affected by block collisions, etc.
     */
    public boolean physics()
    {
        return this.physics;
    }

    /**
     * How much gravity to apply to the particle, can be negative for floating.
     * @return The particle gravity.
     */
    public float gravity()
    {
        return this.gravity;
    }

    /**
     * How much alpha the particle sprite has where 1.0 is 100%.
     * @return The particle sprite alpha.
     */
    public float alpha()
    {
        return this.alpha;
    }

    /**
     * Whether the particle should ignore lighting or not.
     * @return True for full bright sprites, false to use location lighting.
     */
    public boolean glow()
    {
        return this.glow;
    }

    /**
     * A multiplier to apply to the particle speed each tick.
     * @return The multiplier to apply to the particle speed each tick.
     */
    public float speedMultiplier()
    {
        return this.speedMultiplier;
    }

    /**
     * A multiplier to apply to the particle size each tick.
     * @return The multiplier to apply to the particle size each tick.
     */
    public float sizeMultiplier()
    {
        return this.sizeMultiplier;
    }

    /**
     * A multiplier to apply to the particle alpha each tick.
     * @return The multiplier to apply to the particle alpha each tick.
     */
    public float alphaMultiplier()
    {
        return this.alphaMultiplier;
    }

    /**
     * Registers a particle type.
     * @param registry The forge registry.
     */
    public void registerParticleType(IForgeRegistry<ParticleType<?>> registry)
    {
        registry.register(this.particleType());
    }
}
