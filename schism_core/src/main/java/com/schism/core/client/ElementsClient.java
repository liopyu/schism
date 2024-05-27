package com.schism.core.client;

import com.schism.core.elements.ElementDefinition;
import com.schism.core.particles.DefinedParticleType;
import com.schism.core.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ElementsClient
{
    private static ElementsClient INSTANCE;

    /**
     * Gets the singleton ElementsClient instance.
     * @return The Elements Client which is used to play client side element effects, etc.
     */
    public static ElementsClient get()
    {
        if (INSTANCE == null) {
            INSTANCE = new ElementsClient();
        }
        return INSTANCE;
    }

    /**
     * Spawns particles and plays a sound in the world for the provided element.
     * @param element The element to play a splash for.
     * @param position The position to play the splash at.
     * @param power The power of the splash (affects particle count and sound volume).
     */
    public void createSplash(ElementDefinition element, Vec3 position, int power)
    {
        Level level = Minecraft.getInstance().level;
        if (level == null || element == null) {
            return;
        }
        this.createParticle(element, level, position, power);
        this.playSound(element, level, position, power);
    }

    /**
     * Spawns particles in the world for the provided element.
     * @param element The element.
     * @param level The level.
     * @param position The position.
     * @param count The particle count.
     */
    public void createParticle(@NotNull ElementDefinition element, @NotNull Level level, Vec3 position, int count)
    {
        ParticleType<?> particleType = element.particleType().orElse(null);
        if (particleType == null) {
            return;
        }

        Random random = level.getRandom();
        ParticleOptions particleOptions;
        if (particleType instanceof SimpleParticleType) {
            particleOptions = (SimpleParticleType)particleType;
        } else if (particleType instanceof DefinedParticleType) {
            particleOptions = (DefinedParticleType)particleType;
        } else if (particleType == ParticleTypes.BLOCK && element.particleBlock().isPresent()) {
            particleOptions = new BlockParticleOption((ParticleType<BlockParticleOption>) particleType, element.particleBlock().get().defaultBlockState());
        } else {
            return;
        }

        float force = element.particleForce();
        float offset = element.particleOffset();
        for (int i = 0; i < Math.min(count, 100); i++) {
            Vec3 particlePos = position;
            if (offset > 0) {
                particlePos = particlePos.add(random.nextFloat(-offset, offset), random.nextFloat(0, offset), random.nextFloat(-offset, offset));
            }
            Vec3 particleForce = Vec3.ZERO;
            if (force > 0) {
                particleForce = particleForce.add(random.nextFloat(-force, force), random.nextFloat(-force, force), random.nextFloat(-force, force));
            }
            level.addParticle(particleOptions, particlePos.x(), particlePos.y(), particlePos.z(), particleForce.x(), particleForce.y(), particleForce.z());
        }
    }

    /**
     * Plays a sound in the world for the provided element.
     * @param element The element.
     * @param level The level.
     * @param position The position.
     * @param power The power of the sound, this is converted into a volume based on a cap of 5 power.
     */
    public void playSound(@NotNull ElementDefinition element, @NotNull Level level, Vec3 position, int power)
    {
        if (element.soundEvent().isEmpty()) {
            return;
        }

        float volume = 0.2F + Math.min((float)power / 20F, 0.8F);
        float pitch = level.getRandom().nextFloat(0.9F, 1.1F);
        level.playLocalSound(position.x(), position.y(), position.z(), element.soundEvent().get(), SoundSource.AMBIENT, volume, pitch, false);
    }
}
