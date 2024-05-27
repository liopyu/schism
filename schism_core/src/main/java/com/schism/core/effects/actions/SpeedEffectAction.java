package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.util.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class SpeedEffectAction extends AbstractEffectAction
{
    protected final Vec3 multiply;

    public SpeedEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.multiply = dataStore.vec3Prop("multiply");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        int power = effectInstance.getAmplifier() + 1;
        float multiplyX = this.multiply.x() >= 1 ? (float)Math.pow(this.multiply.x(), power) : this.multiply.x() / power;
        float multiplyY = this.multiply.y() >= 1 ? (float)Math.pow(this.multiply.y(), power) : this.multiply.y() / power;
        float multiplyZ = this.multiply.z() >= 1 ? (float)Math.pow(this.multiply.z(), power) : this.multiply.z() / power;
        if (multiplyX <= 0.25) {
            multiplyX = 0;
        }
        if (multiplyY <= 0.25) {
            multiplyY = 0;
        }
        if (multiplyZ <= 0.25) {
            multiplyZ = 0;
        }
        Vec3 multiply = this.multiply.multiply(multiplyX, multiplyY, multiplyZ);
        Vec3 delta = new Vec3(livingEntity.getDeltaMovement().multiply(multiply.physVec3()));
        livingEntity.setDeltaMovement(delta.physVec3());
    }
}
