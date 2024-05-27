package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.util.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Random;

public class VelocityEffectAction extends AbstractEffectAction
{
    protected final Vec3 force;
    protected final float randomMin;
    protected final float randomMax;
    protected final int tickRate;
    protected final float chance;

    public VelocityEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.force = dataStore.vec3Prop("force");
        this.randomMin = dataStore.floatProp("random_min");
        this.randomMax = dataStore.floatProp("random_max");
        this.tickRate = dataStore.intProp("tick_rate");
        this.chance = dataStore.floatProp("chance");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        if (livingEntity.isInvulnerable()) {
            return;
        }

        Level level = livingEntity.getLevel();
        Random random = livingEntity.getRandom();
        if (level.getGameTime() % this.tickRate != 0 || (this.chance < 1 && random.nextFloat() > this.chance)) {
            return;
        }

        int power = effectInstance.getAmplifier() + 1;
        double motionX = this.force.x() * power;
        double motionY = this.force.y() * power;
        double motionZ = this.force.z() * power;
        if (this.randomMin < this.randomMax) {
            motionX *= random.nextFloat(this.randomMin, this.randomMax);
            motionY *= random.nextFloat(this.randomMin, this.randomMax);
            motionZ *= random.nextFloat(this.randomMin, this.randomMax);
        }
        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(motionX, motionY, motionZ));
    }
}
